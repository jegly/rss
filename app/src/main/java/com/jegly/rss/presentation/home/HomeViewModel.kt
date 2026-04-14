package com.jegly.rss.presentation.home
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.jegly.rss.domain.usecase.GetFeedsUseCase
import com.jegly.rss.domain.usecase.AddFeedUseCase
import com.jegly.rss.domain.usecase.UpdateFeedUseCase
import com.jegly.rss.domain.usecase.DeleteFeedUseCase
import com.jegly.rss.domain.usecase.DiscoverFeedUseCase
import com.jegly.rss.domain.model.Feed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getFeedsUseCase: GetFeedsUseCase,
    private val addFeedUseCase: AddFeedUseCase,
    private val updateFeedUseCase: UpdateFeedUseCase,
    private val deleteFeedUseCase: DeleteFeedUseCase,
    private val discoverFeedUseCase: DiscoverFeedUseCase
) : ViewModel() {
    val feeds = getFeedsUseCase().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addFeed(title: String, url: String, category: String) {
        viewModelScope.launch { addFeedUseCase(title, url, category) }
    }

    fun updateFeed(feed: Feed) {
        viewModelScope.launch { updateFeedUseCase(feed) }
    }

    fun updateCategoryOrder(categories: List<String>) {
        viewModelScope.launch {
            val currentFeeds = feeds.value
            val updatedFeeds = currentFeeds.map { feed ->
                val newOrder = categories.indexOf(feed.category)
                feed.copy(categoryOrder = if (newOrder != -1) newOrder else feed.categoryOrder)
            }
            updateFeedUseCase(updatedFeeds)
        }
    }

    fun deleteFeed(feed: Feed) {
        viewModelScope.launch { deleteFeedUseCase(feed) }
    }

    suspend fun discoverFeed(url: String): String? {
        return discoverFeedUseCase(url)
    }
}
