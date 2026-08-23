package com.jegly.rss.presentation.feed_detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.jegly.rss.util.BrowserUtils
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedDetailScreen(
    navController: NavController,
    url: String,
    feedTitle: String = "",
    viewModel: FeedDetailViewModel = hiltViewModel()
) {
    val articles by viewModel.articles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val savedLinks by viewModel.savedLinks.collectAsState()
    val decodedUrl = remember { URLDecoder.decode(url, StandardCharsets.UTF_8.toString()) }
    val context = LocalContext.current
    // Saveable so the scroll position is restored when returning from an article.
    val listState = rememberLazyListState()

    LaunchedEffect(decodedUrl) { viewModel.fetchArticles(decodedUrl) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Articles") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            articles.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("No articles found", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "This site may not have an RSS feed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    OutlinedButton(
                        onClick = {
                            val encoded = URLEncoder.encode(decodedUrl, StandardCharsets.UTF_8.toString())
                            navController.navigate("article/$encoded")
                        }
                    ) {
                        Icon(Icons.Default.Language, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Open as Webpage")
                    }
                }
            }
            else -> LazyColumn(state = listState, contentPadding = padding) {
                items(articles) { article ->
                    val isSaved = article.link in savedLinks
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                val encodedUrl = URLEncoder.encode(article.link, StandardCharsets.UTF_8.toString())
                                val encodedTitle = URLEncoder.encode(article.title, StandardCharsets.UTF_8.toString())
                                val encodedFeedTitle = URLEncoder.encode(feedTitle, StandardCharsets.UTF_8.toString())
                                navController.navigate("article/$encodedUrl?title=$encodedTitle&feedTitle=$encodedFeedTitle")
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = article.title, style = MaterialTheme.typography.titleMedium)
                                Text(text = article.pubDate, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { viewModel.toggleSaved(article, feedTitle) }) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = if (isSaved) "Remove from saved" else "Save for later",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
