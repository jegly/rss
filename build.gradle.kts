// Hilt and KSP must share a classloader so Hilt can resolve KSP task types.
// buildscript{} is the one Gradle mechanism with a single shared classpath —
// no per-entry isolation like the plugins{} block has in Gradle 8.1+/9.x.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.2.10-2.0.2")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.59.2")
    }
}

plugins {
    id("com.android.application") version "9.2.1" apply false
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
}
