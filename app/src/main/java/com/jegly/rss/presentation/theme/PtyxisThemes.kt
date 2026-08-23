package com.jegly.rss.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

/**
 * Terminal palettes ported from GNOME Ptyxis (11 curated + 33 extended Gogh/Ptyxis palettes),
 * merged into a single selectable list. Every entry carries its exact Background/Foreground
 * plus a signature accent set drawn from that palette's own ANSI/cursor colors — nothing
 * invented. All are dark schemes.
 *
 * The 33 "extended" entries only ship a Background/Foreground/full-16-ANSI table in the
 * source they were ported from; this app only needs the 4 semantic roles every other theme
 * here uses, so they're flattened at declaration time using the standard terminal-palette
 * convention: ansi[4] blue -> primary, ansi[5] magenta -> secondary, ansi[6] cyan -> tertiary,
 * ansi[1] red -> error.
 */
data class PtyxisTheme(
    val key: String,
    val displayName: String,
    val background: Long,
    val foreground: Long,
    val primary: Long,
    val secondary: Long,
    val tertiary: Long,
    val error: Long
)

val PTYXIS_THEMES: List<PtyxisTheme> = listOf(
    // --- Curated 11 (Ptyxis.kt) ---
    PtyxisTheme("fairy_floss", "Fairy Floss", 0xFF5A5475, 0xFFC2FFDF, 0xFFFFB8D1, 0xFFAE81FF, 0xFFC2FFDF, 0xFFFF857F),
    PtyxisTheme("nord", "Nord", 0xFF2E3440, 0xFFD8DEE9, 0xFF88C0D0, 0xFF81A1C1, 0xFF8FBCBB, 0xFFBF616A),
    PtyxisTheme("bim", "Bim", 0xFF012849, 0xFFA9BED8, 0xFF5EA2EC, 0xFFF557A0, 0xFFA9EE55, 0xFFF557A0),
    PtyxisTheme("borland", "Borland", 0xFF0000A4, 0xFFFFFF4E, 0xFFFFFF4E, 0xFFFF73FD, 0xFF96CBFE, 0xFFFF6C60),
    PtyxisTheme("c64", "C64", 0xFF40318D, 0xFF7869C4, 0xFF67B6BD, 0xFFBFCE72, 0xFF8B3F96, 0xFF883932),
    PtyxisTheme("cobalt_neon", "Cobalt Neon", 0xFF142838, 0xFF8FF586, 0xFF8FF586, 0xFF3BA5FF, 0xFFE9E75C, 0xFFFF2320),
    PtyxisTheme("grass", "Grass", 0xFF13773D, 0xFFFFF0A5, 0xFFE7B000, 0xFF00BBBB, 0xFFFFF0A5, 0xFFBB0000),
    PtyxisTheme("homebrew_ocean", "Homebrew Ocean", 0xFF224FBC, 0xFFFFFFFF, 0xFF00A6B2, 0xFF00A600, 0xFF999900, 0xFF990000),
    PtyxisTheme("mono_amber", "Mono Amber", 0xFF2B1900, 0xFFFF9400, 0xFFFF9400, 0xFFFF9400, 0xFFFF9400, 0xFFFF9400),
    PtyxisTheme("mono_red", "Mono Red", 0xFF2B0C00, 0xFFFF3600, 0xFFFF3600, 0xFFFF3600, 0xFFFF3600, 0xFFFF3600),
    PtyxisTheme("synthwave", "Synthwave", 0xFF262335, 0xFFFFFFFF, 0xFFFF7EDB, 0xFF03EDF9, 0xFFFEDE5D, 0xFFFE4450),

    // --- Extended 33 (PtyxisPalettesExtended.kt), flattened from ansi[4]/[5]/[6]/[1] ---
    PtyxisTheme("aci", "Aci", 0xFF0D1926, 0xFFB4E1FD, 0xFF0883FF, 0xFF8308FF, 0xFF08FF83, 0xFFFF0883),
    PtyxisTheme("afterglow", "Afterglow", 0xFF222222, 0xFFD0D0D0, 0xFF6C99BB, 0xFF9F4E85, 0xFF7DD6CF, 0xFFA53C23),
    PtyxisTheme("argonaut", "Argonaut", 0xFF0E1019, 0xFFFFFAF4, 0xFF008DF8, 0xFF6D43A6, 0xFF00D8EB, 0xFFFF000F),
    PtyxisTheme("aura", "Aura", 0xFF15141B, 0xFFEDECEE, 0xFFA277FF, 0xFFA277FF, 0xFF61FFCA, 0xFFFF6767),
    PtyxisTheme("ayu_mirage", "Ayu Mirage", 0xFF1F2430, 0xFFCBCCC6, 0xFF73D0FF, 0xFFD4BFFF, 0xFF95E6CB, 0xFFFF3333),
    PtyxisTheme("belafonte", "Belafonte", 0xFF20111B, 0xFF968C83, 0xFF426A79, 0xFF97522C, 0xFF989A9C, 0xFFBE100E),
    PtyxisTheme("birds_of_paradise", "Birds Of Paradise", 0xFF2A1F1D, 0xFFE0DBB7, 0xFF5A86AD, 0xFFAC80A6, 0xFF74A6AD, 0xFFBE2D26),
    PtyxisTheme("blazer", "Blazer", 0xFF0D1926, 0xFFD9E6F2, 0xFF7A7AB8, 0xFFB87AB8, 0xFF7AB8B8, 0xFFB87A7A),
    PtyxisTheme("brogrammer", "Brogrammer", 0xFF131313, 0xFFD6DBE5, 0xFF2A84D2, 0xFF4E5AB7, 0xFF1081D6, 0xFFF81118),
    PtyxisTheme("chalkboard", "Chalkboard", 0xFF29262F, 0xFFD9E6F2, 0xFF7372C3, 0xFFC372C2, 0xFF72C2C3, 0xFFC37372),
    PtyxisTheme("espresso_libre", "Espresso Libre", 0xFF2A211C, 0xFFB8A898, 0xFF0066FF, 0xFFC5656B, 0xFF06989A, 0xFFCC0000),
    PtyxisTheme("everforest", "Everforest", 0xFF2D353B, 0xFFD3C6AA, 0xFF7FBBB3, 0xFFD699B6, 0xFF83C092, 0xFFE67E80),
    PtyxisTheme("flatland", "Flatland", 0xFF1D1F21, 0xFFB8DBEF, 0xFF5096BE, 0xFF695ABC, 0xFFD63865, 0xFFF18339),
    PtyxisTheme("github", "GitHub", 0xFF101216, 0xFF8B949E, 0xFF6CA4F8, 0xFFDB61A2, 0xFF2B7489, 0xFFF78166),
    PtyxisTheme("ibm3270", "IBM3270", 0xFF000000, 0xFFFDFDFD, 0xFF7890F0, 0xFFF078D8, 0xFF54E4E4, 0xFFF01818),
    PtyxisTheme("ic_green_ppl", "IC Green PPL", 0xFF3A3D3F, 0xFFD9EFD3, 0xFF149B45, 0xFF53B82C, 0xFF2CB868, 0xFFFB002A),
    PtyxisTheme("kanagawa", "Kanagawa", 0xFF1F1F28, 0xFFDCD7BA, 0xFF7E9CD8, 0xFF957FB8, 0xFF6A9589, 0xFFC34043),
    PtyxisTheme("material", "Material", 0xFF1E282C, 0xFFC3C7D1, 0xFF80CBC3, 0xFFFF2490, 0xFFAEDDFF, 0xFFEB606B),
    PtyxisTheme("mona_lisa", "Mona Lisa", 0xFF120B0D, 0xFFF7D66A, 0xFF515C5D, 0xFF9B1D29, 0xFF588056, 0xFF9B291C),
    PtyxisTheme("mono_cyan", "Mono Cyan", 0xFF00222B, 0xFF00CCFF, 0xFF00CCFF, 0xFF00CCFF, 0xFF00CCFF, 0xFF00CCFF),
    PtyxisTheme("monokai_pro", "Monokai Pro", 0xFF363537, 0xFFFDF9F3, 0xFFFC9867, 0xFFAB9DF2, 0xFF78DCE8, 0xFFFF6188),
    PtyxisTheme("omni", "Omni", 0xFF191622, 0xFFABB2BF, 0xFF78D1E1, 0xFF988BC7, 0xFFFF79C6, 0xFFE96379),
    PtyxisTheme("paraiso_dark", "Paraiso Dark", 0xFF2F1E2E, 0xFFA39E9B, 0xFF06B6EF, 0xFF815BA4, 0xFF5BC4BF, 0xFFEF6155),
    PtyxisTheme("pixiefloss", "Pixiefloss", 0xFF241F33, 0xFFD1CAE8, 0xFFAE81FF, 0xFFEF6155, 0xFFC2FFFF, 0xFFFF857F),
    PtyxisTheme("powershell", "Powershell", 0xFF052454, 0xFFF6F6F7, 0xFF010083, 0xFFD33682, 0xFF0E807F, 0xFF7E0008),
    PtyxisTheme("relaxed", "Relaxed", 0xFF353A44, 0xFFD9D9D9, 0xFF6A8799, 0xFFB06698, 0xFFC9DFFF, 0xFFBC5653),
    PtyxisTheme("sea_shells", "Sea Shells", 0xFF09141B, 0xFFDEB88D, 0xFF1E4950, 0xFF68D4F1, 0xFF50A3B5, 0xFFD15123),
    PtyxisTheme("solarized", "Solarized", 0xFF002B36, 0xFF839496, 0xFF268BD2, 0xFFD33682, 0xFF2AA198, 0xFFDC322F),
    PtyxisTheme("spacedust", "Spacedust", 0xFF0A1E24, 0xFFECF0C1, 0xFF0F548B, 0xFFE35B00, 0xFF06AFC7, 0xFFE35B00),
    PtyxisTheme("spring", "Spring", 0xFF0A1E24, 0xFFECF0C1, 0xFF1DD3EE, 0xFF8959A8, 0xFF3E999F, 0xFFFF4D83),
    PtyxisTheme("twilight", "Twilight", 0xFF141414, 0xFFFFFFD4, 0xFF44474A, 0xFFB4BE7C, 0xFF778385, 0xFFC06D44),
    PtyxisTheme("urple", "Urple", 0xFF1B1B23, 0xFF877A9B, 0xFF564D9B, 0xFF6C3CA1, 0xFF808080, 0xFFB0425B),
    PtyxisTheme("xterm", "Xterm", 0xFF000000, 0xFFFFFFFF, 0xFF0000EE, 0xFFCD00CD, 0xFF00CDCD, 0xFFCD0000)
)

private val PTYXIS_BY_KEY: Map<String, PtyxisTheme> = PTYXIS_THEMES.associateBy { it.key }

fun ptyxisThemeFromKey(key: String): PtyxisTheme = PTYXIS_BY_KEY[key] ?: PTYXIS_BY_KEY.getValue("nord")

/** Builds a Material 3 dark scheme from a Ptyxis palette's background/foreground/accents. */
fun ptyxisColorScheme(key: String): ColorScheme {
    val palette = ptyxisThemeFromKey(key)
    val bg = Color(palette.background)
    val fg = Color(palette.foreground)
    val primary = Color(palette.primary)
    val secondary = Color(palette.secondary)
    val tertiary = Color(palette.tertiary)
    val error = Color(palette.error)

    fun onColor(c: Color) = if (c.luminance() < 0.5f) Color.White else Color(0xFF0A0A0A)
    fun container(c: Color) = lerp(bg, c, 0.22f)
    fun onContainer(c: Color) = lerp(fg, c, 0.20f)

    return darkColorScheme(
        primary               = primary,
        onPrimary             = onColor(primary),
        primaryContainer      = container(primary),
        onPrimaryContainer    = onContainer(primary),
        secondary             = secondary,
        onSecondary           = onColor(secondary),
        secondaryContainer    = container(secondary),
        onSecondaryContainer  = onContainer(secondary),
        tertiary               = tertiary,
        onTertiary             = onColor(tertiary),
        tertiaryContainer      = container(tertiary),
        onTertiaryContainer    = onContainer(tertiary),
        error                  = error,
        onError                = onColor(error),
        errorContainer         = container(error),
        onErrorContainer       = onContainer(error),
        background             = bg,
        onBackground           = fg,
        surface                = bg,
        onSurface              = fg,
        surfaceVariant         = lerp(bg, fg, 0.12f),
        onSurfaceVariant       = lerp(fg, bg, 0.25f),
        outline                = lerp(bg, fg, 0.40f),
        outlineVariant         = lerp(bg, fg, 0.20f),
        scrim                  = Color.Black,
        inverseSurface         = fg,
        inverseOnSurface       = bg,
        inversePrimary         = primary
    )
}
