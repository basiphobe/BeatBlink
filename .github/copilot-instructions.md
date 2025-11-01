# BeatBlink Project Instructions

This is an Android Kotlin Jetpack Compose app that provides live audio-reactive beat detection.

## Architecture
- **MainActivity**: Entry point, hosts Compose UI
- **MainViewModel**: State management with StateFlow for beat events, BPM, audio level
- **AudioBeatDetector**: Real-time audio capture via AudioRecord, professional beat detection using TarsosDSP library
- **Compose UI**: Flashing box on beats, level meter, BPM display, start/stop controls

## Audio Configuration
- Sample rate: 44.1 kHz
- Format: PCM 16-bit mono
- Source: Microphone only (AudioRecord)
- Buffer size: 2048 samples
- Min SDK: 26, Target: Latest stable

## Beat Detection Algorithm
Uses TarsosDSP library (v2.5) with ComplexOnsetDetector via AudioDispatcher:
- Professional-grade spectral onset detection
- Custom AudioRecordInputStream wraps AudioRecord for TarsosDSP compatibility
- AudioDispatcher manages processing pipeline: LevelMonitor → ComplexOnsetDetector
- Adaptive threshold and sensitivity parameters
- Refractory period (minimum interval between beats)
- BPM estimation from recent inter-beat intervals using median filtering

## Key Files
- `app/src/main/java/com/beatblink/MainActivity.kt`: Main activity and UI
- `app/src/main/java/com/beatblink/MainViewModel.kt`: State management
- `app/src/main/java/com/beatblink/AudioBeatDetector.kt`: TarsosDSP-based beat detection with AudioDispatcher
- `app/src/main/AndroidManifest.xml`: Permissions and app config
- `app/build.gradle.kts`: Dependencies and SDK versions
- `settings.gradle.kts`: Maven repositories including TarsosDSP repo

## Tuning Parameters
Located in AudioBeatDetector:
- `ONSET_THRESHOLD`: Beat sensitivity (default: 0.1, lower = more sensitive, TarsosDSP default is 8.0)
- `ONSET_SENSITIVITY`: Response time (default: 1.0, lower = faster)
- `MIN_BEAT_INTERVAL_MS`: Refractory period between beats (default: 200ms)

## Build Instructions
See README.md for build/run commands from VS Code terminal.
