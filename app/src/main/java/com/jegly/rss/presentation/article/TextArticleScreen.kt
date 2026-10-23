package com.jegly.rss.presentation.article

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Text-only article view: shows the body the feed already delivered, so opening an article costs
 * no extra network traffic. The full page is still one tap away via [onOpenFullPage].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextArticleScreen(
    title: String,
    feedTitle: String,
    pubDate: String,
    body: String,
    isSaved: Boolean,
    onToggleSaved: () -> Unit,
    onOpenFullPage: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(feedTitle.ifBlank { "Article" }, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleSaved) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isSaved) "Remove from saved" else "Save for later"
                        )
                    }
                    IconButton(onClick = onOpenFullPage) {
                        Icon(Icons.Default.Language, contentDescription = "Open full page")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SelectionContainer {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(title, style = MaterialTheme.typography.headlineSmall)
                    if (pubDate.isNotBlank()) {
                        Text(pubDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Text(body, style = MaterialTheme.typography.bodyLarge)
                }
            }
            OutlinedButton(onClick = onOpenFullPage, modifier = Modifier.padding(vertical = 8.dp)) {
                Icon(Icons.Default.Language, null)
                Spacer(Modifier.width(8.dp))
                Text("Open full page")
            }
        }
    }
}
