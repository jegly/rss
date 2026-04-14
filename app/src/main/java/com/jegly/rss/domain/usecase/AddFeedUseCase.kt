package com.jegly.rss.domain.usecase

import com.jegly.rss.domain.repository.FeedRepository
import javax.inject.Inject

class AddFeedUseCase @Inject constructor(private val repository: FeedRepository) {
    suspend operator fun invoke(title: String, url: String, category: String = "Uncategorized") = 
        repository.addFeed(title, url, category)
}
