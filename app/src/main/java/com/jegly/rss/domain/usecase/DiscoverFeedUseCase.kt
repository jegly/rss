package com.jegly.rss.domain.usecase

import com.jegly.rss.data.remote.RssApiService
import com.jegly.rss.domain.model.FeedDiscoveryResult
import com.jegly.rss.network.PrivateNetworkGuard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.inject.Inject

class DiscoverFeedUseCase @Inject constructor(
    private val apiService: RssApiService
) {
    // Patterns for <link rel="alternate" type="application/rss+xml" ...> in any attribute order.
    private val linkTagRegex = Regex("""<link([^>]+/?>)""", RegexOption.IGNORE_CASE)
    private val relAlternateRegex = Regex("""rel=["']alternate["']""", RegexOption.IGNORE_CASE)
    private val feedTypeRegex = Regex("""type=["']application/(?:rss|atom)\+xml[^"']*["']""", RegexOption.IGNORE_CASE)
    private val hrefRegex = Regex("""href=["']([^"']+)["']""", RegexOption.IGNORE_CASE)

    private val commonFeedPaths = listOf(
        "/feed", "/rss", "/rss.xml", "/feed.xml", "/index.xml", "/atom.xml",
        "/feeds/posts/default", "/blog/feed", "/news/rss"
    )

    suspend operator fun invoke(websiteUrl: String): FeedDiscoveryResult? = withContext(Dispatchers.IO) {
        val normalised = when {
            websiteUrl.startsWith("https://", ignoreCase = true) -> websiteUrl
            websiteUrl.startsWith("http://", ignoreCase = true) -> "https://" + websiteUrl.substring(7)
            else -> "https://$websiteUrl"
        }
        val httpUrl = normalised.toHttpUrlOrNull() ?: return@withContext null
        if (!isHostAllowed(httpUrl)) return@withContext null

        // 1. Is the URL itself an RSS feed?
        val direct = fetchBody(normalised)
        if (direct != null && looksLikeRss(direct)) {
            return@withContext FeedDiscoveryResult.RssFeed(normalised)
        }

        // 2. Parse HTML <link rel="alternate"> tags.
        if (direct != null) {
            val htmlFeedUrl = findFeedLinkInHtml(direct, httpUrl)
            if (htmlFeedUrl != null) return@withContext FeedDiscoveryResult.RssFeed(htmlFeedUrl)
        }

        // 3. Try common feed paths.
        for (path in commonFeedPaths) {
            val candidate = httpUrl.newBuilder().encodedPath(path).build()
            if (!isHostAllowed(candidate)) continue
            val body = fetchBody(candidate.toString()) ?: continue
            if (looksLikeRss(body)) return@withContext FeedDiscoveryResult.RssFeed(candidate.toString())
        }

        // No RSS found — treat as a plain web bookmark.
        FeedDiscoveryResult.WebBookmark(normalised)
    }

    private fun findFeedLinkInHtml(html: String, base: HttpUrl): String? {
        // Only scan the <head> section to avoid false positives in body content.
        val head = html.substringBefore("</head>", html.take(8000))
        for (match in linkTagRegex.findAll(head)) {
            val attrs = match.groupValues[1]
            if (!relAlternateRegex.containsMatchIn(attrs)) continue
            if (!feedTypeRegex.containsMatchIn(attrs)) continue
            val href = hrefRegex.find(attrs)?.groupValues?.get(1)?.trim() ?: continue
            val resolved = base.resolve(href) ?: continue
            if (isHostAllowed(resolved)) return resolved.toString()
        }
        return null
    }

    private fun looksLikeRss(body: String): Boolean {
        val trimmed = body.trimStart()
        return trimmed.contains("<rss", ignoreCase = true) ||
               trimmed.contains("<feed", ignoreCase = true) ||
               trimmed.contains("<channel", ignoreCase = true)
    }

    private suspend fun fetchBody(url: String): String? = runCatching {
        apiService.fetchFeedXml(url).string()
    }.getOrNull()

    private fun isHostAllowed(url: HttpUrl): Boolean =
        PrivateNetworkGuard.isAllowed(url.host, if (url.port == -1) url.defaultPort() else url.port)

    private fun HttpUrl.defaultPort(): Int = if (scheme == "https") 443 else 80
}
