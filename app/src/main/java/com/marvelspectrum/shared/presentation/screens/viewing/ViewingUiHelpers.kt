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
import com.marvelspectrum.util.HapticUtils
import com.marvelspectrum.shared.data.viewing.ViewingTrailer
import com.marvelspectrum.shared.presentation.components.icons.Icon
import com.marvelspectrum.shared.presentation.components.icons.MaterialSymbolIcon
import com.marvelspectrum.shared.presentation.components.icons.RhythmIcons
import com.marvelspectrum.shared.presentation.components.viewing.YouTubeTrailerWebPlayer
import com.marvelspectrum.shared.presentation.viewmodel.ViewingViewModel
import com.marvelspectrum.shared.util.ViewingArtworkUtils
import kotlinx.coroutines.delay

internal object ViewingUi {
    val topPad = 30.dp
    val posterWidth = 146.dp
    val rowPosterWidth = 58.dp
    val rowPosterHeight = 86.dp
    val heroHeight = 288.dp
}

@Composable
internal fun ViewingCatalogLoadingState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(RhythmIcons.Back, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(
                SpectrumSpacing.screenPadding,
                ViewingUi.topPad,
                SpectrumSpacing.screenPadding,
                SpectrumSpacing.bottomSafePadding
            ),
            verticalArrangement = Arrangement.spacedBy(SpectrumSpacing.cardGap)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SpectrumSpacing.cardContentPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            MaterialSymbolIcon("theaters", filled = true),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(42.dp)
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                        )
                    }
                }
            }
            items(3) { index ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 58.dp, height = 86.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (index == 0) 0.72f else 0.58f)
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.92f)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.62f)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            )
                        }
                    }
                }
            }
        }
    }
}


internal fun ViewingUserStatus.icon() = when (this) {
    ViewingUserStatus.WATCHLIST -> RhythmIcons.Add
    ViewingUserStatus.WATCH_LATER -> RhythmIcons.AccessTime
    ViewingUserStatus.WATCHING -> MaterialSymbolIcon("play_circle", filled = true)
    ViewingUserStatus.WATCHED -> RhythmIcons.Check
    ViewingUserStatus.FAVORITE -> RhythmIcons.Favorite
    ViewingUserStatus.BOOKMARKED -> MaterialSymbolIcon("bookmark", filled = true)
    ViewingUserStatus.ON_HOLD -> RhythmIcons.Pause
    ViewingUserStatus.HIDDEN -> RhythmIcons.VisibilityOff
}


@Composable
internal fun SectionIdentityBlock(icon: MaterialSymbolIcon, title: String, status: String, subtitle: String) {
    val accent = spectrumUniverseAccent(if (title in setOf("MCU", "Essential")) "MCU" else if (title == "DC") "DC" else null)
    SpectrumGlassSurface(shape = RoundedCornerShape(32.dp), accent = accent) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = RoundedCornerShape(20.dp), color = accent.container, contentColor = accent.onContainer) { Icon(icon, null, Modifier.padding(14.dp).size(26.dp)) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium) }
            Surface(shape = RoundedCornerShape(50), color = accent.primary, contentColor = MaterialTheme.colorScheme.onPrimary) { Text(status, Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold) }
        }
    }
}

internal fun tabIdentityIcon(tab: String) = when (tab) { "Continue" -> RhythmIcons.Play; "MCU", "DC" -> MaterialSymbolIcon("movie", filled = true); "Timeline" -> RhythmIcons.AccessTime; "Collections" -> RhythmIcons.AppsGrid; "Saved" -> RhythmIcons.Favorite; else -> MaterialSymbolIcon("star", filled = true) }
internal fun tabIdentitySubtitle(tab: String) = when (tab) { "Continue" -> "Resume titles in progress or saved for later"; "Essential" -> "Key stories and starting points"; "MCU" -> "Films, shows, specials, and timelines"; "DC" -> "Films, series, Elseworlds, and connected stories"; "Timeline" -> "Stories arranged chronologically"; "Collections" -> "Timelines, phases, sagas, and character arcs"; else -> "Saved titles and viewing progress" }


@Composable
internal fun UniverseGlow(universe: String?, modifier: Modifier = Modifier) {
    val accent = spectrumUniverseAccent(universe)
    Box(
        modifier.background(
            Brush.radialGradient(
                listOf(
                    accent.primary.copy(alpha = 0.54f),
                    accent.secondary.copy(alpha = 0.32f),
                    Color.Transparent
                )
            )
        )
    )
}

@Composable
internal fun SpectrumArtworkPill(label: String) { Surface(shape = RoundedCornerShape(50), color = Color.Black.copy(alpha = .34f), contentColor = Color.White) { Text(label, Modifier.padding(horizontal = 12.dp, vertical = 7.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold) } }

@Composable
internal fun SpectrumPopupMenu(expanded: Boolean, onDismissRequest: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest, shape = RoundedCornerShape(30.dp), containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 8.dp, shadowElevation = 10.dp, content = content)
}

@Composable
internal fun SpectrumPopupMenuItem(label: String, selected: Boolean = false, leading: (@Composable () -> Unit)? = null, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium) },
        onClick = onClick,
        leadingIcon = leading,
        trailingIcon = if (selected) ({ Icon(RhythmIcons.Check, null) }) else null,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    )
}


@Composable
internal fun SectionHeader(title: String, subtitle: String) {
    Column { Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}

@Composable
internal fun EmptyState(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Image(painterResource(R.drawable.ic_cinemaverse), contentDescription = null, modifier = Modifier.size(54.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun PressableCard(modifier: Modifier = Modifier, onClick: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) SpectrumMotion.pressedScale else 1f, SpectrumMotion.pressSpec(), label = "viewingPress")
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    Card(onClick = { HapticUtils.performHapticFeedback(context, haptics, HapticFeedbackType.TextHandleMove); onClick() }, interactionSource = interaction, shape = SpectrumShapes.largeSoftCard, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale }) {
        Column(Modifier.padding(SpectrumSpacing.cardContentPadding), content = content)
    }
}

@Composable
internal fun rememberCachedItem(item: ViewingItem): ViewingItem = ViewingMetadataStore.itemFor(item)


internal fun List<ViewingList>.visibleManagedLists(): List<ViewingList> = filter { list ->
    list.importance == ViewingListImportance.PRIMARY ||
        list.category in setOf("Character Journeys", "Specials", "Defenders Saga", "Marvel One-Shots", "Disney+ Series")
}.distinctBy { it.title }.sortedWith(
    compareByDescending<ViewingList> { it.importance == ViewingListImportance.PRIMARY }
        .thenBy { it.category ?: "" }
        .thenBy { it.title }
)

internal fun List<ViewingItem>.sortedFor(mode: ViewingSortMode): List<ViewingItem> = when (mode) {
    ViewingSortMode.CHRONOLOGICAL -> sortedWith(compareBy<ViewingItem> { it.chronologicalOrder ?: Int.MAX_VALUE }.thenBy { it.releaseDate ?: "9999" })
    ViewingSortMode.PHASE -> sortedWith(compareBy<ViewingItem> { it.phase ?: "" }.thenBy { it.phaseOrder ?: it.releaseOrder ?: Int.MAX_VALUE })
    ViewingSortMode.TITLE -> sortedBy { it.title }
    ViewingSortMode.RATING -> sortedByDescending { it.imdbRating?.toDoubleOrNull() ?: it.tmdbRating ?: 0.0 }
    ViewingSortMode.RUNTIME -> sortedByDescending { it.runtime?.filter(Char::isDigit)?.toIntOrNull() ?: 0 }
    ViewingSortMode.GENRE -> sortedWith(compareBy<ViewingItem> { it.genres.firstOrNull() ?: "" }.thenBy { it.title })
    else -> sortedWith(compareBy<ViewingItem> { it.releaseDate ?: "9999-99-99" }.thenBy { it.releaseOrder ?: Int.MAX_VALUE })
}

internal fun ViewingSortMode.orderFor(item: ViewingItem): Int = when (this) {
    ViewingSortMode.CHRONOLOGICAL -> item.chronologicalOrder
    ViewingSortMode.PHASE -> item.phaseOrder
    else -> item.releaseOrder
} ?: 0

