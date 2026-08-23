package com.jegly.rss.util

import android.content.Context
import android.webkit.WebView

/**
 * Single pre-warmed WebView instance. Calling prime() after auth triggers Chromium renderer
 * startup while the user is on the home screen, so the first article tap has no cold-start delay.
 * acquire() hands the instance out (one-shot); subsequent calls create a fresh WebView normally.
 */
object WebViewPool {

    @Volatile private var instance: WebView? = null

    fun prime(context: Context) {
        if (instance == null) {
            instance = WebView(context)
        }
    }

    fun acquire(fallbackContext: Context): WebView =
        instance?.also { instance = null } ?: WebView(fallbackContext)
}
