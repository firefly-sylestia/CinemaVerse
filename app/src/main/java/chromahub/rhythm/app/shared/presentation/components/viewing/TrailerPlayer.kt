package com.cinemaverse.mcu.shared.presentation.components.viewing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.cinemaverse.mcu.features.local.data.database.entity.MCUTitleEntity
import com.cinemaverse.mcu.shared.data.model.AppSettings

/**
 * MCU Trailer Player Component - Adapted from MiniPlayer for video playback.
 * Displays current trailer being watched with play/pause and skip controls.
 * Supports inline viewing on movie cards or full-screen expansion.
 */
@Composable
fun TrailerPlayer(
    title: MCUTitleEntity?,
    isPlaying: Boolean,
    progress: () -> Float,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit = {},
    onDismiss: () -> Unit = {},
    isMediaLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings.getInstance(context) }
    val playerThemeId by appSettings.miniPlayerThemeId.collectAsState()

    if (playerThemeId == "EXPRESSIVE") {
        ExpressiveTrailerPlayer(
            title = title,
            isPlaying = isPlaying,
            progress = progress,
            onPlayPause = onPlayPause,
            onPlayerClick = onPlayerClick,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            onDismiss = onDismiss,
            isMediaLoading = isMediaLoading,
            modifier = modifier
        )
    } else {
        MaterialTrailerPlayer(
            title = title,
            isPlaying = isPlaying,
            progress = progress,
            onPlayPause = onPlayPause,
            onPlayerClick = onPlayerClick,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            onDismiss = onDismiss,
            isMediaLoading = isMediaLoading,
            modifier = modifier
        )
    }
}

/**
 * Expressive themed trailer player - inherits styling from ExpressiveMiniPlayer
 * but adapted for video playback controls and MCU viewing.
 */
@Composable
private fun ExpressiveTrailerPlayer(
    title: MCUTitleEntity?,
    isPlaying: Boolean,
    progress: () -> Float,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onDismiss: () -> Unit,
    isMediaLoading: Boolean,
    modifier: Modifier
) {
    // TODO: Implement ExpressiveTrailerPlayer UI
    // Reuse ExpressiveMiniPlayer styling but adapt:
    // - Display poster instead of album art
    // - Show title and series info instead of artist/album
    // - Keep all gesture handlers and animation patterns
}

/**
 * Material themed trailer player - inherits Material Design from MaterialMiniPlayer
 * but adapted for video playback and MCU viewing.
 */
@Composable
private fun MaterialTrailerPlayer(
    title: MCUTitleEntity?,
    isPlaying: Boolean,
    progress: () -> Float,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onDismiss: () -> Unit,
    isMediaLoading: Boolean,
    modifier: Modifier
) {
    // TODO: Implement MaterialTrailerPlayer UI
    // Reuse MaterialMiniPlayer styling but adapt:
    // - Display poster thumbnail instead of album art
    // - Show title and series instead of song/artist
    // - Maintain all Material Design tokens and gestures
}
