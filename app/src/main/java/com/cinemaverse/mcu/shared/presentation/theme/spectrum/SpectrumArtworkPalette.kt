package com.cinemaverse.mcu.shared.presentation.theme.spectrum

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.cinemaverse.mcu.util.ExtractedColors

@Immutable
data class SpectrumArtworkPalette(
    val artworkPrimary: Color,
    val artworkSecondary: Color,
    val artworkAccent: Color,
    val artworkGlow: Color,
    val artworkContainer: Color,
    val artworkOnContainer: Color,
    val artworkSurface: Color,
    val artworkScrim: Color
)

fun SpectrumArtworkPalette.CompanionDefault(colors: SpectrumUniverseColors, darkTheme: Boolean): SpectrumArtworkPalette = SpectrumArtworkPalette(
    artworkPrimary = colors.primary,
    artworkSecondary = colors.secondary,
    artworkAccent = colors.accent,
    artworkGlow = colors.glow,
    artworkContainer = colors.container,
    artworkOnContainer = colors.onContainer,
    artworkSurface = if (darkTheme) Color(0xFF10131D) else Color(0xFFFFFBFF),
    artworkScrim = if (darkTheme) Color(0xEE090B10) else Color(0xEEFFF9F3)
)

fun ExtractedColors?.toSpectrumArtworkPalette(fallback: SpectrumUniverseColors, darkTheme: Boolean): SpectrumArtworkPalette {
    if (this == null) return SpectrumArtworkPalette.CompanionDefault(fallback, darkTheme)
    val primary = Color(if (darkTheme) darkPrimary else primary).spectrumClamp(darkTheme)
    val secondary = Color(if (darkTheme) darkSecondary else secondary).spectrumClamp(darkTheme)
    val accent = Color(if (darkTheme) darkTertiary else tertiary).spectrumClamp(darkTheme)
    val container = Color(if (darkTheme) darkPrimaryContainer else primaryContainer).spectrumContainerClamp(darkTheme)
    val onContainer = if (container.luminance() > 0.45f) Color(0xFF101014) else Color.White
    return SpectrumArtworkPalette(primary, secondary, accent, primary.copy(alpha = 0.38f), container, onContainer, if (darkTheme) Color(surface) else Color(0xFFFFFBFF), if (darkTheme) Color(0xEA080A10) else Color(0xEAFFFBF7))
}

private fun Color.spectrumClamp(darkTheme: Boolean): Color {
    val lum = luminance()
    return when {
        darkTheme && lum < 0.28f -> copy(red = (red + 0.25f).coerceAtMost(1f), green = (green + 0.25f).coerceAtMost(1f), blue = (blue + 0.25f).coerceAtMost(1f))
        !darkTheme && lum > 0.72f -> copy(red = red * 0.72f, green = green * 0.72f, blue = blue * 0.72f)
        else -> this
    }
}

private fun Color.spectrumContainerClamp(darkTheme: Boolean): Color = if (darkTheme) {
    copy(alpha = 1f, red = red * 0.45f, green = green * 0.45f, blue = blue * 0.52f)
} else {
    copy(alpha = 1f, red = (red + 0.10f).coerceAtMost(1f), green = (green + 0.10f).coerceAtMost(1f), blue = (blue + 0.10f).coerceAtMost(1f))
}
