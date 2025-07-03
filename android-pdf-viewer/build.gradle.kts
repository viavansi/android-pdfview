plugins {
    id("com.android.library")
    id("maven-publish")
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kapt)
}

val libMinSdk: Int = 21
val libCompileSdk: Int = 34
val libTargetSdk: Int = 34
val libVersionName: String = "3.2.12"
val javaVersion: JavaVersion = JavaVersion.VERSION_17


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
    implementation(libs.core.ktx)
    implementation(libs.recyclerview)
    implementation(libs.viewpager2)

    api(libs.pdfium)
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
