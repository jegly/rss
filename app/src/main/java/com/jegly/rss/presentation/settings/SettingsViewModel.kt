package com.jegly.rss.presentation.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jegly.rss.domain.model.Feed
import com.jegly.rss.domain.repository.FeedRepository
import androidx.fragment.app.FragmentActivity
import com.jegly.rss.network.DohProvider
import com.jegly.rss.security.BiometricAuthManager
import com.jegly.rss.security.EncryptionManager
import com.jegly.rss.security.PassphraseGate
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val encryptionManager: EncryptionManager,
    private val passphraseGate: PassphraseGate,
    private val biometricAuthManager: BiometricAuthManager,
    private val repository: FeedRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _dohProvider = MutableStateFlow(DohProvider.fromKey(encryptionManager.getString("doh_provider")))
    val dohProvider: StateFlow<DohProvider> = _dohProvider.asStateFlow()

    fun setDohProvider(provider: DohProvider) {
        encryptionManager.saveString("doh_provider", provider.key)
        _dohProvider.value = provider
    }

    private val _useBiometrics = MutableStateFlow(encryptionManager.getBoolean("use_biometrics", false))
    val useBiometrics: StateFlow<Boolean> = _useBiometrics.asStateFlow()

    private val _fontSize = MutableStateFlow(encryptionManager.getFloat("font_size", 16f))
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    private val _fontFamily = MutableStateFlow(encryptionManager.getString("font_family") ?: "Default")
    val fontFamily: StateFlow<String> = _fontFamily.asStateFlow()

    private val _cardSizeMultiplier = MutableStateFlow(encryptionManager.getFloat("card_size_multiplier", 1.0f))
    val cardSizeMultiplier: StateFlow<Float> = _cardSizeMultiplier.asStateFlow()

    private val _wifiOnlySync = MutableStateFlow(encryptionManager.getBoolean("wifi_only_sync", false))
    val wifiOnlySync: StateFlow<Boolean> = _wifiOnlySync.asStateFlow()

    private val _preloadImages = MutableStateFlow(encryptionManager.getBoolean("preload_images", true))
    val preloadImages: StateFlow<Boolean> = _preloadImages.asStateFlow()

    private val _screenshotProtection = MutableStateFlow(encryptionManager.getBoolean("screenshot_protection", true))
    val screenshotProtection: StateFlow<Boolean> = _screenshotProtection.asStateFlow()

    private val _syncFrequency = MutableStateFlow(encryptionManager.getString("sync_frequency") ?: "Manual")
    val syncFrequency: StateFlow<String> = _syncFrequency.asStateFlow()

    private val _viewMode = MutableStateFlow(encryptionManager.getString("view_mode") ?: "list")
    val viewMode: StateFlow<String> = _viewMode.asStateFlow()

    fun setViewMode(mode: String) {
        encryptionManager.saveString("view_mode", mode)
        _viewMode.value = mode
    }

    private val _themeMode = MutableStateFlow(encryptionManager.getString("theme_mode") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setThemeMode(mode: String) {
        encryptionManager.saveString("theme_mode", mode)
        _themeMode.value = mode
    }

    private val _catppuccinAccent = MutableStateFlow(encryptionManager.getString("catppuccin_accent") ?: "mauve")
    val catppuccinAccent: StateFlow<String> = _catppuccinAccent.asStateFlow()

    fun setCatppuccinAccent(key: String) {
        encryptionManager.saveString("catppuccin_accent", key)
        _catppuccinAccent.value = key
    }

    private val _catppuccinFlavor = MutableStateFlow(encryptionManager.getString("catppuccin_flavor") ?: "mocha")
    val catppuccinFlavor: StateFlow<String> = _catppuccinFlavor.asStateFlow()

    fun setCatppuccinFlavor(key: String) {
        encryptionManager.saveString("catppuccin_flavor", key)
        _catppuccinFlavor.value = key
    }

    private val _ptyxisPalette = MutableStateFlow(encryptionManager.getString("ptyxis_palette") ?: "nord")
    val ptyxisPalette: StateFlow<String> = _ptyxisPalette.asStateFlow()

    fun setPtyxisPalette(key: String) {
        encryptionManager.saveString("ptyxis_palette", key)
        _ptyxisPalette.value = key
    }

    private val _draculaAccent = MutableStateFlow(encryptionManager.getString("dracula_accent") ?: "purple")
    val draculaAccent: StateFlow<String> = _draculaAccent.asStateFlow()

    fun setDraculaAccent(key: String) {
        encryptionManager.saveString("dracula_accent", key)
        _draculaAccent.value = key
    }

    private val _userAgent = MutableStateFlow(encryptionManager.getString("user_agent") ?: "default")
    val userAgent: StateFlow<String> = _userAgent.asStateFlow()

    fun setUserAgent(key: String) {
        encryptionManager.saveString("user_agent", key)
        _userAgent.value = key
    }

    // --- WebView / Browser privacy ---
    // Cookie policy: "block" | "first_party" | "all". Default first-party only so consent
    // banners and logins work on the page's own domain while cross-site tracking cookies stay blocked.
    private val _cookiePolicy = MutableStateFlow(encryptionManager.getString("cookie_policy") ?: "first_party")
    val cookiePolicy: StateFlow<String> = _cookiePolicy.asStateFlow()

    fun setCookiePolicy(policy: String) {
        encryptionManager.saveString("cookie_policy", policy)
        _cookiePolicy.value = policy
    }

    private val _blockTrackers = MutableStateFlow(encryptionManager.getBoolean("block_trackers", true))
    val blockTrackers: StateFlow<Boolean> = _blockTrackers.asStateFlow()

    fun setBlockTrackers(enabled: Boolean) {
        encryptionManager.saveBoolean("block_trackers", enabled)
        _blockTrackers.value = enabled
    }

    private val _webViewJavaScript = MutableStateFlow(encryptionManager.getBoolean("webview_javascript", true))
    val webViewJavaScript: StateFlow<Boolean> = _webViewJavaScript.asStateFlow()

    fun setWebViewJavaScript(enabled: Boolean) {
        encryptionManager.saveBoolean("webview_javascript", enabled)
        _webViewJavaScript.value = enabled
    }

    private val _webViewDomStorage = MutableStateFlow(encryptionManager.getBoolean("webview_dom_storage", true))
    val webViewDomStorage: StateFlow<Boolean> = _webViewDomStorage.asStateFlow()

    fun setWebViewDomStorage(enabled: Boolean) {
        encryptionManager.saveBoolean("webview_dom_storage", enabled)
        _webViewDomStorage.value = enabled
    }

    private val _clearBrowsingOnClose = MutableStateFlow(encryptionManager.getBoolean("clear_browsing_on_close", false))
    val clearBrowsingOnClose: StateFlow<Boolean> = _clearBrowsingOnClose.asStateFlow()

    fun setClearBrowsingOnClose(enabled: Boolean) {
        encryptionManager.saveBoolean("clear_browsing_on_close", enabled)
        _clearBrowsingOnClose.value = enabled
    }

    private val _doNotTrack = MutableStateFlow(encryptionManager.getBoolean("do_not_track", true))
    val doNotTrack: StateFlow<Boolean> = _doNotTrack.asStateFlow()

    fun setDoNotTrack(enabled: Boolean) {
        encryptionManager.saveBoolean("do_not_track", enabled)
        _doNotTrack.value = enabled
    }

    private val _safeBrowsing = MutableStateFlow(encryptionManager.getBoolean("safe_browsing", true))
    val safeBrowsing: StateFlow<Boolean> = _safeBrowsing.asStateFlow()

    fun setSafeBrowsing(enabled: Boolean) {
        encryptionManager.saveBoolean("safe_browsing", enabled)
        _safeBrowsing.value = enabled
    }

    private val _httpsOnly = MutableStateFlow(encryptionManager.getBoolean("https_only", true))
    val httpsOnly: StateFlow<Boolean> = _httpsOnly.asStateFlow()

    fun setHttpsOnly(enabled: Boolean) {
        encryptionManager.saveBoolean("https_only", enabled)
        _httpsOnly.value = enabled
    }

    private val _autoExpandCategories = MutableStateFlow(encryptionManager.getBoolean("auto_expand_categories", false))
    val autoExpandCategories: StateFlow<Boolean> = _autoExpandCategories.asStateFlow()

    fun setAutoExpandCategories(enabled: Boolean) {
        encryptionManager.saveBoolean("auto_expand_categories", enabled)
        _autoExpandCategories.value = enabled
    }

    val categories: StateFlow<List<String>> = repository.getSavedFeeds()
        .map { feeds -> feeds.map { it.category }.filter { it.isNotBlank() }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _dedupResult = MutableStateFlow<Int?>(null)
    val dedupResult: StateFlow<Int?> = _dedupResult.asStateFlow()

    fun deduplicateFeeds() {
        viewModelScope.launch {
            val feeds = repository.getSavedFeeds().first()
            val toDelete = feeds
                .groupBy { it.url.trim().lowercase() }
                .values
                .filter { it.size > 1 }
                .flatMap { it.drop(1) }
            toDelete.forEach { repository.deleteFeed(it) }
            _dedupResult.value = toDelete.size
        }
    }

    fun clearDedupResult() { _dedupResult.value = null }

    val keystoreSecurityLevel: String = encryptionManager.getKeystoreSecurityLevel()

    fun refreshSettings() {
        _useBiometrics.value = encryptionManager.getBoolean("use_biometrics", false)
        _fontSize.value = encryptionManager.getFloat("font_size", 16f)
        _fontFamily.value = encryptionManager.getString("font_family") ?: "Default"
        _cardSizeMultiplier.value = encryptionManager.getFloat("card_size_multiplier", 1.0f)
        _wifiOnlySync.value = encryptionManager.getBoolean("wifi_only_sync", false)
        _preloadImages.value = encryptionManager.getBoolean("preload_images", true)
        _screenshotProtection.value = encryptionManager.getBoolean("screenshot_protection", true)
        _syncFrequency.value = encryptionManager.getString("sync_frequency") ?: "Manual"
        _viewMode.value = encryptionManager.getString("view_mode") ?: "list"
        _themeMode.value = encryptionManager.getString("theme_mode") ?: "system"
        _catppuccinAccent.value = encryptionManager.getString("catppuccin_accent") ?: "mauve"
        _catppuccinFlavor.value = encryptionManager.getString("catppuccin_flavor") ?: "mocha"
        _draculaAccent.value = encryptionManager.getString("dracula_accent") ?: "purple"
        _ptyxisPalette.value = encryptionManager.getString("ptyxis_palette") ?: "nord"
        _userAgent.value = encryptionManager.getString("user_agent") ?: "default"
        _cookiePolicy.value = encryptionManager.getString("cookie_policy") ?: "first_party"
        _blockTrackers.value = encryptionManager.getBoolean("block_trackers", true)
        _webViewJavaScript.value = encryptionManager.getBoolean("webview_javascript", true)
        _webViewDomStorage.value = encryptionManager.getBoolean("webview_dom_storage", true)
        _clearBrowsingOnClose.value = encryptionManager.getBoolean("clear_browsing_on_close", false)
        _doNotTrack.value = encryptionManager.getBoolean("do_not_track", true)
        _safeBrowsing.value = encryptionManager.getBoolean("safe_browsing", true)
        _httpsOnly.value = encryptionManager.getBoolean("https_only", true)
        _autoExpandCategories.value = encryptionManager.getBoolean("auto_expand_categories", false)
    }

    /**
     * Wire the biometric toggle from the UI. When enabling, prompts for biometric and wraps the
     * passphrase under a Keystore key requiring user authentication. When disabling, the wrap is
     * discarded and the (always-present) plain passphrase remains in EncryptedSharedPreferences.
     */
    fun toggleBiometrics(activity: FragmentActivity, enabled: Boolean, onError: (String) -> Unit) {
        if (!enabled) {
            encryptionManager.saveBoolean("use_biometrics", false)
            passphraseGate.clearBiometricWrap()
            _useBiometrics.value = false
            return
        }
        if (!biometricAuthManager.isBiometricAvailable()) {
            onError("No strong biometrics enrolled on this device.")
            return
        }
        val cipher = runCatching { passphraseGate.makeEncryptionCipher() }.getOrNull()
        if (cipher == null) {
            onError("Keystore unavailable.")
            return
        }
        biometricAuthManager.showCryptoPrompt(
            activity = activity,
            cipher = cipher,
            title = "Enable biometric unlock",
            subtitle = "Wrap your database key under your biometric",
            onSuccess = { unlocked ->
                val plain = passphraseGate.loadOrCreatePlainPassphrase()
                passphraseGate.wrapPlainPassphraseUnderBiometric(plain, unlocked)
                encryptionManager.saveBoolean("use_biometrics", true)
                _useBiometrics.value = true
            },
            onError = { msg ->
                encryptionManager.saveBoolean("use_biometrics", false)
                passphraseGate.clearBiometricWrap()
                _useBiometrics.value = false
                onError(msg)
            }
        )
    }

    fun setFontSize(size: Float) {
        encryptionManager.saveFloat("font_size", size)
        _fontSize.value = size
    }

    fun setFontFamily(family: String) {
        encryptionManager.saveString("font_family", family)
        _fontFamily.value = family
    }

    fun setCardSizeMultiplier(multiplier: Float) {
        encryptionManager.saveFloat("card_size_multiplier", multiplier)
        _cardSizeMultiplier.value = multiplier
    }

    fun setWifiOnlySync(enabled: Boolean) {
        encryptionManager.saveBoolean("wifi_only_sync", enabled)
        _wifiOnlySync.value = enabled
    }

    fun setPreloadImages(enabled: Boolean) {
        encryptionManager.saveBoolean("preload_images", enabled)
        _preloadImages.value = enabled
    }

    fun setScreenshotProtection(enabled: Boolean) {
        encryptionManager.saveBoolean("screenshot_protection", enabled)
        _screenshotProtection.value = enabled
    }

    fun setSyncFrequency(frequency: String) {
        encryptionManager.saveString("sync_frequency", frequency)
        _syncFrequency.value = frequency
    }

    /**
     * Cache Deletion: Clears temporary files without removing user feeds or settings.
     */
    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            context.cacheDir.deleteRecursively()
            context.cacheDir.mkdirs()
        }
    }

    /**
     * Data Deletion: Wipes all app data including database, preferences, cache, and the
     * biometric-bound Keystore key.
     */
    fun wipeAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            passphraseGate.clearBiometricWrap()
            encryptionManager.securePrefs.edit().clear().commit()
            context.deleteDatabase("secure_rss.db")
            context.cacheDir.deleteRecursively()
            refreshSettings()
        }
    }

    fun importOpml(context: Context, uri: Uri, category: String = "Uncategorized") {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val content = inputStream?.bufferedReader()?.use { it.readText() }
                    if (content != null) {
                        val feeds = parseOpml(content)
                        feeds.forEach { repository.addFeed(it.title, it.url, category) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun exportOpml(context: Context, uri: Uri, onComplete: (Uri) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val feeds = repository.getSavedFeeds().first()
                    val opml = buildOpml(feeds)
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.bufferedWriter().use { it.write(opml) }
                    }
                    withContext(Dispatchers.Main) { onComplete(uri) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun parseOpml(xml: String): List<Feed> {
        val feeds = mutableListOf<Feed>()
        val regex = "<outline [^>]*title=\"([^\"]*)\" [^>]*xmlUrl=\"([^\"]*)\"".toRegex()
        regex.findAll(xml).forEach { match ->
            val title = match.groups[1]?.value ?: "Unknown"
            val url = match.groups[2]?.value ?: ""
            if (url.isNotBlank()) feeds.add(Feed(title = title, url = url))
        }
        return feeds
    }

    private fun buildOpml(feeds: List<Feed>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<opml version=\"1.0\">\n")
        sb.append("  <head><title>RSS Feeds Export</title></head>\n")
        sb.append("  <body>\n")
        feeds.forEach { feed ->
            sb.append("    <outline text=\"${feed.title}\" title=\"${feed.title}\" type=\"rss\" xmlUrl=\"${feed.url}\"/>\n")
        }
        sb.append("  </body>\n")
        sb.append("</opml>")
        return sb.toString()
    }
}
