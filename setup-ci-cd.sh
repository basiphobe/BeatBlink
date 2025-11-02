#!/bin/bash

# BeatBlink CI/CD Setup Script
# This script helps you set up GitHub Actions secrets for Android app signing

echo "🎵 BeatBlink CI/CD Setup"
echo "======================="
echo ""

# Check if we're in a Git repository
if [ ! -d ".git" ]; then
    echo "❌ Error: This script must be run from the root of your Git repository"
    exit 1
fi

echo "This script will help you set up CI/CD for your BeatBlink Android app."
echo ""

# Step 1: Generate or locate keystore
echo "📋 Step 1: Android Keystore Setup"
echo "--------------------------------"
echo ""

KEYSTORE_FILE="app/keystore.jks"
KEYSTORE_PASSWORD=""
KEY_ALIAS=""
KEY_PASSWORD=""

if [ -f "$KEYSTORE_FILE" ]; then
    echo "✅ Found existing keystore: $KEYSTORE_FILE"
    read -p "Do you want to use this keystore? (y/n): " use_existing
    
    if [ "$use_existing" = "y" ] || [ "$use_existing" = "Y" ]; then
        echo "Using existing keystore..."
    else
        echo "Please remove or rename the existing keystore first."
        exit 1
    fi
else
    echo "🔑 Creating new Android keystore..."
    echo ""
    
    read -p "Enter keystore password (minimum 6 characters): " -s KEYSTORE_PASSWORD
    echo ""
    read -p "Enter key alias (e.g., 'beatblink'): " KEY_ALIAS
    read -p "Enter key password (minimum 6 characters): " -s KEY_PASSWORD
    echo ""
    
    echo "Creating keystore..."
    keytool -genkey -v -keystore "$KEYSTORE_FILE" \
            -alias "$KEY_ALIAS" \
            -keyalg RSA \
            -keysize 2048 \
            -validity 10000 \
            -storepass "$KEYSTORE_PASSWORD" \
            -keypass "$KEY_PASSWORD" \
            -dname "CN=BeatBlink, OU=Android, O=BeatBlink, L=Unknown, ST=Unknown, C=US"
    
    if [ $? -eq 0 ]; then
        echo "✅ Keystore created successfully!"
    else
        echo "❌ Failed to create keystore"
        exit 1
    fi
fi

echo ""

# Step 2: Generate Base64 encoded keystore
echo "📋 Step 2: Encode Keystore for GitHub"
echo "------------------------------------"
echo ""

if [ ! -f "$KEYSTORE_FILE" ]; then
    echo "❌ Keystore file not found: $KEYSTORE_FILE"
    exit 1
fi

ENCODED_KEYSTORE=$(base64 -w 0 "$KEYSTORE_FILE")
echo "✅ Keystore encoded successfully!"
echo ""

# Step 3: GitHub Secrets Setup Instructions
echo "📋 Step 3: GitHub Secrets Configuration"
echo "======================================="
echo ""
echo "You need to add the following secrets to your GitHub repository:"
echo ""
echo "1. Go to your GitHub repository: https://github.com/basiphobe/BeatBlink"
echo "2. Click on 'Settings' tab"
echo "3. Click on 'Secrets and variables' > 'Actions'"
echo "4. Click 'New repository secret' for each of the following:"
echo ""

echo "Secret Name: KEYSTORE_BASE64"
echo "Secret Value:"
echo "$ENCODED_KEYSTORE"
echo ""
echo "----------------------------------------"
echo ""

if [ -n "$KEYSTORE_PASSWORD" ]; then
    echo "Secret Name: KEYSTORE_PASSWORD"
    echo "Secret Value: $KEYSTORE_PASSWORD"
    echo ""
    echo "Secret Name: KEY_ALIAS" 
    echo "Secret Value: $KEY_ALIAS"
    echo ""
    echo "Secret Name: KEY_PASSWORD"
    echo "Secret Value: $KEY_PASSWORD"
else
    echo "Secret Name: KEYSTORE_PASSWORD"
    echo "Secret Value: [Enter your keystore password]"
    echo ""
    echo "Secret Name: KEY_ALIAS"
    echo "Secret Value: [Enter your key alias]"
    echo ""
    echo "Secret Name: KEY_PASSWORD"
    echo "Secret Value: [Enter your key password]"
fi

echo ""
echo "----------------------------------------"
echo ""

# Step 4: Git Configuration
echo "📋 Step 4: Git Configuration"
echo "============================"
echo ""

echo "Adding keystore to .gitignore to prevent accidental commits..."
if ! grep -q "keystore.jks" .gitignore; then
    echo "" >> .gitignore
    echo "# Android Keystore" >> .gitignore
    echo "app/keystore.jks" >> .gitignore
    echo "*.jks" >> .gitignore
    echo "✅ Added keystore entries to .gitignore"
else
    echo "✅ Keystore already in .gitignore"
fi

echo ""

# Step 5: Release Instructions  
echo "📋 Step 5: Creating Releases"
echo "============================"
echo ""
echo "To trigger a release build:"
echo ""
echo "1. Create and push a version tag:"
echo "   git tag v1.0.0"
echo "   git push origin v1.0.0"
echo ""
echo "2. The CI/CD pipeline will automatically:"
echo "   - Run tests and lint checks"
echo "   - Build signed APK and AAB files"
echo "   - Create a GitHub release"
echo "   - Upload the APK and AAB as release assets"
echo ""

echo "📋 Step 6: Manual Build Commands"
echo "==============================="
echo ""
echo "For local development:"
echo ""
echo "Build debug APK:"
echo "  ./gradlew assembleDebug"
echo ""
echo "Build release APK (requires secrets as environment variables):"
echo "  export KEYSTORE_PASSWORD='your_password'"
echo "  export KEY_ALIAS='your_alias'"  
echo "  export KEY_PASSWORD='your_key_password'"
echo "  ./gradlew assembleRelease"
echo ""

echo "🎉 Setup Complete!"
echo "=================="
echo ""
echo "Your BeatBlink CI/CD pipeline is now configured!"
echo ""
echo "Next steps:"
echo "1. Add the GitHub secrets as shown above"
echo "2. Commit and push the workflow files"
echo "3. Create a tag to trigger your first release build"
echo ""
echo "Happy coding! 🎵"