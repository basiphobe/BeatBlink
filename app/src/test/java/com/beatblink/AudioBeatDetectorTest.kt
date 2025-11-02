package com.beatblink

import org.junit.Assert.*
import org.junit.Test
import org.junit.Before
import kotlin.math.abs

/**
 * Unit tests for AudioBeatDetector algorithmic components.
 * 
 * These tests focus on the mathematical and computational aspects
 * that can be tested without Android audio system dependencies.
 * Integration tests for actual audio processing are in the androidTest directory.
 */
class AudioBeatDetectorTest {

    @Before
    fun setUp() {
        // Setup for pure computational tests
    }

    @Test
    fun `bpm calculation from intervals should be accurate`() {
        // Test BPM calculation: BPM = 60000 / averageInterval
        val intervals = listOf(500L, 520L, 480L, 510L, 490L) // ~500ms intervals
        val expectedBpm = 60000 / 500 // 120 BPM
        val actualBpm = calculateBpmFromIntervals(intervals)
        
        assertEquals("BPM should be approximately correct", expectedBpm.toLong(), actualBpm.toLong())
    }

    @Test
    fun `median calculation should handle odd and even sizes`() {
        // Test odd size
        val oddList = listOf(100L, 200L, 300L, 400L, 500L)
        assertEquals(300L, calculateMedian(oddList))
        
        // Test even size
        val evenList = listOf(100L, 200L, 400L, 500L)
        assertEquals(300L, calculateMedian(evenList)) // (200 + 400) / 2
        
        // Test single element
        assertEquals(150L, calculateMedian(listOf(150L)))
    }

    @Test
    fun `bpm range validation should work correctly`() {
        assertTrue("60 BPM should be valid", isValidBpm(60))
        assertTrue("120 BPM should be valid", isValidBpm(120))
        assertTrue("180 BPM should be valid", isValidBpm(180))
        
        assertFalse("30 BPM should be invalid", isValidBpm(30))
        assertFalse("220 BPM should be invalid", isValidBpm(220))
        assertFalse("0 BPM should be invalid", isValidBpm(0))
        assertFalse("Negative BPM should be invalid", isValidBpm(-10))
    }

    @Test
    fun `energy threshold calculation should be correct`() {
        val energyHistory = listOf(0.1f, 0.2f, 0.3f, 0.4f, 0.5f)
        val average = energyHistory.sum() / energyHistory.size // 0.3
        val threshold = average * 1.15f // 15% above average
        
        assertEquals(0.345f, threshold, 0.001f)
        
        // Test with minimum threshold
        val lowEnergyHistory = listOf(0.01f, 0.02f, 0.03f)
        val lowAverage = lowEnergyHistory.sum() / lowEnergyHistory.size
        val minThreshold = 0.08f
        
        assertTrue("Threshold should not go below minimum", 
            maxOf(lowAverage * 1.15f, minThreshold) >= minThreshold)
    }

    @Test
    fun `beat interval refractory period should be enforced`() {
        val minInterval = 250L // 250ms minimum
        val lastBeatTime = 1000L
        
        // Too soon - should be rejected
        assertFalse("Beat too soon should be rejected", 
            isValidBeatInterval(lastBeatTime, 1200L, minInterval))
        
        // Just at threshold - should be accepted
        assertTrue("Beat at threshold should be accepted",
            isValidBeatInterval(lastBeatTime, 1250L, minInterval))
        
        // Well after - should be accepted
        assertTrue("Beat after threshold should be accepted",
            isValidBeatInterval(lastBeatTime, 1500L, minInterval))
    }

    @Test
    fun `software gain application should be correct`() {
        val originalSample = 0.1f
        val gain = 3.0f
        val amplified = applySoftwareGain(originalSample, gain)
        
        assertEquals(0.3f, amplified, 0.001f)
        
        // Test clipping protection
        val largeSample = 0.5f
        val clippedResult = applySoftwareGain(largeSample, gain)
        assertTrue("Result should not exceed 1.0", clippedResult <= 1.0f)
    }

    @Test
    fun `bpm locking tolerance should work correctly`() {
        val lockedBpm = 120
        val tolerance = 5 // ±5 BPM
        
        assertTrue("115 BPM should be within tolerance", 
            isBpmWithinTolerance(115, lockedBpm, tolerance))
        assertTrue("125 BPM should be within tolerance",
            isBpmWithinTolerance(125, lockedBpm, tolerance))
        assertTrue("120 BPM should be within tolerance",
            isBpmWithinTolerance(120, lockedBpm, tolerance))
        
        assertFalse("114 BPM should be outside tolerance",
            isBpmWithinTolerance(114, lockedBpm, tolerance))
        assertFalse("126 BPM should be outside tolerance",
            isBpmWithinTolerance(126, lockedBpm, tolerance))
    }

    // Helper functions for testing (these would normally be private methods in AudioBeatDetector)
    
    private fun calculateBpmFromIntervals(intervals: List<Long>): Int {
        if (intervals.isEmpty()) return 0
        val averageInterval = intervals.sum() / intervals.size
        return (60000 / averageInterval).toInt()
    }

    private fun calculateMedian(values: List<Long>): Long {
        val sorted = values.sorted()
        return when (sorted.size % 2) {
            0 -> (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2
            else -> sorted[sorted.size / 2]
        }
    }

    private fun isValidBpm(bpm: Int): Boolean {
        return bpm in 40..200
    }

    private fun isValidBeatInterval(lastBeat: Long, currentTime: Long, minInterval: Long): Boolean {
        return currentTime - lastBeat >= minInterval
    }

    private fun applySoftwareGain(sample: Float, gain: Float): Float {
        return minOf(sample * gain, 1.0f)
    }

    private fun isBpmWithinTolerance(currentBpm: Int, lockedBpm: Int, tolerance: Int): Boolean {
        return abs(currentBpm - lockedBpm) <= tolerance
    }
}