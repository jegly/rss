plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}
// KSP must be applied before Hilt so Hilt can find KSP's task class at config time.
// Both come from the root buildscript classpath (shared, no classloader isolation).
apply(plugin = "com.google.devtools.ksp")
apply(plugin = "dagger.hilt.android.plugin")

android {
    namespace = "com.jegly.rss"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.jegly.rss"
        minSdk = 33
        targetSdk = 37
        versionCode = 6
        versionName = "1.2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            // Adopt the future Kotlin default: annotations with no explicit target apply to
            // both the constructor parameter and the backing property (KT-73255).
            "-Xannotation-default-target=param-property"
        )
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation(platform("androidx.compose:compose-bom:2026.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("androidx.browser:browser:1.10.0")

    // Security
    implementation("androidx.security:security-crypto:1.1.0")
    implementation("androidx.biometric:biometric:1.1.0")
    implementation("com.google.crypto.tink:tink-android:1.19.0")

    // Room & SQLCipher
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    "ksp"("androidx.room:room-compiler:2.8.4")
    implementation("net.zetetic:sqlcipher-android:4.14.1@aar")
    implementation("androidx.sqlite:sqlite-ktx:2.6.2")

    // Networking — Retrofit 3.0.0 requires OkHttp 4.12
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:4.12.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.59.2")
    "ksp"("com.google.dagger:hilt-compiler:2.59.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    implementation("androidx.hilt:hilt-lifecycle-viewmodel-compose:1.3.0")

    // Reorderable
    implementation("sh.calvin.reorderable:reorderable:3.1.0")

    // Compose image loader. Routed through our hardened OkHttp (DoH, HTTPS, size caps).
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")

    // Dominant-color extraction for per-feed accent colors.
    implementation("androidx.palette:palette-ktx:1.0.0")
}
