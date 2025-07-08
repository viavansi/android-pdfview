buildscript {

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.2.2")
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
