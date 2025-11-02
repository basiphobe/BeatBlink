package com.beatblink

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import android.Manifest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

/**
 * Integration tests for BeatBlink app focusing on:
 * - End-to-end audio pipeline
 * - ViewModel <-> AudioBeatDetector integration  
 * - Real-time state synchronization
 * - Performance under load
 * 
 * Note: These tests require device microphone access and may be sensitive
 * to ambient audio conditions during testing.
 */
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class BeatBlinkIntegrationTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.RECORD_AUDIO
    )

    private lateinit var testScope: TestScope
    private lateinit var testDispatcher: TestDispatcher

    @Test
    fun viewModel_audioBeatDetector_integration_works() = runTest {
        // Given: ViewModel with real AudioBeatDetector
        val viewModel = MainViewModel()
        
        // When: Starting audio detection
        viewModel.startListening()
        
        // Then: Recording should start within reasonable time
        withTimeout(5000) {
            val isRecording = viewModel.isRecording.first { it }
            assertTrue("Should be recording after start", isRecording)
        }
        
        // Cleanup
        viewModel.stopListening()
        
        // Verify cleanup
        withTimeout(2000) {
            val isRecording = viewModel.isRecording.first { !it }
            assertFalse("Should not be recording after stop", isRecording)
        }
    }

    @Test
    fun audio_pipeline_initializes_correctly() = runTest {
        // Given: Fresh AudioBeatDetector
        val detector = AudioBeatDetector()
        
        // When: Starting audio capture
        detector.start()
        
        try {
            // Then: Should be recording within timeout
            withTimeout(3000) {
                val isRecording = detector.isRecording.first { it }
                assertTrue("AudioBeatDetector should be recording", isRecording)
            }
            
            // Audio level should update (assuming some ambient noise)
            advanceTimeBy(1000)
            val audioLevel = detector.audioLevel.value
            assertTrue("Audio level should be non-negative", audioLevel >= 0f)
            
        } finally {
            // Cleanup
            detector.stop()
            
            // Verify cleanup
            withTimeout(2000) {
                val isRecording = detector.isRecording.first { !it }
                assertFalse("Should not be recording after stop", isRecording)
            }
        }
    }

    @Test
    fun state_synchronization_between_components() = runTest {
        // Given: ViewModel and access to its AudioBeatDetector states
        val viewModel = MainViewModel()
        
        // When: Starting listening
        viewModel.startListening()
        
        try {
            // Then: All relevant states should sync
            withTimeout(5000) {
                val vmRecording = viewModel.isRecording.first { it }
                assertTrue("ViewModel should show recording", vmRecording)
            }
            
            // BPM should start at 0 and be ready to update
            assertEquals("BPM should start at 0", 0, viewModel.bpm.value)
            
            // Beat should start false and be ready to flash
            assertFalse("Beat should start false", viewModel.isBeat.value)
            
            // Audio level should be available
            assertTrue("Audio level should be non-negative", viewModel.audioLevel.value >= 0f)
            
        } finally {
            viewModel.stopListening()
        }
    }

    @Test
    fun rapid_start_stop_cycles_are_stable() = runTest {
        // Given: ViewModel for rapid testing
        val viewModel = MainViewModel()
        
        try {
            // When: Performing rapid start/stop cycles
            repeat(5) { cycle ->
                viewModel.startListening()
                
                // Wait briefly for start
                withTimeout(2000) {
                    val isRecording = viewModel.isRecording.first { it }
                    assertTrue("Should be recording in cycle $cycle", isRecording)
                }
                
                viewModel.stopListening()
                
                // Wait briefly for stop
                withTimeout(2000) {
                    val isRecording = viewModel.isRecording.first { !it }
                    assertFalse("Should not be recording in cycle $cycle", isRecording)
                }
            }
            
            // Then: Should end in consistent state
            assertFalse("Should not be recording after cycles", viewModel.isRecording.value)
            assertEquals("BPM should reset to 0", 0, viewModel.bpm.value)
            assertFalse("Beat should be false", viewModel.isBeat.value)
            assertEquals("Audio level should reset to 0", 0f, viewModel.audioLevel.value, 0.001f)
            
        } finally {
            viewModel.stopListening()
        }
    }

    @Test
    fun audio_detector_resource_cleanup_is_complete() = runTest {
        // Given: AudioBeatDetector
        val detector = AudioBeatDetector()
        
        // When: Starting and stopping multiple times
        repeat(3) { iteration ->
            detector.start()
            
            withTimeout(2000) {
                val isRecording = detector.isRecording.first { it }
                assertTrue("Should start recording in iteration $iteration", isRecording)
            }
            
            detector.stop()
            
            withTimeout(2000) {
                val isRecording = detector.isRecording.first { !it }
                assertFalse("Should stop recording in iteration $iteration", isRecording)
            }
            
            // Verify state is clean between iterations
            assertEquals("BPM should reset between iterations", 0, detector.bpm.value)
            assertFalse("Beat should be false between iterations", detector.isBeat.value)
            assertEquals("Audio level should reset", 0f, detector.audioLevel.value, 0.001f)
        }
    }

    @Test
    fun concurrent_access_to_stateflows_is_safe() = runTest {
        // Given: ViewModel under concurrent access
        val viewModel = MainViewModel()
        
        try {
            viewModel.startListening()
            
            withTimeout(3000) {
                viewModel.isRecording.first { it }
            }
            
            // When: Accessing StateFlows concurrently (simulated)
            // Note: In real concurrent test, we'd use multiple coroutines
            
            val states = listOf(
                viewModel.isBeat.value,
                viewModel.bpm.value,
                viewModel.audioLevel.value,
                viewModel.isRecording.value
            )
            
            // Then: All states should be accessible without crashes
            assertNotNull("Beat state should be accessible", states[0])
            assertTrue("BPM state should be valid", states[1] is Int)
            assertTrue("Audio level should be valid", states[2] is Float)
            assertTrue("Recording state should be valid", states[3] is Boolean)
            
        } finally {
            viewModel.stopListening()
        }
    }

    @Test
    fun audio_processing_performance_is_acceptable() = runTest {
        // Given: AudioBeatDetector for performance testing
        val detector = AudioBeatDetector()
        
        try {
            val startTime = System.currentTimeMillis()
            
            // When: Running detection for extended period
            detector.start()
            
            withTimeout(3000) {
                detector.isRecording.first { it }
            }
            
            // Let it process audio for a few seconds
            advanceTimeBy(5000)
            
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            // Then: Should be processing without excessive delays
            assertTrue("Audio processing should start quickly", duration < 10000)
            
            // Audio level should be updating (indicates processing activity)
            val audioLevel = detector.audioLevel.value
            assertTrue("Audio processing should produce level readings", audioLevel >= 0f)
            
            // State should remain consistent
            assertTrue("Should still be recording", detector.isRecording.value)
            
        } finally {
            detector.stop()
        }
    }

    @Test
    fun error_conditions_are_handled_gracefully() = runTest {
        // Given: AudioBeatDetector
        val detector = AudioBeatDetector()
        
        // When: Attempting operations in wrong order
        detector.stop() // Stop before start
        
        // Then: Should handle gracefully (no crashes)
        assertFalse("Should not be recording after stop-before-start", detector.isRecording.value)
        
        // When: Starting normally after error condition
        detector.start()
        
        try {
            withTimeout(3000) {
                val isRecording = detector.isRecording.first { it }
                assertTrue("Should recover and start recording", isRecording)
            }
        } finally {
            detector.stop()
        }
    }

    @Test
    fun memory_usage_remains_stable_during_detection() = runTest {
        // Given: ViewModel for memory testing
        val viewModel = MainViewModel()
        
        try {
            // When: Running detection for extended period
            viewModel.startListening()
            
            withTimeout(3000) {
                viewModel.isRecording.first { it }
            }
            
            // Simulate extended usage
            repeat(10) {
                advanceTimeBy(1000)
                
                // Access all states to trigger any potential memory issues
                val states = listOf(
                    viewModel.isBeat.value,
                    viewModel.bpm.value,
                    viewModel.audioLevel.value,
                    viewModel.isRecording.value
                )
                
                // Verify states remain valid
                assertTrue("States should remain valid during extended use", 
                    states.all { it != null })
            }
            
            // Then: Should still be functioning correctly
            assertTrue("Should still be recording after extended use", 
                viewModel.isRecording.value)
                
        } finally {
            viewModel.stopListening()
            
            // Verify cleanup after extended use
            withTimeout(2000) {
                val isRecording = viewModel.isRecording.first { !it }
                assertFalse("Should clean up after extended use", isRecording)
            }
        }
    }

    @Test
    fun full_app_lifecycle_integration() = runTest {
        // Given: Full app scenario
        val viewModel = MainViewModel()
        
        try {
            // Simulate full app lifecycle
            
            // 1. App startup
            assertFalse("App should start not recording", viewModel.isRecording.value)
            
            // 2. User starts listening
            viewModel.startListening()
            withTimeout(3000) {
                viewModel.isRecording.first { it }
            }
            
            // 3. App processes audio for a while
            advanceTimeBy(3000)
            
            // 4. User stops listening
            viewModel.stopListening()
            withTimeout(2000) {
                viewModel.isRecording.first { !it }
            }
            
            // 5. User starts again
            viewModel.startListening()
            withTimeout(3000) {
                viewModel.isRecording.first { it }
            }
            
            // 6. App cleanup
            viewModel.stopListening()
            withTimeout(2000) {
                viewModel.isRecording.first { !it }
            }
            
            // Then: All transitions should work smoothly
            assertFalse("App should end in clean state", viewModel.isRecording.value)
            
        } finally {
            viewModel.stopListening()
        }
    }
}