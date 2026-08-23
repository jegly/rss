package com.jegly.rss.domain.model

data class Feed(
    val id: Int = 0,
    val title: String,
    val url: String,
    val category: String = "Uncategorized",
    val categoryOrder: Int = 0,
    val feedType: String = "rss",   // "rss" = RSS/Atom feed, "web" = plain web bookmark
    val accentColor: Long? = null   // dominant color extracted from the feed's favicon, ARGB
)
