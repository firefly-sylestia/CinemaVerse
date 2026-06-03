package com.cinemaverse.mcu.shared.presentation.theme.spectrum

import androidx.compose.ui.graphics.Color

enum class SpectrumMusicUniverse { Reactor, Vibranium, Mystic, Cosmic, Street, Archive }

data class SpectrumUniverseColors(
    val universe: SpectrumMusicUniverse,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val glow: Color,
    val container: Color,
    val onContainer: Color
)

fun spectrumUniverseColors(universe: SpectrumMusicUniverse, darkTheme: Boolean): SpectrumUniverseColors = when (universe) {
    SpectrumMusicUniverse.Reactor -> if (darkTheme) SpectrumUniverseColors(universe, Color(0xFFFF5A5F), Color(0xFFFFC857), Color(0xFF36D7FF), Color(0x6636D7FF), Color(0xFF1B2234), Color(0xFFF6F8FF)) else SpectrumUniverseColors(universe, Color(0xFFB51D2A), Color(0xFF8A5B00), Color(0xFF006783), Color(0x33006783), Color(0xFFFFF1E7), Color(0xFF251A13))
    SpectrumMusicUniverse.Vibranium -> if (darkTheme) SpectrumUniverseColors(universe, Color(0xFFA989FF), Color(0xFF4FA8FF), Color(0xFFFFD166), Color(0x66A989FF), Color(0xFF181527), Color(0xFFF4EEFF)) else SpectrumUniverseColors(universe, Color(0xFF6240B6), Color(0xFF005EA8), Color(0xFF7A5700), Color(0x336240B6), Color(0xFFF0E9FF), Color(0xFF201A2E))
    SpectrumMusicUniverse.Mystic -> if (darkTheme) SpectrumUniverseColors(universe, Color(0xFFFF8A3D), Color(0xFF39D7C8), Color(0xFFFFD166), Color(0x66FF8A3D), Color(0xFF2A171D), Color(0xFFFFF0E8)) else SpectrumUniverseColors(universe, Color(0xFFA84400), Color(0xFF006A61), Color(0xFF795900), Color(0x33A84400), Color(0xFFFFEEE4), Color(0xFF2B170D))
    SpectrumMusicUniverse.Cosmic -> if (darkTheme) SpectrumUniverseColors(universe, Color(0xFFC77DFF), Color(0xFFFF5CC8), Color(0xFF35E4FF), Color(0x66C77DFF), Color(0xFF1E1730), Color(0xFFF8EEFF)) else SpectrumUniverseColors(universe, Color(0xFF7E2BC1), Color(0xFFB3007C), Color(0xFF006A78), Color(0x337E2BC1), Color(0xFFF7E8FF), Color(0xFF271137))
    SpectrumMusicUniverse.Street -> if (darkTheme) SpectrumUniverseColors(universe, Color(0xFFFF4B55), Color(0xFF4C8DFF), Color(0xFFFFF0D6), Color(0x66FF4B55), Color(0xFF22181E), Color(0xFFFFF5F5)) else SpectrumUniverseColors(universe, Color(0xFFBA1A1A), Color(0xFF0059C7), Color(0xFF5C4530), Color(0x33BA1A1A), Color(0xFFFFE8E7), Color(0xFF410006))
    SpectrumMusicUniverse.Archive -> if (darkTheme) SpectrumUniverseColors(universe, Color(0xFFE5B454), Color(0xFF8DAA75), Color(0xFFF2D6A2), Color(0x66E5B454), Color(0xFF241E14), Color(0xFFFFF4DD)) else SpectrumUniverseColors(universe, Color(0xFF765A00), Color(0xFF48633C), Color(0xFF7B5D28), Color(0x33765A00), Color(0xFFFFF2CF), Color(0xFF251A00))
}
