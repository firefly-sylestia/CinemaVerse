// Experimental API opt-ins required for:
// - Material3 APIs (ModalBottomSheet, ExtendedFloatingActionButton behaviors)
// - Foundation APIs (HorizontalPager, stickyHeader in LazyColumn)
// These will become stable in future Compose releases
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
package chromahub.rhythm.app.features.local.presentation.screens

import chromahub.rhythm.app.shared.presentation.components.icons.RhythmIcons
import chromahub.rhythm.app.shared.presentation.components.icons.MaterialSymbolIcon
import chromahub.rhythm.app.shared.presentation.components.icons.Icon

import kotlin.math.abs

import android.widget.Toast
import android.os.Environment
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import java.io.File
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import kotlin.collections.sortedBy
import kotlin.collections.mutableListOf
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Surface
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import android.net.Uri
import android.util.Log
import chromahub.rhythm.app.util.PlaylistImportExportUtils
import chromahub.rhythm.app.util.AppRestarter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import chromahub.rhythm.app.ui.UiConstants
import chromahub.rhythm.app.ui.theme.MusicDimensions
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import chromahub.rhythm.app.ui.LocalMiniPlayerPadding
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import chromahub.rhythm.app.R
import chromahub.rhythm.app.shared.data.model.Album
import chromahub.rhythm.app.shared.data.model.Artist
import chromahub.rhythm.app.shared.data.model.Playlist
import chromahub.rhythm.app.shared.data.model.Song
import chromahub.rhythm.app.shared.data.model.AlbumViewType
import chromahub.rhythm.app.shared.data.model.ArtistViewType
import chromahub.rhythm.app.shared.data.model.PlaylistViewType
import chromahub.rhythm.app.shared.data.model.AppSettings
import chromahub.rhythm.app.features.local.presentation.components.bottomsheets.AddToPlaylistBottomSheet
import chromahub.rhythm.app.features.local.presentation.components.dialogs.CreatePlaylistDialog
import chromahub.rhythm.app.features.local.presentation.components.player.MiniPlayer
import chromahub.rhythm.app.shared.presentation.components.common.M3PlaceholderType
import chromahub.rhythm.app.shared.presentation.components.common.rememberExpressiveShapeFor
import chromahub.rhythm.app.shared.presentation.components.common.ExpressiveShapeTarget
import chromahub.rhythm.app.features.local.presentation.components.dialogs.BulkPlaylistExportDialog
import chromahub.rhythm.app.features.local.presentation.components.dialogs.PlaylistImportDialog
import chromahub.rhythm.app.features.local.presentation.components.dialogs.PlaylistOperationProgressDialog
import chromahub.rhythm.app.features.local.presentation.components.dialogs.PlaylistOperationResultDialog
import chromahub.rhythm.app.features.local.presentation.components.dialogs.AppRestartDialog
import chromahub.rhythm.app.features.local.presentation.components.bottomsheets.SongInfoBottomSheet
import chromahub.rhythm.app.features.local.presentation.components.bottomsheets.AlbumBottomSheet
import chromahub.rhythm.app.features.local.presentation.components.bottomsheets.ArtistBottomSheet
import chromahub.rhythm.app.features.local.presentation.components.settings.LibraryTabOrderBottomSheet
import chromahub.rhythm.app.features.local.presentation.components.bottomsheets.BatchEditTagsSheet
import chromahub.rhythm.app.features.local.presentation.components.bottomsheets.MultiSelectionBottomSheet
import chromahub.rhythm.app.util.ImageUtils
import chromahub.rhythm.app.util.M3ImageUtils
import chromahub.rhythm.app.util.HapticUtils
import chromahub.rhythm.app.features.local.presentation.viewmodel.MusicViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import androidx.compose.material3.ListItemDefaults
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.ui.text.font.FontFamily
import chromahub.rhythm.app.features.local.presentation.components.player.PlayingEqIcon
import chromahub.rhythm.app.shared.presentation.components.common.ContentLoadingIndicator
import chromahub.rhythm.app.shared.presentation.components.common.DataProcessingLoader
import chromahub.rhythm.app.shared.presentation.components.common.AlphabetBar
import chromahub.rhythm.app.shared.presentation.components.common.ScrollToTopButton
import chromahub.rhythm.app.shared.presentation.components.common.TabAnimation
import chromahub.rhythm.app.util.AudioFormatDetector
import chromahub.rhythm.app.util.AudioQualityDetector
import chromahub.rhythm.app.shared.presentation.components.common.ActionProgressLoader
import chromahub.rhythm.app.shared.presentation.components.common.ExpressiveButtonGroup
import chromahub.rhythm.app.shared.presentation.components.common.ButtonGroupStyle
import chromahub.rhythm.app.shared.presentation.components.common.ExpressiveGroupButton
import chromahub.rhythm.app.shared.presentation.components.common.ExpressiveFilledIconButton
import chromahub.rhythm.app.shared.presentation.components.common.ExpressiveShapes


enum class LibraryTab { SONGS, PLAYLISTS, ALBUMS, ARTISTS, EXPLORER }

// Playlist sort order enum for library tab
enum class LibraryPlaylistSortOrder {
    NAME_ASC,
    NAME_DESC,
    DATE_CREATED_ASC,
    DATE_CREATED_DESC,
    SONG_COUNT_ASC,
    SONG_COUNT_DESC
}

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
    onPlayQueue: (List<Song>) -> Unit = { _ -> }, // Added for playing a list of songs with queue replacement
    onPlayQueueFromIndex: (List<Song>, Int) -> Unit = { _, _ -> }, // Added for playing from specific index
    onShuffleQueue: (List<Song>) -> Unit = { _ -> }, // Added for shuffling and playing a list of songs
    onAlbumBottomSheetClick: (Album) -> Unit = { _ -> }, // Added for opening album bottom sheet
    onSort: () -> Unit = {},
    onRefreshClick: () -> Unit, // Changed from onSearchClick to onRefreshClick
    onAddSongToPlaylist: (Song, String) -> Unit = { _, _ -> },
    onCreatePlaylist: (String) -> Unit = { _ -> },
    sortOrder: MusicViewModel.SortOrder = MusicViewModel.SortOrder.TITLE_ASC,
    onSkipNext: () -> Unit = {},
    onAddToQueue: (Song) -> Unit,
    initialTab: LibraryTab = LibraryTab.SONGS,
    musicViewModel: MusicViewModel, // Add MusicViewModel as a parameter
    onExportAllPlaylists: ((PlaylistImportExportUtils.PlaylistExportFormat, Boolean, Uri?, (Result<String>) -> Unit) -> Unit)? = null,
    onImportPlaylist: ((Uri, (Result<String>) -> Unit, (() -> Unit)?) -> Unit)? = null,
    onRestartApp: (() -> Unit)? = null,
    onNavigateToArtist: (Artist) -> Unit = {}
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings.getInstance(context) }
    val tabOrder by appSettings.libraryTabOrder.collectAsState()
    val hiddenTabs by appSettings.hiddenLibraryTabs.collectAsState()
    val enableRatingSystem by appSettings.enableRatingSystem.collectAsState()
    
    // Map tab IDs to display names, filtering out hidden tabs
    val tabs = remember(tabOrder, hiddenTabs) {
        tabOrder
            .filter { !hiddenTabs.contains(it) }
            .map { tabId ->
                when (tabId) {
                    "SONGS" -> "Songs"
                    "PLAYLISTS" -> "Playlists"
                    "ALBUMS" -> "Albums"
                    "ARTISTS" -> "Artists"
                    "EXPLORER" -> "Explorer"
                    else -> tabId
                }
            }
    }
    
    // Create a list of visible tab IDs (after filtering hidden tabs)
    val visibleTabIds = remember(tabOrder, hiddenTabs) {
        tabOrder.filter { !hiddenTabs.contains(it) }
    }
    
    // Find initial tab index based on the visible tabs
    val initialTabIndex = remember(visibleTabIds, initialTab) {
        val tabId = initialTab.name
        visibleTabIds.indexOf(tabId).takeIf { it >= 0 } ?: 0
    }
    
    var selectedTabIndex by rememberSaveable { mutableStateOf(initialTabIndex) }
    val pagerState = rememberPagerState(initialPage = selectedTabIndex) { tabs.size }
    val tabRowState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    
    // Track previous tab order to detect changes
    var previousVisibleTabIds by remember { mutableStateOf(visibleTabIds) }
    
    // Sync pager with selected tab when tabs change (hide/unhide/reorder)
    LaunchedEffect(tabs.size, visibleTabIds) {
        // Check if tab configuration has changed (not just on initial composition)
        val hasTabsChanged = previousVisibleTabIds != visibleTabIds
        
        if (hasTabsChanged) {
            // Tab configuration has changed - always reset to first tab
            selectedTabIndex = 0
            pagerState.scrollToPage(0)
            // Scroll tab row to start
            tabRowState.animateScrollToItem(0)
            previousVisibleTabIds = visibleTabIds
        } else if (selectedTabIndex >= tabs.size) {
            // If current selected tab is out of bounds, reset to first tab
            selectedTabIndex = 0
            pagerState.scrollToPage(0)
        }
    }
    
    // Sync pager state with selected tab index
    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }
    
    // Auto-scroll tab row to show selected tab when returning to this screen
    LaunchedEffect(selectedTabIndex) {
        tabRowState.animateScrollToItem(selectedTabIndex)
    }
    
    // Dialog and bottom sheet states
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistSheet by remember { mutableStateOf(false) }
    var showAlbumBottomSheet by remember { mutableStateOf(false) }
    var showSongInfoSheet by remember { mutableStateOf(false) }
    var showBulkExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showOperationProgress by remember { mutableStateOf(false) }
    var operationInProgress by remember { mutableStateOf("") }
    var operationResult by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    
    // Pending write request for metadata editing (Android 11+)
    val pendingWriteRequest by musicViewModel.pendingWriteRequest.collectAsState()
    
    // Write permission launcher for Android 11+ metadata editing
    val writePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            // User granted permission, complete the write
            musicViewModel.completeMetadataWriteAfterPermission(
                onSuccess = {
                    Toast.makeText(context, "Metadata saved successfully!", Toast.LENGTH_SHORT).show()
                },
                onError = { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            )
        } else {
            // User denied permission
            musicViewModel.cancelPendingMetadataWrite()
            Toast.makeText(context, "Permission denied. Changes saved to library only.", Toast.LENGTH_LONG).show()
        }
    }
    
    // Import/Export related state
    var operationProgressText by remember { mutableStateOf("") }
    var operationError by remember { mutableStateOf<String?>(null) }
    var showExportResultDialog by remember { mutableStateOf(false) }
    var exportResultsData by remember { mutableStateOf<List<Pair<String, Boolean>>?>(null) }
    var showImportResultDialog by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<Pair<Int, String>?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    
    // Explorer reload trigger
    var explorerReloadTrigger by remember { mutableStateOf(0) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }
    val addToPlaylistSheetState = rememberModalBottomSheetState()
    val albumBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    
    // Multi-selection state
    val multiSelectionState = remember { chromahub.rhythm.app.features.local.presentation.viewmodel.MultiSelectionStateHolder() }
    val selectedSongs by multiSelectionState.selectedSongs.collectAsState()
    val isSelectionMode by multiSelectionState.isSelectionMode.collectAsState()
    val selectedSongIds by multiSelectionState.selectedSongIds.collectAsState()
    var showMultiSelectionSheet by remember { mutableStateOf(false) }
    var showBatchEditSheet by remember { mutableStateOf(false) }
    
    // Multi-selection callbacks
    val onSongLongPress: (Song) -> Unit = remember(multiSelectionState) {
        { song -> multiSelectionState.toggleSelection(song) }
    }
    
    val onSongSelectionToggle: (Song) -> Unit = remember(multiSelectionState) {
        { song -> multiSelectionState.toggleSelection(song) }
    }
    
    val favoriteSongs by musicViewModel.favoriteSongs.collectAsState()
    
    // TopAppBar scroll behavior
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    
    // FAB visibility based on scroll
    val fabVisibility by remember {
        derivedStateOf {
            scrollBehavior.state.collapsedFraction < 0.5f
        }
    }

    // FAB menu state
    var showPlaylistFabMenu by remember { mutableStateOf(false) }

    BackHandler(showPlaylistFabMenu) {
        showPlaylistFabMenu = false
    }

    // Handle FAB menu item clicks - close menu after action
    val onCreatePlaylistFromFab: () -> Unit = {
        showCreatePlaylistDialog = true
    }

    val onImportPlaylistFromFab: (() -> Unit)? = if (onImportPlaylist != null) {
        {
            showImportDialog = true
        }
    } else null

    val onExportPlaylistsFromFab: (() -> Unit)? = if (onExportAllPlaylists != null) {
        {
            showBulkExportDialog = true
        }
    } else null

    // Sync tabs with pager - only animate when tab button is clicked
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex != pagerState.currentPage) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }
    }
    


    // Update selectedTabIndex when pager settles on a new page (handles swiping)
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress && selectedTabIndex != pagerState.currentPage) {
            selectedTabIndex = pagerState.currentPage
            // Auto-scroll tab buttons to show selected tab
            tabRowState.animateScrollToItem(pagerState.currentPage)
        }
    }

    // Handle dialogs
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onConfirm = { name ->
                onCreatePlaylist(name)
                showCreatePlaylistDialog = false
            }
        )
    }
    
    if (showSongInfoSheet && selectedSong != null) {
        // Get the latest version of the song from the songs list
        val displaySong = songs.find { it.id == selectedSong!!.id } ?: selectedSong
        
        SongInfoBottomSheet(
            song = displaySong!!,
            onDismiss = { showSongInfoSheet = false },
            appSettings = appSettings,
            onEditSong = { title, artist, album, genre, year, trackNumber, artworkUri, removeArtwork ->
                // Use the ViewModel's new metadata saving function with callbacks
                musicViewModel.saveMetadataChanges(
                    song = displaySong!!,
                    title = title,
                    artist = artist,
                    album = album,
                    genre = genre,
                    year = year,
                    trackNumber = trackNumber,
                    artworkUri = artworkUri,
                    removeArtwork = removeArtwork,
                    onSuccess = { fileWriteSucceeded ->
                        if (fileWriteSucceeded) {
                            Toast.makeText(context, "Metadata saved successfully to file!", Toast.LENGTH_SHORT).show()
                        } else {
                            // Don't show error here - permission request may be triggered
                        }
                        // Don't close the sheet - let the user see the updated info
                    },
                    onError = { errorMessage ->
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    },
                    onPermissionRequired = { pendingRequest ->
                        // Launch the system permission dialog for Android 11+
                        try {
                            val intentSenderRequest = androidx.activity.result.IntentSenderRequest.Builder(
                                pendingRequest.intentSender
                            ).build()
                            writePermissionLauncher.launch(intentSenderRequest)
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Failed to request permission: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            musicViewModel.cancelPendingMetadataWrite()
                        }
                    }
                )
            }
        )
    }
    
    // Use bottom sheet instead of dialog
    if (showAddToPlaylistSheet && selectedSong != null) {
        AddToPlaylistBottomSheet(
            song = selectedSong!!,
            playlists = playlists,
            onDismissRequest = { showAddToPlaylistSheet = false },
            onAddToPlaylist = { playlist ->
                onAddSongToPlaylist(selectedSong!!, playlist.id)
                scope.launch {
                    addToPlaylistSheetState.hide()
                }.invokeOnCompletion {
                    if (!addToPlaylistSheetState.isVisible) {
                        showAddToPlaylistSheet = false
                    }
                }
            },
            onCreateNewPlaylist = {
                scope.launch {
                    addToPlaylistSheetState.hide()
                }.invokeOnCompletion {
                    if (!addToPlaylistSheetState.isVisible) {
                        showAddToPlaylistSheet = false
                        showCreatePlaylistDialog = true
                    }
                }
            },
            sheetState = addToPlaylistSheetState
        )
    }
    
    // Album bottom sheet
    if (showAlbumBottomSheet && selectedAlbum != null) {
        AlbumBottomSheet(
            album = selectedAlbum!!,
            onDismiss = { showAlbumBottomSheet = false },
            onSongClick = onSongClick,
            onPlayAll = { songs ->
                // Play the sorted album songs using proper queue replacement
                if (songs.isNotEmpty()) {
                    onPlayQueue(songs) // Use the new queue replacement callback
                } else {
                    selectedAlbum?.let { onAlbumClick(it) }
                }
            },
            onShufflePlay = { songs ->
                // Play shuffled sorted album songs with proper queue replacement
                if (songs.isNotEmpty()) {
                    onShuffleQueue(songs) // Use the new shuffle queue callback
                } else {
                    selectedAlbum?.let { onAlbumShufflePlay(it) }
                }
            },
            onAddToQueue = onAddToQueue,
            onAddSongToPlaylist = { song ->
                selectedSong = song
                scope.launch {
                    albumBottomSheetState.hide()
                }.invokeOnCompletion {
                    if (!albumBottomSheetState.isVisible) {
                        showAlbumBottomSheet = false
                        showAddToPlaylistSheet = true
                    }
                }
            },
            onPlayerClick = onPlayerClick,
            sheetState = albumBottomSheetState,
            haptics = haptics,
            onPlayNext = { song -> musicViewModel.playNext(song) },
            onToggleFavorite = { song -> musicViewModel.toggleFavorite(song) },
            favoriteSongs = musicViewModel.favoriteSongs.collectAsState().value,
            onShowSongInfo = { song ->
                selectedSong = song
                showSongInfoSheet = true
            },
            onAddToBlacklist = { song ->
                appSettings.addToBlacklist(song.id)
            },
            currentSong = currentSong,
            isPlaying = isPlaying
        )
    }
    
    // Playlist Management now handled in Tuner > Playlists settings
    
    // Track library refreshing state for pull-to-refresh
    val isLibraryRefreshing by musicViewModel.isLibraryRefreshing.collectAsState()
    val scanProgress by musicViewModel.scanProgress.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }
    val isTabletLayout = LocalConfiguration.current.screenWidthDp >= 600
    val baseLibraryBottomPadding =
        if (isTabletLayout) 16.dp else (MusicDimensions.bottomNavigationHeight + 16.dp)
    val libraryBottomOverlayPadding = baseLibraryBottomPadding
    
    // Update refreshing state based on library refreshing
    LaunchedEffect(isLibraryRefreshing) {
        isRefreshing = isLibraryRefreshing
    }
    
    // BackHandler for selection mode
    BackHandler(enabled = isSelectionMode) {
        multiSelectionState.clearSelection()
    }
    
    // Multi-selection bottom sheet - only show when explicitly requested
    if (showMultiSelectionSheet && selectedSongs.isNotEmpty()) {
        MultiSelectionBottomSheet(
            selectedSongs = selectedSongs,
            favoriteSongIds = favoriteSongs.toSet(),
            onDismiss = {
                showMultiSelectionSheet = false
                multiSelectionState.clearSelection()
            },
            onPlayAll = {
                onPlayQueue(selectedSongs)
                multiSelectionState.clearSelection()
            },
            onAddToQueue = {
                selectedSongs.forEach { song -> onAddToQueue(song) }
                multiSelectionState.clearSelection()
            },
            onPlayNext = {
                selectedSongs.reversed().forEach { song -> musicViewModel.playNext(song) }
                multiSelectionState.clearSelection()
            },
            onAddToPlaylist = {
                // Open playlist picker with first selected song for now
                // In future, could support adding multiple songs
                selectedSong = selectedSongs.firstOrNull()
                showMultiSelectionSheet = false
                showAddToPlaylistSheet = true
            },
            onToggleLikeAll = { shouldLike ->
                selectedSongs.forEach { song ->
                    val isFavorited = favoriteSongs.contains(song.id)
                    if (shouldLike != isFavorited) {
                        musicViewModel.toggleFavorite(song)
                    }
                }
            },
            onAddToBlacklist = {
                // Add all selected songs to blacklist
                selectedSongs.forEach { song ->
                    appSettings.addToBlacklist(song.id)
                }
            },
            onBatchEditTags = {
                showMultiSelectionSheet = false
                showBatchEditSheet = true
            }
        )
    }

    // Batch edit tags sheet
    if (showBatchEditSheet && selectedSongs.isNotEmpty()) {
        BatchEditTagsSheet(
            selectedSongs = selectedSongs,
            onDismiss = {
                showBatchEditSheet = false
                multiSelectionState.clearSelection()
            },
            onSave = { artist, album, genre, year, artworkUri, removeArtwork ->
                musicViewModel.batchEditMetadata(
                    songs = selectedSongs,
                    artist = artist,
                    album = album,
                    genre = genre,
                    year = year,
                    artworkUri = artworkUri,
                    removeArtwork = removeArtwork,
                    onProgress = { _, _ -> },
                    onComplete = { successCount, failCount ->
                        showBatchEditSheet = false
                        multiSelectionState.clearSelection()
                        val msg = if (failCount == 0) "Updated $successCount songs"
                                  else "Updated $successCount songs, $failCount failed"
                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                Spacer(modifier = Modifier.height(5.dp)) // Add more padding before the header starts
                
                LargeTopAppBar(
                navigationIcon = { },
                title = {
                    val collapsedFraction = scrollBehavior.state.collapsedFraction
                    val fontSize = (24 + (32 - 24) * (1 - collapsedFraction)).sp // Interpolate between 24sp and 32sp

                    Text(
                        text = context.getString(R.string.library_title),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = fontSize
                        ),
                        modifier = Modifier.padding(start = 14.dp) // Adjust start padding for title
                    )
                },
                actions = {
                    // Tab-specific actions moved from section headers
                    when (visibleTabIds.getOrNull(selectedTabIndex)) {
                        "ALBUMS" -> {
                            // Enhanced Album view toggle
                            val albumViewType by appSettings.albumViewType.collectAsState()
                            
                            // Animation for button press
                            val buttonScale by animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "albumToggleScale"
                            )
                            
                            FilledTonalIconButton(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    val newViewType = if (albumViewType == AlbumViewType.LIST) AlbumViewType.GRID else AlbumViewType.LIST
                                    appSettings.setAlbumViewType(newViewType)
                                },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                modifier = Modifier
                                    .size(42.dp)
                                    .graphicsLayer {
                                        scaleX = buttonScale
                                        scaleY = buttonScale
                                    }
                            ) {
                                Icon(
                                    imageVector = if (albumViewType == AlbumViewType.LIST) RhythmIcons.GridView else MaterialSymbolIcon("view_list", filled = true),
                                    contentDescription = if (albumViewType == AlbumViewType.LIST) "Switch to Grid View" else "Switch to List View",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        "ARTISTS" -> {
                            // Enhanced Artist view toggle  
                            val artistViewType by appSettings.artistViewType.collectAsState()
                            
                            // Animation for button press
                            val buttonScale by animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "artistToggleScale"
                            )
                            
                            FilledTonalIconButton(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    val newViewType = if (artistViewType == ArtistViewType.LIST) ArtistViewType.GRID else ArtistViewType.LIST
                                    appSettings.setArtistViewType(newViewType)
                                },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                modifier = Modifier
                                    .size(42.dp)
                                    .graphicsLayer {
                                        scaleX = buttonScale
                                        scaleY = buttonScale
                                    }
                            ) {
                                Icon(
                                    imageVector = if (artistViewType == ArtistViewType.LIST) RhythmIcons.GridView else MaterialSymbolIcon("view_list", filled = true),
                                    contentDescription = if (artistViewType == ArtistViewType.LIST) "Switch to Grid View" else "Switch to List View",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        
                        "PLAYLISTS" -> {
                            // Enhanced Playlist view toggle
                            val playlistViewType by appSettings.playlistViewType.collectAsState()
                            
                            // Animation for button press
                            val buttonScale by animateFloatAsState(
                                targetValue = 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "playlistToggleScale"
                            )
                            
                            FilledTonalIconButton(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    val newViewType = if (playlistViewType == PlaylistViewType.LIST) PlaylistViewType.GRID else PlaylistViewType.LIST
                                    appSettings.setPlaylistViewType(newViewType)
                                },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                modifier = Modifier
                                    .size(42.dp)
                                    .graphicsLayer {
                                        scaleX = buttonScale
                                        scaleY = buttonScale
                                    }
                            ) {
                                Icon(
                                    imageVector = if (playlistViewType == PlaylistViewType.LIST) RhythmIcons.GridView else MaterialSymbolIcon("view_list", filled = true),
                                    contentDescription = if (playlistViewType == PlaylistViewType.LIST) "Switch to Grid View" else "Switch to List View",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        

                    }
                    
                    // Sort dropdown like AlbumBottomSheet (only show for Songs, Albums, and Playlists)
                    val currentTabId = visibleTabIds.getOrNull(selectedTabIndex)
                    if (currentTabId == "SONGS" || currentTabId == "ALBUMS") {
                        var showSortMenu by remember { mutableStateOf(false) }
                        var pendingSortOrder by remember { mutableStateOf<MusicViewModel.SortOrder?>(null) }
                        
                        // Clear pending sort order when actual sort order changes
                        LaunchedEffect(sortOrder) {
                            pendingSortOrder = null
                        }
                        
                        Box {
                        // Enhanced sort button
                        val sortButtonScale by animateFloatAsState(
                            targetValue = if (showSortMenu) 0.95f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "sortButtonScale"
                        )
                        
                        FilledTonalButton(
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                showSortMenu = true
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            modifier = Modifier.graphicsLayer {
                                scaleX = sortButtonScale
                                scaleY = sortButtonScale
                            }
                        ) {
                            Icon(
                                imageVector = RhythmIcons.Sort,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Sort order text
                            val sortText = when (sortOrder) {
                                MusicViewModel.SortOrder.TITLE_ASC, MusicViewModel.SortOrder.TITLE_DESC -> "Title"
                                MusicViewModel.SortOrder.ARTIST_ASC, MusicViewModel.SortOrder.ARTIST_DESC -> "Artist"
                                MusicViewModel.SortOrder.DATE_ADDED_ASC, MusicViewModel.SortOrder.DATE_ADDED_DESC -> "Date Added"
                                MusicViewModel.SortOrder.DATE_MODIFIED_ASC, MusicViewModel.SortOrder.DATE_MODIFIED_DESC -> "Date Modified"
                            }

                            Text(
                                text = sortText,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            val sortArrowIcon = when (sortOrder) {
                                MusicViewModel.SortOrder.TITLE_ASC, MusicViewModel.SortOrder.ARTIST_ASC, MusicViewModel.SortOrder.DATE_ADDED_ASC, MusicViewModel.SortOrder.DATE_MODIFIED_ASC -> RhythmIcons.ArrowUpward
                                MusicViewModel.SortOrder.TITLE_DESC, MusicViewModel.SortOrder.ARTIST_DESC, MusicViewModel.SortOrder.DATE_ADDED_DESC, MusicViewModel.SortOrder.DATE_MODIFIED_DESC -> RhythmIcons.ArrowDownward
                            }
                            
                            Icon(
                                imageVector = sortArrowIcon,
                                contentDescription = if (sortOrder.name.endsWith("_ASC")) "Ascending" else "Descending",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            MusicViewModel.SortOrder.values().forEach { order ->
                                val isSelected = (pendingSortOrder ?: sortOrder) == order
                                Surface(
                                    color = if (isSelected) 
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                                    else 
                                        Color.Transparent,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                text = when (order) {
                                                    MusicViewModel.SortOrder.TITLE_ASC, MusicViewModel.SortOrder.TITLE_DESC -> "Title"
                                                    MusicViewModel.SortOrder.ARTIST_ASC, MusicViewModel.SortOrder.ARTIST_DESC -> "Artist"
                                                    MusicViewModel.SortOrder.DATE_ADDED_ASC, MusicViewModel.SortOrder.DATE_ADDED_DESC -> "Date Added"
                                                    MusicViewModel.SortOrder.DATE_MODIFIED_ASC, MusicViewModel.SortOrder.DATE_MODIFIED_DESC -> "Date Modified"
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected)
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = when (order) {
                                                    MusicViewModel.SortOrder.TITLE_ASC, MusicViewModel.SortOrder.TITLE_DESC -> RhythmIcons.SortByAlpha
                                                    MusicViewModel.SortOrder.ARTIST_ASC, MusicViewModel.SortOrder.ARTIST_DESC -> RhythmIcons.ArtistFilled
                                                    MusicViewModel.SortOrder.DATE_ADDED_ASC, MusicViewModel.SortOrder.DATE_ADDED_DESC -> RhythmIcons.DateRange
                                                    MusicViewModel.SortOrder.DATE_MODIFIED_ASC, MusicViewModel.SortOrder.DATE_MODIFIED_DESC -> MaterialSymbolIcon("edit_calendar", filled = true)
                                                },
                                                contentDescription = null,
                                                tint = if (isSelected)
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        trailingIcon = {
                                            when (order) {
                                                MusicViewModel.SortOrder.TITLE_ASC, MusicViewModel.SortOrder.ARTIST_ASC, MusicViewModel.SortOrder.DATE_ADDED_ASC, MusicViewModel.SortOrder.DATE_MODIFIED_ASC -> {
                                                    Icon(
                                                        imageVector = RhythmIcons.ArrowUpward,
                                                        contentDescription = "Ascending",
                                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                MusicViewModel.SortOrder.TITLE_DESC, MusicViewModel.SortOrder.ARTIST_DESC, MusicViewModel.SortOrder.DATE_ADDED_DESC, MusicViewModel.SortOrder.DATE_MODIFIED_DESC -> {
                                                    Icon(
                                                        imageVector = RhythmIcons.ArrowDownward,
                                                        contentDescription = "Descending",
                                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                else -> {}
                                            }
                                        },
                                        onClick = {
                                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                            pendingSortOrder = order
                                            showSortMenu = false
                                            // Set the specific sort order instead of cycling
                                            if (sortOrder != order) {
                                                musicViewModel.setSortOrder(order)
                                            }
                                        },
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(
                                            textColor = if (isSelected) 
                                                MaterialTheme.colorScheme.onPrimaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                    
                    // Playlist Sort dropdown
                    if (currentTabId == "PLAYLISTS") {
                        val playlistSortOrderString by appSettings.playlistSortOrder.collectAsState()
                        val playlistSortOrder = try {
                            LibraryPlaylistSortOrder.valueOf(playlistSortOrderString)
                        } catch (e: Exception) {
                            LibraryPlaylistSortOrder.NAME_ASC
                        }
                        var showPlaylistSortMenu by remember { mutableStateOf(false) }
                        
                        Box {
                            // Enhanced sort button
                            val sortButtonScale by animateFloatAsState(
                                targetValue = if (showPlaylistSortMenu) 0.95f else 1f,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "playlistSortButtonScale"
                            )
                            
                            FilledTonalButton(
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    showPlaylistSortMenu = true
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                modifier = Modifier.graphicsLayer {
                                    scaleX = sortButtonScale
                                    scaleY = sortButtonScale
                                }
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Sort,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Sort order text
                                val sortText = when (playlistSortOrder) {
                                    LibraryPlaylistSortOrder.NAME_ASC, LibraryPlaylistSortOrder.NAME_DESC -> context.getString(R.string.sort_name)
                                    LibraryPlaylistSortOrder.DATE_CREATED_ASC, LibraryPlaylistSortOrder.DATE_CREATED_DESC -> context.getString(R.string.sort_date_created)
                                    LibraryPlaylistSortOrder.SONG_COUNT_ASC, LibraryPlaylistSortOrder.SONG_COUNT_DESC -> context.getString(R.string.sort_song_count)
                                }

                                Text(
                                    text = sortText,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                Spacer(modifier = Modifier.width(4.dp))
                                
                                val sortArrowIcon = if (playlistSortOrder.name.endsWith("_ASC")) RhythmIcons.ArrowUpward else RhythmIcons.ArrowDownward
                                
                                Icon(
                                    imageVector = sortArrowIcon,
                                    contentDescription = if (playlistSortOrder.name.endsWith("_ASC")) "Ascending" else "Descending",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            
                            DropdownMenu(
                                expanded = showPlaylistSortMenu,
                                onDismissRequest = { showPlaylistSortMenu = false },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                LibraryPlaylistSortOrder.values().forEach { order ->
                                    val isSelected = playlistSortOrder == order
                                    Surface(
                                        color = if (isSelected) 
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                                        else 
                                            Color.Transparent,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        DropdownMenuItem(
                                            text = { 
                                                Text(
                                                    text = when (order) {
                                                        LibraryPlaylistSortOrder.NAME_ASC, LibraryPlaylistSortOrder.NAME_DESC -> context.getString(R.string.sort_name)
                                                        LibraryPlaylistSortOrder.DATE_CREATED_ASC, LibraryPlaylistSortOrder.DATE_CREATED_DESC -> context.getString(R.string.sort_date_created)
                                                        LibraryPlaylistSortOrder.SONG_COUNT_ASC, LibraryPlaylistSortOrder.SONG_COUNT_DESC -> context.getString(R.string.sort_song_count)
                                                    },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected)
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    else
                                                        MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = when (order) {
                                                        LibraryPlaylistSortOrder.NAME_ASC, LibraryPlaylistSortOrder.NAME_DESC -> RhythmIcons.SortByAlpha
                                                        LibraryPlaylistSortOrder.DATE_CREATED_ASC, LibraryPlaylistSortOrder.DATE_CREATED_DESC -> RhythmIcons.DateRange
                                                        LibraryPlaylistSortOrder.SONG_COUNT_ASC, LibraryPlaylistSortOrder.SONG_COUNT_DESC -> RhythmIcons.MusicNote
                                                    },
                                                    contentDescription = null,
                                                    tint = if (isSelected)
                                                        MaterialTheme.colorScheme.onPrimaryContainer
                                                    else
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            },
                                            trailingIcon = {
                                                Icon(
                                                    imageVector = if (order.name.endsWith("_ASC")) RhythmIcons.ArrowUpward else RhythmIcons.ArrowDownward,
                                                    contentDescription = if (order.name.endsWith("_ASC")) "Ascending" else "Descending",
                                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            },
                                            onClick = {
                                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                                showPlaylistSortMenu = false
                                                if (playlistSortOrder != order) {
                                                    appSettings.setPlaylistSortOrder(order.name)
                                                }
                                            },
                                            colors = androidx.compose.material3.MenuDefaults.itemColors(
                                                textColor = if (isSelected) 
                                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                scrollBehavior = scrollBehavior,
                modifier = Modifier.padding(horizontal = 8.dp) // Added padding
            )
            }
        },
        bottomBar = {},
        floatingActionButton = {
            // Only show FAB on playlists tab
            if (visibleTabIds.getOrNull(selectedTabIndex) == "PLAYLISTS") {
                PlaylistFabMenu(
                    visible = fabVisibility,
                    expanded = showPlaylistFabMenu,
                    onExpandedChange = { showPlaylistFabMenu = it },
                    onCreatePlaylist = onCreatePlaylistFromFab,
                    onImportPlaylist = onImportPlaylistFromFab,
                    onExportPlaylists = onExportPlaylistsFromFab,
                    bottomPadding = baseLibraryBottomPadding,
                    haptics = haptics // Pass haptics to PlaylistFabMenu
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Horizontal Scrollable Navigation Buttons - Stats screen style
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                LazyRow(
                    state = tabRowState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(
                        count = tabs.size,
                        key = { index -> tabOrder.getOrNull(index) ?: "tab_$index" }
                    ) { index ->
                        val isSelected = selectedTabIndex == index
                        
                        TabAnimation(
                            index = index,
                            selectedIndex = selectedTabIndex,
                            title = tabs[index],
                            selectedColor = MaterialTheme.colorScheme.primary,
                            onSelectedColor = MaterialTheme.colorScheme.onPrimary,
                            unselectedColor = MaterialTheme.colorScheme.surfaceContainer,
                            onUnselectedColor = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                selectedTabIndex = index
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                    tabRowState.animateScrollToItem(index)
                                }
                            },
                            modifier = Modifier.padding(all = 2.dp),
                            content = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Get the actual tab ID from the visible tabs list
                                    val currentTabId = visibleTabIds.getOrNull(index)
                                    Icon(
                                        imageVector = when (currentTabId) {
                                            "SONGS" -> RhythmIcons.Relax
                                            "PLAYLISTS" -> RhythmIcons.PlaylistFilled
                                            "ALBUMS" -> RhythmIcons.Music.Album
                                            "ARTISTS" -> RhythmIcons.Artist
                                            "EXPLORER" -> RhythmIcons.Folder
                                            else -> RhythmIcons.Music.Song
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = tabs[index],
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        )
                    }
                    
                    // Edit button at the end to open LibraryTabReorderBottomSheet
                    item {
                        var showLibraryTabOrderSheet by remember { mutableStateOf(false) }

                        TabAnimation(
                            index = tabs.size, // Use tabs.size as index since it's after all tabs
                            selectedIndex = -1, // Never selected
                            title = "Edit",
                            selectedColor = MaterialTheme.colorScheme.secondaryContainer,
                            onSelectedColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unselectedColor = MaterialTheme.colorScheme.surfaceContainer,
                            onUnselectedColor = MaterialTheme.colorScheme.onSurface,
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                showLibraryTabOrderSheet = true
                            },
                            modifier = Modifier.padding(all = 2.dp),
                            content = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = RhythmIcons.Edit,
                                        contentDescription = "Reorder tabs",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Edit",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        )

                        if (showLibraryTabOrderSheet) {
                            LibraryTabOrderBottomSheet(
                                onDismiss = { showLibraryTabOrderSheet = false },
                                appSettings = appSettings,
                                haptics = haptics
                            )
                        }
                    }
                }
            }
            
            // Background Processing Loader - shown between tabs and content
            val isBackgroundProcessing by musicViewModel.isBackgroundProcessing.collectAsState()
            val isMediaScanning by musicViewModel.isMediaScanning.collectAsState()
            val isGenreDetectionRunning by musicViewModel.isGenreDetectionRunning.collectAsState()
            val isFetchingArtwork by musicViewModel.isFetchingArtwork.collectAsState()
            val isExtractingMetadata by musicViewModel.isExtractingMetadata.collectAsState()
            
            AnimatedVisibility(
                visible = isBackgroundProcessing,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(),
                exit = shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 8.dp)
                ) {
                    // Wavy animated progress indicator for library screen with transparent track
                    androidx.compose.material3.LinearWavyProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        trackColor = Color.Transparent
                    )
                    
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    // Processing status text with animated icon
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(8.dp),
//                        modifier = Modifier.padding(start = 4.dp)
//                    ) {
//                        // Animated status icon
//                        val infiniteTransition = rememberInfiniteTransition(label = "statusIconAnimation")
//                        val iconRotation by infiniteTransition.animateFloat(
//                            initialValue = 0f,
//                            targetValue = 360f,
//                            animationSpec = infiniteRepeatable(
//                                animation = tween(2000, easing = LinearEasing),
//                                repeatMode = RepeatMode.Restart
//                            ),
//                            label = "iconRotation"
//                        )
//
//                        Icon(
//                            imageVector = when {
//                                isMediaScanning -> RhythmIcons.Refresh
//                                isExtractingMetadata -> MaterialSymbolIcon("analytics")
//                                isFetchingArtwork -> RhythmIcons.Image
//                                !isGenreDetectionRunning -> RhythmIcons.Category
//                                else -> MaterialSymbolIcon("sync")
//                            },
//                            contentDescription = null,
//                            
//                            modifier = Modifier
//                                .size(16.dp)
//                                .graphicsLayer {
//                                    rotationZ = iconRotation
//                                }
//                        )
//
//                        val statusText = remember(isMediaScanning, isGenreDetectionRunning, isFetchingArtwork, isExtractingMetadata) {
//                            when {
//                                isMediaScanning -> context.getString(R.string.scanning_media)
//                                isExtractingMetadata -> context.getString(R.string.extracting_metadata)
//                                isFetchingArtwork -> context.getString(R.string.fetching_artwork)
//                                !isGenreDetectionRunning -> context.getString(R.string.detecting_genres)
//                                else -> context.getString(R.string.processing_library)
//                            }
//                        }
//
//                        Text(
//                            text = statusText,
//                            style = MaterialTheme.typography.bodySmall.copy(
//                                fontWeight = FontWeight.Medium
//                            ),
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
                }
            }
            
            // Single Big Card Container with Pull-to-Refresh
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = libraryBottomOverlayPadding),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shadowElevation = 0.dp
            ) {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        // If on Explorer tab, reload explorer; otherwise, trigger media scan
                        if (visibleTabIds.getOrNull(selectedTabIndex) == "EXPLORER") {
                            explorerReloadTrigger++
                        } else {
                            onRefreshClick()
                        }
                    },
                    state = pullToRefreshState,
                    modifier = Modifier.fillMaxSize(),
                    indicator = {
                        PullToRefreshDefaults.LoadingIndicator(
                            state = pullToRefreshState,
                            isRefreshing = isRefreshing,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Content with animation
                        HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp)
                ) { page ->
                    // Dynamically show tab content based on visible tab order (filtered)
                    when (visibleTabIds.getOrNull(page)) {
                        "SONGS" -> {
                            // Sort songs according to current sort order
                            val sortedSongs = remember(songs, sortOrder) {
                                when (sortOrder) {
                                    MusicViewModel.SortOrder.TITLE_ASC -> songs.sortedBy { it.title.lowercase() }
                                    MusicViewModel.SortOrder.TITLE_DESC -> songs.sortedByDescending { it.title.lowercase() }
                                    MusicViewModel.SortOrder.ARTIST_ASC -> songs.sortedBy { it.artist.lowercase() }
                                    MusicViewModel.SortOrder.ARTIST_DESC -> songs.sortedByDescending { it.artist.lowercase() }
                                    MusicViewModel.SortOrder.DATE_ADDED_ASC -> songs.sortedBy { it.dateAdded }
                                    MusicViewModel.SortOrder.DATE_ADDED_DESC -> songs.sortedByDescending { it.dateAdded }
                                    MusicViewModel.SortOrder.DATE_MODIFIED_ASC -> songs.sortedBy { it.dateModified }
                                    MusicViewModel.SortOrder.DATE_MODIFIED_DESC -> songs.sortedByDescending { it.dateModified }
                                }
                            }
                            SingleCardSongsContent(
                            songs = sortedSongs,
                            albums = albums,
                            artists = artists,
                            onSongClick = onSongClick,
                            onAddToPlaylist = { song ->
                                selectedSong = song
                                showAddToPlaylistSheet = true
                            },
                            onAddToQueue = onAddToQueue,
                            onPlayNext = { song -> musicViewModel.playNext(song) },
                            onToggleFavorite = { song -> musicViewModel.toggleFavorite(song) },
                            favoriteSongs = musicViewModel.favoriteSongs.collectAsState().value,
                            onGoToArtist = onArtistClick,
                            onGoToAlbum = onAlbumClick,
                            onShowSongInfo = { song ->
                                selectedSong = song
                                showSongInfoSheet = true
                            },
                            onAddToBlacklist = { song ->
                                appSettings.addToBlacklist(song.id)
                            },
                            onPlayQueue = onPlayQueue,
                            onPlayQueueFromIndex = onPlayQueueFromIndex,
                            onShuffleQueue = onShuffleQueue,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            haptics = haptics,
                            enableRatingSystem = enableRatingSystem,
                            isSelectionMode = isSelectionMode,
                            selectedSongIds = selectedSongIds,
                            multiSelectionState = multiSelectionState,
                            onSongLongPress = onSongLongPress,
                            onSongSelectionToggle = onSongSelectionToggle,
                            onShowMultiSelectionSheet = { showMultiSelectionSheet = true },
                            onRefreshClick = onRefreshClick
                        )
                        }
                        "PLAYLISTS" -> SingleCardPlaylistsContent(
                            playlists = playlists,
                            onPlaylistClick = onPlaylistClick,
                            haptics = haptics,
                            onCreatePlaylist = { showCreatePlaylistDialog = true },
                            onImportPlaylist = { showImportDialog = true },
                            onExportPlaylists = { showBulkExportDialog = true },
                            appSettings = appSettings,
                            onRefreshClick = onRefreshClick
                        )
                        "ALBUMS" -> SingleCardAlbumsContent(
                            albums = albums,
                            onAlbumClick = onAlbumClick,
                            onSongClick = onSongClick,
                            onAlbumBottomSheetClick = { album ->
                                selectedAlbum = album
                                showAlbumBottomSheet = true
                            },
                            haptics = haptics,
                            appSettings = appSettings,
                            onPlayQueue = onPlayQueue,
                            onShuffleQueue = onShuffleQueue,
                            onRefreshClick = onRefreshClick
                        )
                        "ARTISTS" -> SingleCardArtistsContent(
                            artists = artists,
                            onArtistClick = { artist ->
                                onNavigateToArtist(artist)
                            },
                            haptics = haptics,
                            onPlayQueue = onPlayQueue,
                            onShuffleQueue = onShuffleQueue,
                            onRefreshClick = onRefreshClick
                        )
                        "EXPLORER" -> SingleCardExplorerContent(
                            songs = songs,
                            onSongClick = onSongClick,
                            onAddToPlaylist = { song ->
                                selectedSong = song
                                showAddToPlaylistSheet = true
                            },
                            onAddToQueue = onAddToQueue,
                            onShowSongInfo = { song ->
                                selectedSong = song
                                showSongInfoSheet = true
                            },
                            onPlayQueue = onPlayQueue,
                            onPlayQueueFromIndex = onPlayQueueFromIndex,
                            onShuffleQueue = onShuffleQueue,
                            haptics = haptics,
                            appSettings = appSettings,
                            reloadTrigger = explorerReloadTrigger,
                            onCreatePlaylist = onCreatePlaylist,
                            musicViewModel = musicViewModel,
                            currentSong = currentSong,
                            isPlaying = isPlaying,
                            enableRatingSystem = enableRatingSystem
                        )
                    }
                }
                        
                        // Show media scanning progress overlay on top
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isMediaScanning,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Animated scanning icon
                                    val infiniteTransition = rememberInfiniteTransition(label = "scanIconRotation")
                                    val rotation by infiniteTransition.animateFloat(
                                        initialValue = 0f,
                                        targetValue = 360f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(2000, easing = LinearEasing),
                                            repeatMode = RepeatMode.Restart
                                        ),
                                        label = "rotation"
                                    )
                                    
                                    Icon(
                                        imageVector = RhythmIcons.Refresh,
                                        contentDescription = "Scanning",
                                        modifier = Modifier
                                            .size(24.dp)
                                            .graphicsLayer { rotationZ = rotation }
                                    )
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = when (scanProgress.stage) {
                                                "Songs" -> "Scanning songs..."
                                                "Albums" -> "Processing albums..."
                                                "Artists" -> "Processing artists..."
                                                "Genres" -> "Detecting genres..."
                                                else -> "Scanning media..."
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        if (scanProgress.total > 0) {
                                            Text(
                                                text = "${scanProgress.current} / ${scanProgress.total}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                    
                                    // Show circular progress indicator
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Playlist import/export dialogs
    if (showBulkExportDialog && onExportAllPlaylists != null) {
        BulkPlaylistExportDialog(
            playlistCount = playlists.size,
            onDismiss = { 
                showBulkExportDialog = false
                operationError = null
            },
            onExport = { format, includeDefault ->
                showBulkExportDialog = false
                showOperationProgress = true
                operationProgressText = context.getString(R.string.exporting_playlists)
                
                onExportAllPlaylists(format, includeDefault, null) { result ->
                    showOperationProgress = false
                    result.fold(
                        onSuccess = { message ->
                            // Success will be shown via snackbar from navigation layer
                        },
                        onFailure = { error ->
                            operationError = error.message ?: "Export failed"
                        }
                    )
                }
            },
            onExportToCustomLocation = { format, includeDefault, directoryUri ->
                showBulkExportDialog = false
                showOperationProgress = true
                operationProgressText = context.getString(R.string.exporting_to_location)
                
                onExportAllPlaylists(format, includeDefault, directoryUri) { result ->
                    showOperationProgress = false
                    result.fold(
                        onSuccess = { message ->
                            // Success will be shown via snackbar from navigation layer
                        },
                        onFailure = { error ->
                            operationError = error.message ?: "Export failed"
                        }
                    )
                }
            }
        )
    }
    
    if (showImportDialog && onImportPlaylist != null) {
        PlaylistImportDialog(
            onDismiss = { 
                showImportDialog = false
                operationError = null
            },
            onImport = { uri, onResult, onRestartRequired ->
                showImportDialog = false
                showOperationProgress = true
                operationProgressText = context.getString(R.string.importing_playlist)
                onImportPlaylist(uri, { result ->
                    showOperationProgress = false
                    result.fold(
                        onSuccess = { message ->
                            operationResult = Pair(message, true)
                            showRestartDialog = true
                        },
                        onFailure = { error ->
                            operationError = error.message ?: "Import failed"
                        }
                    )
                    onResult(result)
                }, onRestartRequired)
            }
        )
    }

    // App Restart Dialog
    if (showRestartDialog && onRestartApp != null) {
        AppRestartDialog(
            onDismiss = { showRestartDialog = false },
            onRestart = {
                showRestartDialog = false
                onRestartApp()
            },
            onContinue = {
                showRestartDialog = false
                // Continue without restart
            }
        )
    }

    // Progress dialog for long operations
    if (showOperationProgress) {
        PlaylistOperationProgressDialog(
            operation = operationProgressText,
            onDismiss = {
                showOperationProgress = false
                operationProgressText = ""
            }
        )
    }
    
    // Simple success/error dialogs for now
    if (operationError != null) {
        AlertDialog(
            onDismissRequest = { operationError = null },
            icon = {
                Icon(
                    imageVector = MaterialSymbolIcon("error", filled = true),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Error") },
            text = { Text(operationError!!) },
            confirmButton = {
                Button(onClick = { operationError = null }) {
                    Icon(
                        imageVector = RhythmIcons.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("OK")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Dialog to show import result and offer restart
    if (showImportResultDialog && importResult != null) {
        AlertDialog(
            onDismissRequest = { showImportResultDialog = false; importResult = null },
            icon = {
                Icon(
                    imageVector = MaterialSymbolIcon("restart_alt", filled = true),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Import Complete") },
            text = {
                val (count, message) = importResult!!
                Text("Successfully imported $count playlists.\n$message\n\nRestart the app to apply changes.")
            },
            confirmButton = {
                Button(onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    showImportResultDialog = false
                    importResult = null
                    AppRestarter.restartApp(context)
                }) {
                    Icon(
                        imageVector = MaterialSymbolIcon("restart_alt", filled = true),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restart App")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    showImportResultDialog = false
                    importResult = null
                }) {
                    Icon(
                        imageVector = RhythmIcons.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Later")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun SingleCardSongsContent(
    songs: List<Song>,
    albums: List<Album> = emptyList(),
    artists: List<Artist> = emptyList(),
    onSongClick: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onPlayNext: (Song) -> Unit = {},
    onToggleFavorite: (Song) -> Unit = {},
    favoriteSongs: Set<String> = emptySet(),
    onGoToArtist: (Artist) -> Unit = {},
    onGoToAlbum: (Album) -> Unit = {},
    onShowSongInfo: (Song) -> Unit,
    onAddToBlacklist: (Song) -> Unit,
    onPlayQueue: (List<Song>) -> Unit = { _ -> },
    onPlayQueueFromIndex: (List<Song>, Int) -> Unit = { _, _ -> }, // New parameter for playing from specific index
    onShuffleQueue: (List<Song>) -> Unit = { _ -> },
    currentSong: Song? = null, // Add current song parameter
    isPlaying: Boolean = false, // Add playing state
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    enableRatingSystem: Boolean = true,
    // Multi-selection parameters
    isSelectionMode: Boolean = false,
    selectedSongIds: Set<String> = emptySet(),
    multiSelectionState: chromahub.rhythm.app.features.local.presentation.viewmodel.MultiSelectionStateHolder? = null,
    onSongLongPress: (Song) -> Unit = {},
    onSongSelectionToggle: (Song) -> Unit = {},
    onShowMultiSelectionSheet: () -> Unit = {},
    onRefreshClick: (() -> Unit)? = null,
    songMenuContent: (@Composable (song: Song, dismissMenu: () -> Unit) -> Unit)? = null
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings.getInstance(context) }
    val groupByAlbumArtist by appSettings.groupByAlbumArtist.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    
    // Multi-selection state - get selected songs from state holder
    val selectedSongs = multiSelectionState?.selectedSongs?.collectAsState()?.value ?: emptyList()
    
    // Loading state for async category computation
    var isLoading by remember { mutableStateOf(true) }
    var preparedSongs by remember { mutableStateOf(songs) }
    var categories by remember { mutableStateOf<List<String>>(listOf("All")) }
    
    // Helper function to split artist names
    val splitArtistNames: (String) -> List<String> = remember {
        { artistName ->
            // Character-level delimiters from settings
            val libAppSettings = AppSettings.getInstance(context)
            val artistSeparatorEnabled = libAppSettings.artistSeparatorEnabled.value
            val charDelimiters = if (artistSeparatorEnabled) {
                libAppSettings.artistSeparatorDelimiters.value.toList().map { it.toString() }
            } else emptyList()

            if (charDelimiters.isEmpty()) {
                listOf(artistName.trim()).filter { it.isNotBlank() }
            } else {
                val selectedDelimiterChars = charDelimiters.mapNotNull { it.firstOrNull() }.toSet()
                val wordSeparators = mutableListOf<String>().apply {
                    if (selectedDelimiterChars.contains('&')) add(" & ")
                    add(" and ")
                    if (selectedDelimiterChars.contains(',')) add(", ")
                    add(" feat. ")
                    add(" feat ")
                    add(" ft. ")
                    add(" ft ")
                    add(" featuring ")
                    add(" x ")
                    add(" X ")
                    add(" vs ")
                    add(" vs. ")
                    add(" with ")
                }

                var names = listOf(artistName)
                for (delimiter in charDelimiters) {
                    names = names.flatMap { it.split(delimiter) }
                }
                for (separator in wordSeparators) {
                    names = names.flatMap { it.split(separator, ignoreCase = true) }
                }
                names.map { it.trim() }.filter { it.isNotBlank() }
            }
        }
    }
    
    // Cache for audio quality detection to avoid re-computation
    val audioQualityCache = remember { mutableMapOf<String, AudioQualityDetector.AudioQuality>() }
    
    // Helper function to get or compute audio quality using AudioFormatDetector and AudioQualityDetector
    // This matches the same logic used in AudioQualityBadges for consistent quality detection
    suspend fun getAudioQuality(song: Song): AudioQualityDetector.AudioQuality {
        // Check cache first
        audioQualityCache[song.id]?.let { return it }
        
        return withContext(Dispatchers.IO) {
            try {
                // Use AudioFormatDetector for accurate codec and format detection
                val formatInfo = AudioFormatDetector.detectFormat(context, song.uri, song)

                val songBitrate = song.bitrate ?: 0
                val songSampleRate = song.sampleRate ?: 0
                val songChannels = song.channels ?: 0
                
                // Prefer Song's metadata when available (more reliable)
                val bitrateKbps = if (songBitrate > 0) {
                    songBitrate / 1000
                } else if (formatInfo.bitrateKbps > 0) {
                    formatInfo.bitrateKbps
                } else {
                    0
                }
                
                val sampleRateHz = if (songSampleRate > 0) {
                    songSampleRate
                } else if (formatInfo.sampleRateHz > 0) {
                    formatInfo.sampleRateHz
                } else {
                    0
                }
                
                val channelCount = if (songChannels > 0) {
                    songChannels
                } else if (formatInfo.channelCount > 0) {
                    formatInfo.channelCount
                } else {
                    2
                }
                
                val codec = formatInfo.codec.ifEmpty { song.codec ?: "Unknown" }
                val bitDepth = formatInfo.bitDepth
                
                // Use AudioQualityDetector for accurate quality classification
                val quality = AudioQualityDetector.detectQuality(
                    codec = codec,
                    sampleRateHz = sampleRateHz,
                    bitrateKbps = bitrateKbps,
                    bitDepth = bitDepth,
                    channelCount = channelCount
                )
                
                // Cache the result
                audioQualityCache[song.id] = quality
                quality
            } catch (e: Exception) {
                android.util.Log.w("SongsTab", "Error detecting audio quality for ${song.title}: ${e.message}")
                // Return a default quality on error
                AudioQualityDetector.AudioQuality(
                    qualityType = AudioQualityDetector.QualityType.UNKNOWN,
                    isLossless = false,
                    isDolby = false,
                    isDTS = false,
                    isHiRes = false,
                    qualityLabel = "Unknown",
                    qualityDescription = "Quality could not be determined",
                    bitDepthEstimate = 0,
                    category = "Unknown"
                )
            }
        }
    }
    
    // Synchronous helper functions for filtering (use cached metadata when possible, fallback to fresh extraction)
    fun isLosslessAudio(song: Song): Boolean {
        val codec = song.codec?.uppercase() ?: ""

        // If we have cached codec data, use it
        if (codec.isNotEmpty()) {
            // Check if it's explicitly a LOSSY codec - these are NEVER lossless
            val isLossyCodec = codec.contains("MP3") || codec.contains("AAC") ||
                              codec.contains("OGG") || codec.contains("OPUS") ||
                              codec.contains("VORBIS") || (codec.contains("WMA") && !codec.contains("LOSSLESS"))

            if (isLossyCodec) return false

            // Check if it's explicitly a LOSSLESS codec
            val isLosslessCodec = codec in listOf("ALAC", "FLAC", "PCM", "WAV", "APE", "DSD", "TRUEHD", "DOLBY ATMOS", "DTS-HD MA", "AIFF", "WV", "TAK", "TTA") ||
                                 codec.contains("LOSSLESS", ignoreCase = true) ||
                                 codec.contains("APPLE LOSSLESS", ignoreCase = true)

            if (isLosslessCodec) return true
        }

        // Fallback: Check file extension for known lossless formats
        val uri = song.uri.toString()
        val isLosslessExtension = uri.endsWith(".flac", ignoreCase = true) ||
                                  uri.endsWith(".wav", ignoreCase = true) ||
                                  uri.endsWith(".alac", ignoreCase = true) ||
                                  uri.endsWith(".ape", ignoreCase = true) ||
                                  uri.endsWith(".aiff", ignoreCase = true) ||
                                  uri.endsWith(".aif", ignoreCase = true) ||
                                  uri.endsWith(".dsd", ignoreCase = true) ||
                                  uri.endsWith(".wv", ignoreCase = true) ||
                                  uri.endsWith(".tta", ignoreCase = true) ||
                                  uri.endsWith(".tak", ignoreCase = true)

        if (isLosslessExtension) return true

        return false
    }

    fun isHiResLossless(song: Song): Boolean {
        if (!isLosslessAudio(song)) {
            android.util.Log.d("SongsTab", "Song ${song.title} is not lossless")
            return false
        }

        val sampleRate = song.sampleRate ?: 0
        val bitrate = song.bitrate ?: 0
        val channels = song.channels ?: 2

        // Hi-Res Lossless requires ≥48kHz sample rate
        if (sampleRate < 48000) {
            android.util.Log.d("SongsTab", "Song ${song.title} sample rate $sampleRate < 48000, not Hi-Res")
            return false
        }

        // For known Hi-Res sample rates, consider them Hi-Res even without bitrate calculation
        if (sampleRate >= 88200) {
            android.util.Log.d("SongsTab", "Song ${song.title} has Hi-Res sample rate $sampleRate")
            return true // 88.2kHz, 96kHz, 176.4kHz, 192kHz etc.
        }

        // Calculate bit depth using improved AudioFormatDetector logic
        if (bitrate > 0 && sampleRate > 0 && channels > 0) {
            val bitrateKbps = bitrate / 1000
            val calculatedBitDepth = (bitrateKbps * 1000) / (sampleRate * channels)
            // Use AudioFormatDetector thresholds: >= 20 bits/sample = 24-bit
            // But be more lenient for Hi-Res detection
            android.util.Log.d("SongsTab", "Song ${song.title} bit depth calculation: bitrate=${bitrateKbps}kbps, sampleRate=$sampleRate, channels=$channels, calculatedBitDepth=$calculatedBitDepth")
            if (calculatedBitDepth >= 18) {
                android.util.Log.d("SongsTab", "Song ${song.title} qualifies as Hi-Res with bit depth $calculatedBitDepth")
                return true // Allow some margin for calculation errors
            }
        }

        // Fallback: High bitrate lossless at 48kHz or higher is likely Hi-Res
        if (bitrate >= 2000000 && sampleRate >= 48000) {
            android.util.Log.d("SongsTab", "Song ${song.title} qualifies as Hi-Res with high bitrate $bitrate")
            return true // 2Mbps+ at 48kHz+
        }

        android.util.Log.d("SongsTab", "Song ${song.title} does not qualify as Hi-Res")
        return false
    }
    
    fun isRegularLossless(song: Song): Boolean {
        // Regular Lossless = Lossless but NOT Hi-Res
        // This ensures mutual exclusivity between "Lossless" and "Hi-Res Lossless" filters
        val lossless = isLosslessAudio(song)
        if (!lossless) return false
        
        // IMPORTANT: Check if it's Hi-Res and exclude it
        val hiRes = isHiResLossless(song)
        if (hiRes) {
            android.util.Log.d("SongsTab", "Song ${song.title} is Hi-Res, excluding from Regular Lossless")
            return false
        }
        
        android.util.Log.d("SongsTab", "Song ${song.title} qualifies as Regular Lossless (not Hi-Res)")
        return true
    }

    fun isDolbyOrSurround(song: Song): Boolean {
        val codec = song.codec?.uppercase() ?: ""
        return (song.channels ?: 2) > 2 || // Multi-channel audio
               codec.contains("AC-3") ||
               codec.contains("E-AC-3") ||
               codec.contains("DOLBY") ||
               codec.contains("TRUEHD") ||
               codec.contains("ATMOS") ||
               codec.contains("DTS")
    }

    // Async category computation to avoid blocking UI on tab switch
    LaunchedEffect(songs, favoriteSongs, enableRatingSystem) {
        isLoading = true
        val result = withContext(Dispatchers.Default) {
            val allCategories = mutableListOf("All")

            android.util.Log.d("SongsTab", "Recomputing categories for ${songs.size} songs")

// Favorites filter - show if there are any favorite songs
        val favoriteSongsList = songs.filter { it.id in favoriteSongs }
        if (favoriteSongsList.isNotEmpty()) {
            allCategories.add("❤️ Favorites")
            }

            // Audio Quality Filters (Mutually Exclusive) - Most specific first

            // Hi-Res Lossless (≥48 kHz + 24-bit lossless)
            val hiResLosslessSongs = songs.filter { isHiResLossless(it) && !isDolbyOrSurround(it) }
            android.util.Log.d("SongsTab", "Found ${hiResLosslessSongs.size} Hi-Res Lossless songs")
            if (hiResLosslessSongs.isNotEmpty()) allCategories.add("Hi-Res Lossless")

            // Regular Lossless (CD quality: 44.1kHz/16-bit or 48kHz/16-bit)
            val regularLosslessSongs = songs.filter { isRegularLossless(it) && !isDolbyOrSurround(it) }
            android.util.Log.d("SongsTab", "Found ${regularLosslessSongs.size} Lossless (CD Quality) songs")
            if (regularLosslessSongs.isNotEmpty()) allCategories.add("Lossless")

            // Dolby (includes AC-3, E-AC-3/D+, TrueHD, Atmos, DTS in 5.1, 7.1, etc.)
            val dolbySongs = songs.filter { isDolbyOrSurround(it) }
            android.util.Log.d("SongsTab", "Found ${dolbySongs.size} Dolby/Surround songs")
            if (dolbySongs.isNotEmpty()) allCategories.add("Dolby")
            
            // Stereo (standard 2-channel, non-quality filtered) - HIDDEN per user request
            val stereoSongs = songs.filter { song ->
                (song.channels ?: 2) == 2 && !isDolbyOrSurround(song)
            }
            android.util.Log.d("SongsTab", "Found ${stereoSongs.size} Stereo songs")
            // if (stereoSongs.isNotEmpty()) allCategories.add("Stereo")  // HIDDEN - user requested to hide stereo filter
            
            val monoSongs = songs.filter { song ->
                (song.channels ?: 2) == 1
            }
            android.util.Log.d("SongsTab", "Found ${monoSongs.size} Mono songs")
            if (monoSongs.isNotEmpty()) allCategories.add("Mono")
            
            // Log sample metadata for debugging
            if (songs.isNotEmpty()) {
                val sampleSong = songs.first()
                android.util.Log.d("SongsTab", "Sample song metadata: ${sampleSong.title} - bitrate=${sampleSong.bitrate}, sampleRate=${sampleSong.sampleRate}, channels=${sampleSong.channels}, codec=${sampleSong.codec}")
            }

            // Rating-based categories (5★ = Absolute Favorite, 4★ = Loved, 3★ = Great, 2★ = Good, 1★ = Liked)
            // Only show if rating system is enabled
            if (enableRatingSystem) {
                val appSettings = chromahub.rhythm.app.shared.data.model.AppSettings.getInstance(context)
                val ratingDistribution = appSettings.getRatingDistribution()
                
                if ((ratingDistribution[5] ?: 0) > 0) {
                    allCategories.add("⭐⭐⭐⭐⭐ Absolute Favorites")
                }
                if ((ratingDistribution[4] ?: 0) > 0) {
                    allCategories.add("⭐⭐⭐⭐ Loved")
                }
                if ((ratingDistribution[3] ?: 0) > 0) {
                    allCategories.add("⭐⭐⭐ Great")
                }
                if ((ratingDistribution[2] ?: 0) > 0) {
                    allCategories.add("⭐⭐ Good")
                }
                if ((ratingDistribution[1] ?: 0) > 0) {
                    allCategories.add("⭐ Liked")
                }
            }

            // Quality-based categories for lossy audio
            val highQualitySongs = songs.filter { song ->
                val bitrate = song.bitrate ?: 0
                bitrate >= 320000 && !isLosslessAudio(song) && !isDolbyOrSurround(song)
            }
            if (highQualitySongs.isNotEmpty()) allCategories.add("High Quality")

            val standardSongs = songs.filter { song ->
                val bitrate = song.bitrate ?: 0
                bitrate in 128000..319999 && !isLosslessAudio(song) && !isDolbyOrSurround(song)
            }
            if (standardSongs.isNotEmpty()) allCategories.add("Standard")

            // Duration-based categories
            val shortSongs = songs.filter { it.duration < 3 * 60 * 1000 }
            if (shortSongs.isNotEmpty()) allCategories.add("Short (< 3 min)")

            val mediumSongs = songs.filter { it.duration in (3 * 60 * 1000)..(5 * 60 * 1000) }
            if (mediumSongs.isNotEmpty()) allCategories.add("Medium (3-5 min)")

            val longSongs = songs.filter { it.duration > 5 * 60 * 1000 }
            if (longSongs.isNotEmpty()) allCategories.add("Long (> 5 min)")

            allCategories
        }
        categories = result
        preparedSongs = songs
        isLoading = false
    }

    // Filter songs based on selected category - computed asynchronously
    // Initialize with all songs (not emptyList) so LazyListState can restore scroll position correctly
    var filteredSongs by remember { mutableStateOf<List<Song>>(songs) }
    
    LaunchedEffect(preparedSongs, selectedCategory, favoriteSongs) {
        filteredSongs = withContext(Dispatchers.Default) {
            when (selectedCategory) {
                "All" -> preparedSongs
                "❤️ Favorites" -> preparedSongs.filter { it.id in favoriteSongs }
                
                // Rating-based filters
                "⭐⭐⭐⭐⭐ Absolute Favorites" -> {
                    val ratedSongIds = chromahub.rhythm.app.shared.data.model.AppSettings.getInstance(context).getSongsByRating(5)
                    preparedSongs.filter { it.id in ratedSongIds }
                }
                "⭐⭐⭐⭐ Loved" -> {
                    val ratedSongIds = chromahub.rhythm.app.shared.data.model.AppSettings.getInstance(context).getSongsByRating(4)
                    preparedSongs.filter { it.id in ratedSongIds }
                }
                "⭐⭐⭐ Great" -> {
                    val ratedSongIds = chromahub.rhythm.app.shared.data.model.AppSettings.getInstance(context).getSongsByRating(3)
                    preparedSongs.filter { it.id in ratedSongIds }
                }
                "⭐⭐ Good" -> {
                    val ratedSongIds = chromahub.rhythm.app.shared.data.model.AppSettings.getInstance(context).getSongsByRating(2)
                    preparedSongs.filter { it.id in ratedSongIds }
                }
                "⭐ Liked" -> {
                    val ratedSongIds = chromahub.rhythm.app.shared.data.model.AppSettings.getInstance(context).getSongsByRating(1)
                    preparedSongs.filter { it.id in ratedSongIds }
                }
                
                "Short (< 3 min)" -> preparedSongs.filter { it.duration < 3 * 60 * 1000 }
                "Medium (3-5 min)" -> preparedSongs.filter { it.duration in (3 * 60 * 1000)..(5 * 60 * 1000) }
                "Long (> 5 min)" -> preparedSongs.filter { it.duration > 5 * 60 * 1000 }

                // Audio Quality Filters (Mutually Exclusive)
                "Hi-Res Lossless" -> preparedSongs.filter { isHiResLossless(it) && !isDolbyOrSurround(it) }
                "Lossless" -> preparedSongs.filter { isRegularLossless(it) && !isDolbyOrSurround(it) }
                "Dolby" -> preparedSongs.filter { isDolbyOrSurround(it) }
                "Stereo" -> preparedSongs.filter { (it.channels ?: 2) == 2 && !isDolbyOrSurround(it) }
                "Mono" -> preparedSongs.filter { (it.channels ?: 2) == 1 }
                
                "High Quality" -> preparedSongs.filter { song ->
                    val bitrate = song.bitrate ?: 0
                    bitrate >= 320000 && !isLosslessAudio(song) && !isDolbyOrSurround(song)
                }

                "Standard" -> preparedSongs.filter { song ->
                    val bitrate = song.bitrate ?: 0
                    bitrate in 128000..319999 && !isLosslessAudio(song) && !isDolbyOrSurround(song)
                }

                else -> preparedSongs // Default to showing all songs for any unrecognized category
            }
        }
    }
    
    // Show loading indicator while preparing
    if (isLoading && preparedSongs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ContentLoadingIndicator(
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = context.getString(R.string.library_loading_songs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    if (preparedSongs.isEmpty()) {
        EmptyState(
            message = context.getString(R.string.library_no_songs),
            icon = RhythmIcons.Music.Song,
            onRefresh = onRefreshClick
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Section Header (Scrollable, not sticky) - Shows selection mode or normal mode
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelectionMode) 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                            else 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column {
                            AnimatedContent(
                                targetState = isSelectionMode,
                                transitionSpec = {
                                    fadeIn(animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )) togetherWith
                                    fadeOut(animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ))
                                },
                                label = "SectionHeaderAnimation"
                            ) { isInSelectionMode ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    if (isInSelectionMode) {
                                        // Selection Mode: Show Close button and selection count
                                        FilledTonalIconButton(
                                            onClick = {
                                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                                multiSelectionState?.clearSelection()
                                            },
                                            shape = rememberExpressiveShapeFor(ExpressiveShapeTarget.PLAYER_CONTROLS),
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                imageVector = RhythmIcons.Close,
                                                contentDescription = "Clear selection"
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${selectedSongs.size} selected",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "from ${filteredSongs.size} tracks",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                    } else {
                                        // Normal Mode: Show icon and track count
                                        Surface(
                                            modifier = Modifier.size(48.dp),
                                            shape = rememberExpressiveShapeFor(ExpressiveShapeTarget.PLAYER_CONTROLS),
                                            color = MaterialTheme.colorScheme.primary,
                                            shadowElevation = 0.dp
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = RhythmIcons.Relax,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = context.getString(R.string.library_your_music),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "${filteredSongs.size} of ${preparedSongs.size} tracks",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }

                                        // Expressive Shuffle Button with modern design
                                        if (filteredSongs.isNotEmpty()) {
                                            ExpressiveFilledIconButton(
                                                onClick = {
                                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                                    onShuffleQueue(filteredSongs)
                                                },
                                                modifier = Modifier.size(44.dp),
                                                shape = ExpressiveShapes.SquircleMedium,
                                                colors = IconButtonDefaults.filledIconButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = RhythmIcons.Shuffle,
                                                    contentDescription = context.getString(R.string.cd_shuffle),
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            }
                            
                            // Selection Mode Actions: Show ExpressiveButtonGroup with quick actions
                            if (isSelectionMode && selectedSongs.isNotEmpty()) {
                                // Quick Actions using ExpressiveButtonGroup
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Primary action - Play
                                    ExpressiveButtonGroup(
                                        modifier = Modifier.weight(1f),
                                        style = ButtonGroupStyle.Filled
                                    ) {
                                        ExpressiveGroupButton(
                                            onClick = {
                                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                                // Play all selected
                                                onPlayQueueFromIndex(selectedSongs, 0)
                                                multiSelectionState?.clearSelection()
                                            },
                                            isStart = true,
                                            isEnd = false,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = RhythmIcons.Play,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "Play",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        
                                        ExpressiveGroupButton(
                                            onClick = {
                                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                                // Shuffle all selected
                                                onShuffleQueue(selectedSongs)
                                                multiSelectionState?.clearSelection()
                                            },
                                            isStart = false,
                                            isEnd = true
                                        ) {
                                            Icon(
                                                imageVector = RhythmIcons.Shuffle,
                                                contentDescription = "Shuffle selected",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    
                                    // Secondary actions
                                    FilledTonalIconButton(
                                        onClick = {
                                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                            // Toggle favorite for all
                                            val allAreLiked = selectedSongs.all { favoriteSongs.contains(it.id) }
                                            selectedSongs.forEach { onToggleFavorite(it) }
                                            if (allAreLiked) {
                                                Toast.makeText(context, "Removed ${selectedSongs.size} from favorites", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Added ${selectedSongs.size} to favorites", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        val allAreLiked = selectedSongs.all { favoriteSongs.contains(it.id) }
                                        Icon(
                                            imageVector = if (allAreLiked) RhythmIcons.FavoriteFilled else RhythmIcons.Favorite,
                                            contentDescription = if (allAreLiked) "Unlike all" else "Like all",
                                            tint = if (allAreLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                    
                                    FilledTonalIconButton(
                                        onClick = {
                                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                                            onShowMultiSelectionSheet()
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = RhythmIcons.More,
                                            contentDescription = "More actions"
                                        )
                                    }
                                }
                            }
                        }
            }

            // Sticky Filter Chips
            if (categories.size > 1) {
                stickyHeader {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(
                                    items = categories,
                                    key = { it }
                                ) { category ->
                                    val isSelected = selectedCategory == category
                                    
                                    // Add TabAnimation-like effects for filter chips
                                    val scaleAnimatable = remember { Animatable(1f) }
                                    val offsetAnimatable = remember { Animatable(0f) }
                                    
                                    // Pop animation for selected filter
                                    LaunchedEffect(isSelected) {
                                        if (isSelected) {
                                            launch {
                                                scaleAnimatable.animateTo(1.05f, animationSpec = tween<Float>(durationMillis = 250, easing = FastOutSlowInEasing))
                                                scaleAnimatable.animateTo(1f, animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing))
                                            }
                                        } else {
                                            scaleAnimatable.snapTo(1f)
                                        }
                                    }
                                    
                                    // Offset animation for neighboring filters
                                    LaunchedEffect(selectedCategory) {
                                        if (!isSelected && selectedCategory != null) {
                                            val currentIndex = categories.indexOf(category)
                                            val selectedIndex = categories.indexOf(selectedCategory)
                                            if (currentIndex >= 0 && selectedIndex >= 0) {
                                                val distance = currentIndex - selectedIndex
                                                if (abs(distance) == 1) { // Only affect direct neighbors
                                                    val direction = if (distance > 0) 1 else -1
                                                    val offsetValue = 8f * direction
                                                    launch {
                                                        offsetAnimatable.animateTo(offsetValue, animationSpec = tween<Float>(durationMillis = 250, easing = FastOutSlowInEasing))
                                                        offsetAnimatable.animateTo(0f, animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing))
                                                    }
                                                } else {
                                                    offsetAnimatable.snapTo(0f)
                                                }
                                            }
                                        } else {
                                            offsetAnimatable.snapTo(0f)
                                        }
                                    }

                                    val containerColor by animateColorAsState(
                                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "chipContainerColor"
                                    )
                                    val labelColor by animateColorAsState(
                                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "chipLabelColor"
                                    )
                                    val borderColor by animateColorAsState(
                                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(
                                            alpha = 0.6f
                                        ),
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "chipBorderColor"
                                    )
                                    val borderWidth by animateDpAsState(
                                        targetValue = if (isSelected) 2.dp else 1.dp,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "chipBorderWidth"
                                    )

                                    FilterChip(
                                        onClick = {
                                            HapticUtils.performHapticFeedback(
                                                context,
                                                haptics,
                                                HapticFeedbackType.LongPress
                                            )
                                            selectedCategory = category
                                        },
                                        label = {
                                            Text(
                                                text = category,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        },
                                        selected = isSelected,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = containerColor,
                                            selectedLabelColor = labelColor,
                                            containerColor = containerColor,
                                            labelColor = labelColor
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = borderColor,
                                            selectedBorderColor = borderColor,
                                            borderWidth = borderWidth
                                        ),
                                        shape = RoundedCornerShape(50.dp),
                                        modifier = Modifier.graphicsLayer {
                                            scaleX = scaleAnimatable.value
                                            scaleY = scaleAnimatable.value
                                            translationX = offsetAnimatable.value
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

            // Songs Items
            items(
                items = filteredSongs,
                key = { "song_${it.id}_${it.uri}" },
                contentType = { "song" }
            ) { song ->
                    AnimateIn(modifier = Modifier.animateItem()) {
                        val isSelected = selectedSongIds.contains(song.id)
                        val selectionIndex = multiSelectionState?.getSelectionIndex(song.id)
                        
                        LibrarySongItemWrapper(
                            song = song,
                            onClick = {
                                if (isSelectionMode) {
                                    // In selection mode, tap toggles selection
                                    onSongSelectionToggle(song)
                                } else {
                                    // Normal mode - play from this position
                                    val songIndex = filteredSongs.indexOf(song)
                                    if (songIndex >= 0) {
                                        onPlayQueueFromIndex(filteredSongs, songIndex)
                                    } else {
                                        onSongClick(song)
                                    }
                                }
                            },
                            onMoreClick = { onAddToPlaylist(song) },
                            onAddToQueue = { onAddToQueue(song) },
                            onPlayNext = { onPlayNext(song) },
                            onToggleFavorite = { onToggleFavorite(song) },
                            isFavorite = favoriteSongs.contains(song.id),
                            onGoToArtist = { 
                                // Find the artist from the list - respect groupByAlbumArtist setting
                                val artist = if (groupByAlbumArtist) {
                                    // When grouping by album artist, match split albumArtist (with split track fallback).
                                    val explicitAlbumArtist = song.albumArtist?.trim().orEmpty()
                                    val songArtistNames = if (explicitAlbumArtist.isNotBlank() && !explicitAlbumArtist.equals("<unknown>", ignoreCase = true)) {
                                        splitArtistNames(explicitAlbumArtist)
                                    } else {
                                        splitArtistNames(song.artist)
                                    }
                                    artists.find { artist ->
                                        songArtistNames.any { it.equals(artist.name, ignoreCase = true) }
                                    }
                                } else {
                                    // When not grouping, check if any split artist name matches
                                    val songArtistNames = splitArtistNames(song.artist)
                                    artists.find { artist ->
                                        songArtistNames.any { it.equals(artist.name, ignoreCase = true) }
                                    }
                                }
                                artist?.let { onGoToArtist(it) }
                            },
                            onGoToAlbum = { 
                                // Find the album from the list
                                val album = albums.find { 
                                    it.title.equals(song.album, ignoreCase = true) && 
                                    it.artist.equals(song.artist, ignoreCase = true)
                                }
                                album?.let { onGoToAlbum(it) }
                            },
                        onShowSongInfo = { onShowSongInfo(song) },
                        onAddToBlacklist = { onAddToBlacklist(song) },
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        haptics = haptics,
                        enableRatingSystem = enableRatingSystem,
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        selectionIndex = selectionIndex,
                        onLongPress = { onSongLongPress(song) },
                        customMenuContent = songMenuContent?.let { menuBuilder ->
                            { dismissMenu -> menuBuilder(song, dismissMenu) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SingleCardPlaylistsContent(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onCreatePlaylist: (() -> Unit)? = null,
    onImportPlaylist: (() -> Unit)? = null,
    onExportPlaylists: (() -> Unit)? = null,
    appSettings: AppSettings,
    onRefreshClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val playlistViewType by appSettings.playlistViewType.collectAsState()
    val playlistSortOrderString by appSettings.playlistSortOrder.collectAsState()
    val playlistSortOrder = try {
        LibraryPlaylistSortOrder.valueOf(playlistSortOrderString)
    } catch (e: Exception) {
        LibraryPlaylistSortOrder.NAME_ASC
    }
    
    // Loading state for initial render
    var isLoading by remember { mutableStateOf(true) }
    var preparedPlaylists by remember { mutableStateOf(playlists) }
    
    // Prepare and sort playlists asynchronously to avoid blocking UI on tab switch
    LaunchedEffect(playlists, playlistSortOrder) {
        isLoading = true
        preparedPlaylists = withContext(Dispatchers.Default) {
            // Sort playlists based on selected order
            when (playlistSortOrder) {
                LibraryPlaylistSortOrder.NAME_ASC -> playlists.sortedBy { it.name.lowercase() }
                LibraryPlaylistSortOrder.NAME_DESC -> playlists.sortedByDescending { it.name.lowercase() }
                LibraryPlaylistSortOrder.DATE_CREATED_ASC -> playlists.sortedBy { it.id.toLongOrNull() ?: 0L }
                LibraryPlaylistSortOrder.DATE_CREATED_DESC -> playlists.sortedByDescending { it.id.toLongOrNull() ?: 0L }
                LibraryPlaylistSortOrder.SONG_COUNT_ASC -> playlists.sortedBy { it.songs.size }
                LibraryPlaylistSortOrder.SONG_COUNT_DESC -> playlists.sortedByDescending { it.songs.size }
            }
        }
        isLoading = false
    }
    
    // Show loading indicator while preparing
    if (isLoading && preparedPlaylists.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ContentLoadingIndicator(
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = context.getString(R.string.library_loading_playlists),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    if (preparedPlaylists.isEmpty()) {
        EmptyState(
            message = "No playlists yet\nCreate your first playlist using the + button",
            icon = RhythmIcons.Music.Playlist,
            onRefresh = onRefreshClick
        )
    } else {
        if (playlistViewType == PlaylistViewType.GRID) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp // Simple spacing - Scaffold handles rest
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sticky Section Header
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = rememberExpressiveShapeFor(ExpressiveShapeTarget.PLAYER_CONTROLS),
                                color = MaterialTheme.colorScheme.primary,
                                shadowElevation = 0.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = RhythmIcons.PlaylistFilled,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = context.getString(R.string.library_your_playlists),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${preparedPlaylists.size} ${if (preparedPlaylists.size == 1) "playlist" else "playlists"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .height(2.dp)
                                    .width(60.dp),
                                shape = RoundedCornerShape(1.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                            ) {}
                        }
                    }
                }
                
                // Playlist Grid Items
                items(
                    items = preparedPlaylists,
                    key = { it.id },
                    contentType = { "playlist" }
                ) { playlist ->
                    AnimateIn(modifier = Modifier.animateItem()) {
                        PlaylistGridItem(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist) },
                            haptics = haptics
                        )
                    }
                }
            }
        } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 16.dp // Simple spacing - Scaffold handles rest
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Section Header (not sticky in list view)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = rememberExpressiveShapeFor(ExpressiveShapeTarget.PLAYER_CONTROLS),
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 0.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = RhythmIcons.PlaylistFilled,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column (modifier = Modifier.weight(1f)) {
                            Text(
                                text = context.getString(R.string.library_your_playlists),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${preparedPlaylists.size} ${if (preparedPlaylists.size == 1) "playlist" else "playlists"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Surface(
                                modifier = Modifier
                                    .height(2.dp)
                                    .width(60.dp),
                                shape = RoundedCornerShape(1.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                        ) {}
                    }
                }
            }

            // Playlist Items
            itemsIndexed(
                items = preparedPlaylists,
                key = { _, playlist -> playlist.id },
                contentType = { _, _ -> "playlist" }
            ) { index, playlist ->
                AnimateIn(modifier = Modifier.animateItem()) {
                    PlaylistItem(
                        playlist = playlist,
                        onClick = { onPlaylistClick(playlist) },
                        haptics = haptics,
                        itemShape = groupedLibraryItemShape(index, preparedPlaylists.size)
                    )
                }
            }
        }
        }
    }
}

@Composable
fun SingleCardAlbumsContent(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    onSongClick: (Song) -> Unit,
    onAlbumBottomSheetClick: (Album) -> Unit = {},
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    appSettings: AppSettings,
    onPlayQueue: (List<Song>) -> Unit = { _ -> },
    onShuffleQueue: (List<Song>) -> Unit = { _ -> },
    onRefreshClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val albumViewType by appSettings.albumViewType.collectAsState()
    
    // Loading state for initial render
    var isLoading by remember { mutableStateOf(true) }
    var preparedAlbums by remember { mutableStateOf(albums) }
    
    // Prepare albums asynchronously to avoid blocking UI on tab switch
    LaunchedEffect(albums) {
        isLoading = true
        preparedAlbums = withContext(Dispatchers.Default) {
            // Pre-process albums (sorting, etc.) in background
            albums.toList()
        }
        isLoading = false
    }
    
    // Show loading indicator while preparing
    if (isLoading && preparedAlbums.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ContentLoadingIndicator(
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = context.getString(R.string.library_loading_albums),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    if (preparedAlbums.isEmpty()) {
        EmptyState(
            message = "No albums yet",
            icon = RhythmIcons.Music.Album,
            onRefresh = onRefreshClick
        )
    } else {
                        if (albumViewType == AlbumViewType.GRID) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp // Simple spacing - Scaffold handles rest
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sticky Section Header
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = rememberExpressiveShapeFor(ExpressiveShapeTarget.PLAYER_CONTROLS),
                                color = MaterialTheme.colorScheme.primary,
                                shadowElevation = 0.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = RhythmIcons.Music.Album,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = context.getString(R.string.library_your_albums),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${preparedAlbums.size} ${if (preparedAlbums.size == 1) "album" else "albums"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }

                            Spacer(modifier = Modifier.weight(0.1f))

                            // Expressive Shuffle Button
                            if (preparedAlbums.isNotEmpty()) {
                                ExpressiveFilledIconButton(
                                    onClick = {
                                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                        // Shuffle album order but keep each album's songs in their correct order
                                        val shuffledAlbums = preparedAlbums.shuffled()
                                        val allSongs = shuffledAlbums.flatMap { it.songs }
                                        if (allSongs.isNotEmpty()) {
                                            onPlayQueue(allSongs)
                                        }
                                    },
                                    modifier = Modifier.size(44.dp),
                                    shape = ExpressiveShapes.SquircleMedium,
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                ) {
                                    Icon(
                                        imageVector = RhythmIcons.Shuffle,
                                        contentDescription = context.getString(R.string.cd_shuffle),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }                // Album Grid Items
                items(
                    items = preparedAlbums,
                    key = { it.id },
                    contentType = { "album" }
                ) { album ->
                    AnimateIn(modifier = Modifier.animateItem()) {
                        AlbumGridItem(
                            album = album,
                            onClick = { onAlbumBottomSheetClick(album) },
                            onPlayClick = { onAlbumClick(album) },
                            haptics = haptics
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = 16.dp // Simple spacing - Scaffold handles rest
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Section Header (not sticky in list view)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = rememberExpressiveShapeFor(ExpressiveShapeTarget.PLAYER_CONTROLS),
                                color = MaterialTheme.colorScheme.primary,
                                shadowElevation = 0.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = RhythmIcons.Music.Album,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = context.getString(R.string.library_your_albums),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${preparedAlbums.size} ${if (preparedAlbums.size == 1) "album" else "albums"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }

                            Spacer(modifier = Modifier.weight(0.1f))

                            // Expressive Shuffle Button
                            if (preparedAlbums.isNotEmpty()) {
                                ExpressiveFilledIconButton(
                                    onClick = {
                                        HapticUtils.performHapticFeedback(
                                            context,
                                            haptics,
                                            HapticFeedbackType.LongPress
                                        )
                                        // Shuffle album order but keep each album's songs in their correct order
                                        val shuffledAlbums = preparedAlbums.shuffled()
                                        val allSongs = shuffledAlbums.flatMap { it.songs }
                                        if (allSongs.isNotEmpty()) {
                                            onPlayQueue(allSongs)
                                        }
                                    },
                                    modifier = Modifier.size(44.dp),
                                    shape = ExpressiveShapes.SquircleMedium,
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                ) {
                                    Icon(
                                        imageVector = RhythmIcons.Shuffle,
                                        contentDescription = context.getString(R.string.cd_shuffle),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Album List Items
                itemsIndexed(
                    items = preparedAlbums,
                    key = { _, album -> album.id },
                    contentType = { _, _ -> "album" }
                ) { index, album ->
                    AnimateIn(modifier = Modifier.animateItem()) {
                        LibraryAlbumItem(
                            album = album,
                            onClick = { onAlbumBottomSheetClick(album) },
                            onPlayClick = { onAlbumClick(album) },
                            haptics = haptics,
                            itemShape = groupedLibraryItemShape(index, preparedAlbums.size)
                        )
                    }
                }
            }
        }
    }
}


@Composable
@Deprecated("Use SingleCardPlaylistsContent instead")
fun PlaylistsTab(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val context = LocalContext.current
    if (playlists.isEmpty()) {
        EmptyState(
            message = "No playlists yet\nCreate your first playlist using the + button",
            icon = RhythmIcons.Music.Playlist
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Enhanced Playlists Section Header (Sticky)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = rememberExpressiveShapeFor(ExpressiveShapeTarget.PLAYER_CONTROLS),
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = RhythmIcons.PlaylistFilled,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = context.getString(R.string.library_your_playlists),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "${playlists.size} ${if (playlists.size == 1) "playlist" else "playlists"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Surface(
                        modifier = Modifier
                            .height(2.dp)
                            .width(60.dp),
                        shape = RoundedCornerShape(1.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                    ) {}
                }
            }

            // Scrollable Playlists List
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Take remaining space
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = 8.dp, // Start below the sticky elements
                        bottom = 16.dp // Simple spacing - Scaffold handles rest
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(
                        items = playlists,
                        key = { it.id }
                    ) { playlist ->
                        AnimateIn {
                            PlaylistItem(
                                playlist = playlist,
                                onClick = { onPlaylistClick(playlist) },
                                haptics = haptics // Pass haptics
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumsTab(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    onSongClick: (Song) -> Unit,
    onAlbumBottomSheetClick: (Album) -> Unit = {},
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Add haptics parameter
) {
    val context = LocalContext.current
    val appSettings = remember { AppSettings.getInstance(context) }
    val albumViewType by appSettings.albumViewType.collectAsState()

    if (albums.isEmpty()) {
        EmptyState(
            message = "No albums yet",
            icon = RhythmIcons.Music.Album
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Enhanced Albums Section Header (Sticky)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = rememberExpressiveShapeFor(ExpressiveShapeTarget.PLAYER_CONTROLS),
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = RhythmIcons.Music.Album,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = context.getString(R.string.library_your_albums),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${albums.size} ${if (albums.size == 1) "album" else "albums"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // View type toggle button
                    FilledIconButton(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            val newViewType = if (albumViewType == AlbumViewType.LIST) AlbumViewType.GRID else AlbumViewType.LIST
                            appSettings.setAlbumViewType(newViewType)
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (albumViewType == AlbumViewType.LIST) RhythmIcons.AppsGrid else RhythmIcons.List,
                            contentDescription = "Toggle view type",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        modifier = Modifier
                            .height(2.dp)
                            .width(60.dp),
                        shape = RoundedCornerShape(1.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                    ) {}
                }
            }

            // Scrollable Albums Content
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Take remaining space
            ) {
                if (albumViewType == AlbumViewType.GRID) {
                    AlbumsGrid(
                        albums = albums,
                        onAlbumClick = { album ->
                            onAlbumBottomSheetClick(album)
                        },
                        onAlbumPlay = onAlbumClick, // This plays the album
                        onSongClick = onSongClick,
                        haptics = haptics // Pass haptics
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = 8.dp, // Start below the sticky elements
                            bottom = 16.dp // Simple spacing - Scaffold handles rest
                        ),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(
                            items = albums,
                            key = { it.id }
                        ) { album ->
                            AnimateIn {
                                LibraryAlbumItem(
                                    album = album,
                                    onClick = { onAlbumBottomSheetClick(album) }, // Changed to open bottom sheet
                                    onPlayClick = {
                                        // Play the entire album
                                        onAlbumClick(album)
                                    },
                                    haptics = haptics // Pass haptics
                                )
                            }
                        }
                    }
                }
            }
        }
    }
                    }


@Composable
fun LibrarySongItem(
    song: Song,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    onAddToQueue: () -> Unit,
    onPlayNext: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    isFavorite: Boolean = false,
    onGoToArtist: () -> Unit = {},
    onGoToAlbum: () -> Unit = {},
    onShowSongInfo: () -> Unit,
    onAddToBlacklist: () -> Unit, // Add blacklist callback
    currentSong: Song? = null, // Add current song parameter
    isPlaying: Boolean = false, // Add playing state
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    enableRatingSystem: Boolean = true, // Add rating system enabled flag
    // Multi-selection parameters
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    selectionIndex: Int? = null,
    onLongPress: () -> Unit = {},
    customMenuContent: (@Composable (dismissMenu: () -> Unit) -> Unit)? = null
) {
    val context = LocalContext.current
    var showDropdown by remember { mutableStateOf(false) }
    // Track rating state for immediate UI updates
    val appSettings = remember { chromahub.rhythm.app.shared.data.model.AppSettings.getInstance(context) }
    var currentRating by remember(song.id) { mutableStateOf(appSettings.getSongRating(song.id)) }
    val isCurrentSong = currentSong?.id == song.id

    // Animated colors for text
    val titleColor by animateColorAsState(
        targetValue = if (isCurrentSong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(300),
        label = "titleColor"
    )
    val supportingColor by animateColorAsState(
        targetValue = if (isCurrentSong) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "supportingColor"
    )

    // Selection animations
    val selectionScale by animateFloatAsState(
        targetValue = if (isSelected) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "selectionScaleAnimation"
    )

    val containerColorForSelection by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.secondaryContainer
            isCurrentSong -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 300),
        label = "containerColorAnimation"
    )

    ListItem(
        headlineContent = {
            Text(
                text = song.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.Medium,
                color = titleColor
            )
        },
        supportingContent = {
            Text(
                text = buildString {
                    append(song.artist)
                    append(" • ")
                    append(song.album)
                },
                style = MaterialTheme.typography.bodySmall,
                color = supportingColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Box {
                Surface(
                    shape = rememberExpressiveShapeFor(
                        ExpressiveShapeTarget.SONG_ART,
                        fallbackShape = MaterialTheme.shapes.large
                    ),
                    modifier = Modifier.size(56.dp),
                    border = if (isCurrentSong && !isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    M3ImageUtils.TrackImage(
                        imageUrl = song.artworkUri,
                        trackName = song.title,
                        modifier = Modifier.fillMaxSize(),
                        applyExpressiveShape = false
                    )
                }
                
                // Selection check overlay on album art
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                shape = rememberExpressiveShapeFor(
                                    ExpressiveShapeTarget.SONG_ART,
                                    fallbackShape = MaterialTheme.shapes.large
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectionIndex != null && selectionIndex >= 0) {
                            Text(
                                text = "${selectionIndex + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = RhythmIcons.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                } else if (isCurrentSong && isPlaying) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp)
                            .offset(x = 4.dp, y = 4.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 2.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            PlayingEqIcon(
                                modifier = Modifier.size(width = 12.dp, height = 10.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                isPlaying = isPlaying,
                                bars = 3
                            )
                        }
                    }
                }
            }
        },
        trailingContent = {
            // Hide more options button in selection mode
            if (!isSelectionMode) {
                FilledIconButton(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        showDropdown = true
                    },
                    modifier = Modifier
                        .size(width = 40.dp, height = 36.dp),
                    shape = RoundedCornerShape(18.dp), // Pill shape like Android 16
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(
                        imageVector = RhythmIcons.More,
                        contentDescription = "More options",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    showDropdown = false
                },
                modifier = Modifier
                    .widthIn(min = 220.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(5.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                if (customMenuContent != null) {
                    customMenuContent {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        showDropdown = false
                    }
                } else {
                // Play next
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Play next",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingIcon = {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.SkipNext,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp)
                                )
                            }
                        },
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            showDropdown = false
                            onPlayNext()
                        }
                    )
                }

                // Add to queue
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Add to queue",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingIcon = {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Queue,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp)
                                )
                            }
                        },
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            showDropdown = false
                            onAddToQueue()
                        }
                    )
                }

                // Toggle favorite
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                if (isFavorite) "Remove from favorites" else "Add to favorites",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingIcon = {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) RhythmIcons.FavoriteFilled else RhythmIcons.Favorite,
                                    contentDescription = null,
                                    
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp)
                                )
                            }
                        },
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            showDropdown = false
                            onToggleFavorite()
                        }
                    )
                }

                // Add to playlist
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Add to playlist",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingIcon = {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.AddToPlaylist,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp)
                                )
                            }
                        },
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            showDropdown = false
                            onMoreClick()
                        }
                    )
                }

                // Go to artist
                // Surface(
                //     color = MaterialTheme.colorScheme.surfaceContainerHigh,
                //     shape = RoundedCornerShape(12.dp),
                //     modifier = Modifier
                //         .fillMaxWidth()
                //         .padding(horizontal = 8.dp, vertical = 2.dp)
                // ) {
                //     DropdownMenuItem(
                //         text = {
                //             Text(
                //                 "Go to artist",
                //                 style = MaterialTheme.typography.bodyMedium,
                //                 fontWeight = FontWeight.Medium,
                //                 color = MaterialTheme.colorScheme.onSurface
                //             )
                //         },
                //         leadingIcon = {
                //             Surface(
                //                 color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                //                 shape = CircleShape,
                //                 modifier = Modifier.size(32.dp)
                //             ) {
                //                 Icon(
                //                     imageVector = RhythmIcons.ArtistFilled,
                //                     contentDescription = null,
                //                     tint = MaterialTheme.colorScheme.onSecondaryContainer,
                //                     modifier = Modifier
                //                         .fillMaxSize()
                //                         .padding(6.dp)
                //                 )
                //             }
                //         },
                //         onClick = {
                //             HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                //             showDropdown = false
                //             onGoToArtist()
                //         }
                //     )
                // }

                // Go to album
                // Surface(
                //     color = MaterialTheme.colorScheme.surfaceContainerHigh,
                //     shape = RoundedCornerShape(12.dp),
                //     modifier = Modifier
                //         .fillMaxWidth()
                //         .padding(horizontal = 8.dp, vertical = 2.dp)
                // ) {
                //     DropdownMenuItem(
                //         text = {
                //             Text(
                //                 "Go to album",
                //                 style = MaterialTheme.typography.bodyMedium,
                //                 fontWeight = FontWeight.Medium,
                //                 color = MaterialTheme.colorScheme.onSurface
                //             )
                //         },
                //         leadingIcon = {
                //             Surface(
                //                 color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                //                 shape = CircleShape,
                //                 modifier = Modifier.size(32.dp)
                //             ) {
                //                 Icon(
                //                     imageVector = RhythmIcons.AlbumFilled,
                //                     contentDescription = null,
                //                     tint = MaterialTheme.colorScheme.onSecondaryContainer,
                //                     modifier = Modifier
                //                         .fillMaxSize()
                //                         .padding(6.dp)
                //                 )
                //             }
                //         },
                //         onClick = {
                //             HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                //             showDropdown = false
                //             onGoToAlbum()
                //         }
                //     )
                // }

                // Rate song - only show if rating system is enabled
                if (enableRatingSystem) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = MaterialSymbolIcon("star", filled = true),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp)
                                        )
                                    }
                                    Text(
                                        "Rate Song",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            chromahub.rhythm.app.shared.presentation.components.RatingStars(
                                rating = currentRating,
                                onRatingChanged = { newRating ->
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    currentRating = newRating  // Update UI immediately
                                    appSettings.setSongRating(song.id, newRating)
                                    if (newRating > 0 && !isFavorite) {
                                        onToggleFavorite()
                                    }
                                },
                                enabled = true,
                                size = 24.dp
                            )
                        }
                    }
                }

                // Song info
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Song info",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingIcon = {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp)
                                )
                            }
                        },
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            showDropdown = false
                            onShowSongInfo()
                        }
                    )
                }

                // Add to blacklist
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Add to blacklist",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        leadingIcon = {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Block,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp)
                                )
                            }
                        },
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                            showDropdown = false
                            onAddToBlacklist()
                        }
                    )
                }
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
    )
}

/**
 * Wrapper composable for LibrarySongItem with rounded corners and border for active song
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibrarySongItemWrapper(
    song: Song,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    onAddToQueue: () -> Unit,
    onPlayNext: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    isFavorite: Boolean = false,
    onGoToArtist: () -> Unit = {},
    onGoToAlbum: () -> Unit = {},
    onShowSongInfo: () -> Unit,
    onAddToBlacklist: () -> Unit,
    currentSong: Song? = null,
    isPlaying: Boolean = false,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    enableRatingSystem: Boolean = true,
    itemShape: RoundedCornerShape = RoundedCornerShape(12.dp),
    // Multi-selection parameters
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    selectionIndex: Int? = null,
    onLongPress: () -> Unit = {},
    customMenuContent: (@Composable (dismissMenu: () -> Unit) -> Unit)? = null
) {
    val context = LocalContext.current
    val isCurrentSong = currentSong?.id == song.id
    
    // Selection animations
    val selectionScale by animateFloatAsState(
        targetValue = if (isSelected) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "selectionScaleAnimation"
    )
    
    // Animated colors
    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            isCurrentSong -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            else -> Color.Transparent
        },
        animationSpec = tween(300),
        label = "containerColor"
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .graphicsLayer {
                scaleX = selectionScale
                scaleY = selectionScale
            }
            .combinedClickable(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    onClick()
                },
                onLongClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    onLongPress()
                }
            ),
        shape = itemShape,
        color = containerColor
    ) {
        LibrarySongItem(
            song = song,
            onClick = {}, // Click handled by combinedClickable
            onMoreClick = onMoreClick,
            onAddToQueue = onAddToQueue,
            onPlayNext = onPlayNext,
            onToggleFavorite = onToggleFavorite,
            isFavorite = isFavorite,
            onGoToArtist = onGoToArtist,
            onGoToAlbum = onGoToAlbum,
            onShowSongInfo = onShowSongInfo,
            onAddToBlacklist = onAddToBlacklist,
            currentSong = currentSong,
            isPlaying = isPlaying,
            haptics = haptics,
            enableRatingSystem = enableRatingSystem,
            isSelected = isSelected,
            isSelectionMode = isSelectionMode,
            selectionIndex = selectionIndex,
            onLongPress = onLongPress,
            customMenuContent = customMenuContent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistItem(
    playlist: Playlist,
    onClick: () -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    itemShape: RoundedCornerShape = RoundedCornerShape(20.dp)
) {
    val context = LocalContext.current
    
    // Get unique album arts from playlist songs (up to 4)
    val albumArts = remember(playlist.songs) {
        playlist.songs
            .distinctBy { it.albumId }
            .take(4)
    }
    
    Surface(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = itemShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stylish playlist artwork with collage
            Surface(
                modifier = Modifier.size(72.dp),
                shape = rememberExpressiveShapeFor(
                    ExpressiveShapeTarget.PLAYLIST_ART,
                    fallbackShape = RoundedCornerShape(16.dp)
                ),
                tonalElevation = 0.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (playlist.artworkUri != null) {
                        // Use custom playlist artwork if available
                        M3ImageUtils.PlaylistImage(
                            imageUrl = playlist.artworkUri,
                            playlistName = playlist.name,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (albumArts.isNotEmpty()) {
                        // Create collage from album arts
                        PlaylistArtCollage(
                            songs = albumArts,
                            playlistName = playlist.name
                        )
                    } else {
                        // Fallback to playlist icon with solid background (matching artwork corner radius)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = RhythmIcons.PlaylistFilled,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Playlist info with better typography
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Enhanced metadata display with pill shape
                    Surface(
                        shape = RoundedCornerShape(50), // Pill shape
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = RhythmIcons.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${playlist.songs.size}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    if (playlist.songs.isNotEmpty()) {
                        val totalDurationMs = playlist.songs.sumOf { it.duration }
                        val totalMinutes = (totalDurationMs / (1000 * 60)).toInt()
                        val durationText = if (totalMinutes >= 60) {
                            val hours = totalMinutes / 60
                            val minutes = totalMinutes % 60
                            "${hours}h ${minutes}m"
                        } else {
                            "${totalMinutes}m"
                        }

                        Surface(
                            shape = RoundedCornerShape(50), // Pill shape
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = durationText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Stylish forward arrow with animation hint
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 0.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = RhythmIcons.Forward,
                        contentDescription = "Open playlist",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

internal fun groupedLibraryItemShape(index: Int, totalCount: Int): RoundedCornerShape {
    return when {
        totalCount <= 1 -> RoundedCornerShape(24.dp)
        index == 0 -> RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
            bottomStart = 6.dp,
            bottomEnd = 6.dp
        )
        index == totalCount - 1 -> RoundedCornerShape(
            topStart = 6.dp,
            topEnd = 6.dp,
            bottomStart = 24.dp,
            bottomEnd = 24.dp
        )
        else -> RoundedCornerShape(6.dp)
    }
}

@Composable
fun PlaylistArtCollage(
    songs: List<Song>,
    playlistName: String
) {
    when (songs.size) {
        1 -> {
            // Single album art
            M3ImageUtils.AlbumArt(
                imageUrl = songs[0].artworkUri,
                albumName = songs[0].album,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(0.dp),
                applyExpressiveShape = false
            )
        }
        2 -> {
            // Two album arts side by side
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()) {
                    M3ImageUtils.AlbumArt(
                        imageUrl = songs[0].artworkUri,
                        albumName = songs[0].album,
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(0.dp),
                        applyExpressiveShape = false
                    )
                }
                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()) {
                    M3ImageUtils.AlbumArt(
                        imageUrl = songs[1].artworkUri,
                        albumName = songs[1].album,
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(0.dp),
                        applyExpressiveShape = false
                    )
                }
            }
        }
        3 -> {
            // Three album arts: one large on left, two stacked on right
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                Box(modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()) {
                    M3ImageUtils.AlbumArt(
                        imageUrl = songs[0].artworkUri,
                        albumName = songs[0].album,
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(0.dp),
                        applyExpressiveShape = false
                    )
                }
                Column(modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    Box(modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()) {
                        M3ImageUtils.AlbumArt(
                            imageUrl = songs[1].artworkUri,
                            albumName = songs[1].album,
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(0.dp),
                            applyExpressiveShape = false
                        )
                    }
                    Box(modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()) {
                        M3ImageUtils.AlbumArt(
                            imageUrl = songs[2].artworkUri,
                            albumName = songs[2].album,
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(0.dp),
                            applyExpressiveShape = false
                        )
                    }
                }
            }
        }
        else -> {
            // Four album arts in a 2x2 grid
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Row(modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    Box(modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()) {
                        M3ImageUtils.AlbumArt(
                            imageUrl = songs[0].artworkUri,
                            albumName = songs[0].album,
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(0.dp),
                            applyExpressiveShape = false
                        )
                    }
                    Box(modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()) {
                        M3ImageUtils.AlbumArt(
                            imageUrl = songs[1].artworkUri,
                            albumName = songs[1].album,
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(0.dp),
                            applyExpressiveShape = false
                        )
                    }
                }
                Row(modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    Box(modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()) {
                        M3ImageUtils.AlbumArt(
                            imageUrl = songs[2].artworkUri,
                            albumName = songs[2].album,
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(0.dp),
                            applyExpressiveShape = false
                        )
                    }
                    Box(modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()) {
                        M3ImageUtils.AlbumArt(
                            imageUrl = songs[3].artworkUri,
                            albumName = songs[3].album,
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(0.dp),
                            applyExpressiveShape = false
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryAlbumItem(
    album: Album,
    onClick: () -> Unit,
    onPlayClick: () -> Unit = {},
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    itemShape: RoundedCornerShape = RoundedCornerShape(20.dp)
) {
    val context = LocalContext.current
    val artworkShape = rememberExpressiveShapeFor(
        ExpressiveShapeTarget.ALBUM_ART,
        fallbackShape = RoundedCornerShape(18.dp)
    )
    
    Surface(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = itemShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Enhanced album artwork
            Surface(
                modifier = Modifier.size(68.dp),
                shape = artworkShape,
                tonalElevation = 0.dp,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (album.artworkUri != null) Color.Transparent
                            else MaterialTheme.colorScheme.secondaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (album.artworkUri != null) {
                        M3ImageUtils.AlbumArt(
                            imageUrl = album.artworkUri,
                            albumName = album.title,
                            modifier = Modifier.fillMaxSize(),
                            shape = artworkShape
                        )
                    } else {
                        Icon(
                            imageVector = RhythmIcons.Album,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(18.dp))
            
            // Enhanced album info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp) // Add padding to prevent text from being cut off
            ) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(6.dp)) // Increase spacing
                
                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp)) // Increase spacing
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Song count pill
                    Surface(
                        shape = RoundedCornerShape(50), // Pill shape
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = RhythmIcons.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${album.numberOfSongs} Songs",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Year pill
                    if (album.year > 0) {
                        Surface(
                            shape = RoundedCornerShape(50), // Pill shape
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.DateRange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${album.year}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
            
            // Enhanced play button
            FilledIconButton(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    onPlayClick()
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = RhythmIcons.Play,
                    contentDescription = "Play album",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    icon: MaterialSymbolIcon,
    onRefresh: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 0.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(48.dp)
            ) {
                val context = LocalContext.current
                val haptics = LocalHapticFeedback.current
                val animatedSize by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.6f,
                        stiffness = 100f
                    ),
                    label = "iconAnimation"
                )
                
                val animatedAlpha by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 800,
                        delayMillis = 200
                    ),
                    label = "alphaAnimation"
                )
                
                // Enhanced icon container with gradient background
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            
                            modifier = Modifier
                                .size(64.dp)
                                .graphicsLayer { alpha = animatedAlpha }
                        )
                    }
                }
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.headlineSmall.lineHeight * 1.2,
                    modifier = Modifier.graphicsLayer { alpha = animatedAlpha }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = context.getString(R.string.library_start_collection),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer { alpha = animatedAlpha * 0.8f }
                )

                if (onRefresh != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    FilledTonalButton(
                        onClick = {
                            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                            onRefresh()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = RhythmIcons.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Refresh")
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimateIn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, delayMillis = 50),
        label = "alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = modifier.graphicsLayer(
            alpha = alpha,
            scaleX = scale,
            scaleY = scale
        )
    ) {
        content()
    }
}

@Composable
fun AlbumsGrid(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    onAlbumPlay: (Album) -> Unit,
    onSongClick: (Song) -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Add haptics parameter
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(
            top = 8.dp,
            bottom = 16.dp // Simple spacing - Scaffold handles rest
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = albums,
            key = { it.id }
        ) { album ->
            AnimateIn {
                AlbumGridItem(
                    album = album,
                    onClick = { onAlbumClick(album) }, // Card click opens bottom sheet
                    onPlayClick = { onAlbumPlay(album) }, // Play button plays album
                    haptics = haptics // Pass haptics
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistGridItem(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val context = LocalContext.current
    
    Card(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            hoveredElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Playlist artwork - maintain square ratio
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = rememberExpressiveShapeFor(
                    ExpressiveShapeTarget.PLAYLIST_ART,
                    fallbackShape = RoundedCornerShape(16.dp)
                ),
                tonalElevation = 0.dp,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (playlist.songs.isNotEmpty()) {
                        PlaylistArtCollage(
                            songs = playlist.songs,
                            playlistName = playlist.name
                        )
                    } else {
                        Icon(
                            imageVector = RhythmIcons.PlaylistFilled,
                            contentDescription = null,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Playlist name
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Song count pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50), // Pill shape
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = RhythmIcons.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "${playlist.songs.size}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumGridItem(
    album: Album,
    onClick: () -> Unit,
    onPlayClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Add haptics parameter
) {
    val context = LocalContext.current
    
    Card(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            hoveredElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Album artwork - maintain square ratio
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    shape = rememberExpressiveShapeFor(
                        ExpressiveShapeTarget.ALBUM_ART,
                        fallbackShape = RoundedCornerShape(16.dp)
                    ),
                    tonalElevation = 0.dp,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (album.artworkUri != null) Color.Transparent
                                else MaterialTheme.colorScheme.secondaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (album.artworkUri != null) {
                            M3ImageUtils.AlbumArt(
                                imageUrl = album.artworkUri,
                                albumName = album.title,
                                modifier = Modifier.fillMaxSize(),
                                applyExpressiveShape = false
                            )
                        } else {
                            Icon(
                                imageVector = RhythmIcons.Album,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(52.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // Album title
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Artist name
                Text(
                    text = album.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Pills row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    // Song count pill
                    Surface(
                        shape = RoundedCornerShape(50), // Pill shape
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = RhythmIcons.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "${album.numberOfSongs}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Year pill
                    if (album.year > 0) {
                        Surface(
                            shape = RoundedCornerShape(50), // Pill shape
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.DateRange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "${album.year}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
            
            // Play button overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                FilledIconButton(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        onPlayClick()
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = RhythmIcons.Play,
                        contentDescription = "Play album",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SingleCardArtistsContent(
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onPlayQueue: (List<Song>) -> Unit = { _ -> },
    onShuffleQueue: (List<Song>) -> Unit = { _ -> },
    onRefreshClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val viewModel = viewModel<chromahub.rhythm.app.viewmodel.MusicViewModel>()
    val appSettings = remember { AppSettings.getInstance(context) }
    
    // Get artist view type from settings
    val artistViewType by appSettings.artistViewType.collectAsState()
    
    var selectedCategory by remember { mutableStateOf("All") }
    var currentSortOption by remember { mutableStateOf(ArtistSortOption.NAME_ASC) }
    var showSortOptions by remember { mutableStateOf(false) }
    
    // Loading state for async sorting
    var isLoading by remember { mutableStateOf(true) }
    var sortedArtists by remember { mutableStateOf(artists) }
    
    // Define categories for artists
    val categories = remember(artists) {
        listOf("All")
    }
    
    // Sort artists asynchronously to avoid blocking UI
    LaunchedEffect(artists, currentSortOption) {
        isLoading = true
        sortedArtists = withContext(Dispatchers.Default) {
            when (currentSortOption) {
                ArtistSortOption.NAME_ASC -> artists.sortedBy { it.name.lowercase() }
                ArtistSortOption.NAME_DESC -> artists.sortedByDescending { it.name.lowercase() }
                ArtistSortOption.TRACK_COUNT_DESC -> artists.sortedByDescending { it.numberOfTracks }
                ArtistSortOption.ALBUM_COUNT_DESC -> artists.sortedByDescending { it.numberOfAlbums }
            }
        }
        isLoading = false
    }
    
    // Determine if we should use grid or list view
    val isGridView = artistViewType == ArtistViewType.GRID
    
    // Show loading indicator while sorting
    if (isLoading && sortedArtists.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ContentLoadingIndicator(
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = context.getString(R.string.library_loading_artists),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }
    
    // Check for empty state
    if (sortedArtists.isEmpty()) {
        EmptyState(
            message = "No artists yet",
            icon = RhythmIcons.Artist,
            onRefresh = onRefreshClick
        )
        return
    }
    
    if (isGridView) {
        // Grid view using LazyVerticalGrid as main container
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 0.dp,
                bottom = 16.dp // Simple spacing - Scaffold handles rest
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sticky header for grid view
            item(span = { GridItemSpan(maxLineSpan) }) {
                ArtistSectionHeader(
                    artistCount = sortedArtists.size,
                    artists = sortedArtists,
                    applyOuterHorizontalPadding = false,
                    onPlayAll = {
                        val allSongs = sortedArtists.flatMap { it.songs }
                        if (allSongs.isNotEmpty()) {
                            onPlayQueue(allSongs)
                        }
                    },
                    onShuffleAll = {
                        val allSongs = sortedArtists.flatMap { it.songs }
                        if (allSongs.isNotEmpty()) {
                            onShuffleQueue(allSongs)
                        }
                    },
                    haptics = haptics
                )
            }
            
            if (sortedArtists.isNotEmpty()) {
                items(
                    items = sortedArtists,
                    key = { "gridartist_${it.id}" },
                    contentType = { "artist" }
                ) { artist ->
                    AnimateIn(modifier = Modifier.animateItem()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ArtistGridCard(
                                artist = artist,
                                onClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    onArtistClick(artist)
                                },
                                onPlayClick = {
                                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                    viewModel.playArtist(artist)
                                }
                            )
                        }
                    }
                }
            }
        }
    } else {
        // List view using LazyColumn as main container
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = 16.dp // Simple spacing - Scaffold handles rest
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Section header (not sticky in list view)
            item {
                ArtistSectionHeader(
                    artistCount = sortedArtists.size,
                    artists = sortedArtists,
                    onPlayAll = {
                        val allSongs = sortedArtists.flatMap { it.songs }
                        if (allSongs.isNotEmpty()) {
                            onPlayQueue(allSongs)
                        }
                    },
                    onShuffleAll = {
                        val allSongs = sortedArtists.flatMap { it.songs }
                        if (allSongs.isNotEmpty()) {
                            onShuffleQueue(allSongs)
                        }
                    },
                    haptics = haptics
                )
            }
            
            if (sortedArtists.isNotEmpty()) {
                itemsIndexed(
                    items = sortedArtists,
                    key = { _, artist -> "listartist_${artist.id}" },
                    contentType = { _, _ -> "artist" }
                ) { index, artist ->
                    AnimateIn(modifier = Modifier.animateItem()) {
                        ArtistListCard(
                            artist = artist,
                            onClick = {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                onArtistClick(artist)
                            },
                            onPlayClick = {
                                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                                viewModel.playArtist(artist)
                            },
                            itemShape = groupedLibraryItemShape(index, sortedArtists.size)
                        )
                    }
                }
            }
        }
    }

    // Sort options bottom sheet
    if (showSortOptions) {
        ModalBottomSheet(
            onDismissRequest = { showSortOptions = false },
            sheetState = rememberModalBottomSheetState(),
            dragHandle = { 
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = context.getString(R.string.library_sort_artists),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                ArtistSortOption.entries.forEach { sortOption ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                HapticUtils.performHapticFeedback(
                                    context,
                                    haptics,
                                    HapticFeedbackType.LongPress
                                )
                                currentSortOption = sortOption
                                showSortOptions = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sortOption.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (currentSortOption == sortOption) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (currentSortOption == sortOption) {
                            Icon(
                                imageVector = RhythmIcons.Check,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ArtistSectionHeader(
    artistCount: Int,
    artists: List<Artist> = emptyList(),
    applyOuterHorizontalPadding: Boolean = true,
    onPlayAll: () -> Unit = {},
    onShuffleAll: () -> Unit = {},
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback? = null
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (applyOuterHorizontalPadding) 20.dp else 0.dp,
                vertical = 8.dp
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = rememberExpressiveShapeFor(ExpressiveShapeTarget.PLAYER_CONTROLS),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = RhythmIcons.Artist,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = context.getString(R.string.library_your_artists),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$artistCount ${if (artistCount == 1) "artist" else "artists"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Surface(
                modifier = Modifier
                .height(2.dp)
                .width(60.dp),
                shape = RoundedCornerShape(1.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
            ) {}
        }
    }
}

enum class ArtistSortOption(val label: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    TRACK_COUNT_DESC("Songs (High to Low)"),
    ALBUM_COUNT_DESC("Albums (High to Low)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArtistGridCard(
    artist: Artist,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val artworkShape = rememberExpressiveShapeFor(ExpressiveShapeTarget.ARTIST_ART)
    
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Artist image with expressive shape container
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = rememberExpressiveShapeFor(
                    ExpressiveShapeTarget.ARTIST_ART,
                    fallbackShape = RoundedCornerShape(16.dp)
                ),
                tonalElevation = 0.dp,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (artist.artworkUri != null) Color.Transparent
                            else MaterialTheme.colorScheme.secondaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    M3ImageUtils.ArtistImage(
                        imageUrl = artist.artworkUri,
                        artistName = artist.name,
                        modifier = Modifier.fillMaxSize(),
                        applyExpressiveShape = false
                    )
                    
                    // Play button overlay positioned at bottom right
                    Surface(
                        onClick = onPlayClick,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = RhythmIcons.Play,
                                contentDescription = "Play ${artist.name}",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                // Artist name
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Artist info row with pills (centered)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Track count pill
                    Surface(
                        shape = RoundedCornerShape(50), // Pill shape
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = RhythmIcons.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "${artist.numberOfTracks}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    
                    // Album count pill
                    if (artist.numberOfAlbums > 0) {
                        Surface(
                            shape = RoundedCornerShape(50), // Pill shape
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Album,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "${artist.numberOfAlbums}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArtistListCard(
    artist: Artist,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
    itemShape: RoundedCornerShape = RoundedCornerShape(20.dp)
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = itemShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artist image with expressive shape
            M3ImageUtils.ArtistImage(
                imageUrl = artist.artworkUri,
                artistName = artist.name,
                modifier = Modifier
                    .size(68.dp)
            )

            Spacer(modifier = Modifier.width(18.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                // Artist name
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Artist info with pills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Track count pill
                    Surface(
                        shape = RoundedCornerShape(50), // Pill shape
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = RhythmIcons.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${artist.numberOfTracks}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Album count pill
                    if (artist.numberOfAlbums > 0) {
                        Surface(
                            shape = RoundedCornerShape(50), // Pill shape
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = RhythmIcons.Album,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${artist.numberOfAlbums} Albums",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Play button
            FilledIconButton(
                onClick = onPlayClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = RhythmIcons.Play,
                    contentDescription = "Play ${artist.name}",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


@Composable
fun PlaylistFabMenuContent(
    onCreatePlaylist: () -> Unit,
    onImportPlaylist: (() -> Unit)?,
    onExportPlaylists: (() -> Unit)?,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Added haptics parameter
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .widthIn(max = 200.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Export playlists item
        if (onExportPlaylists != null) {
            FloatingActionButton(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    scope.launch {
                        onExportPlaylists()
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = MaterialSymbolIcon("file_upload"),
                    contentDescription = "Export playlists",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Import playlist item
        if (onImportPlaylist != null) {
            FloatingActionButton(
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    scope.launch {
                        onImportPlaylist()
                    }
                },
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = RhythmIcons.Actions.Download,
                    contentDescription = "Import playlist",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Create playlist item (always shown)
        FloatingActionButton(
            onClick = {
                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                scope.launch {
                    onCreatePlaylist()
                }
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = RhythmIcons.Add,
                contentDescription = "Create playlist",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun PlaylistFabMenu(
    visible: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCreatePlaylist: () -> Unit,
    onImportPlaylist: (() -> Unit)?,
    onExportPlaylists: (() -> Unit)?,
    modifier: Modifier = Modifier,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Added haptics parameter
) {
    val context = LocalContext.current
    val menuItems = remember(onCreatePlaylist, onImportPlaylist, onExportPlaylists) {
        listOfNotNull(
            Triple("New playlist", RhythmIcons.Add, onCreatePlaylist),
            onImportPlaylist?.let {
                Triple("Import playlist", RhythmIcons.Actions.Download, it)
            },
            onExportPlaylists?.let {
                Triple("Export playlists", MaterialSymbolIcon("file_upload"), it)
            }
        )
    }

    FloatingActionButtonMenu(
        modifier = modifier.padding(bottom = bottomPadding + 8.dp),
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                modifier = Modifier
                    .semantics {
                        traversalIndex = -1f
                        stateDescription = if (expanded) "Expanded" else "Collapsed"
                    }
                    .animateFloatingActionButton(
                        visible = visible || expanded,
                        alignment = Alignment.BottomEnd
                    ),
                checked = expanded,
                onCheckedChange = onExpandedChange
            ) {
                val imageVector by remember {
                    derivedStateOf {
                        if (checkedProgress > 0.5f) {
                            RhythmIcons.Close
                        } else {
                            RhythmIcons.Add
                        }
                    }
                }
                Icon(
                    imageVector = imageVector,
                    contentDescription = if (expanded) "Close playlist menu" else "Open playlist menu",
                    modifier = Modifier.animateIcon({ checkedProgress })
                )
            }
        }
    ) {
        menuItems.forEachIndexed { index, item ->
            FloatingActionButtonMenuItem(
                modifier = Modifier.semantics {
                    isTraversalGroup = true
                    if (index == menuItems.lastIndex) {
                        customActions = listOf(
                            CustomAccessibilityAction(
                                label = "Close menu",
                                action = {
                                    onExpandedChange(false)
                                    true
                                }
                            )
                        )
                    }
                },
                onClick = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                    item.third.invoke()
                    onExpandedChange(false)
                },
                icon = {
                    Icon(
                        imageVector = item.second,
                        contentDescription = null
                    )
                },
                text = { Text(text = item.first) }
            )
        }
    }
}


@Composable
fun FabMenuItem(
    label: String,
    icon: MaterialSymbolIcon,
    contentDescription: String,
    containerColor: Color, // Added containerColor
    contentColor: Color,   // Added contentColor
    onClick: () -> Unit,
    animationDelay: Int = 0,
    modifier: Modifier = Modifier,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback // Added haptics parameter
) {
    val context = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope() // Define scope here

    // Tap animation state
    val pressedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessHigh),
        label = "pressedScale_$label"
    )

    // Staggered entrance animation
    val entranceScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "entranceScale_$label"
    )

    val entranceAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = animationDelay
        ),
        label = "entranceAlpha_$label"
    )

    Card(
        onClick = {
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
            isPressed = true
            onClick()
            // Reset pressed state after animation
            scope.launch { // Use the local scope
                kotlinx.coroutines.delay(100)
                isPressed = false
            }
        },
        shape = RoundedCornerShape(50.dp), // Pill shape
        colors = CardDefaults.cardColors( // Use CardDefaults.cardColors
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 8.dp
        ),
        modifier = modifier
            .graphicsLayer {
                scaleX = entranceScale * pressedScale
                scaleY = entranceScale * pressedScale
                alpha = entranceAlpha
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        awaitRelease()
                        isPressed = false
                    }
                )
            }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Bottom Floating Button Group Component
@Composable
fun BottomFloatingButtonGroup(
    modifier: Modifier = Modifier,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Loading states
    var isPlayAllLoading by remember { mutableStateOf(false) }
    var isShuffleLoading by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Play All Button
            Button(
                onClick = {
                    if (!isPlayAllLoading && !isShuffleLoading) {
                        isPlayAllLoading = true
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        // Must call on Main thread for MediaController
                        scope.launch {
                            try {
                                onPlayAll()
                            } finally {
                                kotlinx.coroutines.delay(500)
                                isPlayAllLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
                enabled = !isPlayAllLoading && !isShuffleLoading
            ) {
                if (isPlayAllLoading) {
                    ActionProgressLoader(
                        size = 20.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = RhythmIcons.Play,
                        contentDescription = "Play all",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = context.getString(R.string.library_play_all),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Expressive Shuffle Button
            ExpressiveFilledIconButton(
                onClick = {
                    if (!isPlayAllLoading && !isShuffleLoading) {
                        isShuffleLoading = true
                        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
                        // Must call on Main thread for MediaController
                        scope.launch {
                            try {
                                onShuffle()
                            } finally {
                                kotlinx.coroutines.delay(500)
                                isShuffleLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.size(52.dp),
                shape = ExpressiveShapes.SquircleMedium,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                enabled = !isPlayAllLoading && !isShuffleLoading
            ) {
                if (isShuffleLoading) {
                    ActionProgressLoader(
                        size = 24.dp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                } else {
                    Icon(
                        imageVector = RhythmIcons.Shuffle,
                        contentDescription = "Shuffle",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

