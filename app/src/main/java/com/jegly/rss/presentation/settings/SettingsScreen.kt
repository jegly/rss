package com.jegly.rss.presentation.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jegly.rss.presentation.theme.fontFamilies
import com.jegly.rss.util.BrowserUtils
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val useBiometrics by viewModel.useBiometrics.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val fontFamily by viewModel.fontFamily.collectAsState()
    val cardSizeMultiplier by viewModel.cardSizeMultiplier.collectAsState()
    val wifiOnlySync by viewModel.wifiOnlySync.collectAsState()
    val preloadImages by viewModel.preloadImages.collectAsState()
    val screenshotProtection by viewModel.screenshotProtection.collectAsState()
    val syncFrequency by viewModel.syncFrequency.collectAsState()
    val keystoreLevel = viewModel.keystoreSecurityLevel
    
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showFrequencyDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }
    
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.importOpml(context, it) }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/x-opml")) { uri: Uri? ->
        uri?.let { 
            viewModel.exportOpml(context, it) {
                scope.launch { snackbarHostState.showSnackbar("Feeds exported successfully") }
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SECURITY ---
            item {
                Text("Security", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                
                ListItem(
                    headlineContent = { Text("Hardware Security Level") },
                    supportingContent = { Text(keystoreLevel) },
                    leadingContent = { Icon(Icons.Default.Shield, null, tint = MaterialTheme.colorScheme.primary) }
                )

                ListItem(
                    headlineContent = { Text("Biometric Authentication") },
                    supportingContent = { Text("Require fingerprint to open the app") },
                    leadingContent = { Icon(Icons.Default.Fingerprint, null) },
                    trailingContent = {
                        Switch(checked = useBiometrics, onCheckedChange = { viewModel.setBiometrics(it) })
                    }
                )
                ListItem(
                    headlineContent = { Text("Screenshot Protection") },
                    supportingContent = { Text("Prevent screenshots and screen recording") },
                    leadingContent = { Icon(Icons.Default.NoEncryption, null) },
                    trailingContent = {
                        Switch(checked = screenshotProtection, onCheckedChange = { viewModel.setScreenshotProtection(it) })
                    }
                )
            }

            // --- NETWORKING ---
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Networking & Privacy", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                ListItem(
                    headlineContent = { Text("Wi-Fi Only Sync") },
                    supportingContent = { Text("Only fetch feeds when connected to Wi-Fi") },
                    leadingContent = { Icon(Icons.Default.Wifi, null) },
                    trailingContent = {
                        Switch(checked = wifiOnlySync, onCheckedChange = { viewModel.setWifiOnlySync(it) })
                    }
                )
                ListItem(
                    headlineContent = { Text("Preload Images") },
                    supportingContent = { Text("Automatically load article images") },
                    leadingContent = { Icon(Icons.Default.Image, null) },
                    trailingContent = {
                        Switch(checked = preloadImages, onCheckedChange = { viewModel.setPreloadImages(it) })
                    }
                )
                ListItem(
                    headlineContent = { Text("Sync Frequency") },
                    supportingContent = { Text(syncFrequency) },
                    leadingContent = { Icon(Icons.Default.Sync, null) },
                    modifier = Modifier.clickable { showFrequencyDialog = true }
                )
            }

            // --- APPEARANCE ---
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Appearance", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }

            item {
                ListItem(
                    headlineContent = { Text("Font Family") },
                    supportingContent = { Text(fontFamily) },
                    leadingContent = { Icon(Icons.Default.FontDownload, null) },
                    modifier = Modifier.clickable { showFontDialog = true }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Global Font Size") },
                    supportingContent = { Text("${fontSize.roundToInt()} sp") },
                    leadingContent = { Icon(Icons.Default.FormatSize, null) }
                )
                Slider(
                    value = fontSize,
                    onValueChange = { viewModel.setFontSize(it) },
                    valueRange = 12f..24f,
                    steps = 6,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Feed Card Size") },
                    supportingContent = { Text("${(cardSizeMultiplier * 100).roundToInt()}%") },
                    leadingContent = { Icon(Icons.Default.Square, null) }
                )
                Slider(
                    value = cardSizeMultiplier,
                    onValueChange = { viewModel.setCardSizeMultiplier(it) },
                    valueRange = 0.5f..1.5f,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // --- BACKUP & EXPORT ---
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Backup & Export", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { importLauncher.launch("text/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileUpload, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Import OPML")
                    }
                    OutlinedButton(
                        onClick = { exportLauncher.launch("feeds.opml") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileDownload, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Export OPML")
                    }
                }
            }

            // --- DATA MANAGEMENT ---
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Data Management", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { 
                        viewModel.clearCache()
                        scope.launch {
                            snackbarHostState.showSnackbar("Cache cleared successfully")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(Icons.Default.Cached, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Cache")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Wipe All App Data")
                }
            }

            // --- ABOUT ---
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("About", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                
                ListItem(
                    headlineContent = { Text("GitHub") },
                    supportingContent = { Text("github.com/jegly") },
                    leadingContent = { Icon(Icons.Default.Code, null) },
                    modifier = Modifier.clickable { 
                        BrowserUtils.openSanitizedUrl(context, "https://github.com/jegly")
                    }
                )
                
                ListItem(
                    headlineContent = { Text("Website") },
                    supportingContent = { Text("jegly.xyz") },
                    leadingContent = { Icon(Icons.Default.Language, null) },
                    modifier = Modifier.clickable { 
                        BrowserUtils.openSanitizedUrl(context, "https://www.jegly.xyz")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "made with love by Jegly",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (showFontDialog) {
        AlertDialog(
            onDismissRequest = { showFontDialog = false },
            title = { Text("Select Font Family") },
            text = {
                val fontList = fontFamilies.keys.toList()
                LazyColumn {
                    items(items = fontList) { family ->
                        Row(
                            Modifier.fillMaxWidth().clickable { 
                                viewModel.setFontFamily(family)
                                showFontDialog = false
                            }.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = fontFamily == family, onClick = null)
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = family,
                                fontFamily = fontFamilies[family] ?: FontFamily.Default,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFontDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showFrequencyDialog) {
        val options = listOf("Manual", "Every 1h", "Every 6h", "Every 12h")
        AlertDialog(
            onDismissRequest = { showFrequencyDialog = false },
            title = { Text("Sync Frequency") },
            text = {
                Column {
                    options.forEach { option ->
                        Row(
                            Modifier.fillMaxWidth().clickable { 
                                viewModel.setSyncFrequency(option)
                                showFrequencyDialog = false
                            }.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = syncFrequency == option, onClick = null)
                            Spacer(Modifier.width(16.dp))
                            Text(option)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFrequencyDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Wipe All Data?") },
            text = { Text("This will permanently delete all your feeds, settings, and cached articles. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.wipeAllData()
                        showDeleteConfirm = false
                        navController.navigate("home") {
                            popUpTo(0)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Wipe Everything") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
