package com.jegly.rss.presentation.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jegly.rss.domain.model.SavedArticle
import com.jegly.rss.domain.repository.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedArticlesViewModel @Inject constructor(private val repository: FeedRepository) : ViewModel() {
    val savedArticles: StateFlow<List<SavedArticle>> = repository.getSavedArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun unsave(link: String) {
        viewModelScope.launch { repository.unsaveArticle(link) }
    }
}
