buildscript {

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.gradle)
    }
}

apply(plugin = "maven-publish")

allprojects {
    repositories {
        google()
        //mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
