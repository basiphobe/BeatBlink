#!/bin/bash

# BeatBlink Version Management Script
# Helps create releases with proper version tagging

set -e  # Exit on any error

echo "🎵 BeatBlink Release Manager"
echo "============================"
echo ""

# Check if we're in a Git repository
if [ ! -d ".git" ]; then
    echo "❌ Error: This script must be run from the root of your Git repository"
    exit 1
fi

# Check for uncommitted changes
if [ -n "$(git status --porcelain)" ]; then
    echo "⚠️  Warning: You have uncommitted changes"
    git status --short
    echo ""
    read -p "Do you want to continue anyway? (y/n): " continue_anyway
    if [ "$continue_anyway" != "y" ] && [ "$continue_anyway" != "Y" ]; then
        echo "Please commit or stash your changes first."
        exit 1
    fi
fi

# Get current version from the last tag
CURRENT_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "v0.0.0")
echo "Current version: $CURRENT_TAG"

# Parse version numbers
CURRENT_VERSION=${CURRENT_TAG#v}  # Remove 'v' prefix
IFS='.' read -ra VERSION_PARTS <<< "$CURRENT_VERSION"

MAJOR=${VERSION_PARTS[0]:-0}
MINOR=${VERSION_PARTS[1]:-0}
PATCH=${VERSION_PARTS[2]:-0}

echo ""
echo "Version components:"
echo "  Major: $MAJOR"
echo "  Minor: $MINOR" 
echo "  Patch: $PATCH"
echo ""

# Menu for version bump type
echo "What type of release is this?"
echo "1) Patch (bug fixes) - ${MAJOR}.${MINOR}.$((PATCH + 1))"
echo "2) Minor (new features) - ${MAJOR}.$((MINOR + 1)).0"
echo "3) Major (breaking changes) - $((MAJOR + 1)).0.0"
echo "4) Custom version"
echo ""

read -p "Select option (1-4): " choice

case $choice in
    1)
        NEW_VERSION="${MAJOR}.${MINOR}.$((PATCH + 1))"
        RELEASE_TYPE="Patch Release"
        ;;
    2)
        NEW_VERSION="${MAJOR}.$((MINOR + 1)).0"
        RELEASE_TYPE="Minor Release"
        ;;
    3)
        NEW_VERSION="$((MAJOR + 1)).0.0"
        RELEASE_TYPE="Major Release" 
        ;;
    4)
        read -p "Enter custom version (x.y.z format): " NEW_VERSION
        RELEASE_TYPE="Custom Release"
        ;;
    *)
        echo "❌ Invalid option selected"
        exit 1
        ;;
esac

NEW_TAG="v$NEW_VERSION"

echo ""
echo "📋 Release Summary"
echo "=================="
echo "Release Type: $RELEASE_TYPE"
echo "Current Version: $CURRENT_TAG"
echo "New Version: $NEW_TAG"
echo ""

# Check if tag already exists
if git rev-parse "$NEW_TAG" >/dev/null 2>&1; then
    echo "❌ Error: Tag $NEW_TAG already exists"
    exit 1
fi

# Get commit messages since last tag for changelog
echo "📝 Recent Changes"
echo "=================="
if [ "$CURRENT_TAG" != "v0.0.0" ]; then
    git log --oneline --pretty=format:"- %s" "$CURRENT_TAG"..HEAD | head -20
else
    git log --oneline --pretty=format:"- %s" HEAD | head -20
fi
echo ""
echo ""

# Confirm release
read -p "Create release $NEW_TAG? (y/n): " confirm
if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
    echo "Release cancelled."
    exit 0
fi

echo ""
echo "🚀 Creating Release"
echo "==================="

# Update version in build.gradle if it exists
if [ -f "app/build.gradle.kts" ]; then
    echo "Updating version in build.gradle.kts..."
    
    # Extract current version code and increment it
    CURRENT_VERSION_CODE=$(grep -o 'versionCode = [0-9]*' app/build.gradle.kts | grep -o '[0-9]*' || echo "1")
    NEW_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))
    
    # Update version name and code
    sed -i.bak "s/versionName = \"[^\"]*\"/versionName = \"$NEW_VERSION\"/" app/build.gradle.kts
    sed -i.bak "s/versionCode = [0-9]*/versionCode = $NEW_VERSION_CODE/" app/build.gradle.kts
    
    # Remove backup file
    rm -f app/build.gradle.kts.bak
    
    echo "✅ Updated build.gradle.kts:"
    echo "   versionName: $NEW_VERSION"
    echo "   versionCode: $NEW_VERSION_CODE"
    
    # Stage the changes
    git add app/build.gradle.kts
fi

# Commit version bump if there are changes
if [ -n "$(git diff --staged)" ]; then
    git commit -m "Bump version to $NEW_VERSION"
    echo "✅ Committed version changes"
fi

# Create and push the tag
echo "Creating tag $NEW_TAG..."
git tag -a "$NEW_TAG" -m "$RELEASE_TYPE: $NEW_VERSION"

echo "Pushing to origin..."
git push origin main
git push origin "$NEW_TAG"

echo ""
echo "🎉 Release Created Successfully!"
echo "==============================="
echo ""
echo "Tag: $NEW_TAG"
echo "GitHub Release: https://github.com/basiphobe/BeatBlink/releases/tag/$NEW_TAG"
echo ""
echo "The CI/CD pipeline will now:"
echo "1. ✅ Run tests and quality checks"
echo "2. 🔨 Build signed APK and AAB files"  
echo "3. 📦 Create GitHub release"
echo "4. ⬆️  Upload release artifacts"
echo ""
echo "Monitor the build progress at:"
echo "https://github.com/basiphobe/BeatBlink/actions"
echo ""
echo "🎵 Happy releasing!"