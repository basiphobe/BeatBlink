# BeatBlink Testing Setup

## Overview
This document describes the comprehensive testing infrastructure added to the BeatBlink Android app, providing unit tests, integration tests, and instrumentation tests for reliable code quality and regression detection.

## Test Structure

### Unit Tests (`app/src/test/`)
Located in `app/src/test/java/com/beatblink/`:

1. **AudioBeatDetectorTest.kt**
   - Tests algorithmic components of beat detection
   - Validates BPM calculation, median computation, energy thresholds
   - Covers parameter validation and software gain application
   - **7 test methods** focusing on mathematical correctness

2. **MainViewModelTest.kt**
   - Tests ViewModel behavior and StateFlow management
   - Validates reactive patterns and lifecycle handling
   - Covers audio processing parameters and timing logic
   - **5 test methods** focusing on architecture patterns

### Integration/UI Tests (`app/src/androidTest/`)
Located in `app/src/androidTest/java/com/beatblink/`:

1. **MainActivityTest.kt**
   - Tests Compose UI interactions and accessibility
   - Validates button behavior, state visualization, permission handling
   - Covers beat animation, BPM display, and audio level meter
   - Requires Android emulator/device to run

2. **BeatBlinkIntegrationTest.kt**
   - Tests end-to-end audio pipeline integration
   - Validates real microphone access and state synchronization
   - Covers performance testing and memory usage
   - Requires Android emulator/device with microphone permissions

## Test Dependencies

### Testing Framework
```kotlin
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito:mockito-core:5.7.0")
testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
testImplementation("io.mockk:mockk:1.13.8")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
testImplementation("androidx.arch.core:core-testing:2.2.0")

androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
androidTestImplementation("androidx.test:runner:1.5.2")
androidTestImplementation("androidx.test:rules:1.5.0")
androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
```

### Configuration Updates
- **JVM Target**: Upgraded from Java 8 to Java 11 to support modern testing libraries
- **Coroutines Testing**: Using `UnconfinedTestDispatcher` for immediate execution in tests
- **Mock Framework**: Combination of Mockito and MockK for comprehensive mocking capabilities

## Running Tests

### Unit Tests
```bash
./gradlew testDebugUnitTest
```
- Runs quickly (< 1 second)
- No Android dependencies required
- Tests algorithmic and architectural components

### Integration/UI Tests
```bash
./gradlew connectedAndroidTest
```
- Requires Android emulator or physical device
- Tests full application stack with real audio system
- May require microphone permissions

### Build with Tests
```bash
./gradlew assembleDebug testDebugUnitTest
```
- Builds debug APK and runs unit tests
- Recommended for CI/CD pipeline integration

## Test Coverage

### Current Coverage Areas
✅ **BPM Calculation**: Interval timing, median computation, range validation  
✅ **Energy Detection**: Threshold calculation, software gain, refractory periods  
✅ **State Management**: StateFlow behavior, reactive patterns, lifecycle  
✅ **UI Architecture**: ViewModel patterns, parameter validation  
✅ **Integration Points**: End-to-end workflows (Android tests)  

### Testing Approach
- **Unit Tests**: Focus on pure functions and business logic without Android dependencies
- **Integration Tests**: Test real Android audio system integration with actual permissions
- **Mocking Strategy**: Mock Android-specific components, test algorithmic logic directly
- **Coroutine Testing**: Use test dispatchers for predictable async behavior

## CI/CD Integration

The unit tests are designed to run in any CI/CD environment without Android emulator requirements. Integration tests can be added to CI/CD with Android emulator support.

### Recommended CI Commands
```bash
# Fast feedback loop
./gradlew testDebugUnitTest

# Full verification (requires emulator)
./gradlew testDebugUnitTest connectedDebugAndroidTest
```

## Maintenance Notes

1. **Test Isolation**: Unit tests are completely isolated from Android runtime
2. **Performance**: Unit tests complete in < 1 second for rapid feedback
3. **Reliability**: Tests use deterministic algorithms without timing dependencies
4. **Extensibility**: Test structure supports easy addition of new test cases
5. **Documentation**: Each test method includes descriptive names and validation comments