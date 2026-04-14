package com.jegly.rss.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jegly.rss.domain.model.Feed

@Composable
fun EditFeedDialog(
    feed: Feed,
    onDismiss: () -> Unit,
    onConfirm: (Feed) -> Unit,
    onDelete: (Feed) -> Unit
) {
    var title by remember { mutableStateOf(feed.title) }
    var url by remember { mutableStateOf(feed.url) }
    var category by remember { mutableStateOf(feed.category) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Feed") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(feed.copy(title = title, url = url, category = category))
                    onDismiss()
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(
                onClick = { 
                    onDelete(feed)
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete") }
        }
    )
}
