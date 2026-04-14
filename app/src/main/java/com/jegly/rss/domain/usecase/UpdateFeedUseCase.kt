package com.jegly.rss.domain.usecase

import com.jegly.rss.domain.model.Feed
import com.jegly.rss.domain.repository.FeedRepository
import javax.inject.Inject

class UpdateFeedUseCase @Inject constructor(private val repository: FeedRepository) {
    suspend operator fun invoke(feed: Feed) = repository.updateFeed(feed)
    suspend operator fun invoke(feeds: List<Feed>) = repository.updateFeeds(feeds)
}
