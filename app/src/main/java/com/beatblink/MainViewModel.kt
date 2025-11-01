package com.beatblink

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for BeatBlink main screen.
 * 
 * Manages audio beat detector lifecycle and exposes state to UI via StateFlow.
 */
class MainViewModel : ViewModel() {
    private val detector = AudioBeatDetector()
    
    // Expose detector state to UI
    val isBeat: StateFlow<Boolean> = detector.isBeat
        .stateIn(viewModelScope, SharingStarted.Lazily, false)
    
    val bpm: StateFlow<Int> = detector.bpm
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)
    
    val audioLevel: StateFlow<Float> = detector.audioLevel
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)
    
    val isRecording: StateFlow<Boolean> = detector.isRecording
        .stateIn(viewModelScope, SharingStarted.Lazily, false)
    
    /**
     * Start audio capture and beat detection.
     */
    fun startListening() {
        detector.start()
    }
    
    /**
     * Stop audio capture and release resources.
     */
    fun stopListening() {
        detector.stop()
    }
    
    override fun onCleared() {
        super.onCleared()
        detector.stop()
    }
}
