package com.cinemaverse.mcu.shared.presentation.theme.spectrum

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.MaterialTheme

private val LocalSpectrumArtworkPalette = staticCompositionLocalOf<SpectrumArtworkPalette> {
    SpectrumArtworkPalette.CompanionDefault(spectrumUniverseColors(SpectrumMusicUniverse.Reactor, true), true)
}
private val LocalSpectrumUniverse = staticCompositionLocalOf { spectrumUniverseColors(SpectrumMusicUniverse.Reactor, true) }
private val LocalSpectrumTokens = staticCompositionLocalOf { SpectrumRhythmTokens() }

object SpectrumRhythmTheme {
    val artwork: SpectrumArtworkPalette
        @Composable @ReadOnlyComposable get() = LocalSpectrumArtworkPalette.current
    val universe: SpectrumUniverseColors
        @Composable @ReadOnlyComposable get() = LocalSpectrumUniverse.current
    val tokens: SpectrumRhythmTokens
        @Composable @ReadOnlyComposable get() = LocalSpectrumTokens.current
}

@Composable
fun ProvideSpectrumRhythmTheme(
    universe: SpectrumMusicUniverse = SpectrumMusicUniverse.Reactor,
    artworkPalette: SpectrumArtworkPalette? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val universeColors = spectrumUniverseColors(universe, darkTheme)
    androidx.compose.runtime.CompositionLocalProvider(
        LocalSpectrumUniverse provides universeColors,
        LocalSpectrumArtworkPalette provides (artworkPalette ?: SpectrumArtworkPalette.CompanionDefault(universeColors, darkTheme)),
        LocalSpectrumTokens provides SpectrumRhythmTokens(),
        content = content
    )
}

@Composable
fun rememberSpectrumUniverseForGenre(genre: String?, fallback: SpectrumMusicUniverse = SpectrumMusicUniverse.Reactor): SpectrumMusicUniverse {
    val normalized = genre.orEmpty().lowercase()
    return when {
        listOf("hip", "rap", "punk", "rock", "trap").any { normalized.contains(it) } -> SpectrumMusicUniverse.Street
        listOf("classical", "soundtrack", "score", "ambient", "orchestral").any { normalized.contains(it) } -> SpectrumMusicUniverse.Mystic
        listOf("electronic", "edm", "pop", "synth", "dance").any { normalized.contains(it) } -> SpectrumMusicUniverse.Cosmic
        listOf("jazz", "soul", "r&b", "bass").any { normalized.contains(it) } -> SpectrumMusicUniverse.Vibranium
        listOf("old", "archive", "history", "podcast").any { normalized.contains(it) } -> SpectrumMusicUniverse.Archive
        else -> fallback
    }
}
