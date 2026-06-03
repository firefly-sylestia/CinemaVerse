package com.cinemaverse.mcu.shared.presentation.screens.viewing

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cinemaverse.mcu.R
import com.cinemaverse.mcu.shared.data.service.MovieMetadataService
import com.cinemaverse.mcu.shared.data.viewing.McuAssetDataSource
import com.cinemaverse.mcu.shared.data.viewing.MetadataSource
import com.cinemaverse.mcu.shared.data.viewing.ViewingItem
import com.cinemaverse.mcu.shared.data.viewing.ViewingList
import com.cinemaverse.mcu.shared.data.viewing.ViewingSortMode
import com.cinemaverse.mcu.shared.data.viewing.ViewingStatus
import com.cinemaverse.mcu.shared.presentation.components.icons.Icon
import com.cinemaverse.mcu.shared.presentation.components.icons.RhythmIcons
import com.cinemaverse.mcu.shared.presentation.components.viewing.YouTubeTrailerWebPlayer
import com.cinemaverse.mcu.shared.util.ViewingArtworkUtils

private object ViewingUi {
    val screenHPad = 20.dp
    val topPad = 12.dp
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val data = remember(context) { McuAssetDataSource.load(context) }
    var selectedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedListId by rememberSaveable { mutableStateOf<String?>(null) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    val selectedItem = data.findItem(selectedItemId)
    val selectedList = data.findList(selectedListId)

    when {
        selectedItem != null -> ViewingDetailScreen(item = selectedItem, list = selectedList, onBack = { selectedItemId = null })
        selectedList != null -> ViewingListDetailScreen(list = selectedList, onBack = { selectedListId = null }, onOpenTitle = { selectedItemId = it.id })
        showSearch -> ViewingSearchScreen(onBack = { showSearch = false }, onOpenDetail = { selectedItemId = it.id }, onOpenSettings = onOpenSettings)
        else -> ViewingHomeContent(
            data = data,
            onOpenLibrary = onOpenLibrary,
            onOpenSearch = { showSearch = true; onOpenSearch() },
            onOpenSettings = onOpenSettings,
            onOpenItem = { selectedItemId = it.id; onOpenDetail() },
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
    val metadataService = remember { MovieMetadataService() }
    val message = remember { metadataService.getConfigurationMessage() }
    val marvel = remember(data) { data.allItems.filter { it.universe == "MCU" }.take(14) }
    val dc = remember(data) { data.allItems.filter { it.universe in setOf("DCU", "DCEU", "Elseworlds") }.take(14) }
    val lists = remember(data) { data.allLists.take(12) }

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
        item { ApiStateCard(message, onOpenSettings) }
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
    val data = remember(context) { McuAssetDataSource.load(context) }
    var tab by rememberSaveable { mutableStateOf("Titles") }
    var sortMode by rememberSaveable { mutableStateOf(ViewingSortMode.RELEASE) }
    var selectedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedListId by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    val selectedItem = data.findItem(selectedItemId)
    val selectedList = data.findList(selectedListId)

    when {
        selectedItem != null -> ViewingDetailScreen(item = selectedItem, list = selectedList, onBack = { selectedItemId = null })
        selectedList != null -> ViewingListDetailScreen(list = selectedList, onBack = { selectedListId = null }, onOpenTitle = { selectedItemId = it.id; onOpenDetail() })
        else -> {
            val filtered = remember(query, tab, sortMode, data) {
                val base = when (tab) {
                    "Marvel" -> data.allItems.filter { it.universe == "MCU" }
                    "DC" -> data.allItems.filter { it.universe in setOf("DCU", "DCEU", "Elseworlds") }
                    else -> data.allItems
                }.filter { item ->
                    query.isBlank() || listOfNotNull(item.title, item.franchise, item.phase, item.saga, item.category, item.director, item.writer, item.year, item.imdbId).any { it.contains(query, true) } || item.actors.any { it.contains(query, true) }
                }
                base.sortedFor(sortMode)
            }
            LazyColumn(
                modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(ViewingUi.screenHPad, ViewingUi.topPad, ViewingUi.screenHPad, ViewingUi.bottomPad),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item { CinemaverseHeader(title = "Library", subtitle = "Titles • Universes • Phases • Collections • Watch orders", onOpenSearch = {}, onOpenSettings = onOpenSettings) }
                item { LibraryTabs(tab, onTab = { tab = it }) }
                item { SearchAndSort(query, { query = it }, sortMode, { sortMode = it }) }
                if (tab == "Collections") {
                    items(data.allLists, key = { it.id }) { list -> WideListCard(list, onClick = { selectedListId = list.id }) }
                } else {
                    item { ListRail("Phases / chapters", "Tap any chip to open a functional filtered route", data.allLists.filter { it.category == "Phases / Chapters" || it.category == "Saga Order" }.take(18), onOpenList = { selectedListId = it.id }) }
                    if (filtered.isEmpty()) item { EmptyState("Try another universe/category", "No titles match this search. The offline catalog remains available without API keys.") }
                    items(filtered, key = { it.id }) { item -> ViewingOrderRow(item, sortMode.orderFor(item), onClick = { selectedItemId = item.id; onOpenDetail() }) }
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
    val data = remember(context) { McuAssetDataSource.load(context) }
    var query by rememberSaveable { mutableStateOf("") }
    val (items, lists) = remember(query, data) { data.search(query) }
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
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(ViewingUi.screenHPad, 10.dp, ViewingUi.screenHPad, ViewingUi.bottomPad),
            verticalArrangement = Arrangement.spacedBy(ViewingUi.cardGap)
        ) {
            item { OutlinedTextField(value = query, onValueChange = { query = it }, placeholder = { Text("Search title, cast, director, phase, year, IMDb ID") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
            if (items.isEmpty() && lists.isEmpty()) item { EmptyState("No viewing results", "Try another universe/category or fetch poster metadata later.") }
            if (lists.isNotEmpty()) item { ListRail("Matching lists", "${lists.size} viewing orders", lists, onOpenList = {}) }
            items(items, key = { it.id }) { item -> ViewingOrderRow(item, item.releaseOrder ?: 0, onClick = { onOpenDetail(item) }) }
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
    val data = remember(context) { McuAssetDataSource.load(context) }
    val selected = item ?: data.featuredItem
    var showTrailer by rememberSaveable(selected.id) { mutableStateOf(false) }
    var favorite by rememberSaveable(selected.id) { mutableStateOf(selected.favorite) }
    var watched by rememberSaveable(selected.id) { mutableStateOf(selected.watched) }
    val related = remember(selected, data) { data.allItems.filter { it.id != selected.id && (it.franchise == selected.franchise || it.universe == selected.universe) }.take(12) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selected.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(RhythmIcons.Back, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(ViewingUi.screenHPad, 12.dp, ViewingUi.screenHPad, ViewingUi.bottomPad),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Box(Modifier.fillMaxWidth().height(430.dp)) {
                    Crossfade(showTrailer, animationSpec = tween(180), label = "posterTrailer") { trailer ->
                        if (trailer) YouTubeTrailerWebPlayer(
                            youtubeVideoId = selected.youtubeVideoId,
                            trailerUrl = selected.trailerUrl,
                            title = selected.title,
                            modifier = Modifier.fillMaxSize()
                        ) else PosterBackdrop(selected, Modifier.fillMaxSize(), ContentScale.Crop, RoundedCornerShape(30.dp))
                    }
                    FilledIconButton(
                        onClick = { showTrailer = !showTrailer },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) { Icon(if (showTrailer) RhythmIcons.Close else RhythmIcons.Play, contentDescription = if (showTrailer) "Show poster" else "Show trailer") }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(selected.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(listOfNotNull(selected.year, selected.type.name.lowercase().replaceFirstChar { it.titlecase() }, selected.universe, selected.phase ?: selected.saga).joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowChips(listOfNotNull(selected.universe, selected.franchise, selected.phase, selected.saga, selected.category))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { showTrailer = true }) { Text("Trailer") }
                        OutlinedButton(onClick = { favorite = !favorite }) { Text(if (favorite) "Favorited" else "Favorite") }
                        OutlinedButton(onClick = { watched = !watched }) { Text(if (watched) "Watched" else "Mark watched") }
                    }
                }
            }
            item { MetadataGrid(selected) }
            item {
                ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(selected.overview ?: selected.plot ?: selected.description ?: "No overview available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item { CreditsBlock(selected) }
            if (related.isNotEmpty()) item { PosterRail("Related titles", selected.franchise ?: selected.universe ?: "Cinemaverse", related, onOpenItem = {}) }
            item { Text("Metadata source: ${selected.metadataSource} • Updated ${selected.lastUpdated ?: "offline catalog"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
            items(list.items, key = { it.id }) { item -> ViewingOrderRow(item, item.releaseOrder ?: item.chronologicalOrder ?: 0, onClick = { onOpenTitle(item) }) }
        }
    }
}

@Composable
private fun CinemaverseHeader(title: String = "Cinemaverse", subtitle: String = "Marvel • DC • Release orders • Trailers", onOpenSearch: () -> Unit, onOpenSettings: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer, tonalElevation = 3.dp) {
            Image(painter = painterResource(R.drawable.ic_cinemaverse), contentDescription = "Cinemaverse", modifier = Modifier.size(58.dp).padding(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onOpenSearch) { Icon(RhythmIcons.Search, contentDescription = "Search") }
        SettingsIconAction(onOpenSettings)
    }
}

@Composable
private fun HeroViewingCard(item: ViewingItem, list: ViewingList, onOpenDetail: () -> Unit, onOpenLibrary: () -> Unit) {
    PressableCard(onClick = onOpenDetail, modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(ViewingUi.heroHeight).clip(RoundedCornerShape(28.dp))) {
            PosterBackdrop(item, Modifier.fillMaxSize(), ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Featured order", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(item.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
    PressableCard(onClick = onClick, modifier = Modifier.width(ViewingUi.posterWidth)) {
        PosterBackdrop(item, Modifier.fillMaxWidth().aspectRatio(2f / 3f), ContentScale.Crop, RoundedCornerShape(22.dp))
        Spacer(Modifier.height(10.dp))
        Text(item.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
        Text(listOfNotNull(item.year, item.universe).joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ViewingListCard(list: ViewingList, onClick: () -> Unit) {
    PressableCard(onClick = onClick, modifier = Modifier.width(220.dp)) {
        ArtworkImage(ViewingArtworkUtils.resolveBackdrop(list) ?: ViewingArtworkUtils.resolvePoster(list), "${list.title} artwork", Modifier.fillMaxWidth().height(118.dp).clip(RoundedCornerShape(22.dp)), ContentScale.Crop)
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
    PressableCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(order.takeIf { it > 0 }?.toString() ?: "•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
            PosterBackdrop(item, Modifier.size(ViewingUi.rowPosterWidth, ViewingUi.rowPosterHeight), ContentScale.Crop, RoundedCornerShape(16.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Text(listOfNotNull(item.year, item.universe, item.phase ?: item.saga).joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text(listOfNotNull(item.runtime, item.imdbRating?.let { "IMDb $it" }).joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun LibraryTabs(selected: String, onTab: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.chipGap)) {
        items(listOf("Titles", "Marvel", "DC", "Collections", "Watchlists")) { tab -> FilterChip(selected = selected == tab, onClick = { onTab(tab) }, label = { Text(tab) }) }
    }
}

@Composable
private fun SearchAndSort(query: String, onQuery: (String) -> Unit, sortMode: ViewingSortMode, onSort: (ViewingSortMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Search title, cast, director, phase, year, IMDb ID") })
        LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUi.chipGap)) { items(listOf(ViewingSortMode.RELEASE, ViewingSortMode.CHRONOLOGICAL, ViewingSortMode.TITLE, ViewingSortMode.RATING, ViewingSortMode.RUNTIME)) { mode -> FilterChip(selected = sortMode == mode, onClick = { onSort(mode) }, label = { Text(mode.label) }) } }
    }
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
private fun ApiStateCard(message: String, onOpenSettings: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), shape = MaterialTheme.shapes.large) {
        Row(Modifier.fillMaxWidth().padding(ViewingUi.cardPad), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Poster & database fetch", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(message, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.78f))
            }
            TextButton(onClick = onOpenSettings) { Text("Settings") }
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
private fun PosterBackdrop(item: ViewingItem, modifier: Modifier, contentScale: ContentScale, shape: RoundedCornerShape? = null) {
    val m = if (shape != null) modifier.clip(shape) else modifier
    ArtworkImage(ViewingArtworkUtils.resolveBackdrop(item) ?: ViewingArtworkUtils.resolvePoster(item), "Poster for ${item.title}", m, contentScale)
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
    IconButton(onClick = onClick) { Icon(RhythmIcons.Settings, contentDescription = "Settings") }
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
