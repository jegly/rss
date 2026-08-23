package com.jegly.rss.domain.model

data class SavedArticle(
    val title: String,
    val link: String,
    val pubDate: String,
    val description: String,
    val feedTitle: String,
    val savedAt: Long
)
