package com.jegly.rss.util

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

object BrowserUtils {
    fun openSanitizedUrl(context: Context, url: String) {
        val sanitizedUrl = LinkSanitizer.sanitize(url)
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
            .setEphemeralBrowsingEnabled(true) // Prevents sharing cookies/history with the browser
            .build()
        
        customTabsIntent.intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NO_HISTORY)
        customTabsIntent.intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        
        customTabsIntent.launchUrl(context, Uri.parse(sanitizedUrl))
    }
}
