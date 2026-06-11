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
internal fun TrailerPlayerDialog(item: ViewingItem, onOpenDetails: () -> Unit, onDismiss: () -> Unit) {
    val displayItem = rememberEnrichedItem(item)
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var expanded by rememberSaveable(displayItem.id) { mutableStateOf(false) }
    var playing by rememberSaveable(displayItem.id) { mutableStateOf(displayItem.hasAnyTrailer()) }
    var moreExpanded by remember { mutableStateOf(false) }
    val trailerOptions = remember(displayItem) { displayItem.availableTrailers() }
    var selectedTrailerIndex by rememberSaveable(displayItem.id, trailerOptions.size) { mutableStateOf(0) }
    val selectedTrailer = trailerOptions.getOrNull(selectedTrailerIndex)
    val userStatuses = ViewingMetadataStore.statusesFor(displayItem)
    val openYouTube = selectedTrailer?.externalUrl()?.let { url -> { runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } } }

    Dialog(onDismissRequest = { HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove); onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(if (expanded) 0.dp else 34.dp),
            color = if (expanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = if (expanded) 0.dp else 8.dp,
            modifier = (if (expanded) Modifier.fillMaxSize().padding(10.dp) else Modifier.fillMaxWidth().padding(horizontal = 18.dp))
        ) {
            Column(Modifier.padding(if (expanded) 12.dp else 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("${displayItem.title} trailer", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(listOfNotNull(displayItem.year, displayItem.runtime, displayItem.universe, displayItem.phase ?: displayItem.saga).joinToString(" • "), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    FilledIconButton(onClick = { HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove); expanded = !expanded }, modifier = Modifier.semantics { contentDescription = if (expanded) "Collapse trailer" else "Expand trailer" }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) { Icon(if (expanded) RhythmIcons.ExpandLess else RhythmIcons.ExpandMore, contentDescription = null) }
                    FilledIconButton(onClick = { HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove); onDismiss() }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest, contentColor = MaterialTheme.colorScheme.onSurface)) { Icon(RhythmIcons.Close, contentDescription = "Close trailer") }
                }
                Box(
                    Modifier.fillMaxWidth().then(if (expanded) Modifier.weight(1f) else Modifier.aspectRatio(16f / 9f)).clip(RoundedCornerShape(if (expanded) 22.dp else 26.dp)).background(Color.Black).semantics { contentDescription = if (playing) "Playing trailer for ${displayItem.title}" else "Trailer poster for ${displayItem.title}" },
                    contentAlignment = Alignment.Center
                ) {
                    if (playing && selectedTrailer != null) {
                        YouTubeTrailerWebPlayer(
                            youtubeVideoId = selectedTrailer.youtubeVideoId,
                            trailerUrl = selectedTrailer.url,
                            title = displayItem.title,
                            autoplay = true,
                            muted = false,
                            showControls = true,
                            shape = RoundedCornerShape(if (expanded) 22.dp else 26.dp),
                            onShowPoster = { playing = false },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        PosterBackdrop(displayItem, Modifier.fillMaxSize(), ContentScale.Crop, intent = ArtworkDisplayIntent.HERO_BACKDROP)
                        Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .08f), Color.Black.copy(alpha = .70f)))))
                        FilledIconButton(
                            enabled = selectedTrailer != null,
                            onClick = { HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove); playing = true },
                            modifier = Modifier.size(72.dp).semantics { contentDescription = "Play trailer for ${displayItem.title}" },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                        ) { Icon(RhythmIcons.Play, contentDescription = null, modifier = Modifier.size(34.dp)) }
                        if (selectedTrailer == null) Text("Trailer unavailable", Modifier.align(Alignment.BottomCenter).padding(18.dp), color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (trailerOptions.size > 1) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.chipGap)) {
                        itemsIndexed(trailerOptions, key = { index, trailer -> "${trailer.label}-$index" }) { index, trailer ->
                            FilterChip(
                                selected = index == selectedTrailerIndex,
                                onClick = { HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove); selectedTrailerIndex = index; playing = false },
                                label = { Text(trailer.label) },
                                leadingIcon = if (index == selectedTrailerIndex) ({ Icon(RhythmIcons.Check, contentDescription = null) }) else null
                            )
                        }
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove); onOpenDetails() }, shape = RoundedCornerShape(20.dp)) { Icon(RhythmIcons.Info, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Details") }
                    TrailerStatusButton(displayItem, ViewingUserStatus.WATCHING, userStatuses)
                    TrailerStatusButton(displayItem, ViewingUserStatus.WATCH_LATER, userStatuses)
                    TrailerStatusButton(displayItem, ViewingUserStatus.FAVORITE, userStatuses)
                    TrailerStatusButton(displayItem, ViewingUserStatus.WATCHED, userStatuses)
                    Box {
                        OutlinedButton(onClick = { HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove); moreExpanded = true }, shape = RoundedCornerShape(20.dp)) { Text("More") }
                        DropdownMenu(expanded = moreExpanded, onDismissRequest = { moreExpanded = false }) {
                            listOf(ViewingUserStatus.BOOKMARKED, ViewingUserStatus.WATCHLIST, ViewingUserStatus.ON_HOLD, ViewingUserStatus.HIDDEN).forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(if (status in userStatuses) status.activeLabel else status.inactiveLabel) },
                                    onClick = { moreExpanded = false; HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress); ViewingMetadataStore.toggleStatus(displayItem, status) },
                                    leadingIcon = { Icon(status.icon(), contentDescription = null) },
                                    trailingIcon = if (status in userStatuses) ({ Icon(RhythmIcons.Check, null) }) else null
                                )
                            }
                        }
                    }
                    OutlinedButton(enabled = openYouTube != null, onClick = { HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove); openYouTube?.invoke() }, shape = RoundedCornerShape(20.dp)) { Icon(RhythmIcons.OpenInNew, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Open YouTube") }
                }
            }
        }
    }
}

@Composable
internal fun TrailerStatusButton(
    item: ViewingItem,
    status: ViewingUserStatus,
    activeStatuses: Set<ViewingUserStatus>
) {
    val selected = status in activeStatuses
    val shape = RoundedCornerShape(22.dp)
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val onClick = {
        HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.LongPress)
        ViewingMetadataStore.toggleStatus(item, status)
    }

    if (selected) {
        FilledTonalButton(
            onClick = onClick,
            modifier = Modifier.semantics { contentDescription = "${status.activeLabel}, selected. Toggle ${status.libraryTitle}." },
            shape = shape,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(RhythmIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(status.activeLabel)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = Modifier.semantics { contentDescription = "${status.inactiveLabel}. Toggle ${status.libraryTitle}." }, shape = shape) {
            Text(status.inactiveLabel)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrailerRail(title: String, subtitle: String, items: List<ViewingItem>, onOpenTrailer: (ViewingItem) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SpectrumSectionHeader(title = title, subtitle = subtitle)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.cardGap)) {
            items(items, key = { "trailer-${it.id}" }) { item ->
                TrailerPreviewCard(item = item, onClick = { onOpenTrailer(item) })
            }
        }
    }
}

@Composable
internal fun TrailerPreviewCard(item: ViewingItem, onClick: () -> Unit) {
    val displayItem = rememberCachedItem(item)
    val trailer = remember(displayItem) { displayItem.primaryTrailer() }
    val accent = spectrumUniverseAccent(displayItem.universe)
    PressableCard(
        onClick = onClick,
        modifier = Modifier
            .width(292.dp)
            .semantics { contentDescription = "Open trailer preview for ${displayItem.title}" }
    ) {
        SpectrumSceneSurface(
            universe = displayItem.universe,
            modifier = Modifier.fillMaxWidth().height(164.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            PosterBackdrop(displayItem, Modifier.fillMaxSize(), ContentScale.Crop, intent = ArtworkDisplayIntent.HERO_BACKDROP)
            Box(Modifier.matchParentSize().background(readabilityGradient(strong = true)))
            Column(Modifier.align(Alignment.BottomStart).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(SpectrumSpacing.chipGap), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = SpectrumShapes.pillTab, color = Color.Black.copy(alpha = 0.48f), contentColor = Color.White) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(RhythmIcons.Play, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(trailer?.label ?: "Preview", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                Text(displayItem.title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(listOfNotNull(displayItem.year, displayItem.runtime, displayItem.universe).joinToString(" • "), color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(trailer?.label ?: "Official preview", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


internal fun ViewingItem.hasAnyTrailer(): Boolean = availableTrailers().isNotEmpty()

internal fun ViewingItem.primaryTrailer(): ViewingTrailer? = availableTrailers().minWithOrNull(
    compareBy<ViewingTrailer> { it.trailerRank() }.thenBy { it.label.lowercase() }
)

internal fun ViewingItem.availableTrailers(): List<ViewingTrailer> {
    val legacyTrailer = if (!youtubeVideoId.isNullOrBlank() || !trailerUrl.isNullOrBlank()) {
        ViewingTrailer("Official trailer", youtubeVideoId, trailerUrl, trailerSource)
    } else {
        null
    }
    return (trailers + listOfNotNull(legacyTrailer))
        .filter { !it.youtubeVideoId.isNullOrBlank() || !it.url.isNullOrBlank() }
        .distinctBy { it.youtubeVideoId?.takeIf(String::isNotBlank)?.let { id -> "youtube:$id" } ?: it.url?.takeIf(String::isNotBlank)?.let { url -> "url:$url" } ?: it.label }
}

private fun ViewingTrailer.trailerRank(): Int {
    val labelLower = label.lowercase()
    val hasYoutube = !youtubeVideoId.isNullOrBlank() || externalUrl()?.contains("youtu", ignoreCase = true) == true
    var score = 0
    if (!hasYoutube) score += 100
    score += when (source) {
        com.marvelspectrum.shared.data.viewing.TrailerSource.TMDB,
        com.marvelspectrum.shared.data.viewing.TrailerSource.YOUTUBE -> 0
        com.marvelspectrum.shared.data.viewing.TrailerSource.LOCAL,
        com.marvelspectrum.shared.data.viewing.TrailerSource.MANUAL -> 8
        null -> 16
        else -> 20
    }
    score += when {
        "official trailer" in labelLower -> 0
        "trailer" in labelLower -> 4
        "teaser" in labelLower -> 14
        "clip" in labelLower || "feature" in labelLower -> 26
        else -> 36
    }
    if (youtubeVideoId.isNullOrBlank()) score += 10
    return score
}

internal fun ViewingTrailer.externalUrl(): String? = url?.takeIf { it.isNotBlank() }
    ?: youtubeVideoId?.takeIf { it.isNotBlank() }?.let { "https://www.youtube.com/watch?v=$it" }


