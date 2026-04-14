package com.jegly.rss.presentation.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jegly.rss.domain.model.Feed
import com.jegly.rss.domain.repository.FeedRepository
import com.jegly.rss.security.EncryptionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val encryptionManager: EncryptionManager,
    private val repository: FeedRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
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
    }

    fun setBiometrics(enabled: Boolean) {
        encryptionManager.saveBoolean("use_biometrics", enabled)
        _useBiometrics.value = enabled
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
     * Data Deletion: Wipes all app data including database, preferences, and cache.
     */
    fun wipeAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            encryptionManager.securePrefs.edit().clear().commit()
            context.deleteDatabase("secure_rss.db")
            context.cacheDir.deleteRecursively()
            refreshSettings()
        }
    }

    fun importOpml(context: Context, uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val content = inputStream?.bufferedReader()?.use { it.readText() }
                    if (content != null) {
                        val feeds = parseOpml(content)
                        feeds.forEach { repository.addFeed(it.title, it.url) }
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
