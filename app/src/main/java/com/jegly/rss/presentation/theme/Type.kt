package com.jegly.rss.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jegly.rss.R

// Font definitions
val CourierPrime = FontFamily(Font(R.font.courier_prime_regular, FontWeight.Normal))

// --- Bundled selectable font families (ported from the theme port kit) ---
val NunitoFamily = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_extralight, FontWeight.ExtraLight),
    Font(R.font.nunito_light, FontWeight.Light),
    Font(R.font.nunito_medium, FontWeight.Medium),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
    Font(R.font.nunito_black, FontWeight.Black)
)

val CormorantGaramondFamily = FontFamily(
    Font(R.font.cormorant_garamond_bold, FontWeight.Bold),
    Font(R.font.cormorant_garamond_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.cormorant_garamond_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.cormorant_garamond_light, FontWeight.Light),
    Font(R.font.cormorant_garamond_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.cormorant_garamond_medium, FontWeight.Medium),
    Font(R.font.cormorant_garamond_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.cormorant_garamond_regular, FontWeight.Normal),
    Font(R.font.cormorant_garamond_semibold, FontWeight.SemiBold),
    Font(R.font.cormorant_garamond_semibolditalic, FontWeight.SemiBold, FontStyle.Italic)
)

val DotGothic16Family = FontFamily(
    Font(R.font.dotgothic16_regular, FontWeight.Normal)
)

val IbmPlexMonoFamily = FontFamily(
    Font(R.font.ibm_plex_mono_bold, FontWeight.Bold),
    Font(R.font.ibm_plex_mono_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.ibm_plex_mono_extralight, FontWeight.ExtraLight),
    Font(R.font.ibm_plex_mono_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.ibm_plex_mono_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.ibm_plex_mono_light, FontWeight.Light),
    Font(R.font.ibm_plex_mono_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.ibm_plex_mono_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_mono_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_mono_semibold, FontWeight.SemiBold),
    Font(R.font.ibm_plex_mono_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.ibm_plex_mono_thin, FontWeight.Thin),
    Font(R.font.ibm_plex_mono_thinitalic, FontWeight.Thin, FontStyle.Italic)
)

val IbmPlexSerifFamily = FontFamily(
    Font(R.font.ibm_plex_serif_bold, FontWeight.Bold),
    Font(R.font.ibm_plex_serif_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.ibm_plex_serif_extralight, FontWeight.ExtraLight),
    Font(R.font.ibm_plex_serif_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.ibm_plex_serif_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.ibm_plex_serif_light, FontWeight.Light),
    Font(R.font.ibm_plex_serif_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.ibm_plex_serif_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_serif_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.ibm_plex_serif_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_serif_semibold, FontWeight.SemiBold),
    Font(R.font.ibm_plex_serif_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.ibm_plex_serif_thin, FontWeight.Thin),
    Font(R.font.ibm_plex_serif_thinitalic, FontWeight.Thin, FontStyle.Italic)
)

val InstrumentSerifFamily = FontFamily(
    Font(R.font.instrument_serif_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.instrument_serif_regular, FontWeight.Normal)
)

val NotoSansEgyptianHieroglyphsFamily = FontFamily(
    Font(R.font.noto_sans_egyptian_hieroglyphs_regular, FontWeight.Normal)
)

val PlayfairDisplayFamily = FontFamily(
    Font(R.font.playfair_display_black, FontWeight.Black),
    Font(R.font.playfair_display_blackitalic, FontWeight.Black, FontStyle.Italic),
    Font(R.font.playfair_display_bold, FontWeight.Bold),
    Font(R.font.playfair_display_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.playfair_display_extrabold, FontWeight.ExtraBold),
    Font(R.font.playfair_display_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.playfair_display_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.playfair_display_medium, FontWeight.Medium),
    Font(R.font.playfair_display_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.playfair_display_regular, FontWeight.Normal),
    Font(R.font.playfair_display_semibold, FontWeight.SemiBold),
    Font(R.font.playfair_display_semibolditalic, FontWeight.SemiBold, FontStyle.Italic)
)

val PressStart2pFamily = FontFamily(
    Font(R.font.press_start_2p_regular, FontWeight.Normal)
)

val QuicksandFamily = FontFamily(
    Font(R.font.quicksand_bold, FontWeight.Bold),
    Font(R.font.quicksand_light, FontWeight.Light),
    Font(R.font.quicksand_medium, FontWeight.Medium),
    Font(R.font.quicksand_regular, FontWeight.Normal),
    Font(R.font.quicksand_semibold, FontWeight.SemiBold)
)

val SpaceGroteskFamily = FontFamily(
    Font(R.font.space_grotesk_bold, FontWeight.Bold),
    Font(R.font.space_grotesk_light, FontWeight.Light),
    Font(R.font.space_grotesk_medium, FontWeight.Medium),
    Font(R.font.space_grotesk_regular, FontWeight.Normal),
    Font(R.font.space_grotesk_semibold, FontWeight.SemiBold)
)

val TurretRoadFamily = FontFamily(
    Font(R.font.turret_road_bold, FontWeight.Bold),
    Font(R.font.turret_road_extrabold, FontWeight.ExtraBold),
    Font(R.font.turret_road_extralight, FontWeight.ExtraLight),
    Font(R.font.turret_road_light, FontWeight.Light),
    Font(R.font.turret_road_medium, FontWeight.Medium),
    Font(R.font.turret_road_regular, FontWeight.Normal)
)

val ViaodaLibreFamily = FontFamily(
    Font(R.font.viaoda_libre_regular, FontWeight.Normal)
)

val fontFamilies = mapOf(
    "Courier (Default)" to CourierPrime,
    "System Default" to FontFamily.Default,
    "Serif" to FontFamily.Serif,
    "SansSerif" to FontFamily.SansSerif,
    "Monospace" to FontFamily.Monospace,
    "Cursive" to FontFamily.Cursive,
    "Nunito" to NunitoFamily,
    "Cormorant Garamond" to CormorantGaramondFamily,
    "DotGothic16" to DotGothic16Family,
    "IBM Plex Mono" to IbmPlexMonoFamily,
    "IBM Plex Serif" to IbmPlexSerifFamily,
    "Instrument Serif" to InstrumentSerifFamily,
    "Egyptian Hieroglyphs" to NotoSansEgyptianHieroglyphsFamily,
    "Playfair Display" to PlayfairDisplayFamily,
    "Press Start 2P" to PressStart2pFamily,
    "Quicksand" to QuicksandFamily,
    "Space Grotesk" to SpaceGroteskFamily,
    "Turret Road" to TurretRoadFamily,
    "Viaoda Libre" to ViaodaLibreFamily
)

/** Display-only faces that are genuinely hard to read as body text — warn before applying. */
val LEGIBILITY_WARNING_FONTS = setOf("Press Start 2P", "DotGothic16")

fun getTypography(baseFontSize: Float, fontFamilyName: String = "Courier (Default)"): Typography {
    val scale = baseFontSize / 16f
    val selectedFont = fontFamilies[fontFamilyName] ?: CourierPrime
    
    return Typography(
        displayLarge = TextStyle(
            fontFamily = selectedFont,
            fontWeight = FontWeight.Normal,
            fontSize = (57 * scale).sp,
            lineHeight = (64 * scale).sp,
            letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = selectedFont,
            fontWeight = FontWeight.Normal,
            fontSize = (45 * scale).sp,
            lineHeight = (52 * scale).sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = selectedFont,
            fontWeight = FontWeight.Normal,
            fontSize = (36 * scale).sp,
            lineHeight = (44 * scale).sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = selectedFont,
            fontWeight = FontWeight.Normal,
            fontSize = (32 * scale).sp,
            lineHeight = (40 * scale).sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = selectedFont,
            fontWeight = FontWeight.Normal,
            fontSize = (28 * scale).sp,
            lineHeight = (36 * scale).sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = selectedFont,
            fontWeight = FontWeight.Normal,
            fontSize = (24 * scale).sp,
            lineHeight = (32 * scale).sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = selectedFont,
            fontWeight = FontWeight.Normal,
            fontSize = (22 * scale).sp,
            lineHeight = (28 * scale).sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = selectedFont,
            fontWeight = FontWeight.Medium,
            fontSize = (16 * scale).sp,
            lineHeight = (24 * scale).sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = selectedFont,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp,
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = selectedFont,
            fontWeight = FontWeight.Normal,
            fontSize = baseFontSize.sp,
            lineHeight = (baseFontSize * 1.5).sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = selectedFont,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = selectedFont,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * scale).sp,
            lineHeight = (16 * scale).sp,
            letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontFamily = selectedFont,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * scale).sp,
            lineHeight = (20 * scale).sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = selectedFont,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * scale).sp,
            lineHeight = (16 * scale).sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = selectedFont,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * scale).sp,
            lineHeight = (16 * scale).sp,
            letterSpacing = 0.5.sp
        )
    )
}

val Typography = getTypography(16f)
