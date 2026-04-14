package com.jegly.rss.domain.usecase

import com.jegly.rss.data.remote.RssApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import javax.inject.Inject

class DiscoverFeedUseCase @Inject constructor(
    private val apiService: RssApiService
) {
    suspend operator fun invoke(websiteUrl: String): String? = withContext(Dispatchers.IO) {
        val url = if (!websiteUrl.startsWith("http")) "https://$websiteUrl" else websiteUrl
        val httpUrl = url.toHttpUrlOrNull() ?: return@withContext null

        val commonPaths = listOf(
            "/feed", "/rss", "/rss.xml", "/feed.xml", "/index.xml", "/atom.xml"
        )

        // Try common paths first
        for (path in commonPaths) {
            val discoveryUrl = httpUrl.newBuilder().encodedPath(path).build().toString()
            if (isValidFeed(discoveryUrl)) return@withContext discoveryUrl
        }

        // If it's already a feed
        if (isValidFeed(url)) return@withContext url

        return@withContext null
    }

    private suspend fun isValidFeed(url: String): Boolean {
        return try {
            val response = apiService.fetchFeedXml(url)
            val content = response.string()
            content.contains("<rss") || content.contains("<feed") || content.contains("<channel")
        } catch (e: Exception) {
            false
        }
    }
}
