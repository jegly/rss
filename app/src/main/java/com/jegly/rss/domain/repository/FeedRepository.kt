package com.jegly.rss.domain.repository

import com.jegly.rss.domain.model.Feed
import com.jegly.rss.domain.model.Article
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    fun getSavedFeeds(): Flow<List<Feed>>
    suspend fun addFeed(title: String, url: String, category: String = "Uncategorized")
    suspend fun updateFeed(feed: Feed)
    suspend fun updateFeeds(feeds: List<Feed>)
    suspend fun deleteFeed(feed: Feed)
    suspend fun fetchArticles(feedUrl: String): List<Article>
}
