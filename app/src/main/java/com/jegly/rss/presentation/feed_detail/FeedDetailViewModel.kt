package com.jegly.rss.presentation.feed_detail
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.jegly.rss.domain.model.Article
import com.jegly.rss.domain.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedDetailViewModel @Inject constructor(private val repository: FeedRepository) : ViewModel() {
    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchArticles(url: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try { _articles.value = repository.fetchArticles(url) } 
            catch (e: Exception) { e.printStackTrace() }
            finally { _isLoading.value = false }
        }
    }
}
