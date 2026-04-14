package com.jegly.rss.domain.usecase
import com.jegly.rss.domain.repository.FeedRepository
import javax.inject.Inject

class GetFeedsUseCase @Inject constructor(private val repository: FeedRepository) {
    operator fun invoke() = repository.getSavedFeeds()
}
