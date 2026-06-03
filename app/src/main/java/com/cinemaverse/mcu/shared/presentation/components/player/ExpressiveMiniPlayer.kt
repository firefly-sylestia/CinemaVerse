package com.cinemaverse.mcu.shared.presentation.components.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cinemaverse.mcu.shared.data.model.Song

@Composable
fun ExpressiveMiniPlayer(
    song: Song?,
    isPlaying: Boolean,
    progress: () -> Float,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit = {},
    onDismiss: () -> Unit = {},
    isMediaLoading: Boolean = false,
    modifier: Modifier = Modifier
) = MiniPlayer(song, isPlaying, progress, onPlayPause, onPlayerClick, onSkipNext, onSkipPrevious, onDismiss, isMediaLoading, modifier)
