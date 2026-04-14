package com.jegly.rss.domain.usecase

import com.jegly.rss.domain.model.Feed
import com.jegly.rss.domain.repository.FeedRepository
import javax.inject.Inject

class DeleteFeedUseCase @Inject constructor(
    private val repository: FeedRepository
) {
    suspend operator fun invoke(feed: Feed) = repository.deleteFeed(feed)
}
