package com.cinemaverse.mcu.features.local.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cinemaverse.mcu.shared.data.model.Album
import com.cinemaverse.mcu.shared.data.model.Artist
import com.cinemaverse.mcu.shared.data.model.Song
import com.cinemaverse.mcu.shared.presentation.components.icons.RhythmIcons
import com.cinemaverse.mcu.shared.presentation.components.player.MiniPlayer
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumAlbumCard
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumArtistCard
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumArtworkBackdrop
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumEmptyState
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumIconButton
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumPanel
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumPrimaryButton
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumSectionHeader
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumSongRow
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.ProvideSpectrumRhythmTheme
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.rememberSpectrumReachMode
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.rememberSpectrumUniverseForGenre
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.spectrumReachMode
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    musicViewModel: com.cinemaverse.mcu.viewmodel.MusicViewModel,
    songs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    recentlyPlayed: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onViewAllSongs: () -> Unit,
    onViewAllAlbums: () -> Unit,
    onViewAllArtists: () -> Unit,
    onSkipNext: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onAddToQueue: (Song) -> Unit = {},
    onAddSongToPlaylist: (Song, String) -> Unit = { _, _ -> },
    onNavigateToPlaylist: (String) -> Unit = {},
    onCreatePlaylist: (String) -> Unit = { _ -> },
    onNavigateToStats: () -> Unit = {},
    onNavigateToArtist: (Artist) -> Unit = {}
) {
    val universe = rememberSpectrumUniverseForGenre(currentSong?.genre)
    val reachMode = rememberSpectrumReachMode()
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val featuredAlbums = remember(albums) { albums.sortedByDescending { it.year }.take(12) }
    val newMusic = remember(albums, currentYear) { albums.filter { it.year == currentYear }.ifEmpty { featuredAlbums }.take(10) }
    val dashboardSongs = remember(recentlyPlayed, songs) { (recentlyPlayed + songs).distinctBy { it.id }.take(12) }

    ProvideSpectrumRhythmTheme(universe = universe) {
        SpectrumArtworkBackdrop(currentSong?.artworkUri, Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f).spectrumReachMode(reachMode),
                    contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 28.dp, bottom = 118.dp),
                    verticalArrangement = Arrangement.spacedBy(26.dp)
                ) {
                    item {
                        SpectrumPanel(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Column(Modifier.weight(1f)) {
                                    Text("Spectrum Dashboard", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                                    Text("${songs.size} songs • ${albums.size} albums • ${artists.size} artists", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                SpectrumIconButton(RhythmIcons.Navigation.Search, "Search music", onClick = onSearchClick)
                                SpectrumIconButton(RhythmIcons.Navigation.Settings, "Music settings", onClick = onSettingsClick)
                            }
                            Spacer(Modifier.height(18.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                SpectrumPrimaryButton("Library", RhythmIcons.Navigation.Library, onNavigateToLibrary, Modifier.weight(1f))
                                SpectrumPrimaryButton("Stats", RhythmIcons.Player.Equalizer, onNavigateToStats, Modifier.weight(1f))
                            }
                        }
                    }
                    if (currentSong != null) item {
                        SpectrumSectionHeader("Reactor console", "Now powering your rhythm")
                        SpectrumSongRow(currentSong, isActive = true, isPlaying = isPlaying, onClick = onPlayerClick)
                    }
                    if (dashboardSongs.isNotEmpty()) item {
                        SpectrumSectionHeader("Continue listening", "Recent signal returns", "All", onViewAllSongs)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { dashboardSongs.take(5).forEach { song -> SpectrumSongRow(song, currentSong?.id == song.id, currentSong?.id == song.id && isPlaying, { onSongClick(song) }) } }
                    }
                    if (featuredAlbums.isNotEmpty()) item {
                        SpectrumSectionHeader("Album reactors", "Artwork-adaptive command cards", "All", onViewAllAlbums)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(horizontal = 2.dp)) { items(featuredAlbums, key = { it.id }) { album -> SpectrumAlbumCard(album, { onAlbumClick(album) }) } }
                    }
                    if (artists.isNotEmpty()) item {
                        SpectrumSectionHeader("Artist constellations", "Your strongest signals", "All", onViewAllArtists)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(horizontal = 2.dp)) { items(artists.take(12), key = { it.id }) { artist -> SpectrumArtistCard(artist, { onArtistClick(artist) }) } }
                    }
                    if (newMusic.isNotEmpty()) item {
                        SpectrumSectionHeader("New spectrum", "Fresh catalog energy")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(horizontal = 2.dp)) { items(newMusic, key = { it.id }) { album -> SpectrumAlbumCard(album, { onAlbumClick(album) }) } }
                    }
                    if (songs.isEmpty()) item { SpectrumEmptyState("No music indexed", "Start a scan or add audio files to initialize the Spectrum Rhythm Reactor.", "Open library", onNavigateToLibrary) }
                }
                MiniPlayer(currentSong, isPlaying, progress = { 0f }, onPlayPause = onPlayPause, onPlayerClick = onPlayerClick, onSkipNext = onSkipNext)
            }
        }
    }
}
