package com.cinemaverse.mcu.shared.presentation.theme.spectrum

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun rememberSpectrumReachMode(timeoutMs: Long = 5000L): SpectrumReachModeState {
    var enabled by remember { mutableStateOf(false) }
    LaunchedEffect(enabled) {
        if (enabled) {
            delay(timeoutMs)
            enabled = false
        }
    }
    return remember { SpectrumReachModeState({ enabled }, { enabled = it }) }
}

class SpectrumReachModeState internal constructor(
    private val getter: () -> Boolean,
    private val setter: (Boolean) -> Unit
) {
    val enabled: Boolean get() = getter()
    fun enter() = setter(true)
    fun exit() = setter(false)
    fun toggle() = setter(!getter())
}

@Composable
fun Modifier.spectrumReachMode(state: SpectrumReachModeState): Modifier {
    val offset by animateDpAsState(if (state.enabled) 112.dp else 0.dp, tween(220), label = "spectrumReachOffset")
    return this
        .offset(y = offset)
        .pointerInput(state.enabled) {
            detectVerticalDragGestures { _, dragAmount ->
                if (dragAmount > 18f) state.enter()
                if (dragAmount < -18f) state.exit()
            }
        }
}
