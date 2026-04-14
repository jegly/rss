package com.jegly.rss.presentation.article
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun ArticleReaderScreen(navController: NavController, url: String) {
    val decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.toString())
    Scaffold { padding ->
        AndroidView(
            modifier = Modifier.padding(padding),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = WebViewClient()
                    loadUrl(decodedUrl)
                }
            }
        )
    }
}
