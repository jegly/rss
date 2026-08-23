package com.jegly.rss.util

import android.net.Uri

/**
 * Derive a likely favicon URL from a feed URL. For "https://feeds.reuters.com/reuters/topNews"
 * returns "https://feeds.reuters.com/favicon.ico". Most sites publish a favicon at the root of
 * the host they serve from; if the load fails Coil will fall through to our placeholder.
 *
 * Returning null skips the network fetch entirely (we render the placeholder icon instead).
 */
fun faviconUrlFor(feedUrl: String): String? {
    if (feedUrl.isBlank()) return null
    val uri = runCatching { Uri.parse(feedUrl) }.getOrNull() ?: return null
    val scheme = uri.scheme?.lowercase()
    val host = uri.host ?: return null
    if (scheme != "https" && scheme != "http") return null
    // Always request via HTTPS — OkHttp upgrades cleartext anyway, but be explicit so the URL
    // string we hand Coil is the cache key Coil uses.
    return "https://$host/favicon.ico"
}
