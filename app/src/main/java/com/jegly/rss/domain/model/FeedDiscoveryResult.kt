package com.jegly.rss.domain.model

sealed class FeedDiscoveryResult {
    data class RssFeed(val feedUrl: String) : FeedDiscoveryResult()
    data class WebBookmark(val url: String) : FeedDiscoveryResult()
}
