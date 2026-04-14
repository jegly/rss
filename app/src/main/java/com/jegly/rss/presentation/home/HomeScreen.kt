package com.jegly.rss.presentation.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jegly.rss.domain.model.Feed
import com.jegly.rss.presentation.settings.SettingsViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController, 
    viewModel: HomeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val feeds by viewModel.feeds.collectAsState()
    val cardSizeMultiplier by settingsViewModel.cardSizeMultiplier.collectAsState()
    
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        settingsViewModel.refreshSettings()
    }
    
    var showAddSheet by remember { mutableStateOf(false) }
    var feedToEdit by remember { mutableStateOf<Feed?>(null) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }
    
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Pull to Refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(1500) 
            isRefreshing = false
        }
    }

    // Filtered feeds for search results (FLAT LIST)
    val searchResults = remember(searchQuery, feeds) {
        if (searchQuery.isBlank()) emptyList()
        else feeds.filter { 
            it.title.contains(searchQuery, ignoreCase = true) || 
            it.url.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    val categories = remember(feeds) { 
        feeds.sortedBy { it.categoryOrder }
            .map { it.category }
            .distinct()
    }
    
    var mutableCategories by remember(categories) { mutableStateOf(categories) }
    val expandedCategories = remember { mutableStateListOf<String>() }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (isSearchActive) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                    active = isSearchActive,
                    onActiveChange = { 
                        isSearchActive = it 
                        if (!it) searchQuery = ""
                    },
                    placeholder = { Text("Search feeds...") },
                    leadingIcon = { 
                        IconButton(onClick = { 
                            isSearchActive = false 
                            searchQuery = ""
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (searchResults.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (searchQuery.isEmpty()) "Search for feeds..." else "No results found",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchResults) { feed ->
                                SwipeToDeleteFeedItem(
                                    feed = feed,
                                    multiplier = cardSizeMultiplier,
                                    onDelete = { viewModel.deleteFeed(feed) },
                                    onClick = {
                                        val encodedUrl = URLEncoder.encode(feed.url, StandardCharsets.UTF_8.toString())
                                        navController.navigate("feed_detail/$encodedUrl")
                                    },
                                    onLongClick = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        feedToEdit = feed 
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                TopAppBar(
                    title = { },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                imageVector = Icons.Default.Search, 
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate("settings") 
                        }) {
                            Icon(
                                imageVector = Icons.Default.Settings, 
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        },
        floatingActionButton = {
            if (!isSearchActive) {
                FloatingActionButton(
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAddSheet = true 
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Feed")
                }
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            if (feeds.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No feeds added yet", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                val lazyListState = rememberLazyListState()
                val reorderableState = rememberReorderableLazyListState(
                    lazyListState = lazyListState,
                    onMove = { from, to ->
                        val newList = mutableCategories.toMutableList().apply {
                            add(to.index, removeAt(from.index))
                        }
                        mutableCategories = newList
                        viewModel.updateCategoryOrder(newList)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                )

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = 80.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mutableCategories, key = { it }) { category ->
                        ReorderableItem(reorderableState, key = category) { isDragging ->
                            val feedsInCategory = feeds.filter { it.category == category }
                            val isExpanded = expandedCategories.contains(category)
                            val elevation by animateFloatAsState(if (isDragging) 8f else 0f)

                            Column {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = MaterialTheme.colorScheme.background,
                                    tonalElevation = elevation.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = (12 * cardSizeMultiplier).dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .combinedClickable(
                                                    onClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        if (isExpanded) expandedCategories.remove(category)
                                                        else expandedCategories.add(category)
                                                    },
                                                    onLongClick = { 
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        categoryToDelete = category 
                                                    }
                                                ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val rotation by animateFloatAsState(if (isExpanded) 90f else 0f)
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .size((24 * cardSizeMultiplier).dp)
                                                    .rotate(rotation)
                                            )
                                            Spacer(modifier = Modifier.width((8 * cardSizeMultiplier).dp))
                                            Text(
                                                text = category,
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontSize = (MaterialTheme.typography.labelLarge.fontSize.value * cardSizeMultiplier).sp
                                                ),
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "${feedsInCategory.size}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = (MaterialTheme.typography.labelSmall.fontSize.value * cardSizeMultiplier).sp
                                                ),
                                                color = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            )
                                        }

                                        Icon(
                                            imageVector = Icons.Default.DragHandle,
                                            contentDescription = "Reorder",
                                            modifier = Modifier
                                                .draggableHandle(
                                                    onDragStarted = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    }
                                                )
                                                .size((24 * cardSizeMultiplier).dp)
                                                .padding(horizontal = 8.dp),
                                            tint = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }
                                }
                                
                                if (isExpanded) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        feedsInCategory.forEach { feed ->
                                            SwipeToDeleteFeedItem(
                                                feed = feed,
                                                multiplier = cardSizeMultiplier,
                                                onDelete = { viewModel.deleteFeed(feed) },
                                                onClick = {
                                                    val encodedUrl = URLEncoder.encode(feed.url, StandardCharsets.UTF_8.toString())
                                                    navController.navigate("feed_detail/$encodedUrl")
                                                },
                                                onLongClick = { 
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    feedToEdit = feed 
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddSheet) {
            AddFeedBottomSheet(
                onDismiss = { showAddSheet = false },
                onAdd = { t, u, c -> viewModel.addFeed(t, u, c) }
            )
        }

        feedToEdit?.let { feed ->
            EditFeedBottomSheet(
                feed = feed,
                onDismiss = { feedToEdit = null },
                onConfirm = { updatedFeed -> viewModel.updateFeed(updatedFeed) },
                onDelete = { feedToDelete -> viewModel.deleteFeed(feedToDelete) }
            )
        }

        categoryToDelete?.let { category ->
            AlertDialog(
                onDismissRequest = { categoryToDelete = null },
                title = { Text("Delete Category", style = MaterialTheme.typography.headlineSmall) },
                text = { Text("Delete '$category' and all feeds inside it?", style = MaterialTheme.typography.bodyMedium) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            feeds.filter { it.category == category }.forEach { 
                                viewModel.deleteFeed(it) 
                            }
                            categoryToDelete = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete All", style = MaterialTheme.typography.labelLarge) }
                },
                dismissButton = {
                    TextButton(onClick = { categoryToDelete = null }) { Text("Cancel", style = MaterialTheme.typography.labelLarge) }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteFeedItem(
    feed: Feed,
    multiplier: Float,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                MaterialTheme.colorScheme.errorContainer
            } else Color.Transparent
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, MaterialTheme.shapes.extraLarge)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) {
        ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            headlineContent = {
                Text(
                    text = feed.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = (MaterialTheme.typography.titleLarge.fontSize.value * multiplier).sp
                    )
                )
            },
            supportingContent = {
                Text(
                    text = feed.url,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = (MaterialTheme.typography.bodyMedium.fontSize.value * multiplier).sp
                    )
                )
            },
            leadingContent = {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.size((48 * multiplier).dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.RssFeed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size((24 * multiplier).dp)
                        )
                    }
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
            )
        )
    }
}
