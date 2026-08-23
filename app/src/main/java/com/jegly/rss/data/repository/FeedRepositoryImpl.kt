package com.jegly.rss.data.repository

import com.jegly.rss.data.local.FeedDao
import com.jegly.rss.data.local.FeedEntity
import com.jegly.rss.data.local.SavedArticleDao
import com.jegly.rss.data.local.SavedArticleEntity
import com.jegly.rss.data.remote.RssApiService
import com.jegly.rss.data.remote.RssParser
import com.jegly.rss.domain.model.Article
import com.jegly.rss.domain.model.Feed
import com.jegly.rss.domain.model.SavedArticle
import com.jegly.rss.domain.repository.FeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val dao: FeedDao,
    private val savedArticleDao: SavedArticleDao,
    private val api: RssApiService,
    private val parser: RssParser
) : FeedRepository {

    override fun getSavedFeeds(): Flow<List<Feed>> = dao.getAllFeeds().map { entities -> 
        entities.map { it.toDomain() } 
    }

    override suspend fun addFeed(title: String, url: String, category: String, feedType: String) {
        dao.insertFeed(FeedEntity(title = title, url = url, category = category, feedType = feedType))
    }

    override suspend fun updateFeed(feed: Feed) {
        dao.updateFeed(FeedEntity.fromDomain(feed))
    }

    override suspend fun updateFeeds(feeds: List<Feed>) {
        feeds.forEach { dao.updateFeed(FeedEntity.fromDomain(it)) }
    }

    override suspend fun deleteFeed(feed: Feed) {
        dao.deleteFeed(FeedEntity.fromDomain(feed))
    }

    override suspend fun fetchArticles(feedUrl: String): List<Article> {
        val response = api.fetchFeedXml(feedUrl)
        return parser.parse(response.byteStream())
    }

    override fun getSavedArticles(): Flow<List<SavedArticle>> =
        savedArticleDao.getAll().map { entities -> entities.map { it.toDomain() } }

    override fun isArticleSaved(link: String): Flow<Boolean> = savedArticleDao.isSaved(link)

    override suspend fun saveArticle(article: Article, feedTitle: String) {
        savedArticleDao.insert(SavedArticleEntity.fromArticle(article, feedTitle, System.currentTimeMillis()))
    }

    override suspend fun unsaveArticle(link: String) {
        savedArticleDao.deleteByLink(link)
    }
}
