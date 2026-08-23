package com.jegly.rss.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jegly.rss.domain.model.Feed
import com.jegly.rss.domain.model.FeedDiscoveryResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeedBottomSheet(
    onDismiss: () -> Unit,
    onAdd: (title: String, url: String, category: String, feedType: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var isDiscovering by remember { mutableStateOf(false) }
    // null = not yet probed, "rss" = RSS found, "web" = web bookmark
    var detectedType by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var statusIsError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val viewModel: HomeViewModel = hiltViewModel()
    val feeds by viewModel.feeds.collectAsState()
    val existingCategories = remember(feeds) {
        feeds.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }

    // Reset detection state when URL changes.
    LaunchedEffect(url) {
        detectedType = null
        statusMessage = null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Add Source", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                leadingIcon = { Icon(Icons.Default.Title, null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Feed or Website URL") },
                leadingIcon = { Icon(Icons.Default.Link, null) },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    val msg = statusMessage
                    when {
                        msg != null -> Text(msg, color = if (statusIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                        else -> Text("Paste any URL — RSS feeds and websites both work.")
                    }
                },
                isError = statusIsError,
                trailingIcon = {
                    when {
                        isDiscovering -> CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        detectedType == "rss" -> Icon(Icons.Default.RssFeed, null, tint = MaterialTheme.colorScheme.primary)
                        detectedType == "web" -> Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.secondary)
                        else -> IconButton(onClick = {
                            if (url.isNotBlank()) {
                                scope.launch {
                                    isDiscovering = true
                                    statusMessage = null
                                    statusIsError = false
                                    when (val result = viewModel.discoverFeed(url)) {
                                        is FeedDiscoveryResult.RssFeed -> {
                                            url = result.feedUrl
                                            detectedType = "rss"
                                            statusMessage = "RSS feed found"
                                        }
                                        is FeedDiscoveryResult.WebBookmark -> {
                                            url = result.url
                                            detectedType = "web"
                                            statusMessage = "No RSS found — will add as web bookmark"
                                        }
                                        null -> {
                                            statusIsError = true
                                            statusMessage = "Could not reach this URL"
                                        }
                                    }
                                    isDiscovering = false
                                }
                            }
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Detect feed type")
                        }
                    }
                }
            )

            CategoryPicker(
                value = category,
                onValueChange = { category = it },
                existingCategories = existingCategories
            )

            val buttonLabel = when (detectedType) {
                "web" -> "Add as Web Bookmark"
                else -> "Add Feed"
            }

            Button(
                onClick = {
                    scope.launch {
                        val finalCategory = category.trim().ifBlank { "Uncategorized" }
                        if (detectedType != null) {
                            onAdd(title, url.trim(), finalCategory, detectedType!!)
                            onDismiss()
                        } else {
                            // Auto-detect on add if user skipped the search button.
                            isDiscovering = true
                            var finalUrl = url.trim()
                            val feedType = when (val result = viewModel.discoverFeed(finalUrl)) {
                                is FeedDiscoveryResult.RssFeed -> {
                                    finalUrl = result.feedUrl
                                    "rss"
                                }
                                is FeedDiscoveryResult.WebBookmark -> {
                                    finalUrl = result.url
                                    "web"
                                }
                                else -> "web"
                            }
                            isDiscovering = false
                            onAdd(title, finalUrl, finalCategory, feedType)
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isDiscovering && title.isNotBlank() && url.isNotBlank()
            ) { Text(buttonLabel) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFeedBottomSheet(
    feed: Feed,
    onDismiss: () -> Unit,
    onConfirm: (Feed) -> Unit,
    onDelete: (Feed) -> Unit
) {
    var title by remember { mutableStateOf(feed.title) }
    var url by remember { mutableStateOf(feed.url) }
    var category by remember { mutableStateOf(feed.category) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val viewModel: HomeViewModel = hiltViewModel()
    val feeds by viewModel.feeds.collectAsState()
    val existingCategories = remember(feeds) {
        feeds.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Feed", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("Remove \"${feed.title}\"?", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(feed)
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete", style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel", style = MaterialTheme.typography.labelLarge) }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Edit Source", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                // Badge showing the feed type.
                SuggestionChip(
                    onClick = {},
                    label = { Text(if (feed.feedType == "web") "Web" else "RSS") },
                    icon = {
                        Icon(
                            if (feed.feedType == "web") Icons.Default.Language else Icons.Default.RssFeed,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                leadingIcon = { Icon(Icons.Default.Title, null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL") },
                leadingIcon = { Icon(Icons.Default.Link, null) },
                modifier = Modifier.fillMaxWidth()
            )

            CategoryPicker(
                value = category,
                onValueChange = { category = it },
                existingCategories = existingCategories
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }

                Button(
                    onClick = {
                        onConfirm(feed.copy(title = title, url = url, category = category))
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Save") }
            }
        }
    }
}
