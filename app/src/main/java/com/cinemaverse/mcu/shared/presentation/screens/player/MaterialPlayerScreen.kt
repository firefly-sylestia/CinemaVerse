package com.cinemaverse.mcu.shared.presentation.screens.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cinemaverse.mcu.features.local.presentation.screens.LibraryTab
import com.cinemaverse.mcu.features.local.presentation.viewmodel.MusicViewModel
import com.cinemaverse.mcu.shared.data.model.Album
import com.cinemaverse.mcu.shared.data.model.AppSettings
import com.cinemaverse.mcu.shared.data.model.Artist
import com.cinemaverse.mcu.shared.data.model.LyricsData
import com.cinemaverse.mcu.shared.data.model.PlaybackLocation
import com.cinemaverse.mcu.shared.data.model.Playlist
import com.cinemaverse.mcu.shared.data.model.Song
import com.cinemaverse.mcu.shared.presentation.components.icons.RhythmIcons
import com.cinemaverse.mcu.shared.presentation.components.player.formatDuration
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumArtworkBackdrop
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumCommandBar
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumEmptyState
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumEqualizerIndicator
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumIconButton
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumPanel
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumPrimaryButton
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumProgressRing
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumSectionHeader
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumSeekBar
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumSongRow
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.ProvideSpectrumRhythmTheme
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.rememberSpectrumReachMode
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.rememberSpectrumUniverseForGenre
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.spectrumReachMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialPlayerScreen(
    song: Song?,
    isPlaying: Boolean,
    progress: () -> Float,
    location: PlaybackLocation?,
    queuePosition: Int = 1,
    queueTotal: Int = 1,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Float) -> Unit,
    onLyricsSeek: ((Long) -> Unit)? = null,
    onBack: () -> Unit,
    onLocationClick: () -> Unit,
    onQueueClick: () -> Unit,
    locations: List<PlaybackLocation> = emptyList(),
    onLocationSelect: (PlaybackLocation) -> Unit = {},
    volume: Float = 0.7f,
    isMuted: Boolean = false,
    onVolumeChange: (Float) -> Unit = {},
    onToggleMute: () -> Unit = {},
    onMaxVolume: () -> Unit = {},
    onRefreshDevices: () -> Unit = {},
    onStopDeviceMonitoring: () -> Unit = {},
    onToggleShuffle: () -> Unit = {},
    onToggleRepeat: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
    isShuffleEnabled: Boolean = false,
    repeatMode: Int = 0,
    isFavorite: Boolean = false,
    showLyrics: Boolean = true,
    onlineOnlyLyrics: Boolean = false,
    lyrics: LyricsData? = null,
    isLoadingLyrics: Boolean = false,
    onRetryLyrics: () -> Unit = {},
    onEditLyrics: (String) -> Unit = {},
    onPickLyricsFile: () -> Unit = {},
    onSaveLyrics: (String, String) -> Unit = { _, _ -> },
    playlists: List<Playlist> = emptyList(),
    queue: List<Song> = emptyList(),
    onSongClick: (Song) -> Unit = {},
    onSongClickAtIndex: (Int) -> Unit = { _ -> },
    onRemoveFromQueueAtIndex: (Int) -> Unit = { _ -> },
    onMoveQueueItem: (Int, Int) -> Unit = { _, _ -> },
    onAddSongsToQueue: () -> Unit = {},
    onNavigateToLibrary: (LibraryTab) -> Unit = {},
    showAddToPlaylistSheet: Boolean = false,
    onAddToPlaylistSheetDismiss: () -> Unit = {},
    onAddSongToPlaylist: (Song, String) -> Unit = { _, _ -> },
    onCreatePlaylist: (String) -> Unit = {},
    onShowCreatePlaylistDialog: () -> Unit = {},
    onClearQueue: () -> Unit = {},
    isMediaLoading: Boolean = false,
    isSeeking: Boolean = false,
    onShowAlbumBottomSheet: () -> Unit = {},
    onShowArtistBottomSheet: () -> Unit = {},
    songs: List<Song> = emptyList(),
    albums: List<Album> = emptyList(),
    artists: List<Artist> = emptyList(),
    onPlayAlbumSongs: (List<Song>) -> Unit = {},
    onShuffleAlbumSongs: (List<Song>) -> Unit = {},
    onPlayArtistSongs: (List<Song>) -> Unit = {},
    onShuffleArtistSongs: (List<Song>) -> Unit = {},
    appSettings: AppSettings,
    musicViewModel: MusicViewModel,
    navController: NavController,
    isStreamingMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val universe = rememberSpectrumUniverseForGenre(song?.genre)
    val reachMode = rememberSpectrumReachMode()
    var showQueue by remember { mutableStateOf(false) }
    var showLyricsSheet by remember { mutableStateOf(false) }
    val progressValue = progress().coerceIn(0f, 1f)

    ProvideSpectrumRhythmTheme(universe = universe) {
        SpectrumArtworkBackdrop(song?.artworkUri, modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                LazyColumn(
                    Modifier.weight(1f).spectrumReachMode(reachMode),
                    contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 184.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            SpectrumIconButton(RhythmIcons.Navigation.Back, "Back", onClick = onBack)
                            Text("Reactor Player Console", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            SpectrumIconButton(RhythmIcons.Actions.More, "More player actions", onClick = onAddToPlaylist)
                        }
                    }
                    item {
                        Box(contentAlignment = Alignment.Center) {
                            SpectrumProgressRing(progressValue, Modifier.size(340.dp))
                            AsyncImage(model = song?.artworkUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(286.dp))
                        }
                    }
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(song?.title ?: "No track selected", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(song?.artist ?: "Select music from your library", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("$queuePosition of $queueTotal • ${formatDuration(song?.duration ?: 0L)}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    item {
                        SpectrumPanel(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                                SpectrumIconButton(RhythmIcons.Player.Shuffle, "Shuffle", selected = isShuffleEnabled, onClick = onToggleShuffle)
                                SpectrumIconButton(RhythmIcons.Player.SkipPrevious, "Previous", onClick = onSkipPrevious)
                                SpectrumIconButton(if (isPlaying) RhythmIcons.Player.Pause else RhythmIcons.Player.Play, if (isPlaying) "Pause" else "Play", selected = isPlaying, onClick = onPlayPause)
                                SpectrumIconButton(RhythmIcons.Player.SkipNext, "Next", onClick = onSkipNext)
                                SpectrumIconButton(if (repeatMode > 0) RhythmIcons.Player.RepeatOne else RhythmIcons.Player.Repeat, "Repeat", selected = repeatMode > 0, onClick = onToggleRepeat)
                            }
                            Spacer(Modifier.height(18.dp))
                            SpectrumSeekBar(progressValue, onSeek, Modifier.fillMaxWidth())
                        }
                    }
                    item {
                        SpectrumPanel(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                SpectrumIconButton(RhythmIcons.Player.FavoriteOutlined, "Favorite", selected = isFavorite, onClick = onToggleFavorite)
                                SpectrumIconButton(RhythmIcons.Player.Lyrics, "Lyrics", selected = showLyricsSheet, onClick = { showLyricsSheet = true })
                                SpectrumIconButton(RhythmIcons.Player.Queue, "Queue", selected = showQueue, onClick = { showQueue = true; onQueueClick() })
                                SpectrumIconButton(RhythmIcons.Player.Equalizer, "Equalizer", onClick = { onNavigateToLibrary(LibraryTab.SONGS) })
                                SpectrumIconButton(RhythmIcons.Devices.Bluetooth, "Output", onClick = onLocationClick)
                            }
                        }
                    }
                    if (lyrics?.hasLyrics() == true) item {
                        LyricsPanel(lyrics, onEditLyrics)
                    }
                }
                SpectrumCommandBar(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SpectrumEqualizerIndicator(isPlaying)
                        Column(Modifier.weight(1f)) {
                            Text(song?.title ?: "Ready", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(location?.name ?: "Local device", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        SpectrumPrimaryButton(if (isPlaying) "Pause" else "Play", if (isPlaying) RhythmIcons.Player.Pause else RhythmIcons.Player.Play, onPlayPause, Modifier.weight(.8f))
                    }
                }
            }
        }
        if (showQueue) ModalBottomSheet(onDismissRequest = { showQueue = false }) { QueuePanel(queue, song, isPlaying, onSongClickAtIndex, onClearQueue) }
        if (showLyricsSheet) ModalBottomSheet(onDismissRequest = { showLyricsSheet = false }) { LyricsPanel(lyrics, onEditLyrics) }
    }
}

@Composable
private fun QueuePanel(queue: List<Song>, currentSong: Song?, isPlaying: Boolean, onSongClickAtIndex: (Int) -> Unit, onClearQueue: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SpectrumSectionHeader("Queue timeline", "${queue.size} tracks staged", "Clear", onClearQueue)
        if (queue.isEmpty()) SpectrumEmptyState("Queue empty", "Add songs to build a playback timeline.") else queue.forEachIndexed { index, queued -> SpectrumSongRow(queued, currentSong?.id == queued.id, currentSong?.id == queued.id && isPlaying, { onSongClickAtIndex(index) }) }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun LyricsPanel(lyrics: LyricsData?, onEditLyrics: (String) -> Unit) {
    SpectrumPanel(Modifier.fillMaxWidth()) {
        SpectrumSectionHeader("Lyrics", lyrics?.source ?: "Readable reactor text")
        val text = lyrics?.getPlainLyricsOrNull() ?: lyrics?.getSyncedLyricsOrNull() ?: "No lyrics available yet."
        Text(text.lines().take(18).joinToString("\n"), style = MaterialTheme.typography.bodyLarge, lineHeight = MaterialTheme.typography.bodyLarge.lineHeight, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(12.dp))
        SpectrumPrimaryButton("Edit lyrics", RhythmIcons.Actions.Edit, { onEditLyrics(text) })
    }
}
