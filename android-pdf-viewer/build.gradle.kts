plugins {
    id("com.android.library")
    id("maven-publish")
    id("org.jetbrains.kotlin.android") version "2.0.21"
    id("org.jetbrains.kotlin.kapt") version "2.0.21"
}

val libMinSdk: Int = 21
val libCompileSdk: Int = 34
val libTargetSdk: Int = 34
val libVersionName: String = "3.2.13"
val javaVersion: JavaVersion = JavaVersion.VERSION_1_8


android {
    namespace = "com.infomaniak.lib.pdfview"

    defaultConfig {
        minSdk = libMinSdk
        compileSdk = libCompileSdk
        targetSdk = libTargetSdk
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    api("com.github.infomaniak:pdfiumandroid:1.9.6")
}

// ./gradlew clean build publish -Prelease=true
val isRelease = project.hasProperty("release") && project.findProperty("release").toString() == "true"
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("mavenAar") {
                from(components.findByName("release")!!)
                groupId = "com.viafirma"
                artifactId = "android-pdfview"
                version = if (isRelease) libVersionName else "$libVersionName-SNAPSHOT"
            }
        }

        repositories {
            maven {
                name = "viafirma"
                url = uri(if (isRelease) properties["releaseUrl"].toString() else properties["snapshotUrl"].toString())
                credentials {
                    username = properties["repoUsername"].toString()
                    password = properties["repoPassword"].toString()
                }
            }
        }
    }
}
