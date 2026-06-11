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

@Composable
fun ViewingSearchScreen(
    onBack: () -> Unit,
    onOpenDetail: (ViewingItem) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewingViewModel: ViewingViewModel = viewModel()
) {
    val viewingState by viewingViewModel.uiState.collectAsState()
    val data = viewingState.data
    if (data == null) {
        ViewingCatalogLoadingState(
            title = "Loading Search",
            message = viewingState.errorMessage ?: "Preparing Cinemaverse search filters and results…",
            modifier = modifier,
            onBack = onBack
        )
        return
    }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedUniverse by rememberSaveable { mutableStateOf("All") }
    var selectedType by rememberSaveable { mutableStateOf("All") }
    var selectedGenre by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var sortMode by rememberSaveable { mutableStateOf(ViewingSearchSortMode.RELEVANCE) }
    var selectedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedItem = data.findItem(selectedItemId)
    if (selectedItem != null) {
        androidx.activity.compose.BackHandler { selectedItemId = null }
        ViewingDetailScreen(item = selectedItem, onBack = { selectedItemId = null }, modifier = modifier, viewingViewModel = viewingViewModel)
        return
    }

    val genres = remember(data) { data.allItems.flatMap { it.genres }.distinct().sorted().take(28) }
    val (rawItems, rawLists) = remember(query, data) { data.search(query) }
    val filteredItems = remember(query, selectedUniverse, selectedType, selectedGenre, selectedCategory, sortMode, rawItems, data) {
        rawItems.asSequence()
            .filter { selectedUniverse == "All" || it.universe == selectedUniverse || (selectedUniverse == "Marvel" && it.universe in setOf("MCU", "Marvel")) || (selectedUniverse == "DC" && it.universe in setOf("DCU", "DCEU", "Elseworlds")) }
            .filter { selectedType == "All" || it.type.name.equals(selectedType, ignoreCase = true) }
            .filter { selectedGenre == null || selectedGenre in it.genres }
            .filter { item ->
                when (selectedCategory) {
                    "Movies" -> item.type == ViewingType.MOVIE
                    "Series" -> item.type == ViewingType.SERIES || item.category?.contains("Series", true) == true
                    "Collections" -> true
                    "Saved" -> ViewingMetadataStore.statusesFor(item).isNotEmpty()
                    "Upcoming" -> item.status != ViewingStatus.RELEASED
                    else -> true
                }
            }
            .toList()
            .sortedForSearch(sortMode)
    }
    val matchingLists = remember(rawLists, selectedCategory) {
        rawLists.filter { list ->
            selectedCategory in setOf("All", "Collections") && list.importance != ViewingListImportance.HIDDEN
        }.distinctBy { it.id }.take(8)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Search") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(RhythmIcons.Back, contentDescription = "Back") } },
                actions = { SettingsIconAction(onOpenSettings) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(SpectrumSpacing.screenPadding, 10.dp, SpectrumSpacing.screenPadding, SpectrumSpacing.bottomSafePadding),
            verticalArrangement = Arrangement.spacedBy(SpectrumSpacing.cardGap)
        ) {
            item { ExpressiveSearchField(query, { query = it }) }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f)) { CategoryChipRail(selectedCategory) { selectedCategory = it } }
                    OutlinedButton(onClick = { showFilters = true }, shape = RoundedCornerShape(22.dp)) { Icon(RhythmIcons.FilterList, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Filters") }
                }
            }
            val recent = ViewingMetadataStore.recentItems(data).take(5)
            if (query.isBlank()) {
                if (recent.isNotEmpty()) item { ResultSection("Recently viewed", "Recent titles opened in Cinemaverse", recent, onOpen = { selectedItemId = it.id; ViewingMetadataStore.markViewed(it); onOpenDetail(it) }) }
                if (matchingLists.isNotEmpty()) item { ListRail("Featured collections", "Timelines, phases, sagas, and character arcs", matchingLists, onOpenList = {}) }
            } else {
                val topMatches = filteredItems.take(8)
                if (topMatches.isNotEmpty()) item { ResultSection("Top matches", "Best matches for your search", topMatches, onOpen = { selectedItemId = it.id; ViewingMetadataStore.markViewed(it); onOpenDetail(it) }) }
                if (matchingLists.isNotEmpty()) item { ListRail("Collections", "Related timelines and grouped stories", matchingLists, onOpenList = {}) }
                val remaining = filteredItems.drop(topMatches.size)
                if (remaining.isNotEmpty()) {
                    item { SectionHeader("All results", "${remaining.size} titles") }
                    groupedViewingItems(remaining, ViewingSortMode.PHASE) { item -> selectedItemId = item.id; ViewingMetadataStore.markViewed(item); onOpenDetail(item) }
                }
                if (filteredItems.isEmpty() && matchingLists.isEmpty()) item { EmptyState("No results found", "Try a title, phase, genre, actor, or collection.") }
            }
        }
    }
    if (showFilters) {
        SearchFiltersDialog(
            genres = genres,
            selectedUniverse = selectedUniverse,
            onUniverse = { selectedUniverse = it },
            selectedType = selectedType,
            onType = { selectedType = it },
            selectedGenre = selectedGenre,
            onGenre = { selectedGenre = it },
            sortMode = sortMode,
            onSort = { sortMode = it },
            onDismiss = { showFilters = false },
            onReset = { selectedUniverse = "All"; selectedType = "All"; selectedGenre = null; sortMode = ViewingSearchSortMode.RELEVANCE }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable

internal fun SearchCompactFilters(
    genres: List<String>,
    selectedGenre: String?,
    onGenre: (String?) -> Unit,
    sortMode: ViewingSearchSortMode,
    onSort: (ViewingSearchSortMode) -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CompactDropdown(
            label = "Genre",
            selected = selectedGenre ?: "All genres",
            options = listOf<String?>(null) + genres,
            optionLabel = { it ?: "All genres" },
            onSelect = onGenre,
            modifier = Modifier.weight(1f)
        )
        CompactDropdown(
            label = "Sort",
            selected = sortMode.label,
            options = ViewingSearchSortMode.entries,
            optionLabel = { it.label },
            onSelect = onSort,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
internal fun ExpressiveSearchField(query: String, onQuery: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQuery,
        placeholder = { Text("Search movies, shows, phases, genres, or cast") },
        leadingIcon = { Icon(RhythmIcons.Search, contentDescription = null) },
        trailingIcon = { if (query.isNotBlank()) IconButton(onClick = { onQuery("") }) { Icon(RhythmIcons.Close, contentDescription = "Clear search") } },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
internal fun SearchChipRail(title: String, values: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.chipGap)) {
            items(values) { value ->
                SpectrumPillTab(
                    selected = selected == value,
                    onClick = { onSelect(value) },
                    modifier = Modifier.semantics { contentDescription = "$title filter ${value.replace('_', '-')}${if (selected == value) ", selected" else ""}" }
                ) {
                    if (selected == value) Icon(RhythmIcons.Check, contentDescription = null)
                    Text(value.replace('_', '-'))
                }
            }
        }
    }
}

@Composable
internal fun GenreChipRail(genres: List<String>, selected: String?, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Genres", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.chipGap)) {
            items(genres) { genre ->
                SpectrumPillTab(selected = selected == genre, onClick = { onSelect(genre) }, modifier = Modifier.semantics { contentDescription = "Genre filter $genre${if (selected == genre) ", selected" else ""}" }) {
                    SpectrumPulseIndicator(active = selected == genre, universe = genre)
                    Text(genre)
                }
            }
        }
    }
}

@Composable
internal fun CategoryChipRail(selected: String, onSelect: (String) -> Unit) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    LazyRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.chipGap)) {
        items(listOf("All", "Movies", "Series", "Collections", "Saved", "Upcoming")) { category ->
            SpectrumPillTab(selected = selected == category, onClick = { HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove); onSelect(category) }, modifier = Modifier.semantics { contentDescription = "Search category $category${if (selected == category) ", selected" else ""}" }) {
                if (selected == category) Icon(RhythmIcons.Check, contentDescription = null)
                Text(category)
            }
        }
    }
}

@Composable
internal fun SearchFiltersDialog(
    genres: List<String>,
    selectedUniverse: String,
    onUniverse: (String) -> Unit,
    selectedType: String,
    onType: (String) -> Unit,
    selectedGenre: String?,
    onGenre: (String?) -> Unit,
    sortMode: ViewingSearchSortMode,
    onSort: (ViewingSearchSortMode) -> Unit,
    onDismiss: () -> Unit,
    onReset: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(30.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 8.dp) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Filters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                SearchChipRail("Universe", listOf("All", "Marvel", "DC", "MCU", "DCEU", "DCU", "Elseworlds"), selectedUniverse) { HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove); onUniverse(it) }
                SearchChipRail("Type", listOf("All", "Movie", "Series", "Special", "Short", "One_Shot"), selectedType) { HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove); onType(it) }
                SearchCompactFilters(genres, selectedGenre, { HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove); onGenre(it) }, sortMode, { HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove); onSort(it) })
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove); onReset() }) { Text("Reset") }
                    Button(onClick = { HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove); onDismiss() }) { Text("Apply") }
                }
            }
        }
    }
}


@Composable
internal fun SearchSortRail(sortMode: ViewingSearchSortMode, onSort: (ViewingSearchSortMode) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.chipGap)) {
        items(ViewingSearchSortMode.entries) { mode -> SpectrumPillTab(selected = sortMode == mode, onClick = { onSort(mode) }) { Text(mode.label) } }
    }
}

@Composable
internal fun ResultSection(title: String, subtitle: String, items: List<ViewingItem>, onOpen: (ViewingItem) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SpectrumSectionHeader(title = title, subtitle = subtitle)
        SpectrumRhythmDivider(bars = 18)
        items.forEachIndexed { index, item -> ViewingOrderRow(item, index + 1, onClick = { onOpen(item) }) }
    }
}


internal fun List<ViewingItem>.sortedForSearch(mode: ViewingSearchSortMode): List<ViewingItem> = when (mode) {
    ViewingSearchSortMode.RELEASE_DATE -> sortedFor(ViewingSortMode.RELEASE)
    ViewingSearchSortMode.CHRONOLOGICAL -> sortedFor(ViewingSortMode.CHRONOLOGICAL)
    ViewingSearchSortMode.RATING -> sortedFor(ViewingSortMode.RATING)
    ViewingSearchSortMode.RUNTIME -> sortedFor(ViewingSortMode.RUNTIME)
    ViewingSearchSortMode.TITLE -> sortedFor(ViewingSortMode.TITLE)
    ViewingSearchSortMode.RELEVANCE -> this
}
