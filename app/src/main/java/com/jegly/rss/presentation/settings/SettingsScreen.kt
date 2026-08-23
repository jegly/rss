package com.jegly.rss.presentation.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.jegly.rss.network.DohProvider
import com.jegly.rss.network.UserAgentTemplate
import com.jegly.rss.presentation.home.CategoryPicker
import com.jegly.rss.presentation.theme.CatppuccinFlavor
import com.jegly.rss.presentation.theme.DraculaColors
import com.jegly.rss.presentation.theme.PTYXIS_THEMES
import com.jegly.rss.presentation.theme.catppuccinAccentsFor
import com.jegly.rss.presentation.theme.LEGIBILITY_WARNING_FONTS
import com.jegly.rss.presentation.theme.fontFamilies
import com.jegly.rss.presentation.theme.ptyxisThemeFromKey
import com.jegly.rss.util.BrowserUtils
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private fun cookiePolicyLabel(key: String): String = when (key) {
    "block" -> "Block all cookies"
    "all" -> "Allow all (incl. third-party)"
    else -> "First-party only"
}

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
    val dohProvider by viewModel.dohProvider.collectAsState()
    val userAgentKey by viewModel.userAgent.collectAsState()
    val cookiePolicy by viewModel.cookiePolicy.collectAsState()
    val blockTrackers by viewModel.blockTrackers.collectAsState()
    val webViewJavaScript by viewModel.webViewJavaScript.collectAsState()
    val webViewDomStorage by viewModel.webViewDomStorage.collectAsState()
    val clearBrowsingOnClose by viewModel.clearBrowsingOnClose.collectAsState()
    val doNotTrack by viewModel.doNotTrack.collectAsState()
    val safeBrowsing by viewModel.safeBrowsing.collectAsState()
    val httpsOnly by viewModel.httpsOnly.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val catppuccinAccentKey by viewModel.catppuccinAccent.collectAsState()
    val catppuccinFlavorKey by viewModel.catppuccinFlavor.collectAsState()
    val draculaAccentKey by viewModel.draculaAccent.collectAsState()
    val ptyxisPaletteKey by viewModel.ptyxisPalette.collectAsState()
    val keystoreLevel = viewModel.keystoreSecurityLevel

    val categories by viewModel.categories.collectAsState()
    val dedupResult by viewModel.dedupResult.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showFrequencyDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var importCategory by remember { mutableStateOf("") }
    var showFontDialog by remember { mutableStateOf(false) }
    var pendingFontWarning by remember { mutableStateOf<String?>(null) }
    var showPtyxisDialog by remember { mutableStateOf(false) }
    var showDohDialog by remember { mutableStateOf(false) }
    var showUserAgentDialog by remember { mutableStateOf(false) }
    var showCookieDialog by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(dedupResult) {
        dedupResult?.let { count ->
            snackbarHostState.showSnackbar(
                if (count == 0) "No duplicate feeds found"
                else "Removed $count duplicate ${if (count == 1) "feed" else "feeds"}"
            )
            viewModel.clearDedupResult()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            importCategory = ""
            pendingImportUri = uri
        }
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
                    supportingContent = { Text("Require fingerprint to decrypt the database") },
                    leadingContent = { Icon(Icons.Default.Fingerprint, null) },
                    trailingContent = {
                        Switch(checked = useBiometrics, onCheckedChange = { enabled ->
                            val activity = context as? FragmentActivity
                            if (activity == null) {
                                scope.launch { snackbarHostState.showSnackbar("Cannot toggle biometric: activity context missing") }
                                return@Switch
                            }
                            viewModel.toggleBiometrics(activity, enabled) { msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        })
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
                ListItem(
                    headlineContent = { Text("DNS-over-HTTPS Resolver") },
                    supportingContent = { Text(dohProvider.displayName) },
                    leadingContent = { Icon(Icons.Default.Dns, null) },
                    modifier = Modifier.clickable { showDohDialog = true }
                )
                ListItem(
                    headlineContent = { Text("WebView User Agent") },
                    supportingContent = { Text(UserAgentTemplate.fromKey(userAgentKey).displayName) },
                    leadingContent = { Icon(Icons.Default.Language, null) },
                    modifier = Modifier.clickable { showUserAgentDialog = true }
                )
            }

            // --- BROWSER PRIVACY ---
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Browser Privacy", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                ListItem(
                    headlineContent = { Text("Cookie Policy") },
                    supportingContent = { Text(cookiePolicyLabel(cookiePolicy)) },
                    leadingContent = { Icon(Icons.Default.Cookie, null) },
                    modifier = Modifier.clickable { showCookieDialog = true }
                )
                ListItem(
                    headlineContent = { Text("Block Trackers & Ads") },
                    supportingContent = { Text("Block known tracker and ad domains in the reader") },
                    leadingContent = { Icon(Icons.Default.Block, null) },
                    trailingContent = {
                        Switch(checked = blockTrackers, onCheckedChange = { viewModel.setBlockTrackers(it) })
                    }
                )
                ListItem(
                    headlineContent = { Text("JavaScript") },
                    supportingContent = { Text("Disabling improves privacy but breaks many sites") },
                    leadingContent = { Icon(Icons.Default.Javascript, null) },
                    trailingContent = {
                        Switch(checked = webViewJavaScript, onCheckedChange = { viewModel.setWebViewJavaScript(it) })
                    }
                )
                ListItem(
                    headlineContent = { Text("Site Data (DOM Storage)") },
                    supportingContent = { Text("Allow pages to use local/session storage") },
                    leadingContent = { Icon(Icons.Default.Storage, null) },
                    trailingContent = {
                        Switch(checked = webViewDomStorage, onCheckedChange = { viewModel.setWebViewDomStorage(it) })
                    }
                )
                ListItem(
                    headlineContent = { Text("Clear Browsing Data on Close") },
                    supportingContent = { Text("Wipe cookies and storage when leaving an article") },
                    leadingContent = { Icon(Icons.Default.DeleteSweep, null) },
                    trailingContent = {
                        Switch(checked = clearBrowsingOnClose, onCheckedChange = { viewModel.setClearBrowsingOnClose(it) })
                    }
                )
                ListItem(
                    headlineContent = { Text("Send \"Do Not Track\"") },
                    supportingContent = { Text("Add DNT and Global Privacy Control request signals") },
                    leadingContent = { Icon(Icons.Default.PrivacyTip, null) },
                    trailingContent = {
                        Switch(checked = doNotTrack, onCheckedChange = { viewModel.setDoNotTrack(it) })
                    }
                )
                ListItem(
                    headlineContent = { Text("Safe Browsing") },
                    supportingContent = { Text("Warn about known malware and phishing pages") },
                    leadingContent = { Icon(Icons.Default.GppGood, null) },
                    trailingContent = {
                        Switch(checked = safeBrowsing, onCheckedChange = { viewModel.setSafeBrowsing(it) })
                    }
                )
                ListItem(
                    headlineContent = { Text("Force HTTPS Only") },
                    supportingContent = { Text("Block insecure HTTP page loads and mixed content") },
                    leadingContent = { Icon(Icons.Default.Lock, null) },
                    trailingContent = {
                        Switch(checked = httpsOnly, onCheckedChange = { viewModel.setHttpsOnly(it) })
                    }
                )
            }

            // --- APPEARANCE ---
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Appearance", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

                Spacer(Modifier.height(12.dp))

                // Theme selector
                Text(
                    "Theme",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    SegmentedButton(
                        selected = themeMode == "system",
                        onClick = { viewModel.setThemeMode("system") },
                        shape = SegmentedButtonDefaults.itemShape(0, 4)
                    ) { Text("System") }
                    SegmentedButton(
                        selected = themeMode == "catppuccin",
                        onClick = { viewModel.setThemeMode("catppuccin") },
                        shape = SegmentedButtonDefaults.itemShape(1, 4)
                    ) { Text("Catppuccin") }
                    SegmentedButton(
                        selected = themeMode == "dracula",
                        onClick = { viewModel.setThemeMode("dracula") },
                        shape = SegmentedButtonDefaults.itemShape(2, 4)
                    ) { Text("Dracula") }
                    SegmentedButton(
                        selected = themeMode == "ptyxis",
                        onClick = { viewModel.setThemeMode("ptyxis") },
                        shape = SegmentedButtonDefaults.itemShape(3, 4)
                    ) { Text("Ptyxis") }
                }

                // Catppuccin flavour picker (Latte/Frappé/Macchiato/Mocha)
                if (themeMode == "catppuccin") {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Flavour",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CatppuccinFlavor.entries.forEach { flavor ->
                            FilterChip(
                                selected = catppuccinFlavorKey == flavor.key,
                                onClick = { viewModel.setCatppuccinFlavor(flavor.key) },
                                label = { Text(flavor.displayName) }
                            )
                        }
                    }
                }

                // Accent colour swatches (Catppuccin / Dracula only — Ptyxis uses the dialog below)
                if (themeMode == "catppuccin" || themeMode == "dracula") {
                    Spacer(Modifier.height(12.dp))
                    val accents: Map<String, Pair<String, Color>>
                    val currentAccent: String
                    val setAccent: (String) -> Unit
                    if (themeMode == "catppuccin") {
                        accents = catppuccinAccentsFor(catppuccinFlavorKey)
                        currentAccent = catppuccinAccentKey
                        setAccent = { viewModel.setCatppuccinAccent(it) }
                    } else {
                        accents = DraculaColors.accents
                        currentAccent = draculaAccentKey
                        setAccent = { viewModel.setDraculaAccent(it) }
                    }
                    Text(
                        "Accent colour",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        accents.forEach { (key, pair) ->
                            val (_, color) = pair
                            val isSelected = key == currentAccent
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                    )
                                    .clickable { setAccent(key) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = Color.Black.copy(alpha = 0.65f)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Ptyxis palette picker — 44 named palettes, too many for swatches, so a list dialog.
                if (themeMode == "ptyxis") {
                    Spacer(Modifier.height(12.dp))
                    ListItem(
                        headlineContent = { Text("Palette") },
                        supportingContent = { Text(ptyxisThemeFromKey(ptyxisPaletteKey).displayName) },
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(ptyxisThemeFromKey(ptyxisPaletteKey).primary))
                            )
                        },
                        modifier = Modifier.clickable { showPtyxisDialog = true }
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Spacer(Modifier.height(4.dp))
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
                        onClick = { importLauncher.launch(arrayOf("text/x-opml", "text/xml", "application/xml", "text/plain", "*/*")) },
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
                    onClick = { viewModel.deduplicateFeeds() },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remove Duplicate Feeds")
                }

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

    pendingImportUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            title = { Text("Import OPML") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Choose which category to import the feeds into.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    CategoryPicker(
                        value = importCategory,
                        onValueChange = { importCategory = it },
                        existingCategories = categories,
                        label = "Target Category"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val cat = importCategory.trim().ifBlank { "Uncategorized" }
                    viewModel.importOpml(context, uri, cat)
                    scope.launch { snackbarHostState.showSnackbar("Importing feeds into \"$cat\"…") }
                    pendingImportUri = null
                }) { Text("Import") }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportUri = null }) { Text("Cancel") }
            }
        )
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
                                if (family != fontFamily && family in LEGIBILITY_WARNING_FONTS) {
                                    pendingFontWarning = family
                                } else {
                                    viewModel.setFontFamily(family)
                                    showFontDialog = false
                                }
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

    pendingFontWarning?.let { family ->
        AlertDialog(
            onDismissRequest = { pendingFontWarning = null },
            title = { Text("Hard-to-read font") },
            text = { Text("\"$family\" can be hard to read as body text. Use it anyway?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setFontFamily(family)
                    pendingFontWarning = null
                    showFontDialog = false
                }) { Text("Use anyway") }
            },
            dismissButton = {
                TextButton(onClick = { pendingFontWarning = null }) { Text("Cancel") }
            }
        )
    }

    if (showPtyxisDialog) {
        AlertDialog(
            onDismissRequest = { showPtyxisDialog = false },
            title = { Text("Select Ptyxis Palette") },
            text = {
                LazyColumn {
                    items(items = PTYXIS_THEMES) { palette ->
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                viewModel.setPtyxisPalette(palette.key)
                                showPtyxisDialog = false
                            }.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = ptyxisPaletteKey == palette.key, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(width = 40.dp, height = 28.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(Color(palette.background)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Aa",
                                    color = Color(palette.primary),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(text = palette.displayName, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPtyxisDialog = false }) { Text("Cancel") }
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

    if (showDohDialog) {
        AlertDialog(
            onDismissRequest = { showDohDialog = false },
            title = { Text("DNS Resolver") },
            text = {
                Column {
                    DohProvider.values().forEach { provider ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDohProvider(provider)
                                    showDohDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = dohProvider == provider, onClick = null)
                            Spacer(Modifier.width(16.dp))
                            Text(provider.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDohDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showUserAgentDialog) {
        AlertDialog(
            onDismissRequest = { showUserAgentDialog = false },
            title = { Text("WebView User Agent") },
            text = {
                Column {
                    UserAgentTemplate.entries.forEach { template ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setUserAgent(template.key)
                                    showUserAgentDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = userAgentKey == template.key, onClick = null)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(template.displayName)
                                if (template.uaString.isNotEmpty()) {
                                    Text(
                                        template.uaString.take(50) + "…",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUserAgentDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showCookieDialog) {
        val options = listOf(
            "block" to "Block all cookies",
            "first_party" to "First-party only",
            "all" to "Allow all (incl. third-party)"
        )
        AlertDialog(
            onDismissRequest = { showCookieDialog = false },
            title = { Text("Cookie Policy") },
            text = {
                Column {
                    options.forEach { (key, label) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCookiePolicy(key)
                                    showCookieDialog = false
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = cookiePolicy == key, onClick = null)
                            Spacer(Modifier.width(16.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCookieDialog = false }) { Text("Cancel") }
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
