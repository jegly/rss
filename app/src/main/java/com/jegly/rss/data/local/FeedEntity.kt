package com.jegly.rss.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jegly.rss.domain.model.Feed

@Entity(tableName = "feeds")
data class FeedEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val category: String = "Uncategorized",
    val categoryOrder: Int = 0
) {
    fun toDomain() = Feed(id, title, url, category, categoryOrder)
    
    companion object {
        fun fromDomain(feed: Feed) = FeedEntity(
            id = feed.id,
            title = feed.title,
            url = feed.url,
            category = feed.category,
            categoryOrder = feed.categoryOrder
        )
    }
}
