package com.beatblink

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.io.TarsosDSPAudioInputStream
import be.tarsos.dsp.onsets.OnsetHandler
import be.tarsos.dsp.onsets.ComplexOnsetDetector
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.InputStream
import kotlin.math.abs
import kotlin.math.max

/**
 * Real-time audio beat detector using TarsosDSP library.
 * 
 * Captures live microphone audio and detects beats using:
 * - TarsosDSP's PercussionOnsetDetector for professional beat detection
 * - Adaptive threshold and spectral analysis
 * - BPM estimation from inter-beat intervals
 * 
 * Tuning Parameters:
 * - ONSET_THRESHOLD: Sensitivity (default: 8.0, lower = more sensitive)
 * - ONSET_SENSITIVITY: Response time (default: 20.0, lower = faster)
 * - MIN_BEAT_INTERVAL_MS: Minimum time between beats (default: 300ms)
 */
class AudioBeatDetector {
    companion object {
        private const val TAG = "AudioBeatDetector"
        
        // Audio configuration
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 2048
        private const val OVERLAP = 0 // No overlap for real-time performance
        private const val SOFTWARE_GAIN = 3.0f // Amplify signal 3x for better sensitivity
        
        // Beat detection parameters - TUNING KNOBS
        private const val ONSET_THRESHOLD = 0.3 // Raise spectral threshold to reduce false positives
        private const val ONSET_SENSITIVITY = 1.0 // For ComplexOnsetDetector
        
        // Simple energy-based detection for more immediate response
        private const val ENERGY_THRESHOLD = 1.08 // Multiplier above recent average (higher = less sensitive)
        private const val ENERGY_MIN_LEVEL = 0.05f // Minimum absolute level after gain (higher = ignore quiet sounds)
        private const val ENERGY_HISTORY_SIZE = 20 // Frames to average (longer = more stable)
        
        private const val MIN_BEAT_INTERVAL_MS = 250L // Prevent rapid double-triggers
        
        // BPM estimation
        private const val BPM_HISTORY_SIZE = 8
    }
    
    private var audioRecord: AudioRecord? = null
    private var dispatcher: AudioDispatcher? = null
    private var dispatcherJob: Job? = null
    
    // State flows
    private val _isBeat = MutableStateFlow(false)
    val isBeat: StateFlow<Boolean> = _isBeat.asStateFlow()
    
    private val _bpm = MutableStateFlow(0)
    val bpm: StateFlow<Int> = _bpm.asStateFlow()
    
    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    // Beat detection state
    private var lastBeatTime = 0L
    private val beatIntervals = ArrayDeque<Long>(BPM_HISTORY_SIZE)

    
    /**
     * Custom InputStream that wraps AudioRecord for TarsosDSP.
     */
    private inner class AudioRecordInputStream(private val audioRecord: AudioRecord) : InputStream() {
        
        override fun read(): Int {
            val buffer = ByteArray(1)
            val result = read(buffer, 0, 1)
            return if (result > 0) buffer[0].toInt() and 0xFF else -1
        }
        
        override fun read(b: ByteArray, off: Int, len: Int): Int {
            if (!_isRecording.value) {
                return -1
            }
            
            val bytesRead = audioRecord.read(b, off, len)
            
            if (bytesRead < 0) {
                Log.e(TAG, "AudioRecord.read error: $bytesRead")
                return -1
            }
            
            if (bytesRead == 0) {
                Thread.sleep(10)
            }
            
            return bytesRead
        }
    }
    
    /**
     * Custom AudioProcessor to track audio levels and detect energy spikes.
     */
    private inner class LevelMonitor : AudioProcessor {
        private val energyHistory = ArrayDeque<Float>(ENERGY_HISTORY_SIZE)
        private var frameCount = 0
        
        override fun process(audioEvent: AudioEvent): Boolean {
            val buffer = audioEvent.floatBuffer
            var sum = 0f
            var maxSample = 0f
            
            // Apply software gain and calculate energy
            for (i in 0 until buffer.size) {
                val sample = abs(buffer[i] * SOFTWARE_GAIN) // Apply gain
                sum += sample
                maxSample = max(maxSample, sample)
            }
            
            val avgLevel = sum / buffer.size
            _audioLevel.value = avgLevel // Display amplified level
            
            // Track energy history
            energyHistory.addLast(avgLevel)
            if (energyHistory.size > ENERGY_HISTORY_SIZE) {
                energyHistory.removeFirst()
            }
            
            // Simple energy-based beat detection
            if (energyHistory.size >= ENERGY_HISTORY_SIZE) {
                val avgEnergy = energyHistory.average().toFloat()
                val threshold = avgEnergy * ENERGY_THRESHOLD
                
                frameCount++
                
                // Log every frame when there's significant energy
                if (maxSample > 0.15f || frameCount % 50 == 0) {
                    Log.d(TAG, "Energy check: current=${"%.3f".format(avgLevel)}, avg=${"%.3f".format(avgEnergy)}, max=${"%.3f".format(maxSample)}, threshold=${"%.3f".format(threshold)}, minLevel=$ENERGY_MIN_LEVEL, needsAbove=${avgLevel > threshold}, needsMax=${maxSample > ENERGY_MIN_LEVEL}")
                }
                
                // Detect sharp energy spike
                if (avgLevel > threshold && maxSample > ENERGY_MIN_LEVEL) {
                    Log.d(TAG, "✓✓✓ Energy beat: level=${"%.3f".format(avgLevel)}, max=${"%.3f".format(maxSample)}")
                    onBeatDetected()
                }
            }
            
            return true
        }
        
        override fun processingFinished() {
            energyHistory.clear()
        }
    }
    
    /**
     * Start audio capture and beat detection using TarsosDSP.
     */
    fun start() {
        if (_isRecording.value) {
            Log.w(TAG, "Already recording")
            return
        }
        
        try {
            // Create AudioRecord for microphone input
            val minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            
            if (minBufferSize <= 0) {
                Log.e(TAG, "Invalid buffer size: $minBufferSize")
                return
            }
            
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize * 4
            )
            
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord not initialized")
                audioRecord?.release()
                audioRecord = null
                return
            }
            
            audioRecord?.startRecording()
            Log.d(TAG, "AudioRecord started")
            
            // Create TarsosDSP format
            val format = TarsosDSPAudioFormat(
                SAMPLE_RATE.toFloat(),
                16, // 16-bit
                1,  // mono
                true, // signed
                false // big endian
            )
            
            // Create AudioDispatcher from AudioRecord
            val inputStream = audioRecord?.let { AudioRecordInputStream(it) }
            if (inputStream == null) {
                Log.e(TAG, "Failed to create input stream")
                stop()
                return
            }
            
            val audioStream = object : TarsosDSPAudioInputStream {
                private var readCount = 0
                
                override fun skip(bytes: Long): Long = inputStream.skip(bytes)
                
                override fun read(b: ByteArray, off: Int, len: Int): Int {
                    val result = inputStream.read(b, off, len)
                    readCount++
                    return result
                }
                
                override fun close() {
                    Log.d(TAG, "AudioStream closed after $readCount reads")
                    inputStream.close()
                }
                
                override fun getFormat(): TarsosDSPAudioFormat = format
                override fun getFrameLength(): Long = -1
            }
            
            dispatcher = AudioDispatcher(audioStream, BUFFER_SIZE, OVERLAP)
            
            // Add level monitor with energy-based detection
            dispatcher?.addAudioProcessor(LevelMonitor())
            
            // Also add ComplexOnsetDetector for musical beat detection
            val onsetHandler = OnsetHandler { time, energy ->
                Log.d(TAG, "ComplexOnset detected at ${time}s (energy=$energy)")
                onBeatDetected()
            }
            
            val onsetDetector = ComplexOnsetDetector(
                BUFFER_SIZE,
                ONSET_THRESHOLD,
                ONSET_SENSITIVITY
            )
            onsetDetector.setHandler(onsetHandler)
            dispatcher?.addAudioProcessor(onsetDetector)
            
            Log.d(TAG, "AudioDispatcher configured with hybrid detection (energy + spectral)")
            
            // Clear state
            beatIntervals.clear()
            lastBeatTime = 0L
            _isRecording.value = true
            
            // Start dispatcher in background thread
            dispatcherJob = CoroutineScope(Dispatchers.IO).launch {
                Log.d(TAG, "Starting AudioDispatcher...")
                try {
                    dispatcher?.run()
                    Log.d(TAG, "AudioDispatcher finished")
                } catch (e: Exception) {
                    Log.e(TAG, "Error in AudioDispatcher", e)
                }
            }
            
            Log.d(TAG, "Started recording with TarsosDSP")
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording", e)
        }
    }
    
    /**
     * Stop audio capture and release resources.
     */
    fun stop() {
        if (!_isRecording.value) {
            return
        }
        
        dispatcher?.stop()
        dispatcher = null
        
        dispatcherJob?.cancel()
        dispatcherJob = null
        
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        
        _isRecording.value = false
        _isBeat.value = false
        _audioLevel.value = 0f
        
        Log.d(TAG, "Stopped recording")
    }
    
    
    /**
     * Called by TarsosDSP when a beat is detected.
     */
    private fun onBeatDetected() {
        val currentTime = System.currentTimeMillis()
        
        // Apply refractory period to avoid spurious double-detections
        if (lastBeatTime > 0 && currentTime - lastBeatTime < MIN_BEAT_INTERVAL_MS) {
            return
        }
        
        Log.d(TAG, "✓ BEAT accepted (${currentTime - lastBeatTime}ms since last)")
        
        // Flash the UI
        CoroutineScope(Dispatchers.Main).launch {
            _isBeat.value = true
            delay(50) // Flash duration
            _isBeat.value = false
        }
        
        // Calculate BPM from inter-beat intervals
        if (lastBeatTime > 0) {
            val interval = currentTime - lastBeatTime
            Log.d(TAG, "  Interval: ${interval}ms")
            
            beatIntervals.addLast(interval)
            if (beatIntervals.size > BPM_HISTORY_SIZE) {
                beatIntervals.removeFirst()
            }
            updateBPM()
        }
        
        lastBeatTime = currentTime
    }
    
    /**
     * Update BPM estimate from recent beat intervals.
     */
    private fun updateBPM() {
        if (beatIntervals.size < 2) {
            return
        }
        
        // Use median interval to avoid outliers
        val sortedIntervals = beatIntervals.sorted()
        val medianInterval = sortedIntervals[sortedIntervals.size / 2]
        
        // Convert interval (ms) to BPM
        val estimatedBPM = (60000.0 / medianInterval).toInt()
        
        // Only update if reasonable BPM range (60-200)
        if (estimatedBPM in 60..200) {
            _bpm.value = estimatedBPM
            Log.d(TAG, "BPM updated: $estimatedBPM (from ${beatIntervals.size} intervals)")
        }
    }
}
