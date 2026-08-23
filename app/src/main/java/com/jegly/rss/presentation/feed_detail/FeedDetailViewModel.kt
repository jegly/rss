package com.jegly.rss.presentation.feed_detail
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.jegly.rss.domain.model.Article
import com.jegly.rss.domain.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedDetailViewModel @Inject constructor(private val repository: FeedRepository) : ViewModel() {
    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Links currently saved for later — derived straight from the DB so it stays correct
    // regardless of which screen (or the saved-articles list) toggled the save state.
    val savedLinks: StateFlow<Set<String>> = repository.getSavedArticles()
        .map { saved -> saved.map { it.link }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // URL whose articles are already loaded. Used to avoid re-fetching (and flipping
    // back into the loading state, which would destroy the list's scroll position)
    // when the screen re-enters composition after returning from an article.
    private var loadedUrl: String? = null

    fun fetchArticles(url: String) {
        if (loadedUrl == url && _articles.value.isNotEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _articles.value = repository.fetchArticles(url)
                loadedUrl = url
            }
            catch (e: Exception) { e.printStackTrace() }
            finally { _isLoading.value = false }
        }
    }

    fun toggleSaved(article: Article, feedTitle: String) {
        viewModelScope.launch {
            if (article.link in savedLinks.value) {
                repository.unsaveArticle(article.link)
            } else {
                repository.saveArticle(article, feedTitle)
            }
        }
    }
}
