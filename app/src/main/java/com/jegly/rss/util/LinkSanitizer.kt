package com.jegly.rss.util

import android.net.Uri

object LinkSanitizer {
    private val TRACKING_PARAMS = listOf(
        "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
        "fbclid", "gclid", "msclkid", "mc_eid", "originalSub", "ref"
    )

    fun sanitize(url: String): String {
        try {
            val uri = Uri.parse(url)
            // Enforce HTTPS for any URL opened in the browser.
            val secureUri = if (uri.scheme.equals("http", ignoreCase = true)) {
                uri.buildUpon().scheme("https").build()
            } else {
                uri
            }
            if (secureUri.query == null) return secureUri.toString()

            val builder = secureUri.buildUpon().clearQuery()
            secureUri.queryParameterNames.forEach { name ->
                if (!TRACKING_PARAMS.contains(name.lowercase())) {
                    secureUri.getQueryParameters(name).forEach { value ->
                        builder.appendQueryParameter(name, value)
                    }
                }
            }
            return builder.build().toString()
        } catch (e: Exception) {
            return url
        }
    }
}
