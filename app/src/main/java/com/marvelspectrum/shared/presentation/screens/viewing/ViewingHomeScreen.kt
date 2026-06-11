@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.marvelspectrum.shared.presentation.screens.viewing

import android.content.Intent
import android.net.Uri

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
fun ViewingHomeScreen(
    onOpenLibrary: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDetail: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    openSearchInternally: Boolean = true,
    homeReselectionKey: Int = 0,
    viewingViewModel: ViewingViewModel = viewModel()
) {
    val viewingState by viewingViewModel.uiState.collectAsState()
    val data = viewingState.data
    if (data == null) {
        ViewingCatalogLoadingState(
            title = "Preparing Cinemaverse",
            message = viewingState.errorMessage ?: "Loading your Marvel and DC viewing catalog…",
            modifier = modifier
        )
        return
    }
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
        selectedItem != null -> ViewingDetailScreen(item = selectedItem, list = selectedList, onBack = { selectedItemId = null }, viewingViewModel = viewingViewModel)
        selectedList != null -> ViewingListDetailScreen(list = selectedList, onBack = { selectedListId = null }, onOpenTitle = { selectedItemId = it.id })
        showSearch -> ViewingSearchScreen(onBack = { showSearch = false }, onOpenDetail = { selectedItemId = it.id }, onOpenSettings = onOpenSettings, viewingViewModel = viewingViewModel)
        else -> ViewingHomeContent(
            data = data,
            onOpenLibrary = onOpenLibrary,
            onOpenSearch = {
                if (openSearchInternally) {
                    showSearch = true
                } else {
                    onOpenSearch()
                }
            },
            onOpenSettings = onOpenSettings,
            onOpenItem = { selectedItemId = it.id; ViewingMetadataStore.markViewed(it); onOpenDetail() },
            onOpenList = { selectedListId = it.id },
            modifier = modifier
        )
    }
}

@Composable
internal fun ViewingHomeContent(
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
        contentPadding = PaddingValues(SpectrumSpacing.screenPadding, ViewingUi.topPad, SpectrumSpacing.screenPadding, SpectrumSpacing.bottomSafePadding),
        verticalArrangement = Arrangement.spacedBy(SpectrumSpacing.sectionGap)
    ) {
        item { CinemaverseHeader(onOpenSearch = onOpenSearch, onOpenSettings = onOpenSettings) }
        if (continueItems.isNotEmpty()) item { ContinueQueueRail("Continue watching", "In progress and saved for later", continueItems, onOpenItem) }
        item {
            CinemaverseFeaturedRail(
                items = remember(data) { data.homeFeaturedTitles() },
                featuredList = data.featuredList,
                onOpenItem = onOpenItem,
                onOpenTrailer = { trailerPreview = it }
            )
        }
        if (trailerItems.isNotEmpty()) item { TrailerRail("Trailers", "Official previews and clips", trailerItems, onOpenTrailer = { trailerPreview = it }) }
        if (upcomingItems.isNotEmpty()) item { SpectrumLaneRail("Upcoming", "Announced and upcoming releases", upcomingItems, onOpenItem, universe = null) }
        item { CollectionAlbumRail("Collections", "Timelines, phases, sagas, and character arcs", lists, onOpenList) }
        item { SpectrumLaneRail("Marvel", "Films, shows, specials, and timelines", marvel, onOpenItem, universe = "MCU") }
        item { SpectrumLaneRail("DC", "Films, series, Elseworlds, and connected stories", dc, onOpenItem, universe = "DC") }
        if (becauseYouWatched.isNotEmpty() && recent.isNotEmpty()) item { SpectrumLaneRail("More like ${recent.first().title}", "Based on universe, saga, franchise, and genre", becauseYouWatched, onOpenItem, universe = recent.first().universe) }
    }
    trailerPreview?.let { item -> TrailerPlayerDialog(item = item, onOpenDetails = { trailerPreview = null; onOpenItem(item) }, onDismiss = { trailerPreview = null }) }
}

@Composable
internal fun CinemaverseHeader(
    title: String = "Home",
    subtitle: String = "Movies, shows, timelines, and watch progress",
    onOpenSearch: (() -> Unit)? = null,
    onOpenSettings: () -> Unit,
    showBrandMark: Boolean = true
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        FilledIconButton(
            onClick = {
                HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                onOpenSettings()
            },
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) { Icon(RhythmIcons.Settings, contentDescription = "Settings") }
    }
}


@Composable
internal fun CollectionAlbumHero(list: ViewingList, onPlayFirst: () -> Unit, onShuffle: () -> Unit) {
    val watchedCount = list.items.count { ViewingUserStatus.WATCHED in ViewingMetadataStore.statusesFor(it) }
    var expanded by remember { mutableStateOf(false) }
    val poster = ViewingArtworkUtils.resolveCollectionBackdrop(list, ViewingMetadataStore.useLocalPosters.value)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        shape = RoundedCornerShape(34.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(Modifier.fillMaxWidth()) {
            ArtworkImage(poster, "${list.title} background", Modifier.matchParentSize(), ContentScale.Crop)
            Box(Modifier.matchParentSize().background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = .82f)))
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
                        SpectrumPopupMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            SpectrumPopupMenuItem("Save list", leading = { Icon(MaterialSymbolIcon("bookmark", filled = true), null) }) { expanded = false; list.items.forEach { if (ViewingUserStatus.BOOKMARKED !in ViewingMetadataStore.statusesFor(it)) ViewingMetadataStore.toggleStatus(it, ViewingUserStatus.BOOKMARKED) } }
                            SpectrumPopupMenuItem("Mark all watched", leading = { Icon(RhythmIcons.Check, null) }) { expanded = false; list.items.forEach { if (ViewingUserStatus.WATCHED !in ViewingMetadataStore.statusesFor(it)) ViewingMetadataStore.toggleStatus(it, ViewingUserStatus.WATCHED) } }
                            SpectrumPopupMenuItem("Clear watched", leading = { Icon(RhythmIcons.Remove, null) }) { expanded = false; list.items.forEach { if (ViewingUserStatus.WATCHED in ViewingMetadataStore.statusesFor(it)) ViewingMetadataStore.toggleStatus(it, ViewingUserStatus.WATCHED) } }
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
internal fun CollectionPill(text: String) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
internal fun CollectionTrackHeader(count: Int) {
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
internal fun CollectionTitleRow(item: ViewingItem, order: Int, onClick: () -> Unit) {
    val displayItem = rememberCachedItem(item)
    var expanded by remember { mutableStateOf(false) }
    var showTrailer by rememberSaveable(displayItem.id) { mutableStateOf(false) }
    val hasTrailer = displayItem.hasAnyTrailer()
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
                SpectrumPopupMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    SpectrumPopupMenuItem("Open details", leading = { Icon(RhythmIcons.Info, null) }) { expanded = false; onClick() }
                    if (hasTrailer) SpectrumPopupMenuItem("Play trailer", leading = { Icon(RhythmIcons.Play, null) }) { expanded = false; showTrailer = true }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                    SpectrumPopupMenuItem("Add to watchlist", leading = { Icon(RhythmIcons.Add, null) }) { expanded = false; ViewingMetadataStore.toggleStatus(displayItem, ViewingUserStatus.WATCHLIST) }
                    SpectrumPopupMenuItem("Mark watched", leading = { Icon(RhythmIcons.Check, null) }) { expanded = false; ViewingMetadataStore.toggleStatus(displayItem, ViewingUserStatus.WATCHED) }
                    SpectrumPopupMenuItem("Favorite", leading = { Icon(RhythmIcons.Favorite, null) }) { expanded = false; ViewingMetadataStore.toggleStatus(displayItem, ViewingUserStatus.FAVORITE) }
                    SpectrumPopupMenuItem("Hide", leading = { Icon(RhythmIcons.VisibilityOff, null) }) { expanded = false; ViewingMetadataStore.toggleStatus(displayItem, ViewingUserStatus.HIDDEN) }
                }
            }
        }
    }
    if (showTrailer) TrailerPlayerDialog(item = displayItem, onOpenDetails = { showTrailer = false; onClick() }, onDismiss = { showTrailer = false })
}

@Composable
internal fun CinemaverseFeaturedRail(
    items: List<ViewingItem>,
    featuredList: ViewingList,
    onOpenItem: (ViewingItem) -> Unit,
    onOpenTrailer: (ViewingItem) -> Unit
) {
    val carouselItems = if (items.isEmpty()) featuredList.items.take(6) else items
    if (carouselItems.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { carouselItems.size })
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var userInteracted by rememberSaveable { mutableStateOf(false) }
    var lastSettledPage by rememberSaveable { mutableStateOf(pagerState.currentPage) }
    val visibleItem = carouselItems[pagerState.currentPage.coerceIn(0, carouselItems.lastIndex)]

    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) userInteracted = true
    }
    LaunchedEffect(pagerState.settledPage, userInteracted) {
        if (userInteracted && pagerState.settledPage != lastSettledPage) {
            lastSettledPage = pagerState.settledPage
            HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SpectrumSectionHeader("Featured picks", subtitle = "Selected movies, series, and timelines")
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(end = 28.dp),
            pageSpacing = SpectrumSpacing.cardGap,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Featured picks" }
        ) { page ->
            val item = carouselItems[page]
            CinemaverseFeaturedCard(
                item = item,
                list = featuredList,
                selected = page == pagerState.currentPage,
                position = page + 1,
                total = carouselItems.size,
                onOpenItem = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                    onOpenItem(item)
                },
                onOpenTrailer = {
                    HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove)
                    onOpenTrailer(item)
                },
                modifier = Modifier.fillMaxWidth(0.92f)
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.carouselIndicatorGap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            carouselItems.forEachIndexed { index, _ ->
                val width by animateDpAsState(
                    if (index == pagerState.currentPage) SpectrumSpacing.carouselIndicatorActiveWidth else SpectrumSpacing.carouselIndicatorInactiveWidth,
                    label = "featuredIndicatorWidth"
                )
                Surface(
                    modifier = Modifier.size(width = width, height = SpectrumSpacing.carouselIndicatorHeight),
                    shape = RoundedCornerShape(50),
                    color = if (index == pagerState.currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                ) {}
            }
        }
        Text(
            "Featured ${pagerState.currentPage + 1} of ${carouselItems.size}: ${visibleItem.title}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun CinemaverseFeaturedCard(
    item: ViewingItem,
    list: ViewingList,
    selected: Boolean,
    position: Int,
    total: Int,
    onOpenItem: () -> Unit,
    onOpenTrailer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayItem = rememberEnrichedItem(item)
    val hasTrailer = displayItem.hasAnyTrailer()
    Card(
        shape = SpectrumShapes.mediaFrame,
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.50f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier.semantics {
            contentDescription = "Featured title $position of $total: ${displayItem.title}"
        }
    ) {
        Box(Modifier.fillMaxWidth().height(354.dp).clip(SpectrumShapes.mediaFrame)) {
            PosterBackdrop(displayItem, Modifier.fillMaxSize(), ContentScale.Crop, intent = ArtworkDisplayIntent.HERO_BACKDROP)
            Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.10f), Color.Black.copy(alpha = 0.44f), Color.Black.copy(alpha = 0.88f)))))
            PosterBackdrop(
                displayItem,
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .width(82.dp)
                    .aspectRatio(2f / 3f),
                ContentScale.Crop,
                SpectrumShapes.posterMask
            )
            if (hasTrailer) {
                val trailer = displayItem.primaryTrailer()
                Box(
                    Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 18.dp)
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black)
                ) {
                    YouTubeTrailerWebPlayer(
                        youtubeVideoId = trailer?.youtubeVideoId,
                        trailerUrl = trailer?.url,
                        title = displayItem.title,
                        autoplay = selected,
                        muted = true,
                        showControls = false,
                        loop = true,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.34f)))))
                    SpectrumPillTab(
                        selected = false,
                        onClick = onOpenTrailer,
                        modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp)
                    ) {
                        Icon(RhythmIcons.Play, contentDescription = null)
                        Text("Trailer")
                    }
                }
            }
            Column(Modifier.align(Alignment.BottomStart).padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOfNotNull(displayItem.universe, displayItem.year, displayItem.phase ?: displayItem.saga).take(3).forEach { SpectrumArtworkPill(it) }
                }
                Text(displayItem.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(listOfNotNull(displayItem.year, displayItem.runtime, displayItem.phase ?: displayItem.saga, list.title).joinToString(" • "), color = Color.White.copy(alpha = .86f), maxLines = 2)
                SpectrumPillTab(selected = true, onClick = onOpenItem) {
                    Icon(RhythmIcons.Info, contentDescription = null)
                    Text("Details")
                }
            }
        }
    }
}


internal fun McuAssetDataSource.ViewingAssetData.homeFeaturedTitles(): List<ViewingItem> {
    val essentials = allLists.firstOrNull { it.id == "mcu-release-order" }?.items.orEmpty().take(5)
    val dcHighlights = allItems.filter { it.universe in setOf("DCU", "DCEU", "Elseworlds") && it.status == ViewingStatus.RELEASED }.take(4)
    val trailerReady = allItems.filter { it.status == ViewingStatus.RELEASED && (it.hasAnyTrailer()) && ViewingArtworkUtils.resolveCardPoster(it) != null }.take(6)
    val upcoming = allItems.filter { it.status == ViewingStatus.UPCOMING || it.status == ViewingStatus.ANNOUNCED }.take(3)
    return (listOf(featuredItem) + trailerReady + essentials + dcHighlights + upcoming).distinctBy { it.id }.take(10)
}

internal fun Int.floorMod(size: Int): Int = ((this % size) + size) % size


@Composable
internal fun PosterRail(title: String, subtitle: String, items: List<ViewingItem>, onOpenItem: (ViewingItem) -> Unit) {
    SpectrumLaneRail(title = title, subtitle = subtitle, items = items, onOpenItem = onOpenItem)
}

@Composable
internal fun ContinueQueueRail(title: String, subtitle: String, items: List<ViewingItem>, onOpenItem: (ViewingItem) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SpectrumSectionHeader(title = title, subtitle = subtitle)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.cardGap)) {
            itemsIndexed(items, key = { index, item -> "queue-${item.id}-$index" }) { index, item ->
                ContinueQueueCard(item = item, queueNumber = index + 1, onClick = { onOpenItem(item) })
            }
        }
    }
}

@Composable
internal fun ContinueQueueCard(item: ViewingItem, queueNumber: Int, onClick: () -> Unit) {
    val displayItem = rememberCachedItem(item)
    val accent = spectrumUniverseAccent(displayItem.universe)
    val statuses = ViewingMetadataStore.statusesFor(displayItem)
    PressableCard(
        onClick = onClick,
        modifier = Modifier
            .width(286.dp)
            .semantics { contentDescription = "Continue queue item $queueNumber: ${displayItem.title}. Open details." }
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PosterBackdrop(displayItem, Modifier.size(66.dp, 96.dp), ContentScale.Crop, SpectrumShapes.posterMask)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.chipGap), verticalAlignment = Alignment.CenterVertically) {
                    SpectrumPulseIndicator(active = queueNumber == 1, universe = displayItem.universe)
                    Text("Queue $queueNumber", style = MaterialTheme.typography.labelMedium, color = accent.primary, fontWeight = FontWeight.Bold)
                }
                Text(displayItem.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                Text(statuses.firstOrNull()?.activeLabel ?: "Ready to resume", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                Surface(shape = SpectrumShapes.pillTab, color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                    Box(Modifier.fillMaxWidth().height(6.dp)) {
                        Box(Modifier.fillMaxWidth(((queueNumber % 5) + 3) / 8f).height(6.dp).background(accent.primary.copy(alpha = 0.78f)))
                    }
                }
            }
        }
    }
}

@Composable
internal fun SpectrumLaneRail(title: String, subtitle: String, items: List<ViewingItem>, onOpenItem: (ViewingItem) -> Unit, universe: String? = items.firstOrNull()?.universe) {
    val accent = spectrumUniverseAccent(universe)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SpectrumSectionHeader(title = title, subtitle = subtitle)
        SpectrumRhythmDivider(universe = universe, bars = 22)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.cardGap)) {
            items(items, key = { it.id }) { item -> SpectrumLaneCard(item, accent) { onOpenItem(item) } }
        }
    }
}

@Composable
internal fun SpectrumLaneCard(item: ViewingItem, accent: SpectrumUniverseAccent, onClick: () -> Unit) {
    val displayItem = rememberCachedItem(item)
    PressableCard(
        onClick = onClick,
        modifier = Modifier
            .width(168.dp)
            .semantics { contentDescription = "Open ${displayItem.title} from ${displayItem.universe ?: "spectrum"} lane" }
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(2f / 3f).clip(SpectrumShapes.posterMask)) {
            PosterBackdrop(displayItem, Modifier.fillMaxSize(), ContentScale.Crop, SpectrumShapes.posterMask)
            Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.42f)))))
            Surface(
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                shape = SpectrumShapes.pillTab,
                color = accent.primary.copy(alpha = 0.86f),
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) { Text(displayItem.year ?: displayItem.type.name, Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(10.dp))
        Text(displayItem.title, modifier = Modifier.height(42.dp), maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
        Text(listOfNotNull(displayItem.phase ?: displayItem.saga, displayItem.runtime).joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun ListRail(title: String, subtitle: String, lists: List<ViewingList>, onOpenList: (ViewingList) -> Unit) {
    CollectionAlbumRail(title, subtitle, lists, onOpenList)
}

@Composable
internal fun CollectionAlbumRail(title: String, subtitle: String, lists: List<ViewingList>, onOpenList: (ViewingList) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SpectrumSectionHeader(title = title, subtitle = subtitle)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.cardGap)) {
            items(lists, key = { it.id }) { list -> CollectionAlbumCard(list) { onOpenList(list) } }
        }
    }
}

@Composable
internal fun CollectionAlbumCard(list: ViewingList, onClick: () -> Unit) {
    val accent = spectrumUniverseAccent(list.universe ?: list.category)
    PressableCard(
        onClick = onClick,
        modifier = Modifier
            .width(248.dp)
            .semantics { contentDescription = "Open collection ${list.title}, ${list.items.size} titles" }
    ) {
        CollectionArtwork(list, Modifier.fillMaxWidth().height(142.dp).clip(SpectrumShapes.mediaFrame), ContentScale.Crop)
        Spacer(Modifier.height(12.dp))
        Text(list.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(list.description.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        SpectrumRhythmDivider(universe = list.universe ?: list.category, bars = 12)
        Row(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.chipGap), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = SpectrumShapes.pillTab, color = accent.container, contentColor = accent.onContainer) {
                Text("${list.items.size} titles", Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
            Text(list.category ?: list.universe ?: "Collection", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}


@Composable
internal fun CinemaActivityMiniSurface(item: ViewingItem, onClick: () -> Unit) {
    val displayItem = rememberCachedItem(item)
    Card(onClick = onClick, shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(104.dp)) {
            PosterBackdrop(displayItem, Modifier.fillMaxSize(), ContentScale.Crop, intent = ArtworkDisplayIntent.HERO_BACKDROP)
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
