package com.jegly.rss

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
import javax.inject.Inject

@HiltAndroidApp
class SecureRSSApp : Application(), SingletonImageLoader.Factory {

    @Inject lateinit var okHttpClient: OkHttpClient

    override fun onCreate() {
        super.onCreate()
        // Initialise Safe Browsing once per process — doing it here means it's ready long before
        // the user opens any article, and we never call it redundantly per-navigation.
        Handler(Looper.getMainLooper()).post {
            @Suppress("DEPRECATION") // Pre-warm only; Safe Browsing is on by default on API 27+.
            WebView.startSafeBrowsing(this, null)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                // Route all image fetches through our hardened client (DoH, HTTPS, size cap, timeouts).
                add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient }))
            }
            .memoryCache {
                MemoryCache.Builder().maxSizePercent(context, 0.10).build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizeBytes(20L * 1024 * 1024)  // 20 MB favicon cache; plenty for a few hundred feeds.
                    .build()
            }
            .crossfade(true)
            .build()
}
