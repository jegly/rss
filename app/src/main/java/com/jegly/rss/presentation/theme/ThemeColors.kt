package com.jegly.rss.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

// ---------------------------------------------------------------------------
// Catppuccin — 4 flavors x 14 accents
// ---------------------------------------------------------------------------

enum class CatppuccinFlavor(val key: String, val displayName: String) {
    LATTE("latte", "Latte"),
    FRAPPE("frappe", "Frappé"),
    MACCHIATO("macchiato", "Macchiato"),
    MOCHA("mocha", "Mocha")
}

fun catppuccinFlavorFromKey(key: String): CatppuccinFlavor =
    CatppuccinFlavor.entries.firstOrNull { it.key == key } ?: CatppuccinFlavor.MOCHA

private data class CatppuccinBase(
    val text: Color,
    val subtext1: Color,
    val overlay1: Color,
    val surface2: Color,
    val surface1: Color,
    val surface0: Color,
    val base: Color,
    val mantle: Color,
    val crust: Color
)

private val CATPPUCCIN_BASE: Map<CatppuccinFlavor, CatppuccinBase> = mapOf(
    CatppuccinFlavor.LATTE to CatppuccinBase(
        text = Color(0xFF4c4f69), subtext1 = Color(0xFF5c5f77), overlay1 = Color(0xFF8c8fa1),
        surface2 = Color(0xFFacb0be), surface1 = Color(0xFFbcc0cc), surface0 = Color(0xFFccd0da),
        base = Color(0xFFeff1f5), mantle = Color(0xFFe6e9ef), crust = Color(0xFFdce0e8)
    ),
    CatppuccinFlavor.FRAPPE to CatppuccinBase(
        text = Color(0xFFc6d0f5), subtext1 = Color(0xFFb5bfe2), overlay1 = Color(0xFF838ba7),
        surface2 = Color(0xFF626880), surface1 = Color(0xFF51576d), surface0 = Color(0xFF414559),
        base = Color(0xFF303446), mantle = Color(0xFF292c3c), crust = Color(0xFF232634)
    ),
    CatppuccinFlavor.MACCHIATO to CatppuccinBase(
        text = Color(0xFFcad3f5), subtext1 = Color(0xFFb8c0e0), overlay1 = Color(0xFF8087a2),
        surface2 = Color(0xFF5b6078), surface1 = Color(0xFF494d64), surface0 = Color(0xFF363a4f),
        base = Color(0xFF24273a), mantle = Color(0xFF1e2030), crust = Color(0xFF181926)
    ),
    CatppuccinFlavor.MOCHA to CatppuccinBase(
        text = Color(0xFFcdd6f4), subtext1 = Color(0xFFbac2de), overlay1 = Color(0xFF7f849c),
        surface2 = Color(0xFF585b70), surface1 = Color(0xFF45475a), surface0 = Color(0xFF313244),
        base = Color(0xFF1e1e2e), mantle = Color(0xFF181825), crust = Color(0xFF11111b)
    )
)

// key -> (displayName, hex) per flavor. Order matches the official Catppuccin accent wheel.
private val CATPPUCCIN_ACCENTS: Map<CatppuccinFlavor, Map<String, Pair<String, Color>>> = mapOf(
    CatppuccinFlavor.LATTE to linkedMapOf(
        "mauve" to ("Mauve" to Color(0xFF8839ef)),
        "blue" to ("Blue" to Color(0xFF1e66f5)),
        "lavender" to ("Lavender" to Color(0xFF7287fd)),
        "sapphire" to ("Sapphire" to Color(0xFF209fb5)),
        "sky" to ("Sky" to Color(0xFF04a5e5)),
        "teal" to ("Teal" to Color(0xFF179299)),
        "green" to ("Green" to Color(0xFF40a02b)),
        "yellow" to ("Yellow" to Color(0xFFdf8e1d)),
        "peach" to ("Peach" to Color(0xFFfe640b)),
        "maroon" to ("Maroon" to Color(0xFFe64553)),
        "red" to ("Red" to Color(0xFFd20f39)),
        "pink" to ("Pink" to Color(0xFFea76cb)),
        "flamingo" to ("Flamingo" to Color(0xFFdd7878)),
        "rosewater" to ("Rosewater" to Color(0xFFdc8a78))
    ),
    CatppuccinFlavor.FRAPPE to linkedMapOf(
        "mauve" to ("Mauve" to Color(0xFFca9ee6)),
        "blue" to ("Blue" to Color(0xFF8caaee)),
        "lavender" to ("Lavender" to Color(0xFFbabbf1)),
        "sapphire" to ("Sapphire" to Color(0xFF85c1dc)),
        "sky" to ("Sky" to Color(0xFF99d1db)),
        "teal" to ("Teal" to Color(0xFF81c8be)),
        "green" to ("Green" to Color(0xFFa6d189)),
        "yellow" to ("Yellow" to Color(0xFFe5c890)),
        "peach" to ("Peach" to Color(0xFFef9f76)),
        "maroon" to ("Maroon" to Color(0xFFea999c)),
        "red" to ("Red" to Color(0xFFe78284)),
        "pink" to ("Pink" to Color(0xFFf4b8e4)),
        "flamingo" to ("Flamingo" to Color(0xFFeebebe)),
        "rosewater" to ("Rosewater" to Color(0xFFf2d5cf))
    ),
    CatppuccinFlavor.MACCHIATO to linkedMapOf(
        "mauve" to ("Mauve" to Color(0xFFc6a0f6)),
        "blue" to ("Blue" to Color(0xFF8aadf4)),
        "lavender" to ("Lavender" to Color(0xFFb7bdf8)),
        "sapphire" to ("Sapphire" to Color(0xFF7dc4e4)),
        "sky" to ("Sky" to Color(0xFF91d7e3)),
        "teal" to ("Teal" to Color(0xFF8bd5ca)),
        "green" to ("Green" to Color(0xFFa6da95)),
        "yellow" to ("Yellow" to Color(0xFFeed49f)),
        "peach" to ("Peach" to Color(0xFFf5a97f)),
        "maroon" to ("Maroon" to Color(0xFFee99a0)),
        "red" to ("Red" to Color(0xFFed8796)),
        "pink" to ("Pink" to Color(0xFFf5bde6)),
        "flamingo" to ("Flamingo" to Color(0xFFf0c6c6)),
        "rosewater" to ("Rosewater" to Color(0xFFf4dbd6))
    ),
    CatppuccinFlavor.MOCHA to linkedMapOf(
        "mauve" to ("Mauve" to Color(0xFFcba6f7)),
        "blue" to ("Blue" to Color(0xFF89b4fa)),
        "lavender" to ("Lavender" to Color(0xFFb4befe)),
        "sapphire" to ("Sapphire" to Color(0xFF74c7ec)),
        "sky" to ("Sky" to Color(0xFF89dceb)),
        "teal" to ("Teal" to Color(0xFF94e2d5)),
        "green" to ("Green" to Color(0xFFa6e3a1)),
        "yellow" to ("Yellow" to Color(0xFFf9e2af)),
        "peach" to ("Peach" to Color(0xFFfab387)),
        "maroon" to ("Maroon" to Color(0xFFeba0ac)),
        "red" to ("Red" to Color(0xFFf38ba8)),
        "pink" to ("Pink" to Color(0xFFf5c2e7)),
        "flamingo" to ("Flamingo" to Color(0xFFf2cdcd)),
        "rosewater" to ("Rosewater" to Color(0xFFf5e0dc))
    )
)

// Hue-adjacent secondary/tertiary pairings on the Catppuccin accent wheel — same for every flavor.
private val CATPPUCCIN_SECONDARY: Map<String, String> = mapOf(
    "rosewater" to "flamingo", "flamingo" to "pink", "pink" to "mauve", "mauve" to "lavender",
    "red" to "maroon", "maroon" to "red", "peach" to "yellow", "yellow" to "peach",
    "green" to "teal", "teal" to "sky", "sky" to "sapphire", "sapphire" to "blue",
    "blue" to "lavender", "lavender" to "blue"
)

private val CATPPUCCIN_TERTIARY: Map<String, String> = mapOf(
    "rosewater" to "pink", "flamingo" to "mauve", "pink" to "lavender", "mauve" to "pink",
    "red" to "peach", "maroon" to "peach", "peach" to "green", "yellow" to "green",
    "green" to "sky", "teal" to "green", "sky" to "teal", "sapphire" to "sky",
    "blue" to "sapphire", "lavender" to "mauve"
)

/** Accent swatches for the given flavor, keyed the same way across all four flavors. */
fun catppuccinAccentsFor(flavorKey: String): Map<String, Pair<String, Color>> =
    CATPPUCCIN_ACCENTS[catppuccinFlavorFromKey(flavorKey)] ?: CATPPUCCIN_ACCENTS.getValue(CatppuccinFlavor.MOCHA)

fun catppuccinColorScheme(flavorKey: String, accentKey: String): ColorScheme {
    val flavor = catppuccinFlavorFromKey(flavorKey)
    val accents = CATPPUCCIN_ACCENTS.getValue(flavor)
    val b = CATPPUCCIN_BASE.getValue(flavor)

    val primary = accents[accentKey]?.second ?: accents.getValue("mauve").second
    val secondaryKey = CATPPUCCIN_SECONDARY[accentKey] ?: "lavender"
    val tertiaryKey = CATPPUCCIN_TERTIARY[accentKey] ?: "pink"
    val secondary = accents[secondaryKey]?.second ?: primary
    val tertiary = accents[tertiaryKey]?.second ?: primary
    val error = accents.getValue("red").second

    fun onColor(c: Color) = if (c.luminance() < 0.35f) Color.White else b.base
    fun container(c: Color) = lerp(b.base, c, 0.22f)
    fun onContainer(c: Color) = lerp(b.text, c, 0.20f)

    val dark = flavor != CatppuccinFlavor.LATTE
    return if (dark) darkColorScheme(
        primary               = primary,
        onPrimary             = onColor(primary),
        primaryContainer      = container(primary),
        onPrimaryContainer    = onContainer(primary),
        secondary             = secondary,
        onSecondary           = onColor(secondary),
        secondaryContainer    = container(secondary),
        onSecondaryContainer  = onContainer(secondary),
        tertiary              = tertiary,
        onTertiary            = onColor(tertiary),
        tertiaryContainer     = container(tertiary),
        onTertiaryContainer   = onContainer(tertiary),
        error                 = error,
        onError               = onColor(error),
        errorContainer        = container(error),
        onErrorContainer      = onContainer(error),
        background            = b.base,
        onBackground          = b.text,
        surface               = b.base,
        onSurface             = b.text,
        surfaceVariant        = b.surface0,
        onSurfaceVariant      = b.subtext1,
        outline               = b.overlay1,
        outlineVariant        = b.surface2,
        scrim                 = b.crust,
        inverseSurface        = b.text,
        inverseOnSurface      = b.base,
        inversePrimary        = lerp(b.text, primary, 0.5f)
    ) else lightColorScheme(
        primary               = primary,
        onPrimary             = onColor(primary),
        primaryContainer      = container(primary),
        onPrimaryContainer    = onContainer(primary),
        secondary             = secondary,
        onSecondary           = onColor(secondary),
        secondaryContainer    = container(secondary),
        onSecondaryContainer  = onContainer(secondary),
        tertiary              = tertiary,
        onTertiary            = onColor(tertiary),
        tertiaryContainer     = container(tertiary),
        onTertiaryContainer   = onContainer(tertiary),
        error                 = error,
        onError               = onColor(error),
        errorContainer        = container(error),
        onErrorContainer      = onContainer(error),
        background            = b.base,
        onBackground          = b.text,
        surface               = b.base,
        onSurface             = b.text,
        surfaceVariant        = b.surface0,
        onSurfaceVariant      = b.subtext1,
        outline               = b.overlay1,
        outlineVariant        = b.surface2,
        scrim                 = b.crust,
        inverseSurface        = b.text,
        inverseOnSurface      = b.base,
        inversePrimary        = lerp(b.text, primary, 0.5f)
    )
}

// ---------------------------------------------------------------------------
// Dracula palette
// ---------------------------------------------------------------------------
object DraculaColors {
    val Background  = Color(0xFF282A36)
    val DarkerBg    = Color(0xFF21222C)
    val CurrentLine = Color(0xFF44475A)
    val Foreground  = Color(0xFFF8F8F2)
    val Comment     = Color(0xFF6272A4)

    val Purple  = Color(0xFFBD93F9)   // default
    val Pink    = Color(0xFFFF79C6)
    val Cyan    = Color(0xFF8BE9FD)
    val Green   = Color(0xFF50FA7B)
    val Orange  = Color(0xFFFFB86C)
    val Red     = Color(0xFFFF5555)
    val Yellow  = Color(0xFFF1FA8C)

    // Fixed secondary / tertiary.
    val Secondary = Cyan
    val Tertiary  = Pink

    val accents: Map<String, Pair<String, Color>> = linkedMapOf(
        "purple" to ("Purple" to Purple),
        "pink"   to ("Pink"   to Pink),
        "cyan"   to ("Cyan"   to Cyan),
        "green"  to ("Green"  to Green),
        "orange" to ("Orange" to Orange),
        "red"    to ("Red"    to Red),
        "yellow" to ("Yellow" to Yellow)
    )
}

fun draculaColorScheme(accentKey: String): ColorScheme {
    val accent    = DraculaColors.accents[accentKey]?.second ?: DraculaColors.Purple
    val bg        = DraculaColors.Background
    val secondary = DraculaColors.Secondary
    val tertiary  = DraculaColors.Tertiary
    fun tint(c: Color) = lerp(bg, c, 0.22f)

    return darkColorScheme(
        primary               = accent,
        onPrimary             = bg,
        primaryContainer      = tint(accent),
        onPrimaryContainer    = accent,
        secondary             = secondary,
        onSecondary           = bg,
        secondaryContainer    = tint(secondary),
        onSecondaryContainer  = secondary,
        tertiary              = tertiary,
        onTertiary            = bg,
        tertiaryContainer     = tint(tertiary),
        onTertiaryContainer   = tertiary,
        background            = bg,
        onBackground          = DraculaColors.Foreground,
        surface               = DraculaColors.DarkerBg,
        onSurface             = DraculaColors.Foreground,
        surfaceVariant        = DraculaColors.CurrentLine,
        onSurfaceVariant      = DraculaColors.Comment,
        outline               = DraculaColors.Comment,
        outlineVariant        = DraculaColors.CurrentLine,
        error                 = DraculaColors.Red,
        onError               = bg,
        errorContainer        = tint(DraculaColors.Red),
        onErrorContainer      = DraculaColors.Red,
        scrim                 = Color(0xFF191A21),
        inverseSurface        = DraculaColors.Foreground,
        inverseOnSurface      = bg,
        inversePrimary        = lerp(DraculaColors.Foreground, accent, 0.5f)
    )
}
