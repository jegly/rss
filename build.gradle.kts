// Hilt and KSP must share a classloader so Hilt can resolve KSP task types.
// buildscript{} is the one Gradle mechanism with a single shared classpath —
// no per-entry isolation like the plugins{} block has in Gradle 8.1+/9.x.
buildscript {
    val nettyVersion = "4.1.139.Final"
    val bouncyCastleVersion = "1.86"
    dependencies {
        constraints {
            classpath("io.netty:netty-buffer:$nettyVersion")
            classpath("io.netty:netty-codec:$nettyVersion")
            classpath("io.netty:netty-codec-http:$nettyVersion")
            classpath("io.netty:netty-codec-http2:$nettyVersion")
            classpath("io.netty:netty-common:$nettyVersion")
            classpath("io.netty:netty-handler:$nettyVersion")
            classpath("io.netty:netty-handler-proxy:$nettyVersion")
            classpath("io.netty:netty-resolver:$nettyVersion")
            classpath("io.netty:netty-transport:$nettyVersion")
            classpath("io.netty:netty-transport-native-epoll:$nettyVersion")
            classpath("org.bouncycastle:bcprov-jdk18on:$bouncyCastleVersion")
            classpath("org.bouncycastle:bcpkix-jdk18on:$bouncyCastleVersion")
            classpath("org.jdom:jdom2:2.0.6.1")
            classpath("org.bitbucket.b_c:jose4j:0.9.7")
        }
    }
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.3.12")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.60.1")
    }
}

plugins {
    id("com.android.application") version "9.4.1" apply false
    id("org.jetbrains.kotlin.android") version "2.4.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.21" apply false
}
