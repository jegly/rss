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
            if (uri.query == null) return url

            val builder = uri.buildUpon().clearQuery()
            uri.queryParameterNames.forEach { name ->
                if (!TRACKING_PARAMS.contains(name.lowercase())) {
                    uri.getQueryParameters(name).forEach { value ->
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
