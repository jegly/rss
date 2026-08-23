package com.jegly.rss.presentation.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import android.graphics.Bitmap
import androidx.palette.graphics.Palette
import coil3.BitmapImage
import coil3.compose.AsyncImage
import com.jegly.rss.domain.model.Feed
import com.jegly.rss.presentation.settings.SettingsViewModel
import com.jegly.rss.util.faviconUrlFor
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.roundToInt

/** Route for opening a feed — a plain webpage for "web" bookmarks, the article list otherwise. */
private fun feedOpenRoute(feed: Feed): String {
    val encodedUrl = URLEncoder.encode(feed.url, StandardCharsets.UTF_8.toString())
    return if (feed.feedType == "web") {
        val encodedTitle = URLEncoder.encode(feed.title, StandardCharsets.UTF_8.toString())
        "article/$encodedUrl?title=$encodedTitle"
    } else {
        val encodedTitle = URLEncoder.encode(feed.title, StandardCharsets.UTF_8.toString())
        "feed_detail/$encodedUrl?title=$encodedTitle"
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val feeds by viewModel.feeds.collectAsState()
    val cardSizeMultiplier by settingsViewModel.cardSizeMultiplier.collectAsState()
    val viewMode by settingsViewModel.viewMode.collectAsState()
    val autoExpandCategories by settingsViewModel.autoExpandCategories.collectAsState()
    val expandedCategories by viewModel.expandedCategories.collectAsState()
    var showViewOptions by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        settingsViewModel.refreshSettings()
    }

    var showAddSheet by remember { mutableStateOf(false) }
    var feedToEdit by remember { mutableStateOf<Feed?>(null) }
    var feedContextMenu by remember { mutableStateOf<Feed?>(null) }
    var feedToMove by remember { mutableStateOf<Feed?>(null) }
    var categoryToDelete by remember { mutableStateOf<String?>(null) }
    var categoryToRename by remember { mutableStateOf<String?>(null) }
    var showCategoryOptions by remember { mutableStateOf<String?>(null) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

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

    LaunchedEffect(autoExpandCategories, categories) {
        if (autoExpandCategories) {
            viewModel.setExpandedCategories(categories)
        } else {
            viewModel.retainExpandedCategories(categories)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (isSearchActive) {
                val onSearchExpandedChange: (Boolean) -> Unit = {
                    isSearchActive = it
                    if (!it) searchQuery = ""
                }
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onSearch = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                            expanded = isSearchActive,
                            onExpandedChange = onSearchExpandedChange,
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
                            }
                        )
                    },
                    expanded = isSearchActive,
                    onExpandedChange = onSearchExpandedChange,
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
                                    onAccentExtracted = { color -> viewModel.setFeedAccent(feed, color) },
                                    onClick = {
                                        navController.navigate(feedOpenRoute(feed))
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        feedContextMenu = feed
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
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showViewOptions = true
                        }) {
                            Icon(
                                imageVector = if (viewMode == "grid") Icons.Default.GridView else Icons.AutoMirrored.Filled.ViewList,
                                contentDescription = "View options",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate("saved")
                        }) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Saved articles",
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
            } else if (viewMode == "grid") {
                // Grid mode: categories as 2-column tiles; tap a tile to expand its feeds below.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(Modifier.height(4.dp))
                    mutableCategories.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { category ->
                                val feedsInCat = feeds.filter { it.category == category }
                                val isExpanded = expandedCategories.contains(category)
                                CategoryGridTile(
                                    category = category,
                                    feedCount = feedsInCat.size,
                                    isExpanded = isExpanded,
                                    multiplier = cardSizeMultiplier,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.toggleCategory(category)
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showCategoryOptions = category
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                        // Expanded feeds appear directly below the row containing them
                        row.forEach { category ->
                            if (expandedCategories.contains(category)) {
                                val feedsInCat = feeds.filter { it.category == category }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                    )
                                    feedsInCat.forEach { feed ->
                                        key(feed.id) {
                                            SwipeToDeleteFeedItem(
                                                feed = feed,
                                                multiplier = cardSizeMultiplier,
                                                onDelete = { viewModel.deleteFeed(feed) },
                                                onAccentExtracted = { color -> viewModel.setFeedAccent(feed, color) },
                                                onClick = {
                                                    navController.navigate(feedOpenRoute(feed))
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    feedContextMenu = feed
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // List mode: accordion categories with drag-to-reorder
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
                                                        viewModel.toggleCategory(category)
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        showCategoryOptions = category
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
                                            key(feed.id) {
                                                SwipeToDeleteFeedItem(
                                                    feed = feed,
                                                    multiplier = cardSizeMultiplier,
                                                    onDelete = { viewModel.deleteFeed(feed) },
                                                    onAccentExtracted = { color -> viewModel.setFeedAccent(feed, color) },
                                                    onClick = {
                                                        navController.navigate(feedOpenRoute(feed))
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        feedContextMenu = feed
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
        }

        if (showViewOptions) {
            ViewOptionsSheet(
                currentMode = viewMode,
                currentSize = cardSizeMultiplier,
                autoExpand = autoExpandCategories,
                onModeChange = { settingsViewModel.setViewMode(it) },
                onSizeChange = { settingsViewModel.setCardSizeMultiplier(it) },
                onAutoExpandChange = { settingsViewModel.setAutoExpandCategories(it) },
                onDismiss = { showViewOptions = false }
            )
        }

        if (showAddSheet) {
            AddFeedBottomSheet(
                onDismiss = { showAddSheet = false },
                onAdd = { t, u, c, ft -> viewModel.addFeed(t, u, c, ft) }
            )
        }

        feedContextMenu?.let { feed ->
            AlertDialog(
                onDismissRequest = { feedContextMenu = null },
                title = {
                    Text(
                        feed.title,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                text = { Text(feed.category, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline) },
                confirmButton = {
                    TextButton(onClick = { feedToEdit = feed; feedContextMenu = null }) {
                        Text("Edit", style = MaterialTheme.typography.labelLarge)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { feedToMove = feed; feedContextMenu = null }) {
                        Text("Move to Category", style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
        }

        feedToMove?.let { feed ->
            val otherCategories = categories.filter { it != feed.category }
            var newCategoryName by remember { mutableStateOf("") }
            var showNewCategoryField by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = { feedToMove = null },
                title = {
                    Text(
                        "Move \"${feed.title}\"",
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                text = {
                    Column {
                        otherCategories.forEach { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(onClick = {
                                        viewModel.updateFeed(feed.copy(category = cat))
                                        feedToMove = null
                                    })
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = false, onClick = null)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(cat, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                        if (showNewCategoryField) {
                            OutlinedTextField(
                                value = newCategoryName,
                                onValueChange = { newCategoryName = it },
                                label = { Text("New category name") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(onClick = { showNewCategoryField = true })
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "New category…",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    if (showNewCategoryField) {
                        TextButton(
                            onClick = {
                                val trimmed = newCategoryName.trim()
                                if (trimmed.isNotEmpty()) {
                                    viewModel.updateFeed(feed.copy(category = trimmed))
                                    feedToMove = null
                                }
                            },
                            enabled = newCategoryName.isNotBlank()
                        ) {
                            Text("Move", style = MaterialTheme.typography.labelLarge)
                        }
                    } else {
                        TextButton(onClick = { feedToMove = null }) {
                            Text("Cancel", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                },
                dismissButton = {
                    if (showNewCategoryField) {
                        TextButton(onClick = { showNewCategoryField = false; newCategoryName = "" }) {
                            Text("Back", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
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

        showCategoryOptions?.let { category ->
            AlertDialog(
                onDismissRequest = { showCategoryOptions = null },
                title = { Text(category, style = MaterialTheme.typography.headlineSmall) },
                text = { Text("Rename this category or delete it and all its feeds?", style = MaterialTheme.typography.bodyMedium) },
                confirmButton = {
                    TextButton(onClick = {
                        categoryToRename = category
                        showCategoryOptions = null
                    }) { Text("Rename", style = MaterialTheme.typography.labelLarge) }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            categoryToDelete = category
                            showCategoryOptions = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Delete", style = MaterialTheme.typography.labelLarge) }
                }
            )
        }

        categoryToRename?.let { category ->
            var newName by remember(category) { mutableStateOf(category) }
            AlertDialog(
                onDismissRequest = { categoryToRename = null },
                title = { Text("Rename Category", style = MaterialTheme.typography.headlineSmall) },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("New name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val trimmed = newName.trim()
                            if (trimmed.isNotBlank() && trimmed != category) {
                                viewModel.renameCategory(category, trimmed)
                            }
                            categoryToRename = null
                        },
                        enabled = newName.trim().isNotBlank()
                    ) { Text("Rename", style = MaterialTheme.typography.labelLarge) }
                },
                dismissButton = {
                    TextButton(onClick = { categoryToRename = null }) { Text("Cancel", style = MaterialTheme.typography.labelLarge) }
                }
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
    onLongClick: () -> Unit,
    onAccentExtracted: (Long) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    @Suppress("DEPRECATION") // confirmValueChange is deprecated without a like-for-like
    // replacement; the swipe-to-confirm interaction here depends on vetoing the state change.
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showDeleteConfirm = true
                true
            } else false
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
                scope.launch { dismissState.reset() }
            },
            title = { Text("Delete Feed", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("Remove \"${feed.title}\"?", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete", style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    scope.launch { dismissState.reset() }
                }) { Text("Cancel", style = MaterialTheme.typography.labelLarge) }
            }
        )
    }

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
                FeedFavicon(feedUrl = feed.url, size = (48 * multiplier).dp, onAccentExtracted = onAccentExtracted)
            },
            colors = ListItemDefaults.colors(
                containerColor = feed.accentColor?.let { accent ->
                    Color(accent).copy(alpha = 0.12f).compositeOver(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                } ?: MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
            )
        )
    }
}

@Composable
fun FeedFavicon(feedUrl: String, size: androidx.compose.ui.unit.Dp, onAccentExtracted: (Long) -> Unit = {}) {
    val faviconUrl = remember(feedUrl) { faviconUrlFor(feedUrl) }
    var loadFailed by remember(faviconUrl) { mutableStateOf(false) }
    var loadedBitmap by remember(faviconUrl) { mutableStateOf<Bitmap?>(null) }

    // Extract a dominant accent color once per feed, off the main thread. onAccentExtracted is a
    // no-op past the first successful extraction (HomeViewModel.setFeedAccent only persists once).
    LaunchedEffect(loadedBitmap) {
        val bitmap = loadedBitmap ?: return@LaunchedEffect
        val swatch = withContext(Dispatchers.Default) {
            runCatching { Palette.from(bitmap).generate() }.getOrNull()
                ?.let { it.vibrantSwatch ?: it.dominantSwatch }
        }
        swatch?.let { onAccentExtracted(it.rgb.toLong() and 0xFFFFFFFFL) }
    }

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (faviconUrl != null && !loadFailed) {
                AsyncImage(
                    model = faviconUrl,
                    contentDescription = null,
                    onError = { loadFailed = true },
                    onSuccess = { state ->
                        (state.result.image as? BitmapImage)?.let { loadedBitmap = it.bitmap }
                    },
                    modifier = Modifier.size(size * 0.6f)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.RssFeed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(size * 0.5f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryGridTile(
    category: String,
    feedCount: Int,
    isExpanded: Boolean,
    multiplier: Float,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(if (isExpanded) 90f else 0f)
    Card(
        modifier = modifier
            .height((88 * multiplier).dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer
                           else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size((20 * multiplier).dp)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer
                           else MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                        .size((16 * multiplier).dp)
                        .rotate(rotation)
                )
            }
            Column {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = (MaterialTheme.typography.labelLarge.fontSize.value * multiplier).sp
                    ),
                    color = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$feedCount ${if (feedCount == 1) "feed" else "feeds"}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = (MaterialTheme.typography.labelSmall.fontSize.value * multiplier).sp
                    ),
                    color = if (isExpanded) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewOptionsSheet(
    currentMode: String,
    currentSize: Float,
    autoExpand: Boolean,
    onModeChange: (String) -> Unit,
    onSizeChange: (Float) -> Unit,
    onAutoExpandChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
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
            Text("View options", style = MaterialTheme.typography.titleLarge)

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = currentMode == "list",
                    onClick = { onModeChange("list") },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = { Icon(Icons.AutoMirrored.Filled.ViewList, null) }
                ) { Text("List") }
                SegmentedButton(
                    selected = currentMode == "grid",
                    onClick = { onModeChange("grid") },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { Icon(Icons.Default.GridView, null) }
                ) { Text("Grid") }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-expand categories", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "Expand all categories automatically",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Switch(
                    checked = autoExpand,
                    onCheckedChange = onAutoExpandChange
                )
            }

            Text("Card size — ${(currentSize * 100).roundToInt()}%", style = MaterialTheme.typography.labelLarge)
            Slider(
                value = currentSize,
                onValueChange = onSizeChange,
                valueRange = 0.5f..1.5f
            )
        }
    }
}
