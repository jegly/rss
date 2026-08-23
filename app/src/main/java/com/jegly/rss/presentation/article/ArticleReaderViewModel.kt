package com.jegly.rss.presentation.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jegly.rss.domain.model.Article
import com.jegly.rss.domain.repository.FeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleReaderViewModel @Inject constructor(private val repository: FeedRepository) : ViewModel() {

    fun isSaved(link: String): Flow<Boolean> = repository.isArticleSaved(link)

    fun toggleSaved(saved: Boolean, link: String, title: String, feedTitle: String) {
        viewModelScope.launch {
            if (saved) {
                repository.unsaveArticle(link)
            } else {
                repository.saveArticle(
                    Article(title = title.ifBlank { link }, link = link, pubDate = "", description = ""),
                    feedTitle
                )
            }
        }
    }
}
