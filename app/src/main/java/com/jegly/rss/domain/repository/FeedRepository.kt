package com.jegly.rss.domain.repository

import com.jegly.rss.domain.model.Feed
import com.jegly.rss.domain.model.Article
import com.jegly.rss.domain.model.SavedArticle
import kotlinx.coroutines.flow.Flow

interface FeedRepository {
    fun getSavedFeeds(): Flow<List<Feed>>
    suspend fun addFeed(title: String, url: String, category: String = "Uncategorized", feedType: String = "rss")
    suspend fun updateFeed(feed: Feed)
    suspend fun updateFeeds(feeds: List<Feed>)
    suspend fun deleteFeed(feed: Feed)
    suspend fun fetchArticles(feedUrl: String): List<Article>

    fun getSavedArticles(): Flow<List<SavedArticle>>
    fun isArticleSaved(link: String): Flow<Boolean>
    suspend fun saveArticle(article: Article, feedTitle: String)
    suspend fun unsaveArticle(link: String)
}
