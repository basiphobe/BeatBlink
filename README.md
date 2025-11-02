# BeatBlink - Live Audio-Reactive Beat Visualizer

An Android app that performs real-time beat detection from live microphone input and provides visual feedback through a flashing UI.

## Features

- **Real-time Beat Detection**: Energy/onset approach with adaptive threshold
- **Live Audio Capture**: AudioRecord at 44.1 kHz, 16-bit PCM mono
- **Visual Feedback**: Flashing box that reacts to detected beats
- **Audio Level Meter**: Shows normalized RMS of input audio
- **BPM Estimation**: Calculates tempo from inter-beat intervals with outlier rejection
- **Clean Architecture**: Activity → ViewModel → Detector pattern

## Requirements

- Android device or emulator with API 26+ (Android 8.0 Oreo or higher)
- Microphone access
- Android SDK and Gradle (for building)

## Building from VS Code Terminal

### 1. Ensure you have Android SDK installed

Set the `ANDROID_HOME` environment variable:

```bash
# For Linux/macOS (add to ~/.bashrc or ~/.zshrc or ~/.config/fish/config.fish)
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

### 2. Build the project

```bash
# Clean and build debug APK
./gradlew clean assembleDebug

# Or on Windows
# gradlew.bat clean assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

### 3. Install on connected device/emulator

```bash
# Install on connected device
./gradlew installDebug

# Or manually with adb
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 4. Run the app

```bash
# Launch on device (requires device/emulator connected)
./gradlew installDebug
adb shell am start -n com.beatblink/.MainActivity
```

## Usage

1. **Grant Permission**: On first launch, tap "Grant Microphone Permission" and allow access
2. **Start Listening**: Tap the "Start Listening" button
3. **Play Music**: Play music near the device or tap rhythmically
4. **Observe**:
   - The large box flashes on detected beats
   - BPM value updates as it detects your tempo
   - Audio level meter shows input amplitude
5. **Stop**: Tap "Stop Listening" to release the microphone

## Architecture

```
MainActivity (Compose UI)
    ↓
MainViewModel (StateFlow)
    ↓
AudioBeatDetector (AudioRecord + Beat Detection)
```

### Key Components

#### AudioBeatDetector.kt
- Captures live audio via `AudioRecord`
- Computes short-time energy from audio samples
- Maintains adaptive threshold: `threshold = mean + multiplier × √variance`
- Detects beats when energy exceeds threshold
- Applies refractory period to avoid double-detections
- Estimates BPM from recent inter-beat intervals

#### MainViewModel.kt
- Manages detector lifecycle
- Exposes state via `StateFlow`: `isBeat`, `bpm`, `audioLevel`, `isRecording`
- Survives configuration changes

#### MainActivity.kt
- Jetpack Compose UI
- Requests runtime microphone permission
- Displays flashing box, level meter, BPM, and controls

## Tuning Beat Detection

Edit `AudioBeatDetector.kt` to adjust these constants:

```kotlin
// Sensitivity: Higher = less sensitive to beats (default: 1.5)
private const val ENERGY_THRESHOLD_MULTIPLIER = 1.5f

// History window for adaptive threshold (default: 43 ~= 1 second)
private const val HISTORY_SIZE = 43

// Minimum interval between beats in ms (default: 300 = 200 BPM max)
private const val MIN_BEAT_INTERVAL_MS = 300L
```

### Tuning Tips

- **Too many false beats?** Increase `ENERGY_THRESHOLD_MULTIPLIER` (try 2.0 or 2.5)
- **Missing beats?** Decrease `ENERGY_THRESHOLD_MULTIPLIER` (try 1.2 or 1.0)
- **Threshold too jittery?** Increase `HISTORY_SIZE` for more smoothing
- **Double-triggering on single beats?** Increase `MIN_BEAT_INTERVAL_MS`

## Acceptance Tests

✅ With music playing, the box flashes roughly in sync with kick drums  
✅ Level meter responds to loud/quiet passages  
✅ BPM stabilizes to plausible range (80-160) after a few seconds  
✅ Start/Stop works and releases microphone properly  

## Testing

The app includes comprehensive unit tests and integration tests:

```bash
# Run unit tests (fast, no emulator required)
./gradlew testDebugUnitTest

# Run integration tests (requires emulator/device with microphone)
./gradlew connectedDebugAndroidTest

# Build and test
./gradlew assembleDebug testDebugUnitTest
```

### Test Coverage
- **Unit Tests**: Beat detection algorithms, BPM calculation, state management
- **Integration Tests**: Full audio pipeline, UI interactions, permissions
- **12 test methods** covering mathematical correctness and architecture patterns

See [TESTING.md](TESTING.md) for detailed testing documentation.

## Project Structure

```
BeatBlink/
├── app/
│   ├── build.gradle.kts          # App dependencies and SDK versions
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml   # Permissions and app config
│   │   │   ├── java/com/beatblink/
│   │   │   │   ├── MainActivity.kt   # Main UI and Compose screens
│   │   │   │   ├── MainViewModel.kt  # State management
│   │   │   │   ├── AudioBeatDetector.kt  # Beat detection logic
│   │   │   │   └── ui/theme/         # Compose theme
│   │   │   └── res/                  # Resources (strings, colors, etc.)
│   │   ├── test/                     # Unit tests (no Android dependencies)
│   │   └── androidTest/              # Integration tests (requires emulator)
├── build.gradle.kts              # Root project config
├── settings.gradle.kts           # Project settings
├── TESTING.md                    # Testing documentation
└── README.md                     # This file
```

## Future Extensions

The code is structured to support:

- **Settings Panel**: Add UI to tune threshold, history, and interval parameters
- **Spectral Flux Detector**: Implement alternative beat detection algorithm
- **GIF Playback**: Add animated GIF that reacts to beats (planned next milestone)
- **Audio Source Selection**: Switch between MIC, VOICE_RECOGNITION, etc.

## Troubleshooting

### Build errors
- Ensure Android SDK is installed and `ANDROID_HOME` is set
- Check that Gradle wrapper has execute permissions: `chmod +x gradlew`

### No beat detection
- Grant microphone permission in app settings
- Check audio level meter responds to sound
- Try adjusting `ENERGY_THRESHOLD_MULTIPLIER` down (e.g., 1.2)

### Choppy/laggy UI
- Beat detection runs on background thread (IO dispatcher)
- UI updates are minimal (50ms flash on beat)
- Check device performance or reduce buffer processing

## License

This project is for educational/demonstration purposes.

---

**Built with**: Kotlin, Jetpack Compose, AudioRecord, StateFlow
# Test trigger
