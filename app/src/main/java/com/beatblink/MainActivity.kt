package com.beatblink

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beatblink.ui.theme.BeatBlinkTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Main activity for BeatBlink audio-reactive visualizer.
 * 
 * Displays:
 * - Large flashing box that reacts to detected beats
 * - Audio input level meter
 * - BPM estimate
 * - Start/Stop controls
 */
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BeatBlinkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BeatBlinkScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BeatBlinkScreen(viewModel: MainViewModel) {
    val micPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    val isBeat by viewModel.isBeat.collectAsState()
    val bpm by viewModel.bpm.collectAsState()
    val audioLevel by viewModel.audioLevel.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Title
        Text(
            text = "BeatBlink",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp)
        )
        
        // Main content area
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Flashing beat box
            BeatFlashBox(isBeat = isBeat)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // BPM Display
            BPMDisplay(bpm = bpm)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Audio level meter
            AudioLevelMeter(level = audioLevel)
        }
        
        // Controls at bottom
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            if (!micPermissionState.status.isGranted) {
                Button(
                    onClick = { micPermissionState.launchPermissionRequest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Grant Microphone Permission")
                }
            } else {
                if (isRecording) {
                    Button(
                        onClick = { viewModel.stopListening() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Stop Listening")
                    }
                } else {
                    Button(
                        onClick = { viewModel.startListening() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Start Listening")
                    }
                }
            }
        }
    }
}

/**
 * Large box that flashes bright color on beat detection.
 */
@Composable
fun BeatFlashBox(isBeat: Boolean) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isBeat) Color(0xFFFF4081) else Color(0xFF303030),
        animationSpec = tween(durationMillis = 50),
        label = "beatFlash"
    )
    
    Box(
        modifier = Modifier
            .size(200.dp)
            .background(color = backgroundColor, shape = MaterialTheme.shapes.large),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isBeat) "BEAT!" else "♪",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Display current BPM estimate.
 */
@Composable
fun BPMDisplay(bpm: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "BPM",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (bpm > 0) bpm.toString() else "--",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Visual indicator of audio input level (0.0 to 1.0).
 */
@Composable
fun AudioLevelMeter(level: Float) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Audio Level",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LinearProgressIndicator(
            progress = level,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(16.dp),
            color = when {
                level < 0.3f -> Color.Green
                level < 0.7f -> Color.Yellow
                else -> Color.Red
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        Text(
            text = "${(level * 100).toInt()}%",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
