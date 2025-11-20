plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}


android {
    namespace = "com.example.smartqueue"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.smartqueue"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // ADD THIS PACKAGING OPTIONS BLOCK
    packagingOptions {
        exclude("META-INF/NOTICE.md")
        exclude("META-INF/LICENSE.md")
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/INDEX.LIST")
    }
}

dependencies {
    implementation(libs.appcompat)// Basic Android compatibility
    implementation(libs.material)// Google's design system
    implementation(libs.activity)// Screen management
    implementation(libs.constraintlayout)// Advanced screen layouts
    testImplementation(libs.junit)// Basic tests
    androidTestImplementation(libs.ext.junit) // Android-specific tests
    androidTestImplementation(libs.espresso.core)// UI automation tests
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))// Firebase "bundle deal"
    implementation("com.google.firebase:firebase-analytics")// Track app usage
    implementation("com.google.firebase:firebase-auth:24.0.1")// USER AUTHENTICATION
    implementation("com.google.firebase:firebase-firestore:26.0.2")// DATABASE CONNECTION
    implementation("com.google.android.material:material:1.9.0")// Buttons, cards, etc.
    implementation("androidx.recyclerview:recyclerview:1.3.1")// Scrollable lists
    implementation("androidx.viewpager2:viewpager2:1.0.0")// Swipe between screens
    implementation("androidx.cardview:cardview:1.0.0") // Card layouts
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.sun.mail:android-mail:1.6.7")// Send emails
    implementation("com.sun.mail:android-activation:1.6.7")// Email attachments
}