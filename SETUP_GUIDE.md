# BeatBlink - Complete Setup Guide for Beginners

This guide will walk you through setting up Android development on your Linux machine, building the app, and running it on your phone.

## Step 1: Install Android Studio (Easiest Method)

Android Studio includes everything you need: Android SDK, emulators, and debugging tools.

### Download and Install

1. Go to: https://developer.android.com/studio
2. Download Android Studio for Linux
3. Extract the downloaded file:
   ```fish
   tar -xzf android-studio-*.tar.gz
   cd android-studio/bin
   ./studio.sh
   ```

4. Follow the setup wizard - it will install:
   - Android SDK
   - Android SDK Platform Tools (includes `adb`)
   - Android Emulator (optional, for testing without a physical device)

### Set Environment Variables

After Android Studio installation, add these to your fish config:

```fish
# Edit your fish config
nano ~/.config/fish/config.fish

# Add these lines at the end:
set -gx ANDROID_HOME $HOME/Android/Sdk
set -gx PATH $PATH $ANDROID_HOME/tools $ANDROID_HOME/platform-tools $ANDROID_HOME/cmdline-tools/latest/bin
```

Then reload:
```fish
source ~/.config/fish/config.fish
```

Verify it works:
```fish
echo $ANDROID_HOME  # Should show /home/aaron/Android/Sdk
adb version         # Should show Android Debug Bridge version
```

---

## Step 2: Prepare Your Android Phone

### Enable Developer Mode

1. Open **Settings** on your Android phone
2. Scroll to **About phone**
3. Find **Build number** and tap it **7 times**
4. You'll see a message: "You are now a developer!"

### Enable USB Debugging

1. Go back to **Settings**
2. Find **Developer options** (usually under System or About)
3. Enable **USB debugging**

### Connect Your Phone

1. Connect your phone to your computer via USB cable
2. On your phone, you'll see a popup asking "Allow USB debugging?" - tap **OK**
3. Check the box "Always allow from this computer"

Verify connection:
```fish
adb devices
```

You should see something like:
```
List of devices attached
ABC123456789    device
```

If it says "unauthorized", check your phone for the USB debugging prompt.

---

## Step 3: Build the BeatBlink App

Now that everything is set up, building is simple!

### Build Debug APK

From the BeatBlink project directory:

```fish
# Make gradlew executable (first time only)
chmod +x gradlew

# Build the app
./gradlew assembleDebug
```

This will:
- Download all dependencies (first time only - takes a few minutes)
- Compile the Kotlin code
- Package everything into an APK file

The built APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

---

## Step 4: Install and Run on Your Phone

### Install the App

```fish
./gradlew installDebug
```

This installs the app directly to your connected phone.

### Launch the App

1. Look for "BeatBlink" in your app drawer
2. Tap to open
3. Grant microphone permission when prompted
4. Tap "Start" to begin beat detection

---

## Step 5: Test the App

1. **Play some music** near your phone (from a speaker or another device)
2. **Watch the box** - it should flash on beats
3. **Check the meter** - should move with audio levels
4. **Wait for BPM** - should stabilize to a reasonable number (80-160 typically)

### Troubleshooting

**No beats detected:**
- Make sure music is playing loud enough
- Check microphone permission was granted
- Try music with clear, strong bass drums

**Box flashing too often:**
- The music might have very strong high frequencies
- Try adjusting audio source or volume

**App crashes:**
- Check logcat output (see Debugging section below)

---

## Debugging with Logcat

View real-time logs from your app:

```fish
adb logcat | grep BeatBlink
```

This shows all debug messages from the app. Look for:
- "Beat detected!" - confirms beats are being found
- "BPM updated" - shows tempo calculations
- Any error messages

To clear old logs:
```fish
adb logcat -c  # Clear
adb logcat | grep BeatBlink  # Start fresh
```

---

## Common Commands Cheat Sheet

```fish
# Check connected devices
adb devices

# Build app
./gradlew assembleDebug

# Install app
./gradlew installDebug

# Build + Install in one command
./gradlew installDebug

# View logs
adb logcat | grep BeatBlink

# Uninstall app
adb uninstall com.beatblink

# Take screenshot (saves to computer)
adb exec-out screencap -p > screenshot.png

# Record screen
adb shell screenrecord /sdcard/demo.mp4
# Press Ctrl+C to stop, then pull the file:
adb pull /sdcard/demo.mp4
```

---

## VS Code Extensions (Optional but Recommended)

Install these VS Code extensions for better Kotlin/Android development:

1. **Kotlin Language** (by Mathias Fröhlich)
2. **Android iOS Emulator** (by DiemasMichiels)
3. **Gradle for Java** (by Microsoft)

---

## Next Steps

Once the app is running successfully:

1. Try tweaking the beat detection parameters in `AudioBeatDetector.kt`
2. Experiment with different music genres
3. Check out the code comments to understand how it works
4. When ready, we can add settings UI or GIF playback!

---

## Need Help?

If something isn't working:

1. Check `adb devices` - is your phone connected?
2. Check logcat - any error messages?
3. Try `./gradlew clean` then `./gradlew assembleDebug` again
4. Make sure USB debugging is enabled on your phone

Happy coding! 🎵
