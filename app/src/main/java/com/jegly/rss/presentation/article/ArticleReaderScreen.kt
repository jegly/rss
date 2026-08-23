package com.jegly.rss.presentation.article

import android.annotation.SuppressLint
import android.net.http.SslError
import android.os.Message
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.jegly.rss.network.UserAgentTemplate
import com.jegly.rss.presentation.settings.SettingsViewModel
import com.jegly.rss.util.BrowserUtils
import com.jegly.rss.util.WebViewPool
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

// Tracker and ad domains blocked in shouldInterceptRequest.
private val BLOCKED_TRACKER_HOSTS = setOf(
    "google-analytics.com", "googletagmanager.com", "analytics.google.com",
    "doubleclick.net", "googlesyndication.com", "adservice.google.com",
    "connect.facebook.net", "staticxx.facebook.com",
    "ads.twitter.com", "analytics.twitter.com",
    "scorecardresearch.com", "quantserve.com",
    "hotjar.com", "fullstory.com",
    "segment.io", "cdn.segment.com", "api.segment.io",
    "cdn.mxpnl.com", "mixpanel.com",
    "amplitude.com", "api.amplitude.com",
    "heapanalytics.com", "heap.io",
    "newrelic.com", "nr-data.net",
    "adnxs.com", "rubiconproject.com", "openx.net",
    "pubmatic.com", "criteo.com", "criteo.net",
    "outbrain.com", "taboola.com", "moatads.com"
)

private fun isBlockedTracker(rawUrl: String): Boolean {
    val host = runCatching { java.net.URL(rawUrl).host.lowercase() }.getOrNull() ?: return false
    return BLOCKED_TRACKER_HOSTS.any { blocked -> host == blocked || host.endsWith(".$blocked") }
}

// The WebView uses Chromium's own network stack, so it never passes through the OkHttp
// http->https interceptor used for feed fetches — yet the OS network-security-config forbids all
// cleartext. Upgrade http to https so plain-HTTP links/feeds load in-app instead of failing with
// ERR_CLEARTEXT_NOT_PERMITTED.
private fun upgradeToHttps(rawUrl: String): String = when {
    rawUrl.startsWith("https://", ignoreCase = true) -> rawUrl
    rawUrl.startsWith("http://", ignoreCase = true) -> "https://" + rawUrl.substring("http://".length)
    else -> "https://$rawUrl"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleReaderScreen(
    navController: NavController,
    url: String,
    articleTitle: String = "",
    feedTitle: String = "",
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    articleReaderViewModel: ArticleReaderViewModel = hiltViewModel()
) {
    val decodedUrl = remember(url) { URLDecoder.decode(url, StandardCharsets.UTF_8.toString()) }
    val decodedTitle = remember(articleTitle) { URLDecoder.decode(articleTitle, StandardCharsets.UTF_8.toString()) }
    val decodedFeedTitle = remember(feedTitle) { URLDecoder.decode(feedTitle, StandardCharsets.UTF_8.toString()) }
    val context = LocalContext.current
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    val userAgentKey by settingsViewModel.userAgent.collectAsState()
    val userAgentString = remember(userAgentKey) { UserAgentTemplate.fromKey(userAgentKey).uaString }
    val isSaved by remember(decodedUrl) { articleReaderViewModel.isSaved(decodedUrl) }.collectAsState(initial = false)

    // WebView privacy settings. Read here so the AndroidView factory/clients capture the
    // current State; toggles that are read inside WebViewClient callbacks (trackers, HTTPS-only)
    // pick up live changes, while the rest apply the next time an article is opened.
    val cookiePolicy by settingsViewModel.cookiePolicy.collectAsState()
    val blockTrackers by settingsViewModel.blockTrackers.collectAsState()
    val jsEnabled by settingsViewModel.webViewJavaScript.collectAsState()
    val domStorageEnabledSetting by settingsViewModel.webViewDomStorage.collectAsState()
    val clearBrowsingOnClose by settingsViewModel.clearBrowsingOnClose.collectAsState()
    val doNotTrack by settingsViewModel.doNotTrack.collectAsState()
    val safeBrowsingEnabled by settingsViewModel.safeBrowsing.collectAsState()
    val httpsOnly by settingsViewModel.httpsOnly.collectAsState()

    // Optionally wipe cookies and web storage when the reader is closed.
    DisposableEffect(Unit) {
        onDispose {
            if (clearBrowsingOnClose) {
                CookieManager.getInstance().apply { removeAllCookies(null); flush() }
                WebStorage.getInstance().deleteAllData()
            }
        }
    }

    BackHandler {
        val wv = webViewRef.value
        if (wv != null && wv.canGoBack()) wv.goBack() else navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article") },
                navigationIcon = {
                    IconButton(onClick = {
                        val wv = webViewRef.value
                        if (wv != null && wv.canGoBack()) wv.goBack() else navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        articleReaderViewModel.toggleSaved(isSaved, decodedUrl, decodedTitle, decodedFeedTitle)
                    }) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = if (isSaved) "Remove from saved" else "Save for later"
                        )
                    }
                    IconButton(onClick = { BrowserUtils.openSanitizedUrl(context, decodedUrl) }) {
                        Icon(Icons.Filled.OpenInBrowser, contentDescription = "Open in browser")
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            factory = { ctx ->
                @SuppressLint("SetJavaScriptEnabled")
                WebViewPool.acquire(ctx).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String) {
                            // React controlled inputs call setSelectionRange() during re-renders,
                            // which resets the Android IME cursor to position 0 (text appears reversed).
                            // Guard: only allow it when the textarea is actually the focused element.
                            view.evaluateJavascript("""
                                (function() {
                                    var orig = HTMLTextAreaElement.prototype.setSelectionRange;
                                    HTMLTextAreaElement.prototype.setSelectionRange = function(s, e, d) {
                                        if (document.activeElement === this) orig.call(this, s, e, d);
                                    };
                                })();
                            """.trimIndent(), null)
                        }

                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            val url = request.url.toString()
                            return when (request.url.scheme?.lowercase()) {
                                // HTTPS always loads in-WebView.
                                "https" -> false
                                // Cleartext can't load in-app (OS network-security-config blocks it),
                                // so upgrade to HTTPS and load in-WebView — mirrors the OkHttp feed
                                // interceptor. The new https URL re-enters here and loads normally.
                                "http" -> {
                                    view.loadUrl(upgradeToHttps(url))
                                    true
                                }
                                // In-page content the WebView renders itself — must NOT be sent to
                                // an external VIEW intent (no Activity handles data:, and it crashed).
                                "data", "blob", "about", "javascript", "file" -> false
                                // mailto:/tel:/etc. — best-effort hand-off; unknown/unsafe schemes
                                // are ignored. Either way it can't crash the reader.
                                else -> { BrowserUtils.openExternal(view.context, url); true }
                            }
                        }

                        // Never proceed on certificate errors.
                        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                            handler.cancel()
                        }

                        // Never respond to HTTP auth challenges (prevents phishing dialogs).
                        override fun onReceivedHttpAuthRequest(
                            view: WebView,
                            handler: HttpAuthHandler,
                            host: String,
                            realm: String
                        ) {
                            handler.cancel()
                        }

                        // Block known tracker and ad domains at the network level.
                        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                            return if (blockTrackers && isBlockedTracker(request.url.toString())) {
                                WebResourceResponse("text/plain", "utf-8", null)
                            } else null
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        // Block all popup windows.
                        override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean = false

                        // Deny camera / microphone / location permission requests.
                        override fun onPermissionRequest(request: PermissionRequest) {
                            request.deny()
                        }
                    }

                    with(settings) {
                        javaScriptEnabled = jsEnabled
                        allowFileAccess = false
                        allowContentAccess = false
                        // file-URL access, WebSQL database and form-data saving are already off by
                        // default on modern Android (their setters are deprecated no-ops).
                        mixedContentMode = if (httpsOnly) WebSettings.MIXED_CONTENT_NEVER_ALLOW
                            else WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                        domStorageEnabled = domStorageEnabledSetting   // Sandboxed per-app.
                        setGeolocationEnabled(false)
                        setSafeBrowsingEnabled(safeBrowsingEnabled)
                        mediaPlaybackRequiresUserGesture = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                    }

                    if (userAgentString.isNotEmpty()) settings.userAgentString = userAgentString

                    // Cookie policy. "First-party only" (default) lets a site's own consent
                    // banner / login persist while blocking cross-site tracking cookies — this is
                    // what makes consent platforms like The Guardian's Sourcepoint CMP usable
                    // (tapping "Accept" writes a first-party cookie) without re-enabling tracking.
                    val cookieManager = CookieManager.getInstance()
                    when (cookiePolicy) {
                        "block" -> {
                            cookieManager.setAcceptCookie(false)
                            cookieManager.setAcceptThirdPartyCookies(this, false)
                        }
                        "all" -> {
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setAcceptThirdPartyCookies(this, true)
                        }
                        else -> { // "first_party"
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setAcceptThirdPartyCookies(this, false)
                        }
                    }

                    isLongClickable = false
                    setOnLongClickListener { true }
                    webViewRef.value = this
                    // DNT / Global Privacy Control are request headers; WebView only lets us attach
                    // them to the main-frame navigation, so this is a best-effort signal.
                    val initialUrl = upgradeToHttps(decodedUrl)
                    if (doNotTrack) {
                        loadUrl(initialUrl, mapOf("DNT" to "1", "Sec-GPC" to "1"))
                    } else {
                        loadUrl(initialUrl)
                    }
                }
            },
            update = { webView ->
                // Reapply UA when the setting changes while the screen is open.
                if (userAgentString.isNotEmpty() && webView.settings.userAgentString != userAgentString) {
                    webView.settings.userAgentString = userAgentString
                }
            }
        )
    }
}
