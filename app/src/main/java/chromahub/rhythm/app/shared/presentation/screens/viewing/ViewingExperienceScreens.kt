@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package chromahub.rhythm.app.shared.presentation.screens.viewing

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import chromahub.rhythm.app.shared.presentation.components.icons.Icon
import chromahub.rhythm.app.shared.presentation.components.icons.MaterialSymbolIcon
import chromahub.rhythm.app.shared.data.service.MovieMetadataService
import chromahub.rhythm.app.shared.data.viewing.MetadataResult
import chromahub.rhythm.app.shared.data.viewing.ViewingItem
import chromahub.rhythm.app.shared.data.viewing.ViewingList
import chromahub.rhythm.app.shared.data.viewing.McuAssetDataSource
import chromahub.rhythm.app.shared.data.viewing.ViewingCatalog
import chromahub.rhythm.app.shared.data.viewing.ViewingLists
import chromahub.rhythm.app.shared.data.viewing.ViewingSortMode
import chromahub.rhythm.app.shared.presentation.components.common.ExpressiveShapeTarget
import chromahub.rhythm.app.shared.presentation.components.common.rememberExpressiveShapeFor
import chromahub.rhythm.app.shared.util.ViewingArtworkUtils

private object ViewingUiDefaults {
    val ScreenHorizontalPadding = 20.dp
    val ScreenTopPadding = 24.dp
    val ScreenBottomPadding = 128.dp
    val DetailBottomPadding = 36.dp
    val SectionSpacing = 22.dp
    val CardSpacing = 14.dp
    val CompactSpacing = 10.dp
    val TinySpacing = 6.dp
    val MicroSpacing = 8.dp
    val DetailContentSpacing = 18.dp
    val EmptyStatePadding = 18.dp
    val CardPadding = 16.dp
    val CompactCardPadding = 14.dp
    val DenseCardPadding = 12.dp
    val DetailHeroHeight = 420.dp
    val HomeHeroHeight = 300.dp
    val ListArtworkHeight = 126.dp
    val PosterWidth = 150.dp
    val ListCardWidth = 220.dp
    val DetailPosterWidth = 150.dp
    val DetailPosterHeight = 225.dp
    val RowPosterWidth = 62.dp
    val RowPosterHeight = 92.dp

    fun subtlePressSpec() = tween<Float>(
        durationMillis = 160,
        easing = FastOutSlowInEasing
    )

    fun subtleColorSpec() = tween<androidx.compose.ui.graphics.Color>(
        durationMillis = 160,
        easing = FastOutSlowInEasing
    )
}


@Composable
private fun rememberViewingCatalog(): ViewingCatalog {
    val context = LocalContext.current.applicationContext
    return remember(context) { McuAssetDataSource(context).loadCatalog() }
}

@Composable
fun ViewingHomeScreen(
    onOpenLibrary: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDetail: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metadataService = remember { MovieMetadataService() }
    val message = remember { metadataService.getConfigurationMessage() }
    val catalog = rememberViewingCatalog()
    val featuredList = remember(catalog) { catalog.featuredList }
    val featuredItem = remember(catalog) { catalog.featuredItem }
    val continueBrowsingItems = remember(catalog) { catalog.allItems.drop(18).take(8).ifEmpty { catalog.allItems.take(8) } }
    val featuredLists = remember(catalog) { catalog.allLists.take(8) }
    val phaseLists = remember(catalog) { catalog.allLists.filter { it.phase?.startsWith("Phase") == true } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = ViewingUiDefaults.ScreenHorizontalPadding, end = ViewingUiDefaults.ScreenHorizontalPadding, top = ViewingUiDefaults.ScreenTopPadding, bottom = ViewingUiDefaults.ScreenBottomPadding),
        verticalArrangement = Arrangement.spacedBy(ViewingUiDefaults.SectionSpacing)
    ) {
        item(key = "marvel-header", contentType = "header") {
            MarvelSpectrumHeader(onOpenSearch = onOpenSearch, onOpenSettings = onOpenSettings)
        }
        item(key = "hero", contentType = "hero") {
            HeroViewingCard(
                item = featuredItem,
                list = featuredList,
                subtitle = "Featured viewing order • ${featuredList.items.size} titles",
                onOpenDetail = onOpenDetail,
                onOpenLibrary = onOpenLibrary
            )
        }
        item(key = "api-state", contentType = "info-card") {
            ApiStateCard(message = message, onOpenSettings = onOpenSettings)
        }
        item(key = "continue", contentType = "section") {
            SectionHeader("Continue the timeline", "Poster-first picks from the bundled MCU order", action = "Search", onAction = onOpenSearch)
            Spacer(Modifier.height(ViewingUiDefaults.DenseCardPadding))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUiDefaults.CardSpacing)) {
                items(
                    items = continueBrowsingItems,
                    key = { it.id },
                    contentType = { "poster-card" }
                ) { item ->
                    PosterCard(item = item, onClick = onOpenDetail)
                }
            }
        }
        item(key = "featured-lists", contentType = "section") {
            SectionHeader("Featured lists", "Curated release, timeline, phase, and collection orders", action = "View all", onAction = onOpenLibrary)
            Spacer(Modifier.height(ViewingUiDefaults.DenseCardPadding))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUiDefaults.CardSpacing)) {
                items(
                    items = featuredLists,
                    key = { it.id },
                    contentType = { "viewing-list-card" }
                ) { list ->
                    ViewingListCard(list = list, onClick = onOpenLibrary)
                }
            }
        }
        item(key = "phases", contentType = "section") {
            SectionHeader("Phase collections", "Explore the saga by MCU phase")
            Spacer(Modifier.height(ViewingUiDefaults.DenseCardPadding))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUiDefaults.CompactSpacing)) {
                items(
                    items = phaseLists,
                    key = { it.id },
                    contentType = { "phase-chip" }
                ) { list ->
                    AssistChip(onClick = onOpenLibrary, label = { Text("${list.title} • ${list.items.size}") })
                }
            }
        }
        item(key = "watchlist", contentType = "shortcut") {
            WatchlistShortcut(onOpenLibrary)
        }
    }
}

@Composable
fun ViewingLibraryScreen(
    onOpenDetail: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sortMode by rememberSaveable { mutableStateOf(ViewingSortMode.RELEASE) }
    val catalog = rememberViewingCatalog()
    val allLists = remember(catalog) { catalog.allLists }
    val sortModes = remember { ViewingSortMode.values().toList() }
    val items = remember(catalog, sortMode) {
        when (sortMode) {
            ViewingSortMode.RELEASE -> catalog.allItems.sortedBy { it.releaseOrder ?: Int.MAX_VALUE }
            ViewingSortMode.CHRONOLOGICAL -> catalog.allItems.sortedBy { it.chronologicalOrder ?: Int.MAX_VALUE }
            ViewingSortMode.PHASE -> catalog.allItems.sortedWith(compareBy<ViewingItem> { it.phase ?: "" }.thenBy { it.phaseOrder ?: it.releaseOrder ?: Int.MAX_VALUE })
            ViewingSortMode.CUSTOM -> catalog.featuredList.items
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = ViewingUiDefaults.ScreenHorizontalPadding, end = ViewingUiDefaults.ScreenHorizontalPadding, top = ViewingUiDefaults.ScreenTopPadding, bottom = ViewingUiDefaults.ScreenBottomPadding),
        verticalArrangement = Arrangement.spacedBy(ViewingUiDefaults.ScreenHorizontalPadding)
    ) {
        item(key = "library-header", contentType = "header") {
            MarvelSpectrumHeader(onOpenSettings = onOpenSettings)
        }
        item(key = "library-title", contentType = "section") {
            SectionHeader(
                title = "Explore the saga",
                subtitle = "Viewing lists, collections, phases, and watch orders",
                action = "Settings",
                onAction = onOpenSettings
            )
        }
        item(key = "lists", contentType = "section") {
            SectionHeader("Featured lists", "Bundled collections from release, timeline, phase, and heroes")
            Spacer(Modifier.height(ViewingUiDefaults.DenseCardPadding))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUiDefaults.CardSpacing)) {
                items(
                    items = allLists,
                    key = { it.id },
                    contentType = { "viewing-list-card" }
                ) { list -> ViewingListCard(list, onClick = {}) }
            }
        }
        item(key = "sort", contentType = "section") {
            SectionHeader("Timeline order", "Switch between release, chronological, phase, and custom order")
            Spacer(Modifier.height(ViewingUiDefaults.CompactSpacing))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUiDefaults.MicroSpacing)) {
                items(
                    items = sortModes,
                    key = { it.name },
                    contentType = { "sort-chip" }
                ) { mode ->
                    FilterChip(selected = sortMode == mode, onClick = { sortMode = mode }, label = { Text(mode.label) })
                }
            }
        }
        items(
            items = items,
            key = { it.id },
            contentType = { "viewing-order-row" }
        ) { item ->
            ViewingOrderRow(item = item, order = when (sortMode) {
                ViewingSortMode.CHRONOLOGICAL -> item.chronologicalOrder
                ViewingSortMode.PHASE -> item.phaseOrder
                else -> item.releaseOrder
            } ?: 0, onClick = onOpenDetail)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewingSearchScreen(
    onBack: () -> Unit,
    onOpenDetail: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    val catalog = rememberViewingCatalog()
    val (movies, lists) = remember(catalog, query) { catalog.search(query) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search titles, phases, sagas, heroes") },
                actions = {
                    SettingsIconAction(onOpenSettings)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = ViewingUiDefaults.ScreenHorizontalPadding, end = ViewingUiDefaults.ScreenHorizontalPadding, top = ViewingUiDefaults.DenseCardPadding, bottom = ViewingUiDefaults.ScreenBottomPadding),
            verticalArrangement = Arrangement.spacedBy(ViewingUiDefaults.CardPadding)
        ) {
            item(key = "search-field", contentType = "search-field") {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search titles, phases, sagas, heroes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (movies.isEmpty() && lists.isEmpty()) {
                item(key = "empty", contentType = "empty") { EmptyState("No viewing results", "Try a movie title, phase, saga, director, actor, or genre.") }
            } else {
                if (lists.isNotEmpty()) {
                    item(key = "matching-lists", contentType = "section") { SectionHeader("Collections", "${lists.size} matching collections") }
                    items(
                        items = lists,
                        key = { it.id },
                        contentType = { "compact-list-result" }
                    ) { list -> CompactListResult(list = list) }
                }
                if (movies.isNotEmpty()) {
                    item(key = "matching-titles", contentType = "section") { SectionHeader("Titles", "${movies.size} matching titles") }
                    items(
                        items = movies,
                        key = { it.id },
                        contentType = { "viewing-order-row" }
                    ) { item -> ViewingOrderRow(item = item, order = item.releaseOrder ?: 0, onClick = onOpenDetail) }
                }
            }
            item(key = "back", contentType = "action") { TextButton(onClick = onBack) { Text("Back") } }
        }
    }
}

@Composable
fun ViewingDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    baseItem: ViewingItem = ViewingLists.featuredItem
) {
    val context = LocalContext.current
    val catalog = rememberViewingCatalog()
    val catalogItem = remember(catalog, baseItem.id) { catalog.findItem(baseItem.id) ?: baseItem }
    val viewingPrefs = remember { context.getSharedPreferences("rhythm_viewing_state", android.content.Context.MODE_PRIVATE) }
    val metadataService = remember { MovieMetadataService() }
    var result by remember(catalogItem.id) { mutableStateOf(MetadataResult(catalogItem)) }
    var isLoading by remember { mutableStateOf(true) }
    var isWatchlisted by rememberSaveable(catalogItem.id) { mutableStateOf(viewingPrefs.getBoolean("watchlist_${catalogItem.id}", false)) }
    var isWatched by rememberSaveable(catalogItem.id) { mutableStateOf(viewingPrefs.getBoolean("watched_${catalogItem.id}", false)) }
    var isFavorite by rememberSaveable(catalogItem.id) { mutableStateOf(viewingPrefs.getBoolean("favorite_${catalogItem.id}", false)) }

    LaunchedEffect(catalogItem.id) {
        isLoading = true
        result = metadataService.getEnrichedViewingItem(catalogItem)
        isLoading = false
    }

    LaunchedEffect(catalogItem.id, isWatchlisted, isWatched, isFavorite) {
        viewingPrefs.edit()
            .putBoolean("watchlist_${catalogItem.id}", isWatchlisted)
            .putBoolean("watched_${catalogItem.id}", isWatched)
            .putBoolean("favorite_${catalogItem.id}", isFavorite)
            .apply()
    }

    val item = result.item
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = ViewingUiDefaults.DetailBottomPadding),
        verticalArrangement = Arrangement.spacedBy(ViewingUiDefaults.DetailContentSpacing)
    ) {
        item(key = "detail-hero", contentType = "hero") {
            DetailHero(item = item, isLoading = isLoading, onBack = onBack)
        }
        item(key = "detail-actions", contentType = "section") {
            Column(Modifier.padding(horizontal = ViewingUiDefaults.ScreenHorizontalPadding), verticalArrangement = Arrangement.spacedBy(ViewingUiDefaults.CardSpacing)) {
                Text(item.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(listOfNotNull(item.year, item.phase, item.runtime, item.genres.take(2).joinToString(" / ").takeIf { it.isNotBlank() }).joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(ViewingUiDefaults.CompactSpacing)) {
                    FilledTonalButton(onClick = { isWatchlisted = !isWatchlisted }) { Text(if (isWatchlisted) "In Watchlist" else "Add to Watchlist") }
                    OutlinedButton(onClick = { isWatched = !isWatched }) { Text(if (isWatched) "Watched" else "Mark watched") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(ViewingUiDefaults.CompactSpacing)) {
                    Button(onClick = { openUrl(context, item.trailerUrl) }, enabled = !item.trailerUrl.isNullOrBlank()) { Text("Watch trailer") }
                    OutlinedButton(onClick = { isFavorite = !isFavorite }) { Text(if (isFavorite) "Favorited" else "Favorite") }
                }
                if (!result.message.isNullOrBlank()) {
                    ApiStateCard(message = result.message ?: "Using local fallback metadata.")
                }
            }
        }
        item(key = "detail-info", contentType = "section") { InfoPanel(item = item) }
        item(key = "detail-trailer", contentType = "section") { TrailerPanel(item = item) }
    }
}

@Composable
private fun MarvelSpectrumHeader(
    onOpenSearch: (() -> Unit)? = null,
    onOpenSettings: () -> Unit
) {
    ElevatedCard(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.48f),
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ViewingUiDefaults.CardPadding)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text("MS", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Black)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Marvel Spectrum", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("MCU viewing order", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Bundled local posters and metadata are available offline", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            if (onOpenSearch != null) {
                TextButton(onClick = onOpenSearch) { Text("Search") }
            }
            FilledTonalButton(onClick = onOpenSettings) { Text("Settings") }
        }
    }
}

@Composable
private fun HeroViewingCard(
    item: ViewingItem,
    list: ViewingList,
    subtitle: String,
    onOpenDetail: () -> Unit,
    onOpenLibrary: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ViewingUiDefaults.HomeHeroHeight)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        PosterBackdrop(item = item, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface.copy(alpha = 0.05f), MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)))))
        Column(
            Modifier.align(Alignment.BottomStart).padding(ViewingUiDefaults.SectionSpacing),
            verticalArrangement = Arrangement.spacedBy(ViewingUiDefaults.MicroSpacing)
        ) {
            Text("Featured MCU pick", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(item.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, maxLines = 2)
            Text(
                listOfNotNull(item.saga, item.phase, item.year, item.runtime).joinToString(" • "),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text("$subtitle • ${list.items.size} titles", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(ViewingUiDefaults.CompactSpacing)) {
                Button(onClick = onOpenDetail) { Text("Open details") }
                OutlinedButton(onClick = onOpenLibrary) { Text("View order") }
            }
        }
    }
}

@Composable
private fun SettingsIconAction(onOpenSettings: () -> Unit) {
    TextButton(
        onClick = onOpenSettings,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier.semantics { contentDescription = "Open viewing settings" }
    ) {
        Icon(
            icon = MaterialSymbolIcon("settings", filled = true),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
            size = 18.dp
        )
        Spacer(Modifier.width(6.dp))
        Text("Settings")
    }
}

@Composable
private fun PosterCard(item: ViewingItem, onClick: () -> Unit) {
    Column(modifier = Modifier
        .width(ViewingUiDefaults.PosterWidth)
        .clip(RoundedCornerShape(8.dp))
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .clickable(onClick = onClick)
        .padding(8.dp)
    ) {
        PosterBackdrop(
            item = item,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop,
            shape = RoundedCornerShape(6.dp)
        )
        Spacer(Modifier.height(ViewingUiDefaults.MicroSpacing))
        Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(listOfNotNull(item.year, item.phase).joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun ViewingListCard(list: ViewingList, onClick: () -> Unit) {
    Column(modifier = Modifier
        .width(ViewingUiDefaults.ListCardWidth)
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .clickable(onClick = onClick)
        .padding(8.dp)
    ) {
        Box(Modifier
            .fillMaxWidth()
            .height(ViewingUiDefaults.ListArtworkHeight)
            .clip(RoundedCornerShape(10.dp))
        ) {
            val poster = ViewingArtworkUtils.resolveBackdrop(list) ?: ViewingArtworkUtils.resolvePoster(list)
            ArtworkImage(data = poster, description = "${list.title} collection artwork", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface.copy(alpha = 0f), MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)))))
            Text("${list.items.size} titles", modifier = Modifier.align(Alignment.BottomStart).padding(ViewingUiDefaults.DenseCardPadding), color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(ViewingUiDefaults.MicroSpacing))
        Text(list.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2)
        Text(list.description ?: list.phase ?: "Viewing list", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
    }
}

@Composable
private fun ViewingOrderRow(item: ViewingItem, order: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(ViewingUiDefaults.DenseCardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ViewingUiDefaults.DenseCardPadding)
    ) {
        Text(order.toString().padStart(2, '0'), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        PosterBackdrop(
            item = item,
            modifier = Modifier
                .size(ViewingUiDefaults.RowPosterWidth, ViewingUiDefaults.RowPosterHeight)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop,
            shape = RoundedCornerShape(6.dp)
        )
        Column(Modifier.weight(1f)) {
            Text(item.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(listOfNotNull(item.year, item.phase, item.runtime).joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text(item.genres.joinToString(" / "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun DetailHero(item: ViewingItem, isLoading: Boolean, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(ViewingUiDefaults.DetailHeroHeight)) {
        PosterBackdrop(item = item, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f), MaterialTheme.colorScheme.background))))
        Column(Modifier.align(Alignment.BottomStart).padding(ViewingUiDefaults.ScreenHorizontalPadding), verticalArrangement = Arrangement.spacedBy(ViewingUiDefaults.DenseCardPadding)) {
            TextButton(onClick = onBack) { Text("Back") }
            PosterBackdrop(
                item = item,
                modifier = Modifier
                    .size(ViewingUiDefaults.DetailPosterWidth, ViewingUiDefaults.DetailPosterHeight)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                shape = RoundedCornerShape(8.dp)
            )
            AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun InfoPanel(item: ViewingItem) {
    Column(Modifier.padding(horizontal = ViewingUiDefaults.ScreenHorizontalPadding), verticalArrangement = Arrangement.spacedBy(ViewingUiDefaults.CardPadding)) {
        SectionHeader("Detailed movie information", "Overview, cast & crew, ratings, release details, and order placement")
        MetadataSection("Overview", listOf("Plot" to (item.plot ?: item.overview ?: "No overview available."), "Genres" to item.genres.joinToString(", ").ifBlank { "Not available" }))
        MetadataSection("Cast & Crew", listOf("Director" to (item.director ?: "Not available"), "Writer" to (item.writer ?: "Not available"), "Actors" to (item.actors.joinToString(", ").ifBlank { item.cast.take(6).joinToString { it.name }.ifBlank { "Not available" } })))
        MetadataSection("Ratings", listOf("IMDb" to (item.imdbRating ?: "Not available"), "TMDB" to (item.tmdbRating?.let { "%.1f".format(it) } ?: "Not available"), "Awards" to (item.awards ?: "Not available")) + item.ratings.map { it.source to it.value })
        MetadataSection("Release details", listOf("Release date" to (item.releaseDate ?: item.year ?: "Not available"), "Runtime" to (item.runtime ?: "Not available"), "Language" to (item.language ?: "Not available"), "Country" to (item.country ?: "Not available"), "IMDb ID" to (item.imdbId ?: "Not available"), "TMDB ID" to (item.tmdbId?.toString() ?: "Not available")))
        MetadataSection("Collection / timeline placement", listOf("Phase" to (item.phase ?: "Not available"), "Saga" to (item.saga ?: "Not available"), "Release order" to (item.releaseOrder?.toString() ?: "Not available"), "Chronological order" to (item.chronologicalOrder?.toString() ?: "Not available")))
    }
}

@Composable
private fun TrailerPanel(item: ViewingItem) {
    Column(Modifier.padding(horizontal = ViewingUiDefaults.ScreenHorizontalPadding), verticalArrangement = Arrangement.spacedBy(ViewingUiDefaults.DenseCardPadding)) {
        SectionHeader("Trailer gallery", "Clips and trailers for this MCU title")
        Card(shape = MaterialTheme.shapes.extraLarge, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
            Column(Modifier.fillMaxWidth().padding(ViewingUiDefaults.EmptyStatePadding), verticalArrangement = Arrangement.spacedBy(ViewingUiDefaults.CompactSpacing)) {
                if (item.trailerUrl.isNullOrBlank()) {
                    EmptyState("Trailer unavailable", "Add a manual trailerUrl in ViewingLists.kt or configure TMDB videos.")
                } else {
                    Text("Trailer available", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(item.trailerUrl, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    val context = LocalContext.current
                    Button(onClick = { openUrl(context, item.trailerUrl) }) { Text("Open trailer externally") }
                }
            }
        }
    }
}

@Composable
private fun MetadataSection(title: String, rows: List<Pair<String, String>>) {
    Card(shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.fillMaxWidth().padding(ViewingUiDefaults.CardPadding), verticalArrangement = Arrangement.spacedBy(ViewingUiDefaults.CompactSpacing)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            rows.forEach { (label, value) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.4f))
                    Text(value, modifier = Modifier.weight(0.6f), maxLines = 4, overflow = TextOverflow.Ellipsis)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
private fun ApiStateCard(message: String, onOpenSettings: (() -> Unit)? = null) {
    Card(shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Row(Modifier.fillMaxWidth().padding(ViewingUiDefaults.CardPadding), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(ViewingUiDefaults.DenseCardPadding)) {
            Column(Modifier.weight(1f)) {
                Text("Offline-safe metadata", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(message, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.78f))
            }
            if (onOpenSettings != null) TextButton(onClick = onOpenSettings) { Text("Settings") }
        }
    }
}

@Composable
private fun WatchlistShortcut(onClick: () -> Unit) {
    Card(onClick = onClick, shape = MaterialTheme.shapes.extraLarge, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(ViewingUiDefaults.ScreenHorizontalPadding), verticalArrangement = Arrangement.spacedBy(ViewingUiDefaults.TinySpacing)) {
            Text("Watchlist shortcut", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("Use detail cards to save favorites, mark watched, and keep a personal viewing queue.", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f))
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (action != null && onAction != null) TextButton(onClick = onAction) { Text(action) }
    }
}

@Composable
private fun CompactListResult(list: ViewingList) {
    Card(shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.fillMaxWidth().padding(ViewingUiDefaults.CompactCardPadding)) {
            Text(list.title, fontWeight = FontWeight.SemiBold)
            Text(listOfNotNull(list.phase, list.saga, "${list.items.size} titles").joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyState(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(ViewingUiDefaults.EmptyStatePadding), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(ViewingUiDefaults.TinySpacing)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PressableCard(modifier: Modifier = Modifier, onClick: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, ViewingUiDefaults.subtlePressSpec(), label = "pressScale")
    val container by animateColorAsState(if (pressed) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer, ViewingUiDefaults.subtleColorSpec(), label = "cardColor")
    Card(
        colors = CardDefaults.cardColors(containerColor = container),
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
        interactionSource = interaction,
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale }
    ) { Column(Modifier.padding(ViewingUiDefaults.DenseCardPadding), content = content) }
}

@Composable
private fun PosterBackdrop(
    item: ViewingItem,
    modifier: Modifier,
    contentScale: ContentScale,
    shape: Shape? = null
) {
    val artworkModifier = if (shape != null) modifier.clip(shape) else modifier
    ArtworkImage(
        data = ViewingArtworkUtils.resolveBackdrop(item) ?: ViewingArtworkUtils.resolvePoster(item),
        description = "Poster for ${item.title}",
        modifier = artworkModifier,
        contentScale = contentScale
    )
}

@Composable
private fun ArtworkImage(data: String?, description: String, modifier: Modifier, contentScale: ContentScale) {
    if (data.isNullOrBlank()) {
        Box(
            modifier = modifier
                .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer)))
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center
        ) {
            Text("Marvel", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
        }
    } else {
        val context = LocalContext.current
        val request = remember(data) {
            ImageRequest.Builder(context)
                .data(data)
                .crossfade(false)
                .memoryCacheKey(data)
                .diskCacheKey(data)
                .build()
        }
        AsyncImage(model = request, contentDescription = description, contentScale = contentScale, modifier = modifier)
    }
}

private fun openUrl(context: android.content.Context, url: String?) {
    if (url.isNullOrBlank()) return
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}
