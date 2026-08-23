package com.jegly.rss.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jegly.rss.presentation.settings.SettingsViewModel

// Material You fallback colours (used only when dynamic colour is unavailable and theme = system).
private val FallbackDark = darkColorScheme(
    primary   = Color(0xFFD0BCFF),
    secondary = Color(0xFFCCC2DC),
    tertiary  = Color(0xFFEFB8C8)
)
private val FallbackLight = lightColorScheme(
    primary   = Color(0xFF6650A4),
    secondary = Color(0xFF625B71),
    tertiary  = Color(0xFF7D5260)
)

@Composable
fun SecureRSSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val fontSize        by settingsViewModel.fontSize.collectAsState()
    val fontFamily      by settingsViewModel.fontFamily.collectAsState()
    val themeMode       by settingsViewModel.themeMode.collectAsState()
    val catppuccinAccent by settingsViewModel.catppuccinAccent.collectAsState()
    val catppuccinFlavor by settingsViewModel.catppuccinFlavor.collectAsState()
    val draculaAccent   by settingsViewModel.draculaAccent.collectAsState()
    val ptyxisPalette   by settingsViewModel.ptyxisPalette.collectAsState()

    val colorScheme = when (themeMode) {
        "catppuccin" -> catppuccinColorScheme(catppuccinFlavor, catppuccinAccent)
        "dracula"    -> draculaColorScheme(draculaAccent)
        "ptyxis"     -> ptyxisColorScheme(ptyxisPalette)
        else -> {
            // System / Material You
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) FallbackDark else FallbackLight
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = getTypography(fontSize, fontFamily),
        content     = content
    )
}
