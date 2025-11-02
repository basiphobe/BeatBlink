#!/bin/bash

# BeatBlink Release Build Script
# This script helps you build a release APK locally

echo "🎵 BeatBlink Release Build"
echo "=========================="
echo ""

# Check if keystore exists
if [ ! -f "app/keystore.jks" ]; then
    echo "❌ Error: Keystore not found at app/keystore.jks"
    echo "Please run the keystore generation command first:"
    echo "keytool -genkey -v -keystore app/keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias beatblink"
    exit 1
fi

echo "✅ Found keystore at app/keystore.jks"

# Check if gradle.properties has signing info
if ! grep -q "KEYSTORE_PASSWORD=" gradle.properties; then
    echo "❌ Error: Signing configuration not found in gradle.properties"
    echo "Please update gradle.properties with your keystore passwords."
    exit 1
fi

# Check for placeholder passwords
if grep -q "your_keystore_password_here" gradle.properties; then
    echo "⚠️  Warning: Please update the passwords in gradle.properties"
    echo "Replace 'your_keystore_password_here' and 'your_key_password_here' with your actual passwords"
    read -p "Continue anyway? (y/n): " continue_build
    if [ "$continue_build" != "y" ] && [ "$continue_build" != "Y" ]; then
        echo "Please update gradle.properties first."
        exit 1
    fi
fi

echo ""
echo "🔨 Building Release APK..."
echo ""

# Clean and build release
./gradlew clean assembleRelease

if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 Release APK Built Successfully!"
    echo "=================================="
    echo ""
    echo "📱 Release APK location:"
    echo "   app/build/outputs/apk/release/app-release.apk"
    echo ""
    echo "📦 File size:"
    if [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
        ls -lh app/build/outputs/apk/release/app-release.apk | awk '{print "   " $5}'
    fi
    echo ""
    echo "🚀 Ready for distribution!"
    echo ""
    echo "To install on a device:"
    echo "   adb install app/build/outputs/apk/release/app-release.apk"
    echo ""
else
    echo ""
    echo "❌ Build failed!"
    echo "================"
    echo ""
    echo "Common issues:"
    echo "1. Check that your keystore passwords are correct in gradle.properties"
    echo "2. Ensure the keystore file exists at app/keystore.jks" 
    echo "3. Check that all dependencies are available"
    echo ""
fi