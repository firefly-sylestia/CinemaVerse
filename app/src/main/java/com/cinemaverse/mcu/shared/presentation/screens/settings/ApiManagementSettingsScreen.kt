@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.cinemaverse.mcu.shared.presentation.screens.settings


import com.cinemaverse.mcu.shared.presentation.components.icons.RhythmIcons
import com.cinemaverse.mcu.shared.presentation.components.icons.MaterialSymbolIcon
import com.cinemaverse.mcu.shared.presentation.components.icons.Icon

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import com.cinemaverse.mcu.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.*
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Slider
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cinemaverse.mcu.BuildConfig
import com.cinemaverse.mcu.shared.data.model.AppSettings
import com.cinemaverse.mcu.shared.data.service.ViewingMetadataStore
import com.cinemaverse.mcu.shared.data.viewing.MetadataProviderMode
import com.cinemaverse.mcu.shared.data.viewing.McuAssetDataSource
import com.cinemaverse.mcu.shared.data.model.Playlist
import com.cinemaverse.mcu.shared.data.model.Song
import com.cinemaverse.mcu.shared.data.repository.PlaybackStatsRepository
import com.cinemaverse.mcu.shared.data.repository.StatsTimeRange
import com.cinemaverse.mcu.util.GsonUtils
import com.cinemaverse.mcu.util.HapticUtils
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import kotlin.system.exitProcess
import com.cinemaverse.mcu.shared.presentation.components.common.CollapsibleHeaderScreen
import com.cinemaverse.mcu.shared.presentation.components.common.ButtonGroupStyle
import com.cinemaverse.mcu.shared.presentation.components.common.ExpressiveScrollBar
import com.cinemaverse.mcu.shared.presentation.components.common.ExpressiveButtonGroup
import com.cinemaverse.mcu.shared.presentation.components.common.ExpressiveGroupButton
import com.cinemaverse.mcu.shared.presentation.components.bottomsheets.StandardBottomSheetHeader
import com.cinemaverse.mcu.shared.presentation.components.common.StyledProgressBar
import com.cinemaverse.mcu.shared.presentation.components.common.ProgressStyle
import com.cinemaverse.mcu.shared.presentation.components.common.ThumbStyle
import com.cinemaverse.mcu.shared.presentation.components.bottomsheets.LicensesBottomSheet
import com.cinemaverse.mcu.shared.presentation.components.bottomsheets.UpdateBottomSheet
import com.cinemaverse.mcu.ui.utils.LazyListStateSaver
import com.cinemaverse.mcu.features.local.presentation.viewmodel.MusicViewModel
import com.cinemaverse.mcu.shared.presentation.components.common.ExpressiveShapeProvider
import com.cinemaverse.mcu.shared.presentation.components.common.ExpressiveShapes
import com.cinemaverse.mcu.shared.presentation.components.common.buildSplashBackdropShapes
import com.cinemaverse.mcu.shared.presentation.components.common.SplashBackgroundOrbs
import com.cinemaverse.mcu.shared.presentation.viewmodel.AppUpdaterViewModel
import com.cinemaverse.mcu.shared.presentation.viewmodel.AppVersion
import com.cinemaverse.mcu.ui.theme.getFontPreviewStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File
import com.cinemaverse.mcu.utils.FontLoader
import com.cinemaverse.mcu.ui.theme.parseCustomColorScheme
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.text.HtmlCompat
import com.cinemaverse.mcu.shared.presentation.components.common.M3FourColorCircularLoader
import com.cinemaverse.mcu.shared.presentation.components.player.PlayingEqIcon
import com.cinemaverse.mcu.shared.presentation.components.dialogs.CreatePlaylistDialog
import com.cinemaverse.mcu.shared.presentation.components.dialogs.BulkPlaylistExportDialog
import com.cinemaverse.mcu.shared.presentation.components.dialogs.PlaylistImportDialog
import com.cinemaverse.mcu.shared.presentation.components.common.rememberExpressiveShape
import com.cinemaverse.mcu.shared.presentation.components.dialogs.PlaylistOperationProgressDialog
import com.cinemaverse.mcu.shared.presentation.components.dialogs.PlaylistOperationResultDialog
import com.cinemaverse.mcu.shared.presentation.components.dialogs.AppRestartDialog
import com.cinemaverse.mcu.shared.presentation.components.player.PlayerChipOrderBottomSheet
import com.cinemaverse.mcu.features.local.presentation.components.settings.HomeSectionOrderBottomSheet
import com.cinemaverse.mcu.features.local.presentation.components.settings.LibraryTabOrderBottomSheet
import com.cinemaverse.mcu.shared.presentation.components.Material3SettingsGroup
import com.cinemaverse.mcu.shared.presentation.components.Material3SettingsItem

import com.cinemaverse.mcu.shared.presentation.screens.settings.TunerSettingRow
import com.cinemaverse.mcu.shared.presentation.screens.settings.TunerAnimatedSwitch
import com.cinemaverse.mcu.shared.presentation.screens.settings.TunerSettingCard
import com.cinemaverse.mcu.shared.presentation.screens.settings.SettingItem
import com.cinemaverse.mcu.shared.presentation.screens.settings.SettingGroup


// API Management Screen
@Composable
fun ApiManagementSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val appSettings = AppSettings.getInstance(context)
    val coroutineScope = rememberCoroutineScope()
    val viewingData = remember(context) { McuAssetDataSource.load(context) }
    val viewingFetchMessage by ViewingMetadataStore.statusMessage
    val viewingFetchInProgress by ViewingMetadataStore.isFetching
    LaunchedEffect(context) { ViewingMetadataStore.initialize(context) }
    val useLocalPosters by ViewingMetadataStore.useLocalPosters
    val metadataProviderMode by ViewingMetadataStore.providerMode
    val watchmodeEnabled by ViewingMetadataStore.watchmodeApiEnabled
    val watchmodeKey by ViewingMetadataStore.watchmodeApiKey
    val tmdbEnabled by ViewingMetadataStore.tmdbApiEnabled
    val tmdbToken by ViewingMetadataStore.tmdbReadAccessToken
    val omdbEnabled by ViewingMetadataStore.omdbApiEnabled
    val omdbKey by ViewingMetadataStore.omdbApiKey
    val availabilityRegion by ViewingMetadataStore.cinemaAvailabilityRegion

    // API states
    val deezerApiEnabled by appSettings.deezerApiEnabled.collectAsState()
    val lrclibApiEnabled by appSettings.lrclibApiEnabled.collectAsState()
    val ytMusicApiEnabled by appSettings.ytMusicApiEnabled.collectAsState()
    val appleMusicApiEnabled by appSettings.appleMusicApiEnabled.collectAsState()

    CollapsibleHeaderScreen(
        title = context.getString(R.string.settings_api_management),
        showBackButton = true,
        onBackClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onBackClick()
        }
    ) { modifier ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Cinema metadata",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Choose OMDb-first or TMDB-first metadata fallback. OMDb returns the Poster field in title/IMDb lookups; TMDB image paths are expanded to image.tmdb.org/t/p URLs.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                        )
                        Text(
                            text = viewingFetchMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                        )
                        Button(
                            onClick = { coroutineScope.launch { ViewingMetadataStore.fetchAll(viewingData) } },
                            enabled = !viewingFetchInProgress,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (viewingFetchInProgress) "Refreshing cinema metadata…" else "Fetch posters, backdrops, trailers and details")
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Metadata source order",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            MetadataProviderMode.entries.forEach { mode ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { ViewingMetadataStore.setProviderMode(mode) }.padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = metadataProviderMode == mode,
                                        onClick = { ViewingMetadataStore.setProviderMode(mode) }
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(mode.label, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        Text(mode.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f))
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "Prefer bundled local posters",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Turn off to show API posters first wherever TMDB/OMDb artwork is available.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                                )
                            }
                            Switch(checked = useLocalPosters, onCheckedChange = ViewingMetadataStore::setUseLocalPosters)
                        }

                    }
                }
            }

            item {
                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Manual cinema API keys", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "Keys stay in this device's private settings and are sent only to the provider you enable. Leave fields blank to keep the offline catalog and current provider behavior.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        CinemaApiKeyRow(
                            provider = "Watchmode",
                            description = "Streaming availability, trailers, ratings and source links.",
                            enabled = watchmodeEnabled,
                            key = watchmodeKey,
                            onEnabled = ViewingMetadataStore::setWatchmodeApiEnabled,
                            onSave = ViewingMetadataStore::setWatchmodeApiKey,
                            getKeyUrl = "https://api.watchmode.com/requestApiKey",
                            docsUrl = "https://api.watchmode.com/docs"
                        )
                        CinemaApiKeyRow(
                            provider = "TMDB",
                            description = "Posters, backdrops, trailers and rich title metadata.",
                            enabled = tmdbEnabled,
                            key = tmdbToken,
                            onEnabled = ViewingMetadataStore::setTmdbApiEnabled,
                            onSave = ViewingMetadataStore::setTmdbReadAccessToken,
                            getKeyUrl = "https://developer.themoviedb.org/docs/authentication-application",
                            docsUrl = "https://developer.themoviedb.org/docs"
                        )
                        CinemaApiKeyRow(
                            provider = "OMDb",
                            description = "IMDb-style ratings, awards, plots and poster metadata.",
                            enabled = omdbEnabled,
                            key = omdbKey,
                            onEnabled = ViewingMetadataStore::setOmdbApiEnabled,
                            onSave = ViewingMetadataStore::setOmdbApiKey,
                            getKeyUrl = "https://www.omdbapi.com/apikey.aspx",
                            docsUrl = "https://www.omdbapi.com/"
                        )
                        CompactRegionSelector(availabilityRegion, ViewingMetadataStore::setCinemaAvailabilityRegion)
                    }
                }
            }

            // API Services
            item {
                Text(
                    text = "Music integrations",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                val apiServiceItems = buildList {
                    if (com.cinemaverse.mcu.BuildConfig.ENABLE_DEEZER) {
                        add(
                            toMaterial3SettingsItem(
                                context = context,
                                hapticFeedback = hapticFeedback,
                                item = SettingItem(
                                    icon = RhythmIcons.Public,
                                    title = stringResource(R.string.onboarding_integration_deezer),
                                    description = "Free artist images and album artwork - no setup needed",
                                    toggleState = deezerApiEnabled,
                                    onToggleChange = { enabled -> appSettings.setDeezerApiEnabled(enabled) }
                                )
                            )
                        )
                    }

                    if (com.cinemaverse.mcu.BuildConfig.ENABLE_LRCLIB) {
                        add(
                            toMaterial3SettingsItem(
                                context = context,
                                hapticFeedback = hapticFeedback,
                                item = SettingItem(
                                    icon = RhythmIcons.Queue,
                                    title = stringResource(R.string.onboarding_integration_lrclib),
                                    description = "Free line-by-line synced lyrics (Fallback)",
                                    toggleState = lrclibApiEnabled,
                                    onToggleChange = { enabled -> appSettings.setLrcLibApiEnabled(enabled) }
                                )
                            )
                        )
                    }

                    if (com.cinemaverse.mcu.BuildConfig.ENABLE_APPLE_MUSIC) {
                        add(
                            toMaterial3SettingsItem(
                                context = context,
                                hapticFeedback = hapticFeedback,
                                item = SettingItem(
                                    icon = MaterialSymbolIcon("music_note"),
                                    title = stringResource(R.string.apimanagementsettingsscreen_apple_music),
                                    description = "Word-by-word synchronized lyrics (Highest Quality)",
                                    toggleState = appleMusicApiEnabled,
                                    onToggleChange = { enabled -> appSettings.setAppleMusicApiEnabled(enabled) }
                                )
                            )
                        )
                    }

                    if (com.cinemaverse.mcu.BuildConfig.ENABLE_YOUTUBE_MUSIC) {
                        add(
                            toMaterial3SettingsItem(
                                context = context,
                                hapticFeedback = hapticFeedback,
                                item = SettingItem(
                                    icon = RhythmIcons.Album,
                                    title = stringResource(R.string.onboarding_integration_ytmusic),
                                    description = "Fallback for artist images and album artwork",
                                    toggleState = ytMusicApiEnabled,
                                    onToggleChange = { enabled -> appSettings.setYTMusicApiEnabled(enabled) }
                                )
                            )
                        )
                    }


                    add(
                        Material3SettingsItem(
                            icon = RhythmIcons.Download,
                            title = { Text(stringResource(R.string.apimanagementsettingsscreen_github)) },
                            description = { Text(stringResource(R.string.apimanagementsettingsscreen_app_updates_and_release)) }
                        )
                    )
                }

                Material3SettingsGroup(
                    items = apiServiceItems,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                imageVector = RhythmIcons.Info,
                                contentDescription = null,
                                
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = context.getString(R.string.api_services),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }

                        Text(
                            text = context.getString(R.string.external_services_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CinemaApiKeyRow(
    provider: String,
    description: String,
    enabled: Boolean,
    key: String,
    onEnabled: (Boolean) -> Unit,
    onSave: (String) -> Unit,
    getKeyUrl: String,
    docsUrl: String
) {
    val context = LocalContext.current
    var draft by rememberSaveable(provider, key) { mutableStateOf(key) }
    var visible by rememberSaveable(provider) { mutableStateOf(false) }
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 1.dp) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(provider, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = enabled, onCheckedChange = onEnabled)
            }
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("$provider API key") },
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { visible = !visible }) {
                        Icon(if (visible) RhythmIcons.VisibilityOff else RhythmIcons.Visibility, contentDescription = if (visible) "Hide key" else "Show key")
                    }
                }
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onSave(draft) }) { Text("Save") }
                OutlinedButton(onClick = { draft = ""; onSave("") }) { Text("Clear") }
                OutlinedButton(onClick = { Toast.makeText(context, if (draft.isBlank()) "$provider key is empty" else "$provider key saved locally; use Fetch to validate with metadata calls", Toast.LENGTH_SHORT).show() }) { Text("Test connection") }
                TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getKeyUrl))) }) { Text("Get $provider key") }
                TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(docsUrl))) }) { Text("Docs") }
            }
        }
    }
}

@Composable
private fun CompactRegionSelector(region: String, onRegion: (String) -> Unit) {
    val regions = listOf("US", "CA", "GB", "AU", "IN", "DE", "FR", "JP")
    Text("Streaming availability region", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(regions) { option ->
            FilterChip(selected = region == option, onClick = { onRegion(option) }, label = { Text(option) })
        }
    }
}
