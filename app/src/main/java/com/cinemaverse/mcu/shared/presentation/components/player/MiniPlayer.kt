package com.cinemaverse.mcu.shared.presentation.components.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cinemaverse.mcu.shared.data.model.Song
import com.cinemaverse.mcu.shared.presentation.components.icons.RhythmIcons
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumArtworkTile
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumCommandBar
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumIconButton
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.ProvideSpectrumRhythmTheme
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.rememberSpectrumUniverseForGenre

@Composable
fun MiniPlayer(
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
) {
    if (song == null) return
    val universe = rememberSpectrumUniverseForGenre(song.genre)
    ProvideSpectrumRhythmTheme(universe = universe) {
        SpectrumCommandBar(modifier = modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SpectrumArtworkTile(song.artworkUri, song.title, size = 54.dp, rounded = 16.dp)
                    Column(Modifier.weight(1f).padding(vertical = 2.dp)) {
                        Text(song.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(song.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    SpectrumIconButton(RhythmIcons.Player.SkipPrevious, "Previous track", onClick = onSkipPrevious)
                    SpectrumIconButton(if (isPlaying) RhythmIcons.Player.Pause else RhythmIcons.Player.Play, if (isPlaying) "Pause" else "Play", selected = isPlaying, onClick = onPlayPause)
                    SpectrumIconButton(RhythmIcons.Player.SkipNext, "Next track", onClick = onSkipNext)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress = { progress().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(5.dp))
            }
        }
    }
}
