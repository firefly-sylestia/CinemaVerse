@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.cinemaverse.mcu.shared.presentation.screens.viewing

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledIconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cinemaverse.mcu.R
import com.cinemaverse.mcu.shared.data.service.ViewingMetadataStore
import com.cinemaverse.mcu.shared.data.viewing.McuAssetDataSource
import com.cinemaverse.mcu.shared.data.viewing.ViewingItem
import com.cinemaverse.mcu.shared.data.viewing.ViewingList
import com.cinemaverse.mcu.shared.data.viewing.ViewingListImportance
import com.cinemaverse.mcu.shared.data.viewing.ViewingSearchCategory
import com.cinemaverse.mcu.shared.data.viewing.ViewingSearchSortMode
import com.cinemaverse.mcu.shared.data.viewing.ViewingSortMode
import com.cinemaverse.mcu.shared.data.viewing.ViewingStatus
import com.cinemaverse.mcu.shared.data.viewing.ViewingType
import com.cinemaverse.mcu.shared.data.viewing.ViewingUserStatus
import com.cinemaverse.mcu.shared.presentation.components.icons.Icon
import com.cinemaverse.mcu.shared.presentation.components.icons.RhythmIcons
import com.cinemaverse.mcu.shared.presentation.components.viewing.YouTubeTrailerWebPlayer
import com.cinemaverse.mcu.shared.util.ViewingArtworkUtils
import kotlinx.coroutines.launch


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
    val coroutineScope = rememberCoroutineScope()
    val message by ViewingMetadataStore.statusMessage
    val isFetching by ViewingMetadataStore.isFetching
    val marvel = remember(data) { data.allItems.filter { it.universe == "MCU" }.take(14) }
    val dc = remember(data) { data.allItems.filter { it.universe in setOf("DCU", "DCEU", "Elseworlds") }.take(14) }
    val lists = remember(data) { data.allLists.filter { it.importance == ViewingListImportance.PRIMARY }.take(12) }
    val recent = ViewingMetadataStore.recentItems(data)

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(ViewingUi.screenHPad, ViewingUi.topPad, ViewingUi.screenHPad, ViewingUi.bottomPad),
        verticalArrangement = Arrangement.spacedBy(ViewingUi.sectionGap)
    ) {
        item { CinemaverseHeader(onOpenSearch = onOpenSearch, onOpenSettings = onOpenSettings) }
        item {
            HeroViewingCard(
                item = data.featuredItem,
                list = data.featuredList,
                onOpenDetail = { onOpenItem(data.featuredItem) },
                onOpenLibrary = onOpenLibrary
            )
        }
        if (recent.isNotEmpty()) item { CinemaActivityMiniSurface(recent.first(), onClick = { onOpenItem(recent.first()) }) }
        item {
            ApiStateCard(
                message = message,
                isFetching = isFetching,
                onOpenSettings = onOpenSettings,
                onFetch = { coroutineScope.launch { ViewingMetadataStore.fetchAll(data) } }
            )
        }
        item { PosterRail("Marvel", "MCU films, series, specials, One-Shots, and Defenders entries", marvel, onOpenItem) }
        item { PosterRail("DC", "DCU, DCEU, Elseworlds, and connected TV", dc, onOpenItem) }
        item { ListRail("Viewing orders", "Release, timeline, phase/chapter, saga, and collection lists", lists, onOpenList) }
    }
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
    var tab by rememberSaveable { mutableStateOf("Continue") }
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
            val filtered = remember(tab, sortMode, data) {
                val base = when (tab) {
                    "Continue" -> ViewingMetadataStore.recentItems(data).ifEmpty { data.allItems.filter { ViewingUserStatus.WATCHING in ViewingMetadataStore.statusesFor(it) } }
                    "Essential" -> data.featuredList.items
                    "Universes" -> data.allItems.filter { it.universe in setOf("MCU", "Marvel", "DCU", "DCEU", "Elseworlds") }
                    "Timeline" -> data.allItems.sortedFor(ViewingSortMode.CHRONOLOGICAL)
                    "Saved" -> data.allItems.filter { ViewingMetadataStore.statusesFor(it).any { status -> status != ViewingUserStatus.HIDDEN } }
                    else -> data.allItems
                }
                base.sortedFor(sortMode)
            }
            LazyColumn(
                modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(ViewingUi.screenHPad, ViewingUi.topPad, ViewingUi.screenHPad, ViewingUi.bottomPad),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item { CinemaverseHeader(title = "Library", subtitle = "Continue • Essential • Universes • Timeline • Collections • Saved", onOpenSettings = onOpenSettings) }
                item { LibraryTabs(tab, onTab = { tab = it }) }
                item { SortChips(sortMode, { sortMode = it }) }
                if (tab == "Collections") {
                    items(data.allLists.filter { it.importance != ViewingListImportance.HIDDEN }.distinctBy { it.title }, key = { it.id }) { list -> WideListCard(list, onClick = { selectedListId = list.id }) }
                } else {
                    item { StatusSummaryRail(data.allItems, onTab = { tab = it }) }
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
            item { GenreChipRail(genres, selectedGenre) { selectedGenre = if (selectedGenre == it) null else it } }
            item { CategoryChipRail(selectedCategory) { selectedCategory = it } }
            item { SearchSortRail(sortMode) { sortMode = it } }
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
    val hasTrailer = !selected.youtubeVideoId.isNullOrBlank() || !selected.trailerUrl.isNullOrBlank()
    val related = remember(selected, data) {
        data.allItems.filter { it.id != selected.id && (it.franchise == selected.franchise || it.universe == selected.universe || it.genres.any(selected.genres::contains)) }.take(12)
    }
    val accent = if (selected.universe == "MCU") Color(0xFFE62429) else Color(0xFF2F80ED)
    val gold = Color(0xFFFFC857)

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
                            gold.copy(alpha = 0.12f),
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
                                Crossfade(showTrailer, animationSpec = tween(180), label = "posterTrailer") { trailer ->
                                    if (trailer) YouTubeTrailerWebPlayer(
                                        youtubeVideoId = selected.youtubeVideoId,
                                        trailerUrl = selected.trailerUrl,
                                        title = selected.title,
                                        modifier = Modifier.fillMaxSize()
                                    ) else PosterBackdrop(selected, Modifier.fillMaxSize(), ContentScale.Crop)
                                }
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
                                    if (userStatuses.isNotEmpty()) FlowChips(userStatuses.take(4).map { it.activeLabel })
                                }
                                FilledIconButton(
                                    onClick = { if (hasTrailer) showTrailer = !showTrailer },
                                    enabled = hasTrailer,
                                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = gold, contentColor = Color(0xFF1B1B1F))
                                ) { Icon(if (showTrailer) RhythmIcons.Close else RhythmIcons.Play, contentDescription = if (showTrailer) "Show poster" else "Play trailer") }
                            }
                            Text(selected.overview ?: selected.plot ?: selected.description ?: "No overview available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewingListDetailScreen(list: ViewingList, onBack: () -> Unit, onOpenTitle: (ViewingItem) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(list.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(RhythmIcons.Back, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
            )
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(ViewingUi.screenHPad, 12.dp, ViewingUi.screenHPad, ViewingUi.bottomPad), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { HeroListCard(list) }
            groupedViewingItems(list.items, ViewingSortMode.PHASE, onOpenTitle)
        }
    }
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
private fun PosterRail(title: String, subtitle: String, items: List<ViewingItem>, onOpenItem: (ViewingItem) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title, subtitle)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.cardGap)) {
            items(items, key = { it.id }) { PosterCard(it) { onOpenItem(it) } }
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
    val displayItem = rememberEnrichedItem(item)
    PressableCard(onClick = onClick, modifier = Modifier.width(ViewingUi.posterWidth)) {
        PosterBackdrop(displayItem, Modifier.fillMaxWidth().aspectRatio(2f / 3f), ContentScale.Crop, RoundedCornerShape(22.dp))
        Spacer(Modifier.height(10.dp))
        Text(displayItem.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
        Text(listOfNotNull(displayItem.year, displayItem.universe).joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ViewingListCard(list: ViewingList, onClick: () -> Unit) {
    PressableCard(onClick = onClick, modifier = Modifier.width(220.dp)) {
        ArtworkImage(ViewingArtworkUtils.resolveBackdrop(list, ViewingMetadataStore.useLocalPosters.value) ?: ViewingArtworkUtils.resolvePoster(list, ViewingMetadataStore.useLocalPosters.value), "${list.title} artwork", Modifier.fillMaxWidth().height(118.dp).clip(RoundedCornerShape(22.dp)), ContentScale.Crop)
        Spacer(Modifier.height(10.dp))
        Text(list.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
        Text("${list.items.size} titles • ${list.category ?: list.universe ?: "Viewing order"}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun WideListCard(list: ViewingList, onClick: () -> Unit) {
    PressableCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(list.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(list.description.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text("${list.items.size} titles • ${list.category ?: "Collection"}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ViewingOrderRow(item: ViewingItem, order: Int, onClick: () -> Unit) {
    val displayItem = rememberEnrichedItem(item)
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
private fun LibraryTabs(selected: String, onTab: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.chipGap)) {
        items(listOf("Continue", "Essential", "Universes", "Timeline", "Collections", "Saved")) { tab -> FilterChip(selected = selected == tab, onClick = { onTab(tab) }, label = { Text(tab) }) }
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Your title statuses", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.chipGap), verticalArrangement = Arrangement.spacedBy(ViewingUi.chipGap)) {
            listOf(ViewingUserStatus.WATCHLIST, ViewingUserStatus.WATCH_LATER, ViewingUserStatus.WATCHING, ViewingUserStatus.WATCHED, ViewingUserStatus.FAVORITE, ViewingUserStatus.BOOKMARKED, ViewingUserStatus.ON_HOLD, ViewingUserStatus.HIDDEN).forEach { status ->
                val active = status in selected
                FilterChip(
                    selected = active,
                    onClick = { onStatus(status) },
                    leadingIcon = { Icon(if (active) RhythmIcons.Check else status.icon(), contentDescription = null) },
                    label = { Text(if (active) status.activeLabel else status.inactiveLabel) },
                    colors = statusChipColors(status, active)
                )
            }
        }
    }
}



@Composable
private fun StatusSummaryRail(catalogItems: List<ViewingItem>, onTab: (String) -> Unit) {
    val statuses = ViewingUserStatus.entries.filter { it != ViewingUserStatus.HIDDEN }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.cardGap)) {
        items(statuses, key = { it.name }) { status ->
            val count = catalogItems.count { status in ViewingMetadataStore.statusesFor(it) }
            Card(
                onClick = { onTab("Saved") },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.width(150.dp)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(status.libraryTitle, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    Text("$count titles", color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.76f), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun CinemaActivityMiniSurface(item: ViewingItem, onClick: () -> Unit) {
    val displayItem = rememberEnrichedItem(item)
    PressableCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            PosterBackdrop(displayItem, Modifier.size(56.dp, 78.dp), ContentScale.Crop, RoundedCornerShape(18.dp))
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
        items.forEach { ViewingOrderRow(it, it.releaseOrder ?: 0, onClick = { onOpen(it) }) }
    }
}

private fun LazyListScope.groupedViewingItems(items: List<ViewingItem>, sortMode: ViewingSortMode, onOpenTitle: (ViewingItem) -> Unit) {
    val grouped = items.groupBy { item -> item.phase ?: item.saga ?: item.universe ?: "Cinemaverse" }
    grouped.forEach { (phase, phaseItems) ->
        item("phase-$phase") { PhaseDivider(phase, phaseItems) }
        items(phaseItems, key = { it.id }) { item -> ViewingOrderRow(item, sortMode.orderFor(item), onClick = { onOpenTitle(item) }) }
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
    ViewingUserStatus.BOOKMARKED -> RhythmIcons.PlaylistFilled
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
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Cast & crew", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            DetailLine("Director", item.director.orEmpty())
            DetailLine("Writer", item.writer.orEmpty())
            DetailLine("Actors", item.actors.joinToString(" • "))
        }
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
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(list.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(list.description.orEmpty(), color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f))
            Text("${list.items.size} titles", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ApiStateCard(message: String, isFetching: Boolean, onOpenSettings: () -> Unit, onFetch: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), shape = MaterialTheme.shapes.large) {
        Row(Modifier.fillMaxWidth().padding(ViewingUi.cardPad), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Poster & database fetch", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(message, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.78f))
            }
            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onFetch, enabled = !isFetching) { Text(if (isFetching) "Loading" else "Fetch") }
                TextButton(onClick = onOpenSettings) { Text("Settings") }
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
private fun rememberEnrichedItem(item: ViewingItem): ViewingItem {
    var current by remember(item.id) { mutableStateOf(ViewingMetadataStore.itemFor(item)) }
    LaunchedEffect(item.id) {
        current = ViewingMetadataStore.itemFor(item)
        current = runCatching { ViewingMetadataStore.enrich(item) }.getOrElse { current }
    }
    return if (ViewingMetadataStore.useLocalPosters.value) current else current.copy(localPoster = null, localBackdrop = null)
}

@Composable
private fun PosterBackdrop(item: ViewingItem, modifier: Modifier, contentScale: ContentScale, shape: RoundedCornerShape? = null) {
    val m = if (shape != null) modifier.clip(shape) else modifier
    ArtworkImage(ViewingArtworkUtils.resolveBackdrop(item, ViewingMetadataStore.useLocalPosters.value) ?: ViewingArtworkUtils.resolvePoster(item, ViewingMetadataStore.useLocalPosters.value), "Poster for ${item.title}", m, contentScale)
}

@Composable
private fun ArtworkImage(data: String?, description: String, modifier: Modifier, contentScale: ContentScale) {
    if (data.isNullOrBlank()) {
        Box(modifier.background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer))).semantics { contentDescription = description }, contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.ic_cinemaverse), contentDescription = null, modifier = Modifier.size(52.dp))
        }
    } else {
        val context = LocalContext.current
        val request = remember(data) { ImageRequest.Builder(context).data(data).crossfade(true).memoryCacheKey(data).diskCacheKey(data).build() }
        AsyncImage(model = request, contentDescription = description, contentScale = contentScale, modifier = modifier)
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

private fun List<ViewingItem>.sortedFor(mode: ViewingSortMode): List<ViewingItem> = when (mode) {
    ViewingSortMode.CHRONOLOGICAL -> sortedWith(compareBy<ViewingItem> { it.chronologicalOrder ?: Int.MAX_VALUE }.thenBy { it.releaseDate ?: "9999" })
    ViewingSortMode.PHASE -> sortedWith(compareBy<ViewingItem> { it.phase ?: "" }.thenBy { it.phaseOrder ?: it.releaseOrder ?: Int.MAX_VALUE })
    ViewingSortMode.TITLE -> sortedBy { it.title }
    ViewingSortMode.RATING -> sortedByDescending { it.imdbRating?.toDoubleOrNull() ?: it.tmdbRating ?: 0.0 }
    ViewingSortMode.RUNTIME -> sortedByDescending { it.runtime?.filter(Char::isDigit)?.toIntOrNull() ?: 0 }
    else -> sortedWith(compareBy<ViewingItem> { it.releaseDate ?: "9999-99-99" }.thenBy { it.releaseOrder ?: Int.MAX_VALUE })
}

private fun ViewingSortMode.orderFor(item: ViewingItem): Int = when (this) {
    ViewingSortMode.CHRONOLOGICAL -> item.chronologicalOrder
    ViewingSortMode.PHASE -> item.phaseOrder
    else -> item.releaseOrder
} ?: 0
