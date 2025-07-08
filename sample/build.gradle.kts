buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

repositories {
    google()
    //mavenLocal()
    mavenCentral()
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.kapt") version "2.0.21"
    id("org.jetbrains.kotlin.android") version "2.0.21"
}

val libMinSdk: Int = 21
val libCompileSdk: Int = 34
val libTargetSdk: Int = 34
val javaVersion: JavaVersion = JavaVersion.VERSION_1_8
val versionName: String = "3.2.12"

android {
    namespace = "com.infomaniak.lib.pdfview.sample"

    defaultConfig {
        minSdk = libMinSdk
        compileSdk = libCompileSdk
        targetSdk = libTargetSdk
        versionCode = 3
        versionName = versionName
    }
    buildFeatures { viewBinding = true }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    kotlinOptions { jvmTarget = javaVersion.toString() }
}

dependencies {
    implementation(project(":android-pdf-viewer"))

    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.github.infomaniak:pdfiumandroid:1.9.6")

    annotationProcessor("org.androidannotations:androidannotations:4.8.0")
}
