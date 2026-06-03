@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.cinemaverse.mcu.shared.presentation.screens.viewing

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
import com.cinemaverse.mcu.shared.presentation.components.icons.Icon
import com.cinemaverse.mcu.shared.presentation.components.icons.MaterialSymbolIcon
import com.cinemaverse.mcu.shared.data.service.MovieMetadataService
import com.cinemaverse.mcu.shared.data.viewing.MetadataResult
import com.cinemaverse.mcu.shared.data.viewing.ViewingItem
import com.cinemaverse.mcu.shared.data.viewing.ViewingList
import com.cinemaverse.mcu.shared.data.viewing.ViewingLists
import com.cinemaverse.mcu.shared.data.viewing.ViewingSortMode
import com.cinemaverse.mcu.shared.presentation.components.common.ExpressiveShapeTarget
import com.cinemaverse.mcu.shared.presentation.components.common.rememberExpressiveShapeFor
import com.cinemaverse.mcu.shared.util.ViewingArtworkUtils

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
fun ViewingHomeScreen(
    onOpenLibrary: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenDetail: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metadataService = remember { MovieMetadataService() }
    val message = remember { metadataService.getConfigurationMessage() }
    val featuredList = ViewingLists.featuredList
    val featuredItem = ViewingLists.featuredItem

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = ViewingUiDefaults.ScreenHorizontalPadding, end = ViewingUiDefaults.ScreenHorizontalPadding, top = ViewingUiDefaults.ScreenTopPadding, bottom = ViewingUiDefaults.ScreenBottomPadding),
        verticalArrangement = Arrangement.spacedBy(ViewingUiDefaults.SectionSpacing)
    ) {
        item {
            HeroViewingCard(
                item = featuredItem,
                list = featuredList,
                subtitle = "Featured viewing order • ${featuredList.items.size} titles",
                onOpenDetail = onOpenDetail,
                onOpenLibrary = onOpenLibrary,
                onOpenSettings = onOpenSettings
            )
        }
        item {
            ApiStateCard(message = message, onOpenSettings = onOpenSettings)
        }
        item {
            SectionHeader("Continue browsing", "Recently viewed and watchlist-ready picks", action = "Search", onAction = onOpenSearch)
            Spacer(Modifier.height(ViewingUiDefaults.DenseCardPadding))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUiDefaults.CardSpacing)) {
                items(ViewingLists.allItems.drop(18).take(8)) { item ->
                    PosterCard(item = item, onClick = onOpenDetail)
                }
            }
        }
        item {
            SectionHeader("Featured lists", "Curated release, timeline, phase, and collection orders", action = "View all", onAction = onOpenLibrary)
            Spacer(Modifier.height(ViewingUiDefaults.DenseCardPadding))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUiDefaults.CardSpacing)) {
                items(ViewingLists.allLists.take(8)) { list ->
                    ViewingListCard(list = list, onClick = onOpenLibrary)
                }
            }
        }
        item {
            SectionHeader("Phase-based sections", "Browse by MCU-style phases without changing Rhythm's navigation")
            Spacer(Modifier.height(ViewingUiDefaults.DenseCardPadding))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUiDefaults.CompactSpacing)) {
                items(ViewingLists.allLists.filter { it.phase?.startsWith("Phase") == true }) { list ->
                    AssistChip(onClick = onOpenLibrary, label = { Text("${list.title} • ${list.items.size}") })
                }
            }
        }
        item {
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
    val items = remember(sortMode) {
        when (sortMode) {
            ViewingSortMode.RELEASE -> ViewingLists.allItems.sortedBy { it.releaseOrder ?: Int.MAX_VALUE }
            ViewingSortMode.CHRONOLOGICAL -> ViewingLists.allItems.sortedBy { it.chronologicalOrder ?: Int.MAX_VALUE }
            ViewingSortMode.PHASE -> ViewingLists.allItems.sortedWith(compareBy<ViewingItem> { it.phase ?: "" }.thenBy { it.phaseOrder ?: it.releaseOrder ?: Int.MAX_VALUE })
            ViewingSortMode.CUSTOM -> ViewingLists.featuredList.items
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = ViewingUiDefaults.ScreenHorizontalPadding, end = ViewingUiDefaults.ScreenHorizontalPadding, top = ViewingUiDefaults.ScreenTopPadding, bottom = ViewingUiDefaults.ScreenBottomPadding),
        verticalArrangement = Arrangement.spacedBy(ViewingUiDefaults.ScreenHorizontalPadding)
    ) {
        item {
            SectionHeader(
                title = "Library",
                subtitle = "Viewing lists, collections, phases, and watch orders",
                action = "Settings",
                onAction = onOpenSettings
            )
        }
        item {
            SectionHeader("Lists / Collections", "All bundled viewing lists remain editable in the local data file")
            Spacer(Modifier.height(ViewingUiDefaults.DenseCardPadding))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUiDefaults.CardSpacing)) {
                items(ViewingLists.allLists) { list -> ViewingListCard(list, onClick = {}) }
            }
        }
        item {
            SectionHeader("Viewing Order", "Switch between release, chronological, phase, and custom order")
            Spacer(Modifier.height(ViewingUiDefaults.CompactSpacing))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(ViewingUiDefaults.MicroSpacing)) {
                items(ViewingSortMode.values().toList()) { mode ->
                    FilterChip(selected = sortMode == mode, onClick = { sortMode = mode }, label = { Text(mode.label) })
                }
            }
        }
        items(items) { item ->
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
    val (movies, lists) = remember(query) { ViewingLists.search(query) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Rhythm") },
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
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Movies, lists, phases, sagas, actors, directors, genres") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (movies.isEmpty() && lists.isEmpty()) {
                item { EmptyState("No viewing results", "Try a movie title, phase, saga, director, actor, or genre.") }
            } else {
                if (lists.isNotEmpty()) {
                    item { SectionHeader("Lists", "${lists.size} matching collections") }
                    items(lists) { list -> CompactListResult(list = list) }
                }
                if (movies.isNotEmpty()) {
                    item { SectionHeader("Movies / Titles", "${movies.size} matching titles") }
                    items(movies) { item -> ViewingOrderRow(item = item, order = item.releaseOrder ?: 0, onClick = onOpenDetail) }
                }
            }
            item { TextButton(onClick = onBack) { Text("Back") } }
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
    val viewingPrefs = remember { context.getSharedPreferences("rhythm_viewing_state", android.content.Context.MODE_PRIVATE) }
    val metadataService = remember { MovieMetadataService() }
    var result by remember { mutableStateOf(MetadataResult(baseItem)) }
    var isLoading by remember { mutableStateOf(true) }
    var isWatchlisted by rememberSaveable(baseItem.id) { mutableStateOf(viewingPrefs.getBoolean("watchlist_${baseItem.id}", false)) }
    var isWatched by rememberSaveable(baseItem.id) { mutableStateOf(viewingPrefs.getBoolean("watched_${baseItem.id}", false)) }
    var isFavorite by rememberSaveable(baseItem.id) { mutableStateOf(viewingPrefs.getBoolean("favorite_${baseItem.id}", false)) }

    LaunchedEffect(baseItem.id) {
        isLoading = true
        result = metadataService.getEnrichedViewingItem(baseItem)
        isLoading = false
    }

    LaunchedEffect(baseItem.id, isWatchlisted, isWatched, isFavorite) {
        viewingPrefs.edit()
            .putBoolean("watchlist_${baseItem.id}", isWatchlisted)
            .putBoolean("watched_${baseItem.id}", isWatched)
            .putBoolean("favorite_${baseItem.id}", isFavorite)
            .apply()
    }

    val item = result.item
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = ViewingUiDefaults.DetailBottomPadding),
        verticalArrangement = Arrangement.spacedBy(ViewingUiDefaults.DetailContentSpacing)
    ) {
        item {
            DetailHero(item = item, isLoading = isLoading, onBack = onBack)
        }
        item {
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
        item { InfoPanel(item = item) }
        item { TrailerPanel(item = item) }
    }
}

@Composable
private fun HeroViewingCard(
    item: ViewingItem,
    list: ViewingList,
    subtitle: String,
    onOpenDetail: () -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ViewingUiDefaults.HomeHeroHeight)
            .clip(RoundedCornerShape(16.dp))
    ) {
        PosterBackdrop(item = item, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0f), MaterialTheme.colorScheme.surfaceContainerHigh))))
        FilledTonalButton(
            onClick = onOpenSettings,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .semantics { contentDescription = "Open viewing settings" }
        ) {
            Icon(
                icon = MaterialSymbolIcon("settings", filled = true),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                size = 18.dp
            )
            Spacer(Modifier.width(8.dp))
            Text("Settings")
        }
        Column(
            Modifier.align(Alignment.BottomStart).padding(ViewingUiDefaults.SectionSpacing),
            verticalArrangement = Arrangement.spacedBy(ViewingUiDefaults.MicroSpacing)
        ) {
            Text("Selected Movie", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            Text(item.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, maxLines = 2)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(ViewingUiDefaults.CompactSpacing)) {
                Button(onClick = onOpenDetail) { Text("Open detail") }
                OutlinedButton(onClick = onOpenLibrary) { Text("View list") }
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
        SectionHeader("Trailer", "Rhythm's focused media space is now a trailer panel")
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
            Text("Rhythm", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
        }
    } else {
        AsyncImage(model = data, contentDescription = description, contentScale = contentScale, modifier = modifier)
    }
}

private fun openUrl(context: android.content.Context, url: String?) {
    if (url.isNullOrBlank()) return
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}
