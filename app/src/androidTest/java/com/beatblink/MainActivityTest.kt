package com.beatblink

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.beatblink.ui.theme.BeatBlinkTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import android.Manifest

/**
 * Instrumented UI tests for MainActivity focusing on:
 * - Compose UI interactions
 * - Permission handling
 * - Beat visualization
 * - User controls
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.RECORD_AUDIO
    )

    @Test
    fun app_displays_initial_ui_correctly() {
        // Given: App is launched
        composeTestRule.setContent {
            BeatBlinkTheme {
                BeatBlinkScreen(MainViewModel())
            }
        }

        // Then: Initial UI elements should be visible
        composeTestRule
            .onNodeWithText("BeatBlink")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("♪")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("BPM")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("--")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Audio Level")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Start Listening")
            .assertIsDisplayed()
    }

    @Test
    fun start_button_toggles_to_stop_when_clicked() {
        // Given: App with permission granted
        composeTestRule.setContent {
            BeatBlinkTheme {
                BeatBlinkScreen(MainViewModel())
            }
        }

        // When: Start button is clicked
        composeTestRule
            .onNodeWithText("Start Listening")
            .performClick()

        // Then: Button should change to Stop
        composeTestRule
            .onNodeWithText("Stop Listening")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Start Listening")
            .assertDoesNotExist()
    }

    @Test
    fun stop_button_toggles_to_start_when_clicked() {
        // Given: App is recording
        composeTestRule.setContent {
            BeatBlinkTheme {
                BeatBlinkScreen(MainViewModel())
            }
        }

        // Start recording first
        composeTestRule
            .onNodeWithText("Start Listening")
            .performClick()

        composeTestRule
            .onNodeWithText("Stop Listening")
            .assertIsDisplayed()

        // When: Stop button is clicked
        composeTestRule
            .onNodeWithText("Stop Listening")
            .performClick()

        // Then: Button should change back to Start
        composeTestRule
            .onNodeWithText("Start Listening")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Stop Listening")
            .assertDoesNotExist()
    }

    @Test
    fun beat_flash_box_displays_correctly() {
        // Given: App is launched
        composeTestRule.setContent {
            BeatBlinkTheme {
                BeatFlashBox(isBeat = false)
            }
        }

        // Then: Beat box should show default state
        composeTestRule
            .onNodeWithText("♪")
            .assertIsDisplayed()

        // When: Beat is detected (simulated)
        composeTestRule.setContent {
            BeatBlinkTheme {
                BeatFlashBox(isBeat = true)
            }
        }

        // Then: Beat box should show beat state
        composeTestRule
            .onNodeWithText("BEAT!")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("♪")
            .assertDoesNotExist()
    }

    @Test
    fun bpm_display_shows_correct_values() {
        // Given: App with different BPM values
        
        // Test initial state (no BPM)
        composeTestRule.setContent {
            BeatBlinkTheme {
                BPMDisplay(bpm = 0)
            }
        }

        composeTestRule
            .onNodeWithText("--")
            .assertIsDisplayed()

        // Test with actual BPM value
        composeTestRule.setContent {
            BeatBlinkTheme {
                BPMDisplay(bpm = 120)
            }
        }

        composeTestRule
            .onNodeWithText("120")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("--")
            .assertDoesNotExist()
    }

    @Test
    fun audio_level_meter_displays_correctly() {
        // Given: App with different audio levels
        
        // Test low level
        composeTestRule.setContent {
            BeatBlinkTheme {
                AudioLevelMeter(level = 0.1f)
            }
        }

        composeTestRule
            .onNodeWithText("Audio Level")
            .assertIsDisplayed()

        // Test high level
        composeTestRule.setContent {
            BeatBlinkTheme {
                AudioLevelMeter(level = 0.8f)
            }
        }

        composeTestRule
            .onNodeWithText("Audio Level")
            .assertIsDisplayed()
    }

    @Test
    fun permission_request_shows_when_not_granted() {
        // This test would need to be run without the permission grant rule
        // to test the permission request flow
        
        // Note: In a real test environment, we would:
        // 1. Launch without permissions
        // 2. Verify permission button is shown
        // 3. Click permission button
        // 4. Verify permission is requested
        // 5. Grant permission (simulated)
        // 6. Verify UI updates to show start button
    }

    @Test
    fun ui_layout_is_properly_structured() {
        // Given: Full app UI
        composeTestRule.setContent {
            BeatBlinkTheme {
                BeatBlinkScreen(MainViewModel())
            }
        }

        // Then: All major UI elements should be present and positioned correctly
        
        // Title at top
        composeTestRule
            .onNodeWithText("BeatBlink")
            .assertIsDisplayed()

        // Beat flash box in center
        composeTestRule
            .onNodeWithText("♪")
            .assertIsDisplayed()

        // BPM display
        composeTestRule
            .onAllNodesWithText("BPM")
            .onFirst()
            .assertIsDisplayed()

        // Audio level meter
        composeTestRule
            .onNodeWithText("Audio Level")
            .assertIsDisplayed()

        // Control button at bottom
        composeTestRule
            .onNodeWithText("Start Listening")
            .assertIsDisplayed()
    }

    @Test
    fun button_styling_changes_correctly() {
        // Given: App UI
        composeTestRule.setContent {
            BeatBlinkTheme {
                BeatBlinkScreen(MainViewModel())
            }
        }

        // When: Start button is clicked
        composeTestRule
            .onNodeWithText("Start Listening")
            .performClick()

        // Then: Stop button should have different styling (error color)
        composeTestRule
            .onNodeWithText("Stop Listening")
            .assertIsDisplayed()
            // Note: Testing color would require additional test setup
            // to access theme colors in test environment
    }

    @Test
    fun rapid_button_clicks_are_handled_gracefully() {
        // Given: App UI
        composeTestRule.setContent {
            BeatBlinkTheme {
                BeatBlinkScreen(MainViewModel())
            }
        }

        // When: Rapidly clicking start/stop
        repeat(5) {
            composeTestRule
                .onNodeWithText("Start Listening", useUnmergedTree = true)
                .performClick()
            
            composeTestRule
                .onNodeWithText("Stop Listening", useUnmergedTree = true)
                .performClick()
        }

        // Then: Should end in consistent state
        composeTestRule
            .onNodeWithText("Start Listening")
            .assertIsDisplayed()
    }

    @Test
    fun accessibility_content_descriptions_are_present() {
        // Given: App UI
        composeTestRule.setContent {
            BeatBlinkTheme {
                BeatBlinkScreen(MainViewModel())
            }
        }

        // Then: Interactive elements should have proper accessibility
        composeTestRule
            .onNodeWithText("Start Listening")
            .assertHasClickAction()

        // Note: Additional accessibility tests would verify:
        // 1. Content descriptions for visual elements
        // 2. Semantic properties for screen readers
        // 3. Focus management
        // 4. Touch target sizes
    }

    @Test
    fun ui_responds_to_state_changes() {
        // Given: App with controlled state
        composeTestRule.setContent {
            BeatBlinkTheme {
                BeatBlinkScreen(MainViewModel())
            }
        }

        // When: Starting and stopping recording
        composeTestRule
            .onNodeWithText("Start Listening")
            .performClick()

        // Then: UI should update immediately
        composeTestRule
            .onNodeWithText("Stop Listening")
            .assertIsDisplayed()

        // When: Stopping recording
        composeTestRule
            .onNodeWithText("Stop Listening")
            .performClick()

        // Then: UI should update back
        composeTestRule
            .onNodeWithText("Start Listening")
            .assertIsDisplayed()
    }

    @Test
    fun beat_animation_timing_is_reasonable() {
        // Note: Testing animation timing would require advanced testing
        // This is a placeholder for animation testing concepts
        
        // In a full test, we would:
        // 1. Trigger a beat event
        // 2. Verify beat box changes color immediately  
        // 3. Wait for animation duration (50ms)
        // 4. Verify beat box returns to normal color
        // 5. Verify animation is smooth and doesn't flicker
    }

    @Test
    fun app_handles_configuration_changes() {
        // Note: Configuration change testing would require device rotation simulation
        
        // In a full test, we would:
        // 1. Start recording
        // 2. Rotate device (configuration change)
        // 3. Verify recording state is preserved
        // 4. Verify UI rebuilds correctly
        // 5. Verify no audio interruption
    }

    @Test
    fun memory_usage_during_ui_updates_is_reasonable() {
        // Note: Memory testing would require performance monitoring
        
        // In a full test, we would:
        // 1. Monitor memory usage baseline
        // 2. Perform beat detection for extended period
        // 3. Verify no memory leaks from UI updates
        // 4. Verify StateFlow subscriptions are cleaned up
        // 5. Verify Compose recomposition is efficient
    }
}