package com.jegly.rss.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

object BrowserUtils {
    /**
     * Open an http/https link in an ephemeral, tracking-stripped Custom Tab. Guarded so a device
     * with no browser available can never crash the app.
     */
    fun openSanitizedUrl(context: Context, url: String) {
        val sanitizedUrl = LinkSanitizer.sanitize(url)
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
            .setEphemeralBrowsingEnabled(true) // Prevents sharing cookies/history with the browser
            .build()

        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            customTabsIntent.launchUrl(context, Uri.parse(sanitizedUrl))
        } catch (e: ActivityNotFoundException) {
            // No browser/handler installed; nothing we can safely do.
        }
    }

    /**
     * Best-effort hand-off of a non-web scheme (mailto:, tel:, sms:, geo:) to the system. Anything
     * else is ignored, and a missing handler is swallowed, so untrusted page content can neither
     * crash the reader nor launch arbitrary apps via intent:/custom schemes.
     */
    private val EXTERNAL_SCHEMES = setOf("mailto", "tel", "sms", "smsto", "mms", "geo")

    fun openExternal(context: Context, url: String) {
        val scheme = runCatching { Uri.parse(url).scheme?.lowercase() }.getOrNull() ?: return
        if (scheme !in EXTERNAL_SCHEMES) return
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // No app handles this scheme; ignore rather than crash.
        }
    }
}
