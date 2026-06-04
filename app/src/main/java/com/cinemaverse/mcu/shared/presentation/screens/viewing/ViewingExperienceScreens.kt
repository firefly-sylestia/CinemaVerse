@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.cinemaverse.mcu.shared.presentation.screens.viewing

import android.content.Intent
import android.net.Uri

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledIconButton
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
import com.cinemaverse.mcu.R
import com.cinemaverse.mcu.shared.data.service.ViewingMetadataStore
import com.cinemaverse.mcu.shared.data.viewing.McuAssetDataSource
import com.cinemaverse.mcu.shared.data.viewing.ViewingCastMember
import com.cinemaverse.mcu.shared.data.viewing.ViewingItem
import com.cinemaverse.mcu.shared.data.viewing.ViewingList
import com.cinemaverse.mcu.shared.data.viewing.ViewingListImportance
import com.cinemaverse.mcu.shared.data.viewing.ViewingSearchCategory
import com.cinemaverse.mcu.shared.data.viewing.ViewingSearchSortMode
import com.cinemaverse.mcu.shared.data.viewing.ViewingSortMode
import com.cinemaverse.mcu.shared.data.viewing.ViewingStatus
import com.cinemaverse.mcu.shared.data.viewing.ViewingType
import com.cinemaverse.mcu.shared.data.viewing.ViewingUserStatus
import com.cinemaverse.mcu.shared.data.viewing.ViewingTrailer
import com.cinemaverse.mcu.shared.presentation.components.icons.Icon
import com.cinemaverse.mcu.shared.presentation.components.icons.MaterialSymbolIcon
import com.cinemaverse.mcu.shared.presentation.components.icons.RhythmIcons
import com.cinemaverse.mcu.shared.presentation.components.viewing.YouTubeTrailerWebPlayer
import com.cinemaverse.mcu.shared.util.ViewingArtworkUtils
import kotlinx.coroutines.delay


private object ViewingUi {
    val screenHPad = 20.dp
    val topPad = 30.dp
    val bottomPad = 120.dp
    val sectionGap = 22.dp
    val cardGap = 14.dp
    val chipGap = 8.dp
    val cardPad = 16.dp
    val posterWidth = 146.dp
    val rowPosterWidth = 58.dp
    val rowPosterHeight = 86.dp
    val heroHeight = 288.dp
    fun pressSpec() = tween<Float>(durationMillis = 160, easing = FastOutSlowInEasing)
}

@Composable
fun ViewingHomeScreen(
    onOpenLibrary: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDetail: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    homeReselectionKey: Int = 0
) {
    val context = LocalContext.current
    LaunchedEffect(context) { ViewingMetadataStore.initialize(context) }
    val data = remember(context) { McuAssetDataSource.load(context) }
    var selectedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedListId by rememberSaveable { mutableStateOf<String?>(null) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(homeReselectionKey) {
        selectedItemId = null
        selectedListId = null
        showSearch = false
    }
    val selectedItem = data.findItem(selectedItemId)
    val selectedList = data.findList(selectedListId)

    androidx.activity.compose.BackHandler(enabled = selectedItem != null || selectedList != null || showSearch) {
        when {
            selectedItem != null -> selectedItemId = null
            selectedList != null -> selectedListId = null
            showSearch -> showSearch = false
        }
    }

    when {
        selectedItem != null -> ViewingDetailScreen(item = selectedItem, list = selectedList, onBack = { selectedItemId = null })
        selectedList != null -> ViewingListDetailScreen(list = selectedList, onBack = { selectedListId = null }, onOpenTitle = { selectedItemId = it.id })
        showSearch -> ViewingSearchScreen(onBack = { showSearch = false }, onOpenDetail = { selectedItemId = it.id }, onOpenSettings = onOpenSettings)
        else -> ViewingHomeContent(
            data = data,
            onOpenLibrary = onOpenLibrary,
            onOpenSearch = { showSearch = true; onOpenSearch() },
            onOpenSettings = onOpenSettings,
            onOpenItem = { selectedItemId = it.id; ViewingMetadataStore.markViewed(it); onOpenDetail() },
            onOpenList = { selectedListId = it.id },
            modifier = modifier
        )
    }
}

@Composable
private fun ViewingHomeContent(
    data: McuAssetDataSource.ViewingAssetData,
    onOpenLibrary: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenItem: (ViewingItem) -> Unit,
    onOpenList: (ViewingList) -> Unit,
    modifier: Modifier = Modifier
) {
    val marvel = remember(data) { data.allItems.filter { it.universe == "MCU" }.take(14) }
    val dc = remember(data) { data.allItems.filter { it.universe in setOf("DCU", "DCEU", "Elseworlds") }.take(14) }
    val lists = remember(data) { data.allLists.visibleManagedLists().take(8) }
    val recent = ViewingMetadataStore.recentItems(data)
    val continueItems = remember(data, recent) {
        (recent + data.allItems.filter { item ->
            val statuses = ViewingMetadataStore.statusesFor(item)
            ViewingUserStatus.WATCHING in statuses || ViewingUserStatus.WATCH_LATER in statuses
        }).distinctBy { it.id }.take(12)
    }
    val trailerItems = remember(data) { data.allItems.filter { it.hasAnyTrailer() }.take(16) }
    val upcomingItems = remember(data) { data.allItems.filter { it.status == ViewingStatus.UPCOMING || it.status == ViewingStatus.ANNOUNCED }.take(14) }
    val becauseYouWatched = remember(data, recent) {
        val last = recent.firstOrNull()
        if (last == null) emptyList() else data.allItems.filter { item ->
            item.id != last.id && (item.universe == last.universe || item.franchise == last.franchise || item.saga == last.saga || item.genres.any(last.genres::contains))
        }.take(12)
    }
    var trailerPreview by remember { mutableStateOf<ViewingItem?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(ViewingUi.screenHPad, ViewingUi.topPad, ViewingUi.screenHPad, ViewingUi.bottomPad),
        verticalArrangement = Arrangement.spacedBy(ViewingUi.sectionGap)
    ) {
        item { CinemaverseHeader(onOpenSearch = onOpenSearch, onOpenSettings = onOpenSettings) }
        item {
            FeaturedTitleCarousel(
                items = remember(data) { data.homeFeaturedTitles() },
                featuredList = data.featuredList,
                onOpenItem = onOpenItem,
                onOpenLibrary = onOpenLibrary
            )
        }
        if (recent.isNotEmpty()) item { CinemaActivityMiniSurface(recent.first(), onClick = { onOpenItem(recent.first()) }) }
        if (continueItems.isNotEmpty()) item { PosterRail("Continue watching", "Recently opened, watching, and watch-later titles", continueItems, onOpenItem) }
        if (trailerItems.isNotEmpty()) item { TrailerRail("Trailers", "Preview the latest available title trailers", trailerItems, onOpenTrailer = { trailerPreview = it }) }
        if (upcomingItems.isNotEmpty()) item { PosterRail("Upcoming", "Announced and upcoming Cinemaverse releases", upcomingItems, onOpenItem) }
        if (becauseYouWatched.isNotEmpty()) item { PosterRail("Because you watched ${recent.first().title}", recent.first().universe ?: recent.first().franchise ?: "Personal picks", becauseYouWatched, onOpenItem) }
        item { PosterRail("MCU", "Marvel Studios films, shows, specials, One-Shots, and Defenders", marvel, onOpenItem) }
        item { PosterRail("DC", "DCU, DCEU, Elseworlds, and connected TV", dc, onOpenItem) }
        item { ListRail("Managed collections", "Essentials, timelines, chapters, and character journeys", lists, onOpenList) }
    }
    trailerPreview?.let { item -> TrailerPlayerDialog(item = item, onOpenDetails = { trailerPreview = null; onOpenItem(item) }, onDismiss = { trailerPreview = null }) }
}

@Composable
fun ViewingLibraryScreen(
    onOpenDetail: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(context) { ViewingMetadataStore.initialize(context) }
    val data = remember(context) { McuAssetDataSource.load(context) }
    var tab by rememberSaveable { mutableStateOf("Essentials") }
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
        selectedItem != null -> ViewingDetailScreen(item = selectedItem, list = selectedList, onBack = { selectedItemId = null })
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
                contentPadding = PaddingValues(ViewingUi.screenHPad, ViewingUi.topPad, ViewingUi.screenHPad, ViewingUi.bottomPad),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item { CinemaverseHeader(title = "Library", subtitle = "Essentials • Continue • MCU • DC • Timeline • Collections • Saved", onOpenSettings = onOpenSettings) }
                item { LibraryTabs(tab, onTab = { tab = it }) }
                item { LibrarySecondaryControls(sortMode, { sortMode = it }, statusFilter, { statusFilter = it }, genreFilter, { genreFilter = it }, genres, data.allItems) }
                if (tab == "Collections") {
                    items(data.allLists.visibleManagedLists(), key = { it.id }) { list -> WideListCard(list, onClick = { selectedListId = list.id }) }
                } else {
                    if (filtered.isEmpty()) item { EmptyState("Nothing here yet", "Open a title and add it to Watchlist, Favorite, or Watched.") }
                    groupedViewingItems(filtered, sortMode) { item -> selectedItemId = item.id; ViewingMetadataStore.markViewed(item); onOpenDetail() }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewingSearchScreen(
    onBack: () -> Unit,
    onOpenDetail: (ViewingItem) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(context) { ViewingMetadataStore.initialize(context) }
    val data = remember(context) { McuAssetDataSource.load(context) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedUniverse by rememberSaveable { mutableStateOf("All") }
    var selectedType by rememberSaveable { mutableStateOf("All") }
    var selectedGenre by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCategory by rememberSaveable { mutableStateOf(ViewingSearchCategory.ESSENTIAL) }
    var sortMode by rememberSaveable { mutableStateOf(ViewingSearchSortMode.RELEVANCE) }
    var selectedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedItem = data.findItem(selectedItemId)
    if (selectedItem != null) {
        androidx.activity.compose.BackHandler { selectedItemId = null }
        ViewingDetailScreen(item = selectedItem, onBack = { selectedItemId = null }, modifier = modifier)
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
                    ViewingSearchCategory.ESSENTIAL -> data.allLists.any { it.importance == ViewingListImportance.PRIMARY && item.id in it.itemIds }
                    ViewingSearchCategory.SERIES -> item.type == ViewingType.SERIES || item.category?.contains("Series", true) == true
                    ViewingSearchCategory.SPECIALS -> item.type in setOf(ViewingType.SPECIAL, ViewingType.SHORT, ViewingType.ONE_SHOT)
                    ViewingSearchCategory.UPCOMING -> item.status != ViewingStatus.RELEASED
                    ViewingSearchCategory.SAVED -> ViewingMetadataStore.statusesFor(item).isNotEmpty()
                    else -> true
                }
            }
            .toList()
            .sortedForSearch(sortMode)
    }
    val matchingLists = remember(rawLists, selectedCategory) {
        rawLists.filter { list ->
            when (selectedCategory) {
                ViewingSearchCategory.ESSENTIAL -> list.importance == ViewingListImportance.PRIMARY
                ViewingSearchCategory.COLLECTIONS -> list.importance in setOf(ViewingListImportance.PRIMARY, ViewingListImportance.SECONDARY)
                ViewingSearchCategory.PHASES -> list.category?.contains("Phase", true) == true || !list.phase.isNullOrBlank()
                ViewingSearchCategory.SAGAS -> !list.saga.isNullOrBlank()
                ViewingSearchCategory.RELEASE_ORDER -> list.category?.contains("Release", true) == true
                ViewingSearchCategory.TIMELINE -> list.category?.contains("Chronological", true) == true
                else -> list.importance != ViewingListImportance.HIDDEN
            }
        }.distinctBy { it.id }.take(8)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Cinemaverse Search") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(RhythmIcons.Back, contentDescription = "Back") } },
                actions = { SettingsIconAction(onOpenSettings) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(ViewingUi.screenHPad, 10.dp, ViewingUi.screenHPad, ViewingUi.bottomPad),
            verticalArrangement = Arrangement.spacedBy(ViewingUi.cardGap)
        ) {
            item { ExpressiveSearchField(query, { query = it }) }
            item { SearchChipRail("Universe", listOf("All", "Marvel", "DC", "MCU", "DCEU", "DCU", "Elseworlds"), selectedUniverse) { selectedUniverse = it } }
            item { SearchChipRail("Type", listOf("All", "Movie", "Series", "Special", "Short", "One_Shot"), selectedType) { selectedType = it } }
            item { CategoryChipRail(selectedCategory) { selectedCategory = it } }
            item { SearchCompactFilters(genres, selectedGenre, { selectedGenre = it }, sortMode, { sortMode = it }) }
            val recent = ViewingMetadataStore.recentItems(data).filter { it in filteredItems }.take(5)
            if (recent.isNotEmpty()) item { ResultSection("Recently viewed", "Last opened in Cinemaverse", recent, onOpen = { selectedItemId = it.id; ViewingMetadataStore.markViewed(it); onOpenDetail(it) }) }
            if (matchingLists.isNotEmpty()) item { ListRail("Matching collections", "Essential and generated orders", matchingLists, onOpenList = {}) }
            val topMatches = filteredItems.filterNot { it in recent }.take(8)
            if (topMatches.isNotEmpty()) item { ResultSection("Top matches", "Best title matches for your filters", topMatches, onOpen = { selectedItemId = it.id; ViewingMetadataStore.markViewed(it); onOpenDetail(it) }) }
            val remaining = filteredItems.drop(topMatches.size).filterNot { it in recent }
            if (remaining.isNotEmpty()) {
                item { SectionHeader("All results", "${remaining.size} more titles grouped by phase/chapter") }
                groupedViewingItems(remaining, ViewingSortMode.PHASE) { item -> selectedItemId = item.id; ViewingMetadataStore.markViewed(item); onOpenDetail(item) }
            }
            if (filteredItems.isEmpty() && matchingLists.isEmpty()) item { EmptyState("No viewing results", "Try Marvel, DC, Timeline, Trailers, Specials, or clear a filter.") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewingDetailScreen(
    item: ViewingItem? = null,
    list: ViewingList? = null,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(context) { ViewingMetadataStore.initialize(context) }
    val data = remember(context) { McuAssetDataSource.load(context) }
    var relatedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    val nestedRelated = data.findItem(relatedItemId)
    if (nestedRelated != null) {
        ViewingDetailScreen(item = nestedRelated, list = list, onBack = { relatedItemId = null }, modifier = modifier)
        return
    }
    val baseSelected = item ?: data.featuredItem
    LaunchedEffect(baseSelected.id) { ViewingMetadataStore.markViewed(baseSelected) }
    val selected = rememberEnrichedItem(baseSelected)
    var showTrailer by rememberSaveable(selected.id) { mutableStateOf(false) }
    val userStatuses = ViewingMetadataStore.statusesFor(selected)
    val hasTrailer = selected.hasAnyTrailer()
    val haptics = LocalHapticFeedback.current
    val related = remember(selected, data) {
        data.allItems.filter { it.id != selected.id && (it.franchise == selected.franchise || it.universe == selected.universe || it.genres.any(selected.genres::contains)) }.take(12)
    }
    val accent = if (selected.universe == "MCU") Color(0xFFE62429) else Color(0xFF2F80ED)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selected.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(RhythmIcons.Back, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
            )
        },
        modifier = modifier
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            accent.copy(alpha = 0.28f),
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(ViewingUi.screenHPad, 12.dp, ViewingUi.screenHPad, ViewingUi.bottomPad),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(34.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f))
                    ) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(Modifier.fillMaxWidth().height(430.dp).clip(RoundedCornerShape(28.dp))) {
                                PosterBackdrop(selected, Modifier.fillMaxSize(), ContentScale.Crop)
                                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)))))
                                Column(
                                    Modifier.align(Alignment.BottomStart).padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(selected.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    Text(
                                        listOfNotNull(selected.year, selected.runtime, selected.universe, selected.phase ?: selected.saga).joinToString(" • "),
                                        color = Color.White.copy(alpha = 0.86f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (selected.genres.isNotEmpty()) FlowChips(selected.genres.take(4))
                                }
                                if (hasTrailer) {
                                    FilledIconButton(
                                        onClick = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); showTrailer = true },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                                    ) { Icon(RhythmIcons.Play, contentDescription = "Play trailer for ${selected.title}") }
                                }
                            }
                            Text(selected.overview ?: selected.plot ?: selected.description ?: "No overview available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (hasTrailer) {
                                Button(
                                    onClick = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); showTrailer = true },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(RhythmIcons.Play, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Watch trailer")
                                }
                            }
                            StatusSelector(userStatuses) { next ->
                                ViewingMetadataStore.toggleStatus(selected, next)
                            }
                        }
                    }
                }
                item { MetadataGrid(selected) }
                item { CreditsBlock(selected) }
                if (related.isNotEmpty()) item { PosterRail("Related titles", selected.franchise ?: selected.universe ?: "Similar genre", related, onOpenItem = { relatedItemId = it.id }) }
                item { Text("Metadata source: ${selected.metadataSource} • Updated ${selected.lastUpdated ?: "offline catalog"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
    if (showTrailer) {
        TrailerPlayerDialog(
            item = selected,
            onOpenDetails = { showTrailer = false },
            onDismiss = { showTrailer = false }
        )
    }
}

@Composable
private fun TrailerPlayerDialog(item: ViewingItem, onOpenDetails: () -> Unit, onDismiss: () -> Unit) {
    val displayItem = rememberEnrichedItem(item)
    val context = LocalContext.current
    var expanded by rememberSaveable(displayItem.id) { mutableStateOf(false) }
    val trailerOptions = remember(displayItem) { displayItem.availableTrailers() }
    var selectedTrailerIndex by rememberSaveable(displayItem.id, trailerOptions.size) { mutableStateOf(0) }
    val selectedTrailer = trailerOptions.getOrNull(selectedTrailerIndex)
    val trailerAvailable = selectedTrailer != null
    val openYouTube = selectedTrailer?.externalUrl()?.let { url -> { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } } }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = !expanded)
    ) {
        Surface(
            shape = RoundedCornerShape(if (expanded) 0.dp else 34.dp),
            color = if (expanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = if (expanded) 0.dp else 8.dp,
            modifier = (if (expanded) Modifier.fillMaxSize().padding(12.dp) else Modifier.fillMaxWidth().padding(horizontal = 10.dp))
        ) {
            Column(Modifier.padding(if (expanded) 12.dp else 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PosterBackdrop(displayItem, Modifier.size(46.dp, 64.dp), ContentScale.Crop, RoundedCornerShape(14.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Trailer preview", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(displayItem.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(listOfNotNull(selectedTrailer?.label, displayItem.year, displayItem.runtime, displayItem.universe).joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    FilledIconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.semantics { contentDescription = if (expanded) "Collapse trailer preview" else "Expand trailer preview" },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) { Icon(if (expanded) RhythmIcons.ExpandLess else RhythmIcons.ExpandMore, contentDescription = null) }
                    FilledIconButton(
                        enabled = openYouTube != null,
                        onClick = { openYouTube?.invoke() },
                        modifier = Modifier.semantics { contentDescription = "Open trailer on YouTube" },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) { Icon(RhythmIcons.OpenInNew, contentDescription = null) }
                    FilledIconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest, contentColor = MaterialTheme.colorScheme.onSurface)
                    ) { Icon(RhythmIcons.Close, contentDescription = "Close trailer preview") }
                }
                if (trailerOptions.size > 1) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.chipGap)) {
                        itemsIndexed(trailerOptions, key = { index, trailer -> "${trailer.label}-$index" }) { index, trailer ->
                            FilterChip(
                                selected = index == selectedTrailerIndex,
                                onClick = { selectedTrailerIndex = index },
                                label = { Text(trailer.label) },
                                leadingIcon = if (index == selectedTrailerIndex) ({ Icon(RhythmIcons.Check, contentDescription = null) }) else null
                            )
                        }
                    }
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .then(if (expanded) Modifier.weight(1f) else Modifier.aspectRatio(16f / 9f))
                        .clip(RoundedCornerShape(if (expanded) 22.dp else 26.dp))
                        .background(Color.Black)
                        .semantics { contentDescription = "Video player region for ${displayItem.title} trailer" },
                    contentAlignment = Alignment.Center
                ) {
                    YouTubeTrailerWebPlayer(
                        youtubeVideoId = selectedTrailer?.youtubeVideoId,
                        trailerUrl = selectedTrailer?.url,
                        title = displayItem.title,
                        shape = RoundedCornerShape(if (expanded) 22.dp else 26.dp),
                        modifier = Modifier.fillMaxSize()
                    )
                    if (!trailerAvailable) {
                        Text("Trailer unavailable", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onOpenDetails, shape = RoundedCornerShape(22.dp)) { Text("Details") }
                    OutlinedButton(onClick = { ViewingMetadataStore.toggleStatus(displayItem, ViewingUserStatus.WATCH_LATER) }, shape = RoundedCornerShape(22.dp)) { Text("Watch later") }
                    OutlinedButton(onClick = { ViewingMetadataStore.toggleStatus(displayItem, ViewingUserStatus.FAVORITE) }, shape = RoundedCornerShape(22.dp)) { Text("Favorite") }
                    OutlinedButton(onClick = { ViewingMetadataStore.toggleStatus(displayItem, ViewingUserStatus.WATCHED) }, shape = RoundedCornerShape(22.dp)) { Text("Mark watched") }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewingListDetailScreen(list: ViewingList, onBack: () -> Unit, onOpenTitle: (ViewingItem) -> Unit) {
    var showOrderHelp by rememberSaveable { mutableStateOf(false) }
    val firstPlayable = list.items.firstOrNull { ViewingUserStatus.WATCHED !in ViewingMetadataStore.statusesFor(it) } ?: list.items.firstOrNull()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(list.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(RhythmIcons.Back, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
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
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(ViewingUi.screenHPad, 14.dp, ViewingUi.screenHPad, ViewingUi.bottomPad),
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
private fun ViewingOrderEducationChip(list: ViewingList, onWhy: () -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(RhythmIcons.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(list.orderingBasisText(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text("Numbers reflect this visible collection order.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.78f))
            }
            AssistChip(onClick = onWhy, label = { Text("Why this order?") })
        }
    }
}

@Composable
private fun ViewingOrderHelpDialog(list: ViewingList, onDismiss: () -> Unit) {
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
private fun OrderHelpLine(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun ViewingList.orderingBasisText(): String = when {
    category?.contains("Chronological", ignoreCase = true) == true -> "Ordered by in-universe chronology."
    category?.contains("Release", ignoreCase = true) == true -> "Ordered by public release sequence."
    !phase.isNullOrBlank() -> "Ordered inside $phase."
    !saga.isNullOrBlank() -> "Ordered inside $saga."
    !franchise.isNullOrBlank() -> "Ordered as a curated $franchise journey."
    else -> "Ordered as a curated ${category ?: universe ?: "Cinemaverse"} collection."
}

@Composable
private fun CinemaverseHeader(title: String = "Cinemaverse", subtitle: String = "Marvel • DC • Release orders • Trailers", onOpenSearch: (() -> Unit)? = null, onOpenSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer, tonalElevation = 3.dp) {
            Image(painter = painterResource(R.drawable.ic_cinemaverse), contentDescription = "Cinemaverse", modifier = Modifier.size(58.dp).padding(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (onOpenSearch != null) {
            FilledIconButton(onClick = onOpenSearch, colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                Icon(RhythmIcons.Search, contentDescription = "Search Cinemaverse")
            }
        }
        SettingsIconAction(onOpenSettings)
    }
}

@Composable
private fun CollectionAlbumHero(list: ViewingList, onPlayFirst: () -> Unit, onShuffle: () -> Unit) {
    val watchedCount = list.items.count { ViewingUserStatus.WATCHED in ViewingMetadataStore.statusesFor(it) }
    var expanded by remember { mutableStateOf(false) }
    val poster = ViewingArtworkUtils.resolvePoster(list, ViewingMetadataStore.useLocalPosters.value)
        ?: ViewingArtworkUtils.resolveBackdrop(list, ViewingMetadataStore.useLocalPosters.value)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)),
        shape = RoundedCornerShape(34.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(Modifier.fillMaxWidth()) {
            ArtworkImage(poster, "${list.title} background", Modifier.matchParentSize(), ContentScale.Crop)
            Box(Modifier.matchParentSize().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)))
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    CollectionArtwork(list, Modifier.size(132.dp).clip(RoundedCornerShape(38.dp)), ContentScale.Crop)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(list.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Text(list.category ?: list.universe ?: "Cinemaverse collection", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$watchedCount of ${list.items.size} watched", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CollectionPill("${list.items.size} titles")
                            CollectionPill(list.phase ?: list.saga ?: "Curated")
                        }
                    }
                    Box {
                        FilledIconButton(
                            onClick = { expanded = true },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) { Icon(RhythmIcons.More, contentDescription = "More actions for ${list.title}") }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
                            DropdownMenuItem(text = { Text("Save list") }, onClick = { expanded = false; list.items.forEach { if (ViewingUserStatus.BOOKMARKED !in ViewingMetadataStore.statusesFor(it)) ViewingMetadataStore.toggleStatus(it, ViewingUserStatus.BOOKMARKED) } })
                            DropdownMenuItem(text = { Text("Mark all watched") }, onClick = { expanded = false; list.items.forEach { if (ViewingUserStatus.WATCHED !in ViewingMetadataStore.statusesFor(it)) ViewingMetadataStore.toggleStatus(it, ViewingUserStatus.WATCHED) } })
                            DropdownMenuItem(text = { Text("Clear watched") }, onClick = { expanded = false; list.items.forEach { if (ViewingUserStatus.WATCHED in ViewingMetadataStore.statusesFor(it)) ViewingMetadataStore.toggleStatus(it, ViewingUserStatus.WATCHED) } })
                        }
                    }
                }
                Text(list.description.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onPlayFirst, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp)) {
                        Icon(RhythmIcons.Play, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Start")
                    }
                    OutlinedButton(onClick = onShuffle, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp)) {
                        Icon(RhythmIcons.Shuffle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Shuffle")
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionPill(text: String) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.70f)) {
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun CollectionTrackHeader(count: Int) {
    Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text("Titles", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
            Text("$count total", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primaryContainer) {
            Text(count.toString(), modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun CollectionTitleRow(item: ViewingItem, order: Int, onClick: () -> Unit) {
    val displayItem = rememberCachedItem(item)
    var expanded by remember { mutableStateOf(false) }
    var showTrailer by rememberSaveable(displayItem.id) { mutableStateOf(false) }
    val hasTrailer = displayItem.hasAnyTrailer()
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(order.toString().padStart(2, '0'), modifier = Modifier.width(32.dp).semantics { contentDescription = "Number $order in this collection" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            PosterBackdrop(displayItem, Modifier.size(56.dp).clip(RoundedCornerShape(18.dp)), ContentScale.Crop)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(displayItem.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(listOfNotNull(displayItem.runtime, displayItem.year, displayItem.universe).joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box {
                FilledIconButton(
                    onClick = { expanded = true },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) { Icon(RhythmIcons.More, contentDescription = "More actions for ${displayItem.title}") }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    DropdownMenuItem(text = { Text("Open details", color = MaterialTheme.colorScheme.onSurface) }, onClick = { expanded = false; onClick() })
                    if (hasTrailer) DropdownMenuItem(text = { Text("Play trailer", color = MaterialTheme.colorScheme.onSurface) }, onClick = { expanded = false; showTrailer = true })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                    DropdownMenuItem(text = { Text("Add to watchlist", color = MaterialTheme.colorScheme.onSurface) }, onClick = { expanded = false; ViewingMetadataStore.toggleStatus(displayItem, ViewingUserStatus.WATCHLIST) })
                    DropdownMenuItem(text = { Text("Mark watched", color = MaterialTheme.colorScheme.onSurface) }, onClick = { expanded = false; ViewingMetadataStore.toggleStatus(displayItem, ViewingUserStatus.WATCHED) })
                    DropdownMenuItem(text = { Text("Favorite", color = MaterialTheme.colorScheme.onSurface) }, onClick = { expanded = false; ViewingMetadataStore.toggleStatus(displayItem, ViewingUserStatus.FAVORITE) })
                    DropdownMenuItem(text = { Text("Hide", color = MaterialTheme.colorScheme.onSurfaceVariant) }, onClick = { expanded = false; ViewingMetadataStore.toggleStatus(displayItem, ViewingUserStatus.HIDDEN) })
                }
            }
        }
    }
    if (showTrailer) TrailerPlayerDialog(item = displayItem, onOpenDetails = { showTrailer = false; onClick() }, onDismiss = { showTrailer = false })
}

@Composable
private fun HeroViewingCard(item: ViewingItem, list: ViewingList, onOpenDetail: () -> Unit, onOpenLibrary: () -> Unit) {
    val displayItem = rememberEnrichedItem(item)
    PressableCard(onClick = onOpenDetail, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(ViewingUi.heroHeight).clip(RoundedCornerShape(28.dp))) {
            PosterBackdrop(displayItem, Modifier.fillMaxSize(), ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Featured order", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(displayItem.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${list.title} • ${list.items.size} titles", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onOpenDetail) { Text("Details") }
                    OutlinedButton(onClick = onOpenLibrary) { Text("Library") }
                }
            }
        }
    }
}


@Composable
private fun FeaturedTitleCarousel(
    items: List<ViewingItem>,
    featuredList: ViewingList,
    onOpenItem: (ViewingItem) -> Unit,
    onOpenLibrary: () -> Unit
) {
    val carouselItems = if (items.isEmpty()) featuredList.items.take(6) else items
    val listState = rememberLazyListState()
    var selectedIndex by rememberSaveable(carouselItems.map { it.id }.joinToString()) { mutableStateOf(0) }
    var autoAdvance by rememberSaveable { mutableStateOf(true) }
    val visibleItem = carouselItems.getOrNull(selectedIndex) ?: return

    LaunchedEffect(carouselItems, selectedIndex, autoAdvance, listState.isScrollInProgress) {
        if (carouselItems.size <= 1 || !autoAdvance || listState.isScrollInProgress) return@LaunchedEffect
        delay(6_000)
        selectedIndex = (selectedIndex + 1) % carouselItems.size
    }
    LaunchedEffect(selectedIndex) {
        if (carouselItems.isNotEmpty() && listState.firstVisibleItemIndex != selectedIndex) listState.animateScrollToItem(selectedIndex)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Featured titles", "Auto-cycling Cinemaverse highlights with accessible controls")
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(ViewingUi.cardGap),
            modifier = Modifier.semantics { contentDescription = "Featured title carousel" }
        ) {
            itemsIndexed(carouselItems, key = { index, item -> "featured-${item.id}-$index" }) { index, item ->
                FeaturedTitleCarouselCard(
                    item = item,
                    list = featuredList,
                    selected = index == selectedIndex,
                    onClick = {
                        autoAdvance = false
                        selectedIndex = index
                        onOpenItem(item)
                    },
                    onLibrary = onOpenLibrary,
                    modifier = Modifier.fillParentMaxWidth(0.90f)
                )
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                FilledIconButton(
                    onClick = {
                        autoAdvance = false
                        selectedIndex = (selectedIndex - 1).floorMod(carouselItems.size)
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) { Icon(RhythmIcons.SkipPrevious, contentDescription = "Previous featured title") }
                FilledIconButton(
                    onClick = {
                        autoAdvance = !autoAdvance
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) { Text(if (autoAdvance) "Auto" else "Manual", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold) }
                FilledIconButton(
                    onClick = {
                        autoAdvance = false
                        selectedIndex = (selectedIndex + 1) % carouselItems.size
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) { Icon(RhythmIcons.SkipNext, contentDescription = "Next featured title") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                carouselItems.forEachIndexed { index, _ ->
                    Surface(
                        modifier = Modifier.size(width = if (index == selectedIndex) 22.dp else 8.dp, height = 8.dp),
                        shape = RoundedCornerShape(50),
                        color = if (index == selectedIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ) {}
                }
            }
        }
        Text(
            "Showing ${selectedIndex + 1} of ${carouselItems.size}: ${visibleItem.title}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FeaturedTitleCarouselCard(
    item: ViewingItem,
    list: ViewingList,
    selected: Boolean,
    onClick: () -> Unit,
    onLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayItem = rememberEnrichedItem(item)
    val border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.70f)) else null
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(30.dp),
        border = border,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier
    ) {
        Box(Modifier.fillMaxWidth().height(ViewingUi.heroHeight).clip(RoundedCornerShape(30.dp))) {
            PosterBackdrop(displayItem, Modifier.fillMaxSize(), ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(displayItem.universe ?: "Cinemaverse highlight", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(displayItem.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(listOfNotNull(displayItem.year, displayItem.runtime, displayItem.phase ?: displayItem.saga, list.title).joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onClick) { Text("Details") }
                    OutlinedButton(onClick = onLibrary) { Text("Library") }
                }
            }
        }
    }
}

private fun McuAssetDataSource.ViewingAssetData.homeFeaturedTitles(): List<ViewingItem> {
    val essentials = allLists.firstOrNull { it.id == "mcu-release-order" }?.items.orEmpty().take(5)
    val dcHighlights = allItems.filter { it.universe in setOf("DCU", "DCEU", "Elseworlds") && it.status == ViewingStatus.RELEASED }.take(4)
    val trailerReady = allItems.filter { it.status == ViewingStatus.RELEASED && (it.hasAnyTrailer()) && (ViewingArtworkUtils.resolvePoster(it) != null || ViewingArtworkUtils.resolveBackdrop(it) != null) }.take(6)
    val upcoming = allItems.filter { it.status == ViewingStatus.UPCOMING || it.status == ViewingStatus.ANNOUNCED }.take(3)
    return (listOf(featuredItem) + trailerReady + essentials + dcHighlights + upcoming).distinctBy { it.id }.take(10)
}

private fun Int.floorMod(size: Int): Int = ((this % size) + size) % size

@Composable
private fun PosterRail(title: String, subtitle: String, items: List<ViewingItem>, onOpenItem: (ViewingItem) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title, subtitle)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.cardGap)) {
            items(items, key = { it.id }) { PosterCard(it) { onOpenItem(it) } }
        }
    }
}


@Composable
private fun TrailerRail(title: String, subtitle: String, items: List<ViewingItem>, onOpenTrailer: (ViewingItem) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title, subtitle)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.cardGap)) {
            items(items, key = { "trailer-${it.id}" }) { item ->
                PosterCard(item) { onOpenTrailer(item) }
            }
        }
    }
}

@Composable
private fun ListRail(title: String, subtitle: String, lists: List<ViewingList>, onOpenList: (ViewingList) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title, subtitle)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.cardGap)) { items(lists, key = { it.id }) { ViewingListCard(it) { onOpenList(it) } } }
    }
}

@Composable
private fun PosterCard(item: ViewingItem, onClick: () -> Unit) {
    val displayItem = rememberCachedItem(item)
    PressableCard(onClick = onClick, modifier = Modifier.width(ViewingUi.posterWidth)) {
        PosterBackdrop(displayItem, Modifier.fillMaxWidth().aspectRatio(2f / 3f), ContentScale.Crop, RoundedCornerShape(22.dp))
        Spacer(Modifier.height(10.dp))
        Text(displayItem.title, modifier = Modifier.height(40.dp), maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
        Text(listOfNotNull(displayItem.year, displayItem.universe).joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ViewingListCard(list: ViewingList, onClick: () -> Unit) {
    PressableCard(onClick = onClick, modifier = Modifier.width(220.dp)) {
        CollectionArtwork(list, Modifier.fillMaxWidth().height(118.dp).clip(RoundedCornerShape(22.dp)), ContentScale.Crop)
        Spacer(Modifier.height(10.dp))
        Text(list.title, modifier = Modifier.height(40.dp), maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
        Text("${list.items.size} titles • ${list.category ?: list.universe ?: "Viewing order"}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun WideListCard(list: ViewingList, onClick: () -> Unit) {
    PressableCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CollectionArtwork(list, Modifier.size(64.dp, 88.dp).clip(RoundedCornerShape(18.dp)), ContentScale.Crop)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(list.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(list.description.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${list.items.size} titles • ${list.category ?: "Collection"}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ViewingOrderRow(item: ViewingItem, order: Int, onClick: () -> Unit) {
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
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
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
private fun LibrarySecondaryControls(
    sortMode: ViewingSortMode,
    onSort: (ViewingSortMode) -> Unit,
    statusFilter: ViewingUserStatus?,
    onStatus: (ViewingUserStatus?) -> Unit,
    genreFilter: String?,
    onGenre: (String?) -> Unit,
    genres: List<String>,
    catalogItems: List<ViewingItem>
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh), shape = RoundedCornerShape(28.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Browse", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(listOfNotNull(statusFilter?.libraryTitle, genreFilter).ifEmpty { listOf("All statuses and genres") }.joinToString(" • "), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                CompactDropdown(
                    label = "Sort",
                    selected = sortMode.label,
                    options = listOf(ViewingSortMode.RELEASE, ViewingSortMode.CHRONOLOGICAL, ViewingSortMode.TITLE, ViewingSortMode.RATING, ViewingSortMode.RUNTIME, ViewingSortMode.GENRE),
                    optionLabel = { it.label },
                    onSelect = onSort,
                    modifier = Modifier.weight(1f)
                )
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.chipGap)) {
                item {
                    FilterChip(
                        selected = genreFilter == null,
                        onClick = { onGenre(null) },
                        label = { Text("All genres") },
                        leadingIcon = { Icon(if (genreFilter == null) RhythmIcons.Check else RhythmIcons.AppsGrid, contentDescription = null) }
                    )
                }
                items(genres.take(24), key = { "genre-$it" }) { genre ->
                    FilterChip(
                        selected = genreFilter == genre,
                        onClick = { onGenre(if (genreFilter == genre) null else genre) },
                        label = { Text(genre) }
                    )
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.chipGap)) {
                item {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { onStatus(null) },
                        label = { Text("All") },
                        leadingIcon = { Icon(if (statusFilter == null) RhythmIcons.Check else RhythmIcons.AppsGrid, contentDescription = null) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            iconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                items(ViewingUserStatus.entries.filter { it != ViewingUserStatus.HIDDEN }, key = { it.name }) { status ->
                    val count = catalogItems.count { status in ViewingMetadataStore.statusesFor(it) }
                    val active = statusFilter == status
                    FilterChip(
                        selected = active,
                        onClick = { onStatus(if (active) null else status) },
                        leadingIcon = { Icon(status.icon(), contentDescription = null) },
                        label = { Text("${status.libraryTitle} $count") },
                        colors = statusChipColors(status, active)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchCompactFilters(
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
private fun LibraryControlPanel(
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
                options = listOf("Essentials", "Continue", "MCU", "DC", "Timeline", "Collections", "Saved"),
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
private fun SearchFilterMenu(
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
private fun CompactMenuCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            content()
        }
    }
}

@Composable
private fun <T> CompactDropdown(
    label: String,
    selected: String,
    options: List<T>,
    modifier: Modifier = Modifier,
    optionLabel: (T) -> String = { it.toString() },
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(selected, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Icon(RhythmIcons.ExpandMore, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = { expanded = false; onSelect(option) }
                )
            }
        }
    }
}

@Composable
private fun LibraryTabs(selected: String, onTab: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.chipGap)) {
        items(listOf("Essentials", "Continue", "MCU", "DC", "Timeline", "Collections", "Saved")) { tab -> FilterChip(selected = selected == tab, onClick = { onTab(tab) }, label = { Text(tab) }) }
    }
}

@Composable
private fun SortChips(sortMode: ViewingSortMode, onSort: (ViewingSortMode) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.chipGap)) {
        items(listOf(ViewingSortMode.RELEASE, ViewingSortMode.CHRONOLOGICAL, ViewingSortMode.TITLE, ViewingSortMode.RATING, ViewingSortMode.RUNTIME)) { mode ->
            FilterChip(selected = sortMode == mode, onClick = { onSort(mode) }, label = { Text(mode.label) })
        }
    }
}

@Composable
private fun StatusSelector(selected: Set<ViewingUserStatus>, onStatus: (ViewingUserStatus) -> Unit) {
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
            FlowRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.chipGap), verticalArrangement = Arrangement.spacedBy(ViewingUi.chipGap)) {
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



@Composable
private fun CinemaActivityMiniSurface(item: ViewingItem, onClick: () -> Unit) {
    val displayItem = rememberCachedItem(item)
    Card(onClick = onClick, shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(104.dp)) {
            PosterBackdrop(displayItem, Modifier.fillMaxSize(), ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.96f), MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.78f), Color.Transparent))))
            Row(Modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                PosterBackdrop(displayItem, Modifier.size(50.dp, 72.dp), ContentScale.Crop, RoundedCornerShape(16.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Recently viewed", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(displayItem.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                    Text(ViewingMetadataStore.statusesFor(displayItem).firstOrNull()?.activeLabel ?: "Open details", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                }
                FilledIconButton(onClick = onClick, colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
                    Icon(RhythmIcons.Play, contentDescription = "Resume ${displayItem.title}")
                }
            }
        }
    }
}

@Composable
private fun ExpressiveSearchField(query: String, onQuery: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQuery,
        placeholder = { Text("Search movies, series, phases, genres, cast…") },
        leadingIcon = { Icon(RhythmIcons.Search, contentDescription = null) },
        trailingIcon = { if (query.isNotBlank()) IconButton(onClick = { onQuery("") }) { Icon(RhythmIcons.Close, contentDescription = "Clear search") } },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SearchChipRail(title: String, values: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.chipGap)) {
            items(values) { value -> FilterChip(selected = selected == value, onClick = { onSelect(value) }, leadingIcon = if (selected == value) ({ Icon(RhythmIcons.Check, contentDescription = null) }) else null, label = { Text(value.replace('_', '-')) }) }
        }
    }
}

@Composable
private fun GenreChipRail(genres: List<String>, selected: String?, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Genres", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.chipGap)) {
            items(genres) { genre -> FilterChip(selected = selected == genre, onClick = { onSelect(genre) }, leadingIcon = { Text("•", color = if (selected == genre) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary) }, label = { Text(genre) }) }
        }
    }
}

@Composable
private fun CategoryChipRail(selected: ViewingSearchCategory, onSelect: (ViewingSearchCategory) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.chipGap)) {
        items(ViewingSearchCategory.entries) { category -> FilterChip(selected = selected == category, onClick = { onSelect(category) }, leadingIcon = if (selected == category) ({ Icon(RhythmIcons.Check, contentDescription = null) }) else null, label = { Text(category.label) }) }
    }
}

@Composable
private fun SearchSortRail(sortMode: ViewingSearchSortMode, onSort: (ViewingSearchSortMode) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.chipGap)) {
        items(ViewingSearchSortMode.entries) { mode -> FilterChip(selected = sortMode == mode, onClick = { onSort(mode) }, label = { Text(mode.label) }) }
    }
}

@Composable
private fun ResultSection(title: String, subtitle: String, items: List<ViewingItem>, onOpen: (ViewingItem) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title, subtitle)
        items.forEachIndexed { index, item -> ViewingOrderRow(item, index + 1, onClick = { onOpen(item) }) }
    }
}

private fun LazyListScope.groupedViewingItems(items: List<ViewingItem>, sortMode: ViewingSortMode, onOpenTitle: (ViewingItem) -> Unit) {
    val grouped = items.groupBy { item -> item.phase ?: item.saga ?: item.universe ?: "Cinemaverse" }
    grouped.forEach { (phase, phaseItems) ->
        item("phase-$phase") { PhaseDivider(phase, phaseItems) }
        itemsIndexed(phaseItems, key = { index, item -> "$phase-${item.id}-$index" }) { index, item -> ViewingOrderRow(item, index + 1, onClick = { onOpenTitle(item) }) }
    }
}

@Composable
private fun PhaseDivider(title: String, items: List<ViewingItem>) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("${items.size} titles", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f))
        }
    }
}

@Composable
private fun SmallStatusPill(status: ViewingUserStatus) {
    Surface(shape = RoundedCornerShape(50), color = status.activeColor().copy(alpha = 0.22f)) {
        Text(status.activeLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun statusChipColors(status: ViewingUserStatus, active: Boolean) = FilterChipDefaults.filterChipColors(
    selectedContainerColor = status.activeColor().copy(alpha = 0.82f),
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
)

@Composable
private fun ViewingUserStatus.activeColor(): Color = when (this) {
    ViewingUserStatus.WATCHLIST -> MaterialTheme.colorScheme.primaryContainer
    ViewingUserStatus.WATCH_LATER -> MaterialTheme.colorScheme.tertiaryContainer
    ViewingUserStatus.WATCHING -> Color(0xFFFFB74D)
    ViewingUserStatus.WATCHED -> Color(0xFF81C784)
    ViewingUserStatus.FAVORITE -> Color(0xFFF48FB1)
    ViewingUserStatus.BOOKMARKED -> Color(0xFF9575CD)
    ViewingUserStatus.ON_HOLD -> MaterialTheme.colorScheme.secondaryContainer
    ViewingUserStatus.HIDDEN -> MaterialTheme.colorScheme.surfaceVariant
}

private fun ViewingUserStatus.icon() = when (this) {
    ViewingUserStatus.WATCHLIST -> RhythmIcons.Add
    ViewingUserStatus.WATCH_LATER -> RhythmIcons.Playlist
    ViewingUserStatus.WATCHING -> RhythmIcons.Play
    ViewingUserStatus.WATCHED -> RhythmIcons.Check
    ViewingUserStatus.FAVORITE -> RhythmIcons.Favorite
    ViewingUserStatus.BOOKMARKED -> MaterialSymbolIcon("bookmark", filled = true)
    ViewingUserStatus.ON_HOLD -> RhythmIcons.Pause
    ViewingUserStatus.HIDDEN -> RhythmIcons.VisibilityOff
}

private fun List<ViewingItem>.sortedForSearch(mode: ViewingSearchSortMode): List<ViewingItem> = when (mode) {
    ViewingSearchSortMode.RELEASE_DATE -> sortedFor(ViewingSortMode.RELEASE)
    ViewingSearchSortMode.CHRONOLOGICAL -> sortedFor(ViewingSortMode.CHRONOLOGICAL)
    ViewingSearchSortMode.RATING -> sortedFor(ViewingSortMode.RATING)
    ViewingSearchSortMode.RUNTIME -> sortedFor(ViewingSortMode.RUNTIME)
    ViewingSearchSortMode.TITLE -> sortedFor(ViewingSortMode.TITLE)
    ViewingSearchSortMode.RELEVANCE -> this
}

@Composable
private fun MetadataGrid(item: ViewingItem) {
    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            DetailLine("Release", item.releaseDate ?: item.year.orEmpty())
            DetailLine("Runtime", item.runtime.orEmpty())
            DetailLine("Genres", item.genres.joinToString(" • "))
            DetailLine("Ratings", listOfNotNull(item.imdbRating?.let { "IMDb $it" }, item.tmdbRating?.let { "TMDb $it" }).joinToString(" • "))
            DetailLine("Language", listOfNotNull(item.language, item.country).joinToString(" • "))
            if (item.status != ViewingStatus.RELEASED) DetailLine("Status", item.status.name)
        }
    }
}

@Composable
private fun CreditsBlock(item: ViewingItem) {
    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Cast & crew", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            DetailLine("Director", item.director.orEmpty())
            DetailLine("Writer", item.writer.orEmpty())
            if (item.cast.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(item.cast.take(10), key = { it.id ?: it.name }) { CastPosterCard(it) }
                }
            } else {
                DetailLine("Actors", item.actors.joinToString(" • "))
            }
        }
    }
}

@Composable
private fun CastPosterCard(member: ViewingCastMember) {
    Column(Modifier.width(92.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ArtworkImage(
            data = ViewingArtworkUtils.tmdbProfile(member.profilePath),
            description = "${member.name} as ${member.character ?: "cast member"}",
            modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f).clip(RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Crop
        )
        Text(member.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Text(member.character.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    if (value.isBlank()) return
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(82.dp))
        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun FlowChips(values: List<String>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.chipGap)) { items(values.distinct()) { AssistChip(onClick = {}, label = { Text(it) }) } }
}

@Composable
private fun HeroListCard(list: ViewingList) {
    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CollectionArtwork(list, Modifier.size(86.dp, 122.dp).clip(RoundedCornerShape(22.dp)), ContentScale.Crop)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(list.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(list.description.orEmpty(), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f))
                Text("${list.items.size} titles", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}

@Composable
private fun EmptyState(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Image(painterResource(R.drawable.ic_cinemaverse), contentDescription = null, modifier = Modifier.size(54.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PressableCard(modifier: Modifier = Modifier, onClick: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, ViewingUi.pressSpec(), label = "viewingPress")
    Card(onClick = onClick, interactionSource = interaction, shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale }) {
        Column(Modifier.padding(ViewingUi.cardPad), content = content)
    }
}

@Composable
private fun rememberCachedItem(item: ViewingItem): ViewingItem = ViewingMetadataStore.itemFor(item)

@Composable
private fun rememberEnrichedItem(item: ViewingItem): ViewingItem {
    var current by remember(item.id) { mutableStateOf(ViewingMetadataStore.itemFor(item)) }
    LaunchedEffect(item.id) {
        current = ViewingMetadataStore.itemFor(item)
        current = runCatching { ViewingMetadataStore.enrich(item) }.getOrElse { current }
    }
    return if (ViewingMetadataStore.useLocalPosters.value) current else current.copy(localPoster = null, localBackdrop = null)
}


@Composable
private fun CollectionArtwork(list: ViewingList, modifier: Modifier, contentScale: ContentScale) {
    val direct = ViewingArtworkUtils.resolvePoster(list, ViewingMetadataStore.useLocalPosters.value)
        ?: ViewingArtworkUtils.resolveBackdrop(list, ViewingMetadataStore.useLocalPosters.value)
    val artworkItems = remember(list.id, list.artworkItems, list.items) {
        list.artworkItems.ifEmpty { list.items }.filter { ViewingArtworkUtils.resolvePoster(it, ViewingMetadataStore.useLocalPosters.value) != null || ViewingArtworkUtils.resolveBackdrop(it, ViewingMetadataStore.useLocalPosters.value) != null }.take(4)
    }
    when {
        direct != null -> ArtworkImage(direct, "${list.title} artwork", modifier, contentScale, fallbackTitle = list.title, fallbackLabel = list.accentLabel ?: list.category ?: list.universe)
        artworkItems.size >= 2 -> CollectionArtworkMosaic(list, artworkItems, modifier)
        else -> BrandedArtworkPlaceholder(list.title, list.accentLabel ?: list.category ?: list.universe ?: "Collection", modifier, list.universe)
    }
}

@Composable
private fun CollectionArtworkMosaic(list: ViewingList, items: List<ViewingItem>, modifier: Modifier) {
    Column(modifier.background(collectionBrush(list.universe ?: list.category))) {
        items.chunked(2).take(2).forEach { rowItems ->
            Row(Modifier.weight(1f)) {
                rowItems.forEach { item ->
                    PosterBackdrop(item, Modifier.weight(1f).fillMaxSize(), ContentScale.Crop)
                }
                if (rowItems.size == 1) BrandedArtworkPlaceholder(list.title, list.accentLabel ?: "Collection", Modifier.weight(1f).fillMaxSize(), list.universe)
            }
        }
    }
}

@Composable
private fun BrandedArtworkPlaceholder(title: String, label: String?, modifier: Modifier, universe: String? = null) {
    Box(modifier.background(collectionBrush(universe ?: label)).semantics { contentDescription = "Artwork placeholder for $title" }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(12.dp)) {
            Text(title.initials(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center, maxLines = 1)
            if (!label.isNullOrBlank()) {
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)) {
                    Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        Image(painterResource(R.drawable.ic_cinemaverse), contentDescription = null, modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp).size(28.dp).graphicsLayer { alpha = 0.42f })
    }
}

@Composable
private fun collectionBrush(seed: String?): Brush {
    val scheme = MaterialTheme.colorScheme
    val colors = when (seed) {
        "MCU", "Marvel" -> listOf(scheme.primaryContainer, scheme.tertiaryContainer)
        "DCU", "DCEU", "Elseworlds", "DC" -> listOf(scheme.secondaryContainer, scheme.primaryContainer)
        else -> listOf(scheme.primaryContainer, scheme.secondaryContainer, scheme.tertiaryContainer)
    }
    return Brush.linearGradient(colors)
}

private fun String.initials(): String = trim().split(Regex("\\s+")).filter { it.isNotBlank() && it.first().isLetterOrDigit() }.take(3).joinToString("") { it.first().uppercase() }.ifBlank { "CU" }

@Composable
private fun PosterBackdrop(item: ViewingItem, modifier: Modifier, contentScale: ContentScale, shape: RoundedCornerShape? = null) {
    val m = if (shape != null) modifier.clip(shape) else modifier
    ArtworkImage(
        ViewingArtworkUtils.resolveBackdrop(item, ViewingMetadataStore.useLocalPosters.value) ?: ViewingArtworkUtils.resolvePoster(item, ViewingMetadataStore.useLocalPosters.value),
        "Poster for ${item.title}",
        m,
        contentScale,
        fallbackTitle = item.title,
        fallbackLabel = item.universe ?: item.category ?: item.type.name.lowercase().replaceFirstChar { it.titlecase() },
        fallbackSeed = item.universe
    )
}

@Composable
private fun ArtworkImage(data: String?, description: String, modifier: Modifier, contentScale: ContentScale, fallbackTitle: String? = null, fallbackLabel: String? = null, fallbackSeed: String? = null) {
    var loadFailed by remember(data) { mutableStateOf(false) }
    if (data.isNullOrBlank() || loadFailed) {
        BrandedArtworkPlaceholder(fallbackTitle ?: description.removePrefix("Poster for "), fallbackLabel, modifier.semantics { contentDescription = description }, fallbackSeed ?: fallbackLabel)
    } else {
        val context = LocalContext.current
        val request = remember(data) { ImageRequest.Builder(context).data(data).crossfade(true).memoryCacheKey(data).diskCacheKey(data).build() }
        AsyncImage(
            model = request,
            contentDescription = description,
            contentScale = contentScale,
            modifier = modifier,
            onError = { loadFailed = true }
        )
    }
}

@Composable
private fun SettingsIconAction(onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) { Icon(RhythmIcons.Settings, contentDescription = "Settings") }
}

private fun ViewingItem.hasAnyTrailer(): Boolean = availableTrailers().isNotEmpty()

private fun ViewingItem.availableTrailers(): List<ViewingTrailer> = trailers.ifEmpty {
    if (!youtubeVideoId.isNullOrBlank() || !trailerUrl.isNullOrBlank()) listOf(ViewingTrailer("Trailer", youtubeVideoId, trailerUrl, trailerSource)) else emptyList()
}.filter { !it.youtubeVideoId.isNullOrBlank() || !it.url.isNullOrBlank() }

private fun ViewingTrailer.externalUrl(): String? = url?.takeIf { it.isNotBlank() }
    ?: youtubeVideoId?.takeIf { it.isNotBlank() }?.let { "https://www.youtube.com/watch?v=$it" }

private fun List<ViewingList>.visibleManagedLists(): List<ViewingList> = filter { list ->
    list.importance == ViewingListImportance.PRIMARY ||
        list.category in setOf("Character Journeys", "Specials", "Defenders Saga", "Marvel One-Shots", "Disney+ Series")
}.distinctBy { it.title }.sortedWith(
    compareByDescending<ViewingList> { it.importance == ViewingListImportance.PRIMARY }
        .thenBy { it.category ?: "" }
        .thenBy { it.title }
)

private fun List<ViewingItem>.sortedFor(mode: ViewingSortMode): List<ViewingItem> = when (mode) {
    ViewingSortMode.CHRONOLOGICAL -> sortedWith(compareBy<ViewingItem> { it.chronologicalOrder ?: Int.MAX_VALUE }.thenBy { it.releaseDate ?: "9999" })
    ViewingSortMode.PHASE -> sortedWith(compareBy<ViewingItem> { it.phase ?: "" }.thenBy { it.phaseOrder ?: it.releaseOrder ?: Int.MAX_VALUE })
    ViewingSortMode.TITLE -> sortedBy { it.title }
    ViewingSortMode.RATING -> sortedByDescending { it.imdbRating?.toDoubleOrNull() ?: it.tmdbRating ?: 0.0 }
    ViewingSortMode.RUNTIME -> sortedByDescending { it.runtime?.filter(Char::isDigit)?.toIntOrNull() ?: 0 }
    ViewingSortMode.GENRE -> sortedWith(compareBy<ViewingItem> { it.genres.firstOrNull() ?: "" }.thenBy { it.title })
    else -> sortedWith(compareBy<ViewingItem> { it.releaseDate ?: "9999-99-99" }.thenBy { it.releaseOrder ?: Int.MAX_VALUE })
}

private fun ViewingSortMode.orderFor(item: ViewingItem): Int = when (this) {
    ViewingSortMode.CHRONOLOGICAL -> item.chronologicalOrder
    ViewingSortMode.PHASE -> item.phaseOrder
    else -> item.releaseOrder
} ?: 0
