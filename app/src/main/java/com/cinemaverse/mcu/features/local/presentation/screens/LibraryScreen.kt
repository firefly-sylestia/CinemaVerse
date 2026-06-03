package com.cinemaverse.mcu.features.local.presentation.screens

import android.net.Uri
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cinemaverse.mcu.features.local.presentation.viewmodel.MusicViewModel
import com.cinemaverse.mcu.shared.data.model.Album
import com.cinemaverse.mcu.shared.data.model.Artist
import com.cinemaverse.mcu.shared.data.model.Playlist
import com.cinemaverse.mcu.shared.data.model.Song
import com.cinemaverse.mcu.shared.presentation.components.icons.RhythmIcons
import com.cinemaverse.mcu.shared.presentation.components.player.MiniPlayer
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumAlbumCard
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumArtistCard
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumArtworkBackdrop
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumEmptyState
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumIconButton
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumPanel
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumPlaylistCard
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumPrimaryButton
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumSectionHeader
import com.cinemaverse.mcu.shared.presentation.components.spectrum.SpectrumSongRow
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.ProvideSpectrumRhythmTheme
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.SpectrumMusicUniverse
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.rememberSpectrumReachMode
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.spectrumReachMode
import com.cinemaverse.mcu.util.PlaylistImportExportUtils

enum class LibraryTab { SONGS, PLAYLISTS, ALBUMS, ARTISTS, EXPLORER }
enum class LibraryPlaylistSortOrder { NAME, SONG_COUNT, DATE_MODIFIED }

@Composable
fun LibraryScreen(
    songs: List<Song>,
    albums: List<Album>,
    playlists: List<Playlist>,
    artists: List<Artist>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    onPlayPause: () -> Unit,
    onPlayerClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onAddPlaylist: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onAlbumShufflePlay: (Album) -> Unit = { _ -> },
    onPlayQueue: (List<Song>) -> Unit = { _ -> },
    onPlayQueueFromIndex: (List<Song>, Int) -> Unit = { _, _ -> },
    onShuffleQueue: (List<Song>) -> Unit = { _ -> },
    onAlbumBottomSheetClick: (Album) -> Unit = { _ -> },
    onSort: () -> Unit = {},
    onRefreshClick: () -> Unit,
    onAddSongToPlaylist: (Song, String) -> Unit = { _, _ -> },
    onCreatePlaylist: (String) -> Unit = { _ -> },
    sortOrder: MusicViewModel.SortOrder = MusicViewModel.SortOrder.TITLE_ASC,
    onSkipNext: () -> Unit = {},
    onAddToQueue: (Song) -> Unit,
    initialTab: LibraryTab = LibraryTab.SONGS,
    musicViewModel: MusicViewModel,
    onExportAllPlaylists: ((PlaylistImportExportUtils.PlaylistExportFormat, Boolean, Uri?, (Result<String>) -> Unit) -> Unit)? = null,
    onImportPlaylist: ((Uri, (Result<String>) -> Unit, (() -> Unit)?) -> Unit)? = null,
    onRestartApp: (() -> Unit)? = null,
    onNavigateToArtist: (Artist) -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }
    val reachMode = rememberSpectrumReachMode()
    ProvideSpectrumRhythmTheme(universe = if (selectedTab == LibraryTab.EXPLORER) SpectrumMusicUniverse.Archive else SpectrumMusicUniverse.Reactor) {
        SpectrumArtworkBackdrop(currentSong?.artworkUri, Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f).spectrumReachMode(reachMode),
                    contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 122.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    item {
                        SpectrumPanel(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text("Music Library", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                                    Text("Songs • playlists • albums • artists • explorer", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                SpectrumIconButton(RhythmIcons.Actions.Refresh, "Refresh music library", onClick = onRefreshClick)
                            }
                            Spacer(Modifier.height(16.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                lazyItems(LibraryTab.entries) { tab ->
                                    FilterChip(selected = selectedTab == tab, onClick = { selectedTab = tab }, label = { Text(tab.name.lowercase().replaceFirstChar { it.titlecase() }) })
                                }
                            }
                        }
                    }
                    item {
                        when (selectedTab) {
                            LibraryTab.SONGS -> SongsTab(songs, currentSong, isPlaying, onSongClick, onPlayQueue, onShuffleQueue)
                            LibraryTab.PLAYLISTS -> PlaylistsTab(playlists, onPlaylistClick, onAddPlaylist)
                            LibraryTab.ALBUMS -> AlbumsTab(albums, onAlbumClick, onAlbumShufflePlay)
                            LibraryTab.ARTISTS -> ArtistsTab(artists, onArtistClick, onNavigateToArtist)
                            LibraryTab.EXPLORER -> ExplorerTab(songs, onSongClick)
                        }
                    }
                }
                MiniPlayer(currentSong, isPlaying, progress = { 0f }, onPlayPause = onPlayPause, onPlayerClick = onPlayerClick, onSkipNext = onSkipNext)
            }
        }
    }
}

@Composable
private fun SongsTab(songs: List<Song>, currentSong: Song?, isPlaying: Boolean, onSongClick: (Song) -> Unit, onPlayQueue: (List<Song>) -> Unit, onShuffleQueue: (List<Song>) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SpectrumSectionHeader("Songs", "${songs.size} indexed tracks")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { SpectrumPrimaryButton("Play all", RhythmIcons.Player.Play, { onPlayQueue(songs) }, Modifier.weight(1f)); SpectrumPrimaryButton("Shuffle", RhythmIcons.Player.Shuffle, { onShuffleQueue(songs) }, Modifier.weight(1f)) }
        if (songs.isEmpty()) SpectrumEmptyState("No songs", "Your audio files will appear here after scanning.") else songs.forEach { song -> SpectrumSongRow(song, currentSong?.id == song.id, currentSong?.id == song.id && isPlaying, { onSongClick(song) }) }
    }
}

@Composable
private fun PlaylistsTab(playlists: List<Playlist>, onPlaylistClick: (Playlist) -> Unit, onAddPlaylist: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) { SpectrumSectionHeader("Playlists", "Timeline-ready collections", "New", onAddPlaylist); if (playlists.isEmpty()) SpectrumEmptyState("No playlists", "Create a playlist to build your own command sequence.", "Create", onAddPlaylist) else LazyVerticalGrid(columns = GridCells.Adaptive(170.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.height(600.dp)) { items(playlists, key = { it.id }) { SpectrumPlaylistCard(it, { onPlaylistClick(it) }, Modifier.fillMaxWidth()) } } }
}

@Composable
private fun AlbumsTab(albums: List<Album>, onAlbumClick: (Album) -> Unit, onAlbumShufflePlay: (Album) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) { SpectrumSectionHeader("Albums", "Cinematic cover grid"); if (albums.isEmpty()) SpectrumEmptyState("No albums", "Albums appear after your local library scan completes.") else LazyVerticalGrid(columns = GridCells.Adaptive(170.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.height(700.dp)) { items(albums, key = { it.id }) { SpectrumAlbumCard(it, { onAlbumClick(it) }, Modifier.fillMaxWidth()) } } }
}

@Composable
private fun ArtistsTab(artists: List<Artist>, onArtistClick: (Artist) -> Unit, onNavigateToArtist: (Artist) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) { SpectrumSectionHeader("Artists", "Premium signal profiles"); if (artists.isEmpty()) SpectrumEmptyState("No artists", "Artists appear after songs are indexed.") else LazyVerticalGrid(columns = GridCells.Adaptive(170.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.height(700.dp)) { items(artists, key = { it.id }) { artist -> SpectrumArtistCard(artist, { onNavigateToArtist(artist); onArtistClick(artist) }, Modifier.fillMaxWidth()) } } }
}

@Composable
private fun ExplorerTab(songs: List<Song>, onSongClick: (Song) -> Unit) {
    val byFolder = remember(songs) { songs.groupBy { it.uri.path?.substringBeforeLast('/') ?: "Audio archive" } }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { SpectrumSectionHeader("Explorer", "Archive-style filesystem index"); if (songs.isEmpty()) SpectrumEmptyState("Explorer empty", "Scan audio files to populate the archive.") else byFolder.forEach { (folder, folderSongs) -> SpectrumPanel(Modifier.fillMaxWidth()) { Text(folder, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Spacer(Modifier.height(10.dp)); folderSongs.take(8).forEach { song -> SpectrumSongRow(song, false, false, { onSongClick(song) }, Modifier.padding(vertical = 4.dp)) } } } }
}

@Composable
fun LibrarySongItemWrapper(
    song: Song,
    onClick: () -> Unit,
    onMoreClick: () -> Unit = {},
    onAddToQueue: () -> Unit = {},
    onShowSongInfo: () -> Unit = {},
    onAddToBlacklist: () -> Unit = {},
    currentSong: Song? = null,
    isPlaying: Boolean = false,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback? = null,
    enableRatingSystem: Boolean = false,
    itemShape: androidx.compose.ui.graphics.Shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
) {
    SpectrumSongRow(song = song, isActive = currentSong?.id == song.id, isPlaying = currentSong?.id == song.id && isPlaying, onClick = onClick)
}
