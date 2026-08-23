package com.jegly.rss.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jegly.rss.domain.model.Feed
import com.jegly.rss.domain.model.FeedDiscoveryResult
import com.jegly.rss.domain.usecase.AddFeedUseCase
import com.jegly.rss.domain.usecase.DeleteFeedUseCase
import com.jegly.rss.domain.usecase.DiscoverFeedUseCase
import com.jegly.rss.domain.usecase.GetFeedsUseCase
import com.jegly.rss.domain.usecase.UpdateFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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

    private val _expandedCategories = MutableStateFlow<Set<String>>(emptySet())
    val expandedCategories: StateFlow<Set<String>> = _expandedCategories

    fun toggleCategory(category: String) {
        _expandedCategories.update { if (category in it) it - category else it + category }
    }

    fun setExpandedCategories(categories: Collection<String>) {
        _expandedCategories.value = categories.toSet()
    }

    fun retainExpandedCategories(categories: Collection<String>) {
        _expandedCategories.update { it.intersect(categories.toSet()) }
    }

    fun addFeed(title: String, url: String, category: String, feedType: String = "rss") {
        viewModelScope.launch { addFeedUseCase(title, url, category, feedType) }
    }

    fun updateFeed(feed: Feed) {
        viewModelScope.launch { updateFeedUseCase(feed) }
    }

    /** Persists a one-time favicon-derived accent color for [feed] (no-op once already set). */
    fun setFeedAccent(feed: Feed, accentColor: Long) {
        if (feed.accentColor != null) return
        viewModelScope.launch { updateFeedUseCase(feed.copy(accentColor = accentColor)) }
    }

    fun updateCategoryOrder(categories: List<String>) {
        viewModelScope.launch {
            val updatedFeeds = feeds.value.map { feed ->
                val newOrder = categories.indexOf(feed.category)
                feed.copy(categoryOrder = if (newOrder != -1) newOrder else feed.categoryOrder)
            }
            updateFeedUseCase(updatedFeeds)
        }
    }

    fun renameCategory(oldName: String, newName: String) {
        viewModelScope.launch {
            val updated = feeds.value
                .filter { it.category == oldName }
                .map { it.copy(category = newName) }
            updateFeedUseCase(updated)
        }
    }

    fun deleteFeed(feed: Feed) {
        viewModelScope.launch { deleteFeedUseCase(feed) }
    }

    suspend fun discoverFeed(url: String): FeedDiscoveryResult? = discoverFeedUseCase(url)
}
