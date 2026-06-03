package com.cinemaverse.mcu.shared.presentation.theme.spectrum

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

fun spectrumReadableOn(color: Color): Color = if (color.luminance() > 0.45f) Color(0xFF111217) else Color.White
