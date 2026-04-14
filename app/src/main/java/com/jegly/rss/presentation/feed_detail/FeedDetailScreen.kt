package com.jegly.rss.presentation.feed_detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jegly.rss.util.BrowserUtils
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedDetailScreen(navController: NavController, url: String, viewModel: FeedDetailViewModel = hiltViewModel()) {
    val articles by viewModel.articles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val decodedUrl = remember { URLDecoder.decode(url, StandardCharsets.UTF_8.toString()) }
    val context = LocalContext.current

    LaunchedEffect(decodedUrl) { viewModel.fetchArticles(decodedUrl) }

    Scaffold(topBar = { TopAppBar(title = { Text("Articles") }) }) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(contentPadding = padding) {
                items(articles) { article ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                BrowserUtils.openSanitizedUrl(context, article.link)
                            }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = article.title, style = MaterialTheme.typography.titleMedium)
                            Text(text = article.pubDate, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
