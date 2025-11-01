# BeatBlink Android CI/CD Pipeline

This document explains the automated build and release process for the BeatBlink Android app using GitHub Actions.

## 🚀 Features

- **Automated Testing**: Runs unit tests and lint checks on every push and PR
- **Multi-Build Support**: Generates both debug and release APKs
- **Security Scanning**: Includes security analysis with Qark
- **Automatic Releases**: Creates GitHub releases with signed APKs on version tags
- **Artifact Management**: Stores build artifacts for easy download
- **Code Quality**: Enforces linting and generates quality reports

## 📋 Pipeline Overview

The CI/CD pipeline consists of several jobs:

### 1. **Test Job**
- Runs on every push and PR
- Executes unit tests
- Uploads test reports as artifacts

### 2. **Lint Job**  
- Runs code quality checks
- Generates lint reports
- Uploads results for review

### 3. **Build Debug Job**
- Builds debug APK for testing
- Runs on every push to main/develop
- Uploads debug APK as artifact

### 4. **Build Release Job**
- Builds signed release APK and AAB
- Only runs on version tags (v*)
- Requires signing secrets to be configured

### 5. **Create Release Job**
- Creates GitHub release with changelog
- Attaches signed APK and AAB files
- Only runs on version tags

### 6. **Security Scan Job**
- Scans the built APK for security issues
- Generates security reports

## 🔧 Setup Instructions

### Step 1: Run the Setup Script

```bash
# Make sure you're in the BeatBlink repository root
cd /path/to/BeatBlink
./setup-ci-cd.sh
```

This script will:
- Create or help you configure an Android keystore
- Generate base64 encoded keystore for GitHub
- Provide the exact secrets you need to add to GitHub

### Step 2: Add GitHub Secrets

Go to your repository settings and add these secrets:

| Secret Name | Description | Example |
|-------------|-------------|---------|
| `KEYSTORE_BASE64` | Base64 encoded keystore file | Output from setup script |
| `KEYSTORE_PASSWORD` | Password for the keystore | `mySecurePassword123` |
| `KEY_ALIAS` | Alias for the signing key | `beatblink` |
| `KEY_PASSWORD` | Password for the signing key | `myKeyPassword123` |

**To add secrets:**
1. Go to `https://github.com/basiphobe/BeatBlink/settings/secrets/actions`
2. Click "New repository secret"
3. Add each secret with the exact name and value from the setup script

### Step 3: Update build.gradle.kts

Add the signing configuration to your `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS") 
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### Step 4: Commit and Push

```bash
git add .
git commit -m "Add CI/CD pipeline and signing configuration"
git push origin main
```

## 🏷️ Creating Releases

### Automatic Releases (Recommended)

1. **Create a version tag:**
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. **The pipeline will automatically:**
   - Run all tests and quality checks
   - Build signed APK and AAB files
   - Create a GitHub release
   - Upload files as release assets

### Manual Releases

If you prefer manual control:

```bash
# Build debug locally
./gradlew assembleDebug

# Build release locally (requires environment variables)
export KEYSTORE_PASSWORD='your_password'
export KEY_ALIAS='your_alias'
export KEY_PASSWORD='your_key_password'  
./gradlew assembleRelease
```

## 📁 Build Outputs

The pipeline generates several artifacts:

| Artifact | Description | When Created |
|----------|-------------|--------------|
| `debug-apk` | Debug APK for testing | Every push |
| `release-apk` | Signed release APK | Version tags only |
| `release-aab` | Android App Bundle | Version tags only |
| `test-results` | Unit test reports | Every run |
| `lint-results` | Code quality reports | Every run |
| `security-scan-results` | Security analysis | After debug builds |

## 🔍 Monitoring Builds

### View Build Status
- Go to the "Actions" tab in your GitHub repository
- Click on any workflow run to see detailed logs
- Green checkmark = success, red X = failure

### Download Artifacts
1. Go to a completed workflow run
2. Scroll down to "Artifacts" section  
3. Click to download any generated files

### Check Release Files
- Go to the "Releases" section of your repository
- Each tagged version will have APK and AAB files attached

## 🛠️ Troubleshooting

### Common Issues

**Build fails with "Keystore not found"**
- Make sure you've added the `KEYSTORE_BASE64` secret correctly
- Verify the base64 encoding is complete (no line breaks)

**Signing fails**
- Check that all four secrets are added with exact names
- Verify keystore passwords are correct
- Ensure the key alias exists in the keystore

**Tests fail**
- Check the test logs in the workflow run
- Fix any failing unit tests locally first
- Make sure all dependencies are properly configured

**Lint errors**
- Review the lint report artifact
- Fix code quality issues locally
- Update lint rules if needed

### Debug Steps

1. **Check workflow logs:**
   - Click on the failing job in GitHub Actions
   - Expand each step to see detailed output

2. **Test locally:**
   ```bash
   # Run the same commands locally
   ./gradlew clean
   ./gradlew testDebugUnitTest
   ./gradlew lintDebug
   ./gradlew assembleDebug
   ```

3. **Verify secrets:**
   - Go to repository Settings > Secrets
   - Ensure all required secrets are present
   - Re-add secrets if there are any doubts

## 🔐 Security Best Practices

- ✅ Keystore file is never committed to Git (added to .gitignore)
- ✅ All sensitive data stored as GitHub secrets
- ✅ Secrets are only accessible to authorized workflows  
- ✅ Release builds use ProGuard for code obfuscation
- ✅ Security scanning is included in the pipeline

## 🎯 Next Steps

Once your CI/CD is working:

1. **Add more tests** to increase coverage
2. **Set up Play Store deployment** for automatic publishing
3. **Add code coverage reporting** with tools like Codecov
4. **Configure branch protection rules** to require CI checks
5. **Add integration tests** with Android Test Orchestrator

## 📞 Support

If you encounter issues:

1. Check the troubleshooting section above
2. Review GitHub Actions logs for detailed error messages
3. Ensure all prerequisites are met (JDK, Android SDK, etc.)
4. Verify that your local build works before debugging CI/CD

---

**Happy building! 🎵**