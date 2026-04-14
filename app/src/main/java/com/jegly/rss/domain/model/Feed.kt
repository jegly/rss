package com.jegly.rss.domain.model

data class Feed(
    val id: Int = 0,
    val title: String,
    val url: String,
    val category: String = "Uncategorized",
    val categoryOrder: Int = 0
)
