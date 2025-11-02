# BeatBlink Release Guide

This guide covers how to create and distribute releases for the BeatBlink Android app.

## 🚀 Quick Release Guide

The easiest way to create a release is using the automated release script:

```bash
./release.sh
```

This script will:
1. Check for uncommitted changes
2. Show recent commits for changelog
3. Bump version numbers automatically  
4. Create and push a git tag
5. Trigger CI/CD pipeline for signed APK building

## 📋 Release Process Overview

### 1. Local Release APK (Manual)

If you want to build a release APK locally:

```bash
# First time setup - generate keystore
keytool -genkey -v -keystore app/keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias beatblink

# Update gradle.properties with your passwords:
# KEYSTORE_PASSWORD=your_actual_password
# KEY_PASSWORD=your_actual_password  

# Build release APK
./build-release.sh
```

**Output**: `app/build/outputs/apk/release/app-release.apk`

### 2. Automated Release (Recommended)

For official releases that are signed and distributed:

```bash
# Create and push release tag
./release.sh

# Select release type:
# 1) Patch (1.0.1) - Bug fixes
# 2) Minor (1.1.0) - New features  
# 3) Major (2.0.0) - Breaking changes
# 4) Custom version
```

**What happens automatically:**
- ✅ Version bump in `app/build.gradle.kts`
- ✅ Git tag creation and push
- ✅ GitHub Actions CI/CD pipeline triggered
- ✅ Signed APK and AAB built in cloud
- ✅ GitHub Release created with artifacts
- ✅ Release notes generated from commits

## 🔐 Signing Configuration

### Local Development Signing

For local testing, you need a keystore:

```bash
# Generate keystore (one-time setup)
keytool -genkey -v -keystore app/keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias beatblink

# When prompted, enter:
# - Keystore password (remember this!)
# - Key password (remember this!) 
# - Your name/organization details
```

Update `gradle.properties`:
```properties
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=beatblink
KEY_PASSWORD=your_key_password
```

**Important**: Never commit `gradle.properties` or `app/keystore.jks` to Git!

### Production Signing (CI/CD)

Production releases are signed automatically in GitHub Actions using encrypted secrets:

- `KEYSTORE_PASSWORD` - Keystore password
- `KEY_ALIAS` - Key alias (usually "beatblink")  
- `KEY_PASSWORD` - Key password
- `KEYSTORE_BASE64` - Base64-encoded keystore file

These are configured in GitHub repository settings under Secrets and Variables.

## 📦 Release Artifacts

Each release creates multiple artifacts:

### APK Files
- **Debug APK**: `app-debug.apk` (for testing, not signed)
- **Release APK**: `app-release.apk` (signed, ready for sideloading)

### Android App Bundle (AAB)
- **Release AAB**: `app-release.aab` (for Google Play Store)

### Locations
- **Local builds**: `app/build/outputs/apk/release/`
- **CI/CD builds**: Attached to GitHub Release
- **Download links**: Available in GitHub Releases page

## 🏷️ Version Management

BeatBlink uses semantic versioning: `MAJOR.MINOR.PATCH`

### Version Components
- **MAJOR**: Breaking changes (1.0.0 → 2.0.0)
- **MINOR**: New features, backward compatible (1.0.0 → 1.1.0)  
- **PATCH**: Bug fixes, backward compatible (1.0.0 → 1.0.1)

### Automatic Version Bumping
The `release.sh` script automatically:
- Updates `versionName` in `build.gradle.kts`
- Increments `versionCode` for Play Store
- Creates git tag with `v` prefix (e.g., `v1.2.3`)

## 🔄 CI/CD Pipeline

GitHub Actions automatically:

### On Every Push
- ✅ Run unit tests (`./gradlew testDebugUnitTest`)
- ✅ Run lint checks
- ✅ Build debug APK
- ✅ Upload test results

### On Release Tags (`v*`)
- ✅ Run full test suite
- ✅ Build signed release APK and AAB
- ✅ Create GitHub Release
- ✅ Upload signed artifacts
- ✅ Generate release notes from commits

## 🛠️ Manual Commands

### Build Commands
```bash
# Debug builds
./gradlew assembleDebug
./gradlew installDebug

# Release builds  
./gradlew assembleRelease
./gradlew bundleRelease

# Testing
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

### Version Commands
```bash
# Check current version
grep versionName app/build.gradle.kts

# Check latest tag
git describe --tags --abbrev=0

# List all releases
git tag -l "v*"
```

## 📱 Distribution

### Direct Installation (APK)
```bash
# Install release APK on connected device
adb install app/build/outputs/apk/release/app-release.apk

# Or download from GitHub releases
# Install via file manager on Android device
```

### Google Play Store (AAB)
1. Upload `app-release.aab` to Google Play Console
2. Review and publish through Play Console
3. Users get updates via Play Store

## 🚦 Release Checklist

Before creating a release:

- [ ] All tests passing (`./gradlew testDebugUnitTest`)
- [ ] No lint errors
- [ ] Updated CHANGELOG or release notes
- [ ] Version bump appropriate for changes
- [ ] Tested on physical device
- [ ] Performance acceptable
- [ ] No debug logs in release build

## 🔍 Troubleshooting

### Common Issues

**Build fails with signing error:**
- Check keystore passwords in `gradle.properties`
- Verify `app/keystore.jks` exists
- Ensure alias matches (`beatblink`)

**Release script fails:**
- Commit or stash uncommitted changes
- Check git repository is clean
- Verify you have push permissions

**CI/CD pipeline fails:**
- Check GitHub Actions logs
- Verify repository secrets are configured
- Check for Android SDK compatibility issues

### Debug Commands
```bash
# Check signing configuration
./gradlew signingReport

# Verify APK is signed
jarsigner -verify -verbose app/build/outputs/apk/release/app-release.apk

# Check APK contents
aapt dump badging app/build/outputs/apk/release/app-release.apk
```

## 📞 Support

For release-related issues:
- Check GitHub Actions logs for CI/CD failures
- Review build output for local build issues  
- Ensure all dependencies are up to date
- Verify Android SDK and build tools versions