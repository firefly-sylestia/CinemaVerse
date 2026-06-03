package com.cinemaverse.mcu.shared.presentation.theme.spectrum

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

@Immutable
data class SpectrumRhythmSpacing(
    val screen: androidx.compose.ui.unit.Dp = 22.dp,
    val card: androidx.compose.ui.unit.Dp = 18.dp,
    val section: androidx.compose.ui.unit.Dp = 28.dp,
    val row: androidx.compose.ui.unit.Dp = 10.dp,
    val command: androidx.compose.ui.unit.Dp = 14.dp,
    val touch: androidx.compose.ui.unit.Dp = 48.dp
)

@Immutable
data class SpectrumRhythmMotion(
    val quick: Int = 180,
    val normal: Int = 240,
    val cinematic: Int = 280
)

@Immutable
data class SpectrumRhythmTokens(
    val spacing: SpectrumRhythmSpacing = SpectrumRhythmSpacing(),
    val motion: SpectrumRhythmMotion = SpectrumRhythmMotion()
)
