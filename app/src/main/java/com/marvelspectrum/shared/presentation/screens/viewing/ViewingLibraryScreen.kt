@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.marvelspectrum.shared.presentation.screens.viewing

import android.content.Intent
import android.net.Uri

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marvelspectrum.R
import com.marvelspectrum.shared.data.service.ViewingMetadataStore
import com.marvelspectrum.shared.data.viewing.McuAssetDataSource
import com.marvelspectrum.shared.data.viewing.WatchProvider
import com.marvelspectrum.shared.data.viewing.ViewingCastMember
import com.marvelspectrum.shared.data.viewing.ViewingItem
import com.marvelspectrum.shared.data.viewing.ViewingList
import com.marvelspectrum.shared.data.viewing.ViewingListImportance
import com.marvelspectrum.shared.data.viewing.ViewingSearchCategory
import com.marvelspectrum.shared.data.viewing.ViewingSearchSortMode
import com.marvelspectrum.shared.data.viewing.ViewingSortMode
import com.marvelspectrum.shared.data.viewing.ViewingStatus
import com.marvelspectrum.shared.data.viewing.ViewingType
import com.marvelspectrum.shared.data.viewing.ViewingUserStatus
import com.marvelspectrum.shared.data.viewing.ViewingTrailer
import com.marvelspectrum.shared.presentation.components.icons.Icon
import com.marvelspectrum.shared.presentation.components.icons.MaterialSymbolIcon
import com.marvelspectrum.shared.presentation.components.icons.RhythmIcons
import com.marvelspectrum.shared.presentation.components.viewing.YouTubeTrailerWebPlayer
import com.marvelspectrum.shared.presentation.viewmodel.ViewingViewModel
import com.marvelspectrum.shared.util.ViewingArtworkUtils
import com.marvelspectrum.util.HapticUtils
import kotlinx.coroutines.delay

@Composable
fun ViewingLibraryScreen(
    onOpenDetail: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewingViewModel: ViewingViewModel = viewModel()
) {
    val viewingState by viewingViewModel.uiState.collectAsState()
    val data = viewingState.data
    if (data == null) {
        ViewingCatalogLoadingState(
            title = "Loading Library",
            message = viewingState.errorMessage ?: "Organizing viewing timelines, collections, and saved state…",
            modifier = modifier
        )
        return
    }
    var tab by rememberSaveable { mutableStateOf("Essential") }
    var genreFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var statusFilter by rememberSaveable { mutableStateOf<ViewingUserStatus?>(null) }
    var sortMode by rememberSaveable { mutableStateOf(ViewingSortMode.RELEASE) }
    var selectedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedListId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedItem = data.findItem(selectedItemId)
    val selectedList = data.findList(selectedListId)

    androidx.activity.compose.BackHandler(enabled = selectedItem != null || selectedList != null) {
        if (selectedItem != null) selectedItemId = null else selectedListId = null
    }

    when {
        selectedItem != null -> ViewingDetailScreen(item = selectedItem, list = selectedList, onBack = { selectedItemId = null }, viewingViewModel = viewingViewModel)
        selectedList != null -> ViewingListDetailScreen(list = selectedList, onBack = { selectedListId = null }, onOpenTitle = { selectedItemId = it.id; onOpenDetail() })
        else -> {
            val genres = remember(data) { data.allItems.flatMap { it.genres }.distinct().sorted() }
            val filtered = remember(tab, sortMode, statusFilter, genreFilter, data) {
                val base = when (tab) {
                    "Continue" -> ViewingMetadataStore.recentItems(data).ifEmpty { data.allItems.filter { ViewingUserStatus.WATCHING in ViewingMetadataStore.statusesFor(it) } }
                    "Essentials", "Essential" -> data.featuredList.items
                    "MCU" -> data.allItems.filter { it.universe in setOf("MCU", "Marvel") }
                    "DC" -> data.allItems.filter { it.universe in setOf("DCU", "DCEU", "Elseworlds") }
                    "Timeline" -> data.allItems.sortedFor(ViewingSortMode.CHRONOLOGICAL)
                    "Saved" -> data.allItems.filter { item -> ViewingMetadataStore.statusesFor(item).any { status -> status != ViewingUserStatus.HIDDEN } }
                    else -> data.allItems
                }
                base.filter { item -> statusFilter == null || statusFilter in ViewingMetadataStore.statusesFor(item) }
                    .filter { item -> genreFilter == null || genreFilter in item.genres }
                    .sortedFor(sortMode)
            }
            LazyColumn(
                modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(SpectrumSpacing.screenPadding, ViewingUi.topPad, SpectrumSpacing.screenPadding, SpectrumSpacing.bottomSafePadding),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item { CinemaverseHeader(title = "Library", subtitle = "Every universe, timeline, and collection in one place", onOpenSettings = onOpenSettings, showBrandMark = false) }
                item { LibraryTabs(tab, onTab = { tab = it }) }
                item { LibraryContextHeader(tab, if (tab == "Collections") data.allLists.visibleManagedLists().size else filtered.size, if (tab == "Collections") "collections" else "titles", sortMode.label, listOfNotNull(statusFilter?.libraryTitle, genreFilter).joinToString(" • ").takeIf { it.isNotBlank() }) }
                item { LibrarySecondaryControls(sortMode, { sortMode = it }, statusFilter, { statusFilter = it }, genreFilter, { genreFilter = it }, genres, data.allItems) }
                if (tab == "Collections") {
                    item { CollectionCardGrid(data.allLists.visibleManagedLists(), onOpenList = { selectedListId = it.id }) }
                } else {
                    if (filtered.isEmpty()) item { EmptyState("Nothing here yet", "Open a title and add it to Watchlist, Favorite, or Watched.") }
                    if (tab in setOf("MCU", "DC")) {
                        item { LibraryPosterGrid(filtered) { item -> selectedItemId = item.id; ViewingMetadataStore.markViewed(item); onOpenDetail() } }
                    } else {
                        groupedViewingItems(filtered, sortMode) { item -> selectedItemId = item.id; ViewingMetadataStore.markViewed(item); onOpenDetail() }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ViewingListDetailScreen(list: ViewingList, onBack: () -> Unit, onOpenTitle: (ViewingItem) -> Unit) {
    var showOrderHelp by rememberSaveable { mutableStateOf(false) }
    val firstPlayable = list.items.firstOrNull { ViewingUserStatus.WATCHED !in ViewingMetadataStore.statusesFor(it) } ?: list.items.firstOrNull()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(list.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(RhythmIcons.Back, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(SpectrumSpacing.screenPadding, 14.dp, SpectrumSpacing.screenPadding, SpectrumSpacing.bottomSafePadding),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { CollectionAlbumHero(list, onPlayFirst = { firstPlayable?.let(onOpenTitle) }, onShuffle = { list.items.shuffled().firstOrNull()?.let(onOpenTitle) }) }
                item { ViewingOrderEducationChip(list, onWhy = { showOrderHelp = true }) }
                item { CollectionTrackHeader(list.items.size) }
                itemsIndexed(list.items, key = { index, item -> "${list.id}-${item.id}-$index" }) { index, item ->
                    CollectionTitleRow(
                        item = item,
                        order = index + 1,
                        onClick = { onOpenTitle(item) }
                    )
                }
            }
        }
    }
    if (showOrderHelp) ViewingOrderHelpDialog(list = list, onDismiss = { showOrderHelp = false })
}


@Composable
internal fun ViewingOrderEducationChip(list: ViewingList, onWhy: () -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(RhythmIcons.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(list.orderingBasisText(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text("Numbers reflect this visible collection order.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            AssistChip(onClick = onWhy, label = { Text("Why this order?") })
        }
    }
}

@Composable
internal fun ViewingOrderHelpDialog(list: ViewingList, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(30.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 6.dp, modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Why this order?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    IconButton(onClick = onDismiss) { Icon(RhythmIcons.Close, contentDescription = "Close order help") }
                }
                Text(list.orderingBasisText(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                OrderHelpLine("Release order", "Sorts titles by public release date and release-order metadata.")
                OrderHelpLine("Chronological order", "Uses in-universe story placement when the catalog has timeline metadata.")
                OrderHelpLine("Phases and sagas", "Groups MCU phases, DC chapters, sagas, and character arcs before showing row numbers.")
                OrderHelpLine("Custom collections", "Uses the curated collection sequence; duplicate titles still receive stable visible numbers.")
            }
        }
    }
}

@Composable
internal fun OrderHelpLine(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

internal fun ViewingList.orderingBasisText(): String = when {
    category?.contains("Chronological", ignoreCase = true) == true -> "Ordered by in-universe chronology."
    category?.contains("Release", ignoreCase = true) == true -> "Ordered by public release sequence."
    !phase.isNullOrBlank() -> "Ordered inside $phase."
    !saga.isNullOrBlank() -> "Ordered inside $saga."
    !franchise.isNullOrBlank() -> "Ordered as a curated $franchise journey."
    else -> "Ordered as a curated ${category ?: universe ?: "Cinemaverse"} collection."
}

@Composable
internal fun ViewingOrderRow(item: ViewingItem, order: Int, onClick: () -> Unit) {
    val displayItem = rememberCachedItem(item)
    val statuses = ViewingMetadataStore.statusesFor(displayItem)
    PressableCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box {
                PosterBackdrop(displayItem, Modifier.size(ViewingUi.rowPosterWidth, ViewingUi.rowPosterHeight), ContentScale.Crop, RoundedCornerShape(16.dp))
                if (order > 0) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
                        shape = RoundedCornerShape(9.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) { Text(order.toString(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) }
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(displayItem.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Text(listOfNotNull(displayItem.year, displayItem.runtime, displayItem.universe, displayItem.type.name.lowercase().replaceFirstChar { it.titlecase() }).joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text(listOfNotNull(displayItem.phase ?: displayItem.saga, displayItem.genres.take(2).joinToString(" • ").takeIf { it.isNotBlank() }).joinToString(" • "), color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (statuses.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { statuses.take(3).forEach { SmallStatusPill(it) } }
                } else {
                    Text(displayItem.imdbRating?.let { "IMDb $it" } ?: displayItem.tmdbRating?.let { "TMDb ${String.format("%.1f", it)}" }.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}





@Composable
@Composable
internal fun LibraryContextHeader(tab: String, count: Int, unit: String, sortLabel: String, filterSummary: String?) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(tabIdentityIcon(tab), contentDescription = null, modifier = Modifier.size(24.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(tab, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(listOf("$count $unit", sortLabel, filterSummary).filterNotNull().joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
internal fun LibrarySecondaryControls(sortMode: ViewingSortMode, onSort: (ViewingSortMode) -> Unit, statusFilter: ViewingUserStatus?, onStatus: (ViewingUserStatus?) -> Unit, genreFilter: String?, onGenre: (String?) -> Unit, genres: List<String>, catalogItems: List<ViewingItem>) {
    var sheetOpen by remember { mutableStateOf(false) }
    val summary = listOfNotNull(statusFilter?.libraryTitle, genreFilter).ifEmpty { listOf("All titles") }.joinToString(" • ")
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 1.dp) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Sort & filter", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("${sortMode.label} • $summary", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Button(onClick = { sheetOpen = true }, shape = RoundedCornerShape(20.dp)) { Icon(RhythmIcons.FilterList, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Sort & filter") }
        }
    }
    if (sheetOpen) {
        Dialog(onDismissRequest = { sheetOpen = false }) {
            Surface(shape = RoundedCornerShape(30.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 8.dp) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Sort & filter", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    CompactDropdown("Sort", sortMode.label, ViewingSortMode.entries, optionLabel = { it.label }, onSelect = onSort, modifier = Modifier.fillMaxWidth())
                    CompactDropdown("Genre", genreFilter ?: "All genres", listOf<String?>(null) + genres.take(32), optionLabel = { it ?: "All genres" }, onSelect = onGenre, modifier = Modifier.fillMaxWidth())
                    val statusOptions = listOf<ViewingUserStatus?>(null) + ViewingUserStatus.entries.filter { it != ViewingUserStatus.HIDDEN }
                    CompactDropdown("Status", statusFilter?.libraryTitle ?: "Any saved status", statusOptions, optionLabel = { it?.libraryTitle ?: "Any saved status" }, onSelect = onStatus, modifier = Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { onStatus(null); onGenre(null) }) { Text("Reset") }
                        Button(onClick = { sheetOpen = false }) { Text("Apply") }
                    }
                }
            }
        }
    }
}


@Composable

internal fun LibraryControlPanel(
    selectedTab: String,
    onTab: (String) -> Unit,
    sortMode: ViewingSortMode,
    onSort: (ViewingSortMode) -> Unit,
    statusFilter: ViewingUserStatus?,
    onStatus: (ViewingUserStatus?) -> Unit,
    catalogItems: List<ViewingItem>
) {
    CompactMenuCard(title = "Browse controls", subtitle = "Category • sort • saved status") {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CompactDropdown(
                label = "Category",
                selected = selectedTab,
                options = listOf("Continue", "Essential", "MCU", "DC", "Timeline", "Collections", "Saved"),
                onSelect = onTab,
                modifier = Modifier.weight(1f)
            )
            CompactDropdown(
                label = "Sort",
                selected = sortMode.label,
                options = listOf(ViewingSortMode.RELEASE, ViewingSortMode.CHRONOLOGICAL, ViewingSortMode.TITLE, ViewingSortMode.RATING, ViewingSortMode.RUNTIME, ViewingSortMode.GENRE),
                optionLabel = { it.label },
                onSelect = onSort,
                modifier = Modifier.weight(1f)
            )
        }
        val statusOptions = listOf<ViewingUserStatus?>(null) + ViewingUserStatus.entries.filter { it != ViewingUserStatus.HIDDEN }
        CompactDropdown(
            label = "Status",
            selected = statusFilter?.libraryTitle ?: "Any saved status",
            options = statusOptions,
            optionLabel = { it?.libraryTitle ?: "Any saved status" },
            onSelect = onStatus,
            modifier = Modifier.fillMaxWidth()
        )
        val summary = ViewingUserStatus.entries
            .filter { it != ViewingUserStatus.HIDDEN }
            .map { status -> status.libraryTitle to catalogItems.count { status in ViewingMetadataStore.statusesFor(it) } }
            .filter { it.second > 0 }
            .take(4)
        if (summary.isNotEmpty()) {
            Text(summary.joinToString(" • ") { "${it.first} ${it.second}" }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun SearchFilterMenu(
    selectedUniverse: String,
    onUniverse: (String) -> Unit,
    selectedType: String,
    onType: (String) -> Unit,
    genres: List<String>,
    selectedGenre: String?,
    onGenre: (String?) -> Unit,
    selectedCategory: ViewingSearchCategory,
    onCategory: (ViewingSearchCategory) -> Unit,
    sortMode: ViewingSearchSortMode,
    onSort: (ViewingSearchSortMode) -> Unit
) {
    CompactMenuCard(
        title = "Filters",
        subtitle = listOf(selectedUniverse, selectedType.replace('_', '-'), selectedGenre, selectedCategory.label, sortMode.label).filterNotNull().joinToString(" • ")
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CompactDropdown("Universe", selectedUniverse, listOf("All", "Marvel", "DC", "MCU", "DCEU", "DCU", "Elseworlds"), onSelect = onUniverse, modifier = Modifier.weight(1f))
            CompactDropdown("Type", selectedType.replace('_', '-'), listOf("All", "Movie", "Series", "Special", "Short", "One_Shot"), optionLabel = { it.replace('_', '-') }, onSelect = onType, modifier = Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CompactDropdown("Genre", selectedGenre ?: "All genres", listOf<String?>(null) + genres, optionLabel = { it ?: "All genres" }, onSelect = onGenre, modifier = Modifier.weight(1f))
            CompactDropdown("Category", selectedCategory.label, ViewingSearchCategory.entries, optionLabel = { it.label }, onSelect = onCategory, modifier = Modifier.weight(1f))
        }
        CompactDropdown("Sort", sortMode.label, ViewingSearchSortMode.entries, optionLabel = { it.label }, onSelect = onSort, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
internal fun CompactMenuCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    SpectrumGlassSurface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp)) {
        Column(Modifier.fillMaxWidth().padding(SpectrumSpacing.cardContentPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            SpectrumRhythmDivider(bars = 16)
            content()
        }
    }
}

@Composable
internal fun <T> CompactDropdown(
    label: String,
    selected: String,
    options: List<T>,
    modifier: Modifier = Modifier,
    optionLabel: (T) -> String = { it.toString() },
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    Box(modifier) {
        SpectrumPillTab(selected = false, onClick = { HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove); expanded = true }, modifier = Modifier.fillMaxWidth().semantics { contentDescription = "$label filter, selected $selected" }) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(selected, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(RhythmIcons.ExpandMore, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, shape = RoundedCornerShape(22.dp), containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
            options.forEach { option ->
                val optionText = optionLabel(option)
                DropdownMenuItem(
                    text = { Text(optionText, fontWeight = if (optionText == selected) FontWeight.Bold else FontWeight.Medium) },
                    onClick = { expanded = false; HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove); onSelect(option) },
                    leadingIcon = if (optionText == selected) ({ Icon(RhythmIcons.Check, contentDescription = null) }) else null
                )
            }
        }
    }
}


@Composable
internal fun LibraryTabs(selected: String, onTab: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.chipGap)) {
        items(listOf("Continue", "Essential", "MCU", "DC", "Timeline", "Collections", "Saved")) { tab ->
            SpectrumPillTab(
                selected = selected == tab,
                onClick = { onTab(tab) },
                modifier = Modifier.semantics { contentDescription = "Library tab $tab${if (selected == tab) ", selected" else ""}" }
            ) {
                Icon(tabIdentityIcon(tab), contentDescription = null)
                Text(tab)
            }
        }
    }
}

@Composable

internal fun SortChips(sortMode: ViewingSortMode, onSort: (ViewingSortMode) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.chipGap)) {
        items(listOf(ViewingSortMode.RELEASE, ViewingSortMode.CHRONOLOGICAL, ViewingSortMode.TITLE, ViewingSortMode.RATING, ViewingSortMode.RUNTIME)) { mode ->
            SpectrumPillTab(selected = sortMode == mode, onClick = { onSort(mode) }) { Text(mode.label) }
        }
    }
}

@Composable
internal fun StatusSelector(selected: Set<ViewingUserStatus>, onStatus: (ViewingUserStatus) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh), shape = RoundedCornerShape(26.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Your status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(selected.takeIf { it.isNotEmpty() }?.joinToString(" • ") { it.libraryTitle } ?: "Choose a status for this title", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Done" else "Edit") }
            }
            val visibleStatuses = listOf(ViewingUserStatus.BOOKMARKED, ViewingUserStatus.WATCHLIST, ViewingUserStatus.WATCH_LATER, ViewingUserStatus.WATCHING, ViewingUserStatus.WATCHED, ViewingUserStatus.FAVORITE, ViewingUserStatus.ON_HOLD, ViewingUserStatus.HIDDEN)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.chipGap), verticalArrangement = Arrangement.spacedBy(SpectrumSpacing.chipGap)) {
                visibleStatuses.filter { expanded || it in selected }.ifEmpty { visibleStatuses.take(4) }.forEach { status ->
                    val active = status in selected
                    FilterChip(
                        selected = active,
                        onClick = { onStatus(status) },
                        leadingIcon = { Icon(status.icon(), contentDescription = null) },
                        label = { Text(if (active) status.activeLabel else status.inactiveLabel) },
                        colors = statusChipColors(status, active)
                    )
                }
            }
        }
    }
}




internal fun LazyListScope.groupedViewingItems(items: List<ViewingItem>, sortMode: ViewingSortMode, onOpenTitle: (ViewingItem) -> Unit) {
    val grouped = items.groupBy { item -> item.phase ?: item.saga ?: item.universe ?: "Cinemaverse" }
    grouped.forEach { (phase, phaseItems) ->
        item("phase-$phase") { PhaseDivider(phase, phaseItems) }
        itemsIndexed(phaseItems, key = { index, item -> "$phase-${item.id}-$index" }) { index, item -> ViewingOrderRow(item, index + 1, onClick = { onOpenTitle(item) }) }
    }
}

@Composable
internal fun PhaseDivider(title: String, items: List<ViewingItem>) {
    SpectrumGlassSurface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), accent = spectrumUniverseAccent(title)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("${items.size} titles", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            SpectrumRhythmDivider(universe = title, bars = 14)
        }
    }
}

@Composable
internal fun SmallStatusPill(status: ViewingUserStatus) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
        Text(status.activeLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
internal fun statusChipColors(status: ViewingUserStatus, active: Boolean) = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
)



@Composable
internal fun LibraryPosterGrid(items: List<ViewingItem>, onOpenItem: (ViewingItem) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { items.chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) { row.forEach { item -> Box(Modifier.weight(1f)) { PosterCard(item) { onOpenItem(item) } } }; if (row.size == 1) Spacer(Modifier.weight(1f)) } } }
}

@Composable
internal fun CollectionCardGrid(lists: List<ViewingList>, onOpenList: (ViewingList) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(SpectrumSpacing.cardGap)) {
        lists.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.cardGap)) {
                row.forEach { list ->
                    PressableCard(
                        Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Open collection ${list.title}, ${list.items.size} titles" },
                        { onOpenList(list) }
                    ) {
                        CollectionArtwork(list, Modifier.fillMaxWidth().aspectRatio(1f).clip(SpectrumShapes.mediaFrame), ContentScale.Crop)
                        Spacer(Modifier.height(10.dp))
                        Text(list.title, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        SpectrumRhythmDivider(universe = list.universe ?: list.category, bars = 8)
                        Text("${list.items.size} titles", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
