# Android Signing Configuration
# Add these properties to your app/build.gradle.kts or app/build.gradle file

# In your app/build.gradle.kts (or build.gradle), add the signing configuration:

android {
    compileSdk 34

    defaultConfig {
        applicationId "com.beatblink"
        minSdk 26
        targetSdk 34
        versionCode 1
        versionName "1.0.0"
        
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        
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
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

# ProGuard rules for release builds (create/update app/proguard-rules.pro)
# -keep class com.beatblink.** { *; }
# -keep class be.tarsos.dsp.** { *; }
# -keepclassmembers class com.beatblink.AudioBeatDetector { *; }
# -dontwarn java.awt.**
# -dontwarn javax.sound.**