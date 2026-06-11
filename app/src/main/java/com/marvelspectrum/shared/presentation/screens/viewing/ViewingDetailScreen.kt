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
import kotlinx.coroutines.delay

@Composable
fun ViewingDetailScreen(
    item: ViewingItem? = null,
    list: ViewingList? = null,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewingViewModel: ViewingViewModel = viewModel()
) {
    val viewingState by viewingViewModel.uiState.collectAsState()
    val data = viewingState.data
    if (data == null) {
        ViewingCatalogLoadingState(
            title = "Loading Details",
            message = viewingState.errorMessage ?: "Getting title details ready…",
            modifier = modifier,
            onBack = onBack
        )
        return
    }
    var relatedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    data.findItem(relatedItemId)?.let { related -> ViewingDetailScreen(related, list, { relatedItemId = null }, modifier, viewingViewModel); return }
    val selected = rememberEnrichedItem(item ?: data.featuredItem)
    LaunchedEffect(selected.id) { ViewingMetadataStore.markViewed(selected) }
    var showTrailer by rememberSaveable(selected.id) { mutableStateOf(false) }
    var statuses by remember(selected.id) { mutableStateOf(ViewingMetadataStore.statusesFor(selected)) }
    val refreshStatuses = { statuses = ViewingMetadataStore.statusesFor(selected) }
    val related = remember(selected, data) { data.allItems.filter { it.id != selected.id && (it.franchise == selected.franchise || it.universe == selected.universe || it.genres.any(selected.genres::contains)) }.take(12) }
    Scaffold(topBar = { TopAppBar(title = {}, navigationIcon = { SpectrumIconButton(onClick = onBack) { Icon(RhythmIcons.Back, "Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)) }, modifier = modifier) { padding ->
        SpectrumGradientScaffoldBackground(universe = selected.universe) {
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, SpectrumSpacing.bottomSafePadding), verticalArrangement = Arrangement.spacedBy(22.dp)) {
                item { CinematicDetailHero(selected, statuses, onTrailer = { showTrailer = true }, onStatusToggle = { status ->
                    ViewingMetadataStore.toggleStatus(selected, status)
                    refreshStatuses()
                }) }
                item { Column(Modifier.padding(horizontal = SpectrumSpacing.screenPadding), verticalArrangement = Arrangement.spacedBy(22.dp)) {
                    Text(selected.overview ?: selected.plot ?: selected.description ?: "No overview available.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (selected.genres.isNotEmpty()) FlowChips(selected.genres.take(6))
                    MetadataGrid(selected)
                    WhereToWatchSection(selected)
                    CreditsBlock(selected)
                    MetadataSourceFooter(selected)
                    list?.let { HeroListCard(it) }
                } }
                if (related.isNotEmpty()) item { Column(Modifier.padding(horizontal = SpectrumSpacing.screenPadding)) { PosterRail("More in your spectrum", "Related by universe, franchise, and genre", related) { relatedItemId = it.id } } }
            }
        }
    }
    if (showTrailer) TrailerPlayerDialog(selected, onOpenDetails = { showTrailer = false }, onDismiss = { showTrailer = false })
}

@Composable
internal fun WhereToWatchSection(item: ViewingItem) {
    val context = LocalContext.current
    val providers = item.watchProviders
    SpectrumGlassSurface(modifier = Modifier.fillMaxWidth(), accent = spectrumUniverseAccent(item.universe)) {
        Column(Modifier.fillMaxWidth().padding(SpectrumSpacing.cardContentPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Where to watch", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Region ${ViewingMetadataStore.cinemaAvailabilityRegion.value}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (item.metadataSource == com.marvelspectrum.shared.data.viewing.MetadataSource.WATCHMODE || providers.isNotEmpty()) {
                    AssistChip(onClick = {}, label = { Text("Watchmode") })
                }
            }
            if (providers.isEmpty()) {
                Text("No streaming availability has been found yet. Add a Watchmode key in API Management and refresh metadata.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                providers.groupBy { provider -> provider.type?.lowercase().orEmpty().providerGroupLabel() }
                    .entries
                    .sortedBy { entry -> listOf("Stream", "Free", "Rent / Buy", "TV Everywhere", "Other").indexOf(entry.key).let { index -> if (index < 0) Int.MAX_VALUE else index } }
                    .forEach { (group, groupProviders) ->
                        Text(group, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            groupProviders.sortedWith(compareBy<WatchProvider> { it.displayPriority ?: Int.MAX_VALUE }.thenBy { it.providerName }).forEach { provider ->
                                val url = provider.androidUrl ?: provider.webUrl
                                SpectrumPillTab(
                                    selected = !url.isNullOrBlank(),
                                    onClick = { url?.let { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) } } }
                                ) {
                                    Text(listOfNotNull(provider.providerName, provider.format, provider.price).joinToString(" • "))
                                    if (!url.isNullOrBlank()) Icon(RhythmIcons.OpenInNew, contentDescription = "Open ${provider.providerName}")
                                }
                            }
                        }
                    }
                Text("Streaming data by Watchmode. Provider images and links may be third-party content.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
internal fun MetadataSourceFooter(item: ViewingItem) {
    SpectrumGlassSurface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), accent = spectrumUniverseAccent(item.universe)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("Local") })
                if (item.metadataSource != com.marvelspectrum.shared.data.viewing.MetadataSource.LOCAL) AssistChip(onClick = {}, label = { Text(item.metadataSource.name) })
                item.remoteArtworkAttribution?.let { attribution -> AssistChip(onClick = {}, label = { Text("Artwork: ${attribution.provider}") }) }
            }
            Text("Metadata source: ${item.metadataSource} • Updated ${item.lastUpdated ?: "offline catalog"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            item.remoteArtworkAttribution?.takeIf { it.requiresAttribution }?.let { attribution ->
                Text("${attribution.provider} artwork URLs are third-party metadata and are used only when remote artwork is enabled.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

internal fun String.providerGroupLabel(): String = when {
    this in setOf("sub", "subscription") -> "Stream"
    this == "free" -> "Free"
    this in setOf("rent", "buy", "purchase") -> "Rent / Buy"
    this in setOf("tve", "tv_everywhere") -> "TV Everywhere"
    else -> "Other"
}


@Composable
internal fun CinematicDetailHero(item: ViewingItem, statuses: Set<ViewingUserStatus>, onTrailer: () -> Unit, onStatusToggle: (ViewingUserStatus) -> Unit) {
    val accent = spectrumUniverseAccent(item.universe)
    val context = LocalContext.current
    val primaryTrailer = remember(item) { item.primaryTrailer() }
    val openTrailer = primaryTrailer?.externalUrl()?.let { url ->
        { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }; Unit }
    }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = SpectrumSpacing.screenPadding, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SpectrumSceneSurface(
            modifier = Modifier
                .fillMaxWidth()
                .height(SpectrumSpacing.heroCinematicHeight)
                .semantics {
                    contentDescription = if (primaryTrailer != null) {
                        "Muted cinematic trailer preview for ${item.title}"
                    } else {
                        "Cinematic poster and backdrop preview for ${item.title}"
                    }
                },
            universe = item.universe
        ) {
            if (primaryTrailer != null) {
                YouTubeTrailerWebPlayer(
                    youtubeVideoId = primaryTrailer.youtubeVideoId,
                    trailerUrl = primaryTrailer.url,
                    title = item.title,
                    autoplay = true,
                    muted = true,
                    showControls = false,
                    loop = false,
                    shape = SpectrumShapes.mediaFrame,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                PosterBackdrop(item, Modifier.fillMaxSize(), ContentScale.Crop, intent = ArtworkDisplayIntent.HERO_BACKDROP)
                PosterBackdrop(
                    item,
                    Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 18.dp, top = 28.dp, bottom = 92.dp)
                        .width(128.dp)
                        .aspectRatio(2f / 3f),
                    ContentScale.Crop,
                    SpectrumShapes.posterMask
                )
            }
            UniverseGlow(item.universe, Modifier.matchParentSize())
            Box(Modifier.matchParentSize().background(readabilityGradient(strong = true)))
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    SpectrumPulseIndicator(active = primaryTrailer != null, universe = item.universe)
                    SpectrumArtworkPill(if (primaryTrailer != null) "Muted trailer" else "Poster preview")
                    if (primaryTrailer == null) SpectrumArtworkPill("Trailer unavailable")
                }
                Text(item.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(
                    listOfNotNull(item.year, item.runtime, item.universe, item.phase ?: item.saga, item.imdbRating?.let { "IMDb $it" }).joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.84f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (openTrailer != null || item.availableTrailers().size > 1) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        openTrailer?.let { action ->
                            SpectrumPillTab(selected = false, onClick = action) {
                                Icon(RhythmIcons.OpenInNew, contentDescription = null)
                                Text("Open in YouTube")
                            }
                        }
                        if (item.availableTrailers().size > 1) {
                            SpectrumPillTab(selected = false, onClick = onTrailer) {
                                Icon(RhythmIcons.Play, contentDescription = null)
                                Text("More trailers")
                            }
                        }
                    }
                }
            }
        }
        SpectrumGlassSurface(Modifier.fillMaxWidth(), RoundedCornerShape(28.dp), accent) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ViewingUserStatus.entries.filter { it != ViewingUserStatus.HIDDEN }.forEach { status ->
                        DetailActionPill(
                            label = if (status in statuses) status.activeLabel else status.inactiveLabel,
                            icon = status.icon(),
                            selected = status in statuses
                        ) { onStatusToggle(status) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DetailActionPill(label: String, icon: MaterialSymbolIcon, selected: Boolean, onClick: () -> Unit) {
    SpectrumPillTab(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.semantics { contentDescription = "$label status${if (selected) ", selected" else ""}. Toggle $label." }
    ) {
        Icon(icon, null)
        Text(label)
    }
}



@Composable
internal fun MetadataGrid(item: ViewingItem) {
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
internal fun CreditsBlock(item: ViewingItem) {
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
internal fun CastPosterCard(member: ViewingCastMember) {
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
internal fun DetailLine(label: String, value: String) {
    if (value.isBlank()) return
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(82.dp))
        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
    }
}

@Composable
internal fun FlowChips(values: List<String>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.chipGap)) { items(values.distinct()) { AssistChip(onClick = {}, label = { Text(it) }) } }
}

@Composable
internal fun HeroListCard(list: ViewingList) {
    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CollectionArtwork(list, Modifier.size(86.dp, 122.dp).clip(RoundedCornerShape(22.dp)), ContentScale.Crop)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(list.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(list.description.orEmpty(), color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("${list.items.size} titles", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


