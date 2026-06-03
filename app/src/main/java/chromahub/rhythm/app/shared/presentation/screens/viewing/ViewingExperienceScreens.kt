@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package chromahub.rhythm.app.shared.presentation.screens.viewing

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import chromahub.rhythm.app.shared.presentation.components.icons.Icon
import chromahub.rhythm.app.shared.presentation.components.icons.MaterialSymbolIcon
import chromahub.rhythm.app.shared.data.service.MovieMetadataService
import chromahub.rhythm.app.shared.data.viewing.MetadataResult
import chromahub.rhythm.app.shared.data.viewing.ViewingItem
import chromahub.rhythm.app.shared.data.viewing.ViewingList
import chromahub.rhythm.app.shared.data.viewing.ViewingLists
import chromahub.rhythm.app.shared.data.viewing.ViewingSortMode
import chromahub.rhythm.app.shared.util.ViewingArtworkUtils

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
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 128.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
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
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(ViewingLists.allItems.drop(18).take(8)) { item ->
                    PosterCard(item = item, onClick = onOpenDetail)
                }
            }
        }
        item {
            SectionHeader("Featured lists", "Curated release, timeline, phase, and collection orders", action = "View all", onAction = onOpenLibrary)
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(ViewingLists.allLists.take(8)) { list ->
                    ViewingListCard(list = list, onClick = onOpenLibrary)
                }
            }
        }
        item {
            SectionHeader("Phase-based sections", "Browse by MCU-style phases without changing Rhythm's navigation")
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 128.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
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
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(ViewingLists.allLists) { list -> ViewingListCard(list, onClick = {}) }
            }
        }
        item {
            SectionHeader("Viewing Order", "Switch between release, chronological, phase, and custom order")
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 128.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
        contentPadding = PaddingValues(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            DetailHero(item = item, isLoading = isLoading, onBack = onBack)
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(item.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(listOfNotNull(item.year, item.phase, item.runtime, item.genres.take(2).joinToString(" / ").takeIf { it.isNotBlank() }).joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(onClick = { isWatchlisted = !isWatchlisted }) { Text(if (isWatchlisted) "In Watchlist" else "Add to Watchlist") }
                    OutlinedButton(onClick = { isWatched = !isWatched }) { Text(if (isWatched) "Watched" else "Mark watched") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
    ElevatedCard(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(Modifier.fillMaxWidth().height(300.dp)) {
            PosterBackdrop(item = item, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.surfaceContainerHigh))))
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
                Modifier.align(Alignment.BottomStart).padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Selected Movie", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                Text(item.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, maxLines = 2)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onOpenDetail) { Text("Open detail") }
                    OutlinedButton(onClick = onOpenLibrary) { Text("View list") }
                }
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
    PressableCard(onClick = onClick, modifier = Modifier.width(150.dp)) {
        PosterBackdrop(item = item, modifier = Modifier.fillMaxWidth().aspectRatio(2f / 3f).clip(RoundedCornerShape(20.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.height(8.dp))
        Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(listOfNotNull(item.year, item.phase).joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun ViewingListCard(list: ViewingList, onClick: () -> Unit) {
    PressableCard(onClick = onClick, modifier = Modifier.width(220.dp)) {
        Box(Modifier.fillMaxWidth().height(126.dp).clip(RoundedCornerShape(24.dp))) {
            val poster = ViewingArtworkUtils.resolveBackdrop(list) ?: ViewingArtworkUtils.resolvePoster(list)
            ArtworkImage(data = poster, description = "${list.title} collection artwork", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)))))
            Text("${list.items.size} titles", modifier = Modifier.align(Alignment.BottomStart).padding(12.dp), color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(8.dp))
        Text(list.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2)
        Text(list.description ?: list.phase ?: "Viewing list", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
    }
}

@Composable
private fun ViewingOrderRow(item: ViewingItem, order: Int, onClick: () -> Unit) {
    Card(onClick = onClick, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), shape = RoundedCornerShape(22.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(order.toString().padStart(2, '0'), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            PosterBackdrop(item = item, modifier = Modifier.size(62.dp, 92.dp).clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
            Column(Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(listOfNotNull(item.year, item.phase, item.runtime).joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Text(item.genres.joinToString(" / "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

@Composable
private fun DetailHero(item: ViewingItem, isLoading: Boolean, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().height(420.dp)) {
        PosterBackdrop(item = item, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f), MaterialTheme.colorScheme.background))))
        Column(Modifier.align(Alignment.BottomStart).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onBack) { Text("Back") }
            PosterBackdrop(item = item, modifier = Modifier.size(150.dp, 225.dp).clip(RoundedCornerShape(28.dp)), contentScale = ContentScale.Crop)
            AnimatedVisibility(visible = isLoading, enter = fadeIn(), exit = fadeOut()) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun InfoPanel(item: ViewingItem) {
    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Trailer", "Rhythm's focused media space is now a trailer panel")
        Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
    Card(onClick = onClick, shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text(list.title, fontWeight = FontWeight.SemiBold)
            Text(listOfNotNull(list.phase, list.saga, "${list.items.size} titles").joinToString(" • "), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyState(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PressableCard(modifier: Modifier = Modifier, onClick: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, spring(), label = "pressScale")
    val container by animateColorAsState(if (pressed) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer, label = "cardColor")
    Card(
        colors = CardDefaults.cardColors(containerColor = container),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale }.clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) { Column(Modifier.padding(12.dp), content = content) }
}

@Composable
private fun PosterBackdrop(item: ViewingItem, modifier: Modifier, contentScale: ContentScale) {
    ArtworkImage(
        data = ViewingArtworkUtils.resolveBackdrop(item) ?: ViewingArtworkUtils.resolvePoster(item),
        description = "Poster for ${item.title}",
        modifier = modifier,
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
