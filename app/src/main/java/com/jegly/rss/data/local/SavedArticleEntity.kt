package com.jegly.rss.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jegly.rss.domain.model.Article
import com.jegly.rss.domain.model.SavedArticle

@Entity(tableName = "saved_articles", indices = [Index(value = ["link"], unique = true)])
data class SavedArticleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val link: String,
    val title: String,
    val pubDate: String,
    val description: String,
    val feedTitle: String,
    val savedAt: Long
) {
    fun toDomain() = SavedArticle(
        title = title,
        link = link,
        pubDate = pubDate,
        description = description,
        feedTitle = feedTitle,
        savedAt = savedAt
    )

    companion object {
        fun fromArticle(article: Article, feedTitle: String, savedAt: Long) = SavedArticleEntity(
            link = article.link,
            title = article.title,
            pubDate = article.pubDate,
            description = article.description,
            feedTitle = feedTitle,
            savedAt = savedAt
        )
    }
}
