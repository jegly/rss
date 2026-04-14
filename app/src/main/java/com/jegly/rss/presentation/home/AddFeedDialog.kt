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
import kotlinx.coroutines.launch

@Composable
fun AddFeedDialog(
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add RSS Feed") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            Text("Paste a site URL and we'll find the feed.")
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
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (title.isNotBlank() && url.isNotBlank()) {
                        onAdd(title, url, category)
                        onDismiss() 
                    }
                },
                enabled = !isDiscovering && title.isNotBlank() && url.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
