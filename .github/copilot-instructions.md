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
- AudioDispatcher manages processing pipeline: LowPassFS (200Hz) → LevelMonitor → ComplexOnsetDetector
- **Bass filtering**: 200Hz lowpass filter isolates kick drum frequencies before energy detection
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
- `BASS_FILTER_CUTOFF`: Lowpass filter frequency (200Hz - isolates kick drum fundamentals 20-100Hz)
- `ONSET_THRESHOLD`: Spectral sensitivity (0.3, lower = more sensitive)
- `ONSET_SENSITIVITY`: Response time (1.0, lower = faster)
- `MIN_BEAT_INTERVAL_MS`: Refractory period between beats (250ms)
- `ENERGY_THRESHOLD`: Energy spike multiplier (1.15 = 15% above rolling average)
- `ENERGY_MIN_LEVEL`: Minimum amplitude threshold (0.08 after 3x gain)
- `SOFTWARE_GAIN`: Fixed at 3.0x (user-validated perfect, do not modify)

## Build Instructions
See README.md for build/run commands from VS Code terminal.
