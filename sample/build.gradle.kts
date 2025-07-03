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

    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kapt)
}

val libMinSdk: Int = 21
val libCompileSdk: Int = 34
val libTargetSdk: Int = 34
val javaVersion: JavaVersion = JavaVersion.VERSION_17
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

    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.pdfium)

    annotationProcessor(libs.androidannotations)
}
