package com.jegly.rss.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jegly.rss.domain.model.Feed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeedBottomSheet(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Uncategorized") }
    var isDiscovering by remember { mutableStateOf(false) }
    var discoveryError by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val viewModel: HomeViewModel = hiltViewModel()

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
            Text("Add RSS Feed", style = MaterialTheme.typography.headlineSmall)
            
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
                    if (discoveryError != null) {
                        Text(discoveryError!!, color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Paste a site URL to find the feed.")
                    }
                },
                isError = discoveryError != null,
                trailingIcon = {
                    if (isDiscovering) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        IconButton(onClick = {
                            if (url.isNotBlank()) {
                                scope.launch {
                                    isDiscovering = true
                                    discoveryError = null
                                    val discovered = viewModel.discoverFeed(url)
                                    if (discovered != null) {
                                        url = discovered
                                    } else {
                                        discoveryError = "No feed found at this URL"
                                    }
                                    isDiscovering = false
                                }
                            }
                        }) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = "Discover Feed")
                        }
                    }
                }
            )
            
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                leadingIcon = { Icon(Icons.Default.Category, null) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                onClick = { 
                    onAdd(title, url, category)
                    onDismiss() 
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isDiscovering && title.isNotBlank() && url.isNotBlank()
            ) { Text("Add Feed") }
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
            Text("Edit Feed", style = MaterialTheme.typography.headlineSmall)
            
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
            
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                leadingIcon = { Icon(Icons.Default.Category, null) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { 
                        onDelete(feed)
                        onDismiss()
                    },
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
