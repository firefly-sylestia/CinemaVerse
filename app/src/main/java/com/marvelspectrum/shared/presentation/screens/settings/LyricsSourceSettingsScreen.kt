@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.marvelspectrum.shared.presentation.screens.settings


import com.marvelspectrum.shared.presentation.components.icons.RhythmIcons
import com.marvelspectrum.shared.presentation.components.icons.MaterialSymbolIcon
import com.marvelspectrum.shared.presentation.components.icons.Icon

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
import com.marvelspectrum.R
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marvelspectrum.BuildConfig
import com.marvelspectrum.shared.data.model.AppSettings
import com.marvelspectrum.shared.data.model.Playlist
import com.marvelspectrum.shared.data.model.Song
import com.marvelspectrum.shared.data.repository.PlaybackStatsRepository
import com.marvelspectrum.shared.data.repository.StatsTimeRange
import com.marvelspectrum.util.GsonUtils
import com.marvelspectrum.util.HapticUtils
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlin.system.exitProcess
import com.marvelspectrum.shared.presentation.components.common.CollapsibleHeaderScreen
import com.marvelspectrum.shared.presentation.components.common.ButtonGroupStyle
import com.marvelspectrum.shared.presentation.components.common.ExpressiveScrollBar
import com.marvelspectrum.shared.presentation.components.common.ExpressiveButtonGroup
import com.marvelspectrum.shared.presentation.components.common.ExpressiveGroupButton
import com.marvelspectrum.shared.presentation.components.bottomsheets.StandardBottomSheetHeader
import com.marvelspectrum.shared.presentation.components.common.StyledProgressBar
import com.marvelspectrum.shared.presentation.components.common.ProgressStyle
import com.marvelspectrum.shared.presentation.components.common.ThumbStyle
import com.marvelspectrum.shared.presentation.components.bottomsheets.LicensesBottomSheet
import com.marvelspectrum.shared.presentation.components.bottomsheets.UpdateBottomSheet
import com.marvelspectrum.shared.presentation.components.bottomsheets.LyricsApiPriorityBottomSheet
import com.marvelspectrum.ui.utils.LazyListStateSaver
import com.marvelspectrum.features.local.presentation.viewmodel.MusicViewModel
import com.marvelspectrum.shared.presentation.components.common.ExpressiveShapeProvider
import com.marvelspectrum.shared.presentation.components.common.ExpressiveShapes
import com.marvelspectrum.shared.presentation.components.common.buildSplashBackdropShapes
import com.marvelspectrum.shared.presentation.components.common.SplashBackgroundOrbs
import com.marvelspectrum.shared.presentation.viewmodel.AppUpdaterViewModel
import com.marvelspectrum.shared.presentation.viewmodel.AppVersion
import com.marvelspectrum.ui.theme.getFontPreviewStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File
import com.marvelspectrum.utils.FontLoader
import com.marvelspectrum.ui.theme.parseCustomColorScheme
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.text.HtmlCompat
import com.marvelspectrum.shared.presentation.components.common.M3FourColorCircularLoader
import com.marvelspectrum.shared.presentation.components.player.PlayingEqIcon
import com.marvelspectrum.shared.presentation.components.dialogs.CreatePlaylistDialog
import com.marvelspectrum.shared.presentation.components.dialogs.BulkPlaylistExportDialog
import com.marvelspectrum.shared.presentation.components.dialogs.PlaylistImportDialog
import com.marvelspectrum.shared.presentation.components.common.rememberExpressiveShape
import com.marvelspectrum.shared.presentation.components.dialogs.PlaylistOperationProgressDialog
import com.marvelspectrum.shared.presentation.components.dialogs.PlaylistOperationResultDialog
import com.marvelspectrum.shared.presentation.components.dialogs.AppRestartDialog
import com.marvelspectrum.shared.presentation.components.player.PlayerChipOrderBottomSheet
import com.marvelspectrum.features.local.presentation.components.settings.HomeSectionOrderBottomSheet
import com.marvelspectrum.features.local.presentation.components.settings.LibraryTabOrderBottomSheet
import com.marvelspectrum.shared.presentation.components.Material3SettingsGroup
import com.marvelspectrum.shared.presentation.components.Material3SettingsItem

import com.marvelspectrum.shared.presentation.screens.settings.TunerSettingRow
import com.marvelspectrum.shared.presentation.screens.settings.TunerAnimatedSwitch
import com.marvelspectrum.shared.presentation.screens.settings.TunerSettingCard
import com.marvelspectrum.shared.presentation.screens.settings.SettingItem
import com.marvelspectrum.shared.presentation.screens.settings.SettingGroup


// Lyrics Source Settings Screen
@Composable
fun LyricsSourceSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val appSettings = AppSettings.getInstance(context)
    val hapticFeedback = LocalHapticFeedback.current

    val lyricsSourcePreference by appSettings.lyricsSourcePreference.collectAsState()
    var showPriorityBottomSheet by remember { mutableStateOf(false) }

    CollapsibleHeaderScreen(
        title = context.getString(R.string.settings_lyrics_source),
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
                Text(
                    text = context.getString(R.string.lyrics_source_priority_title),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
                Text(
                    text = context.getString(R.string.lyrics_source_priority_desc_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)
                )
            }

            val sourceOptions = listOf<Pair<com.marvelspectrum.shared.data.model.LyricsSourcePreference, Triple<String, String, MaterialSymbolIcon>>>(
                com.marvelspectrum.shared.data.model.LyricsSourcePreference.EMBEDDED_FIRST to Triple(
                    "Embedded First",
                    "Prefer lyrics embedded in audio files, fallback to online APIs",
                    RhythmIcons.MusicNote
                ),
                com.marvelspectrum.shared.data.model.LyricsSourcePreference.API_FIRST to Triple(
                    "Online First",
                    "Prefer online APIs (Rhythm word-by-word, LRCLib), fallback to embedded",
                    MaterialSymbolIcon("cloud_queue")
                ),
                com.marvelspectrum.shared.data.model.LyricsSourcePreference.LOCAL_FIRST to Triple(
                    "Local First",
                    "Prefer local .lrc files, then embedded lyrics, then online APIs",
                    RhythmIcons.Storage
                )
            )

            items(sourceOptions, key = { (pref, _) -> "source_${pref.name}" }) { (preference, info) ->
                val (title, description, icon) = info
                val isSelected = lyricsSourcePreference == preference

                Card(
                    onClick = {
                        HapticUtils.performHapticFeedback(
                            context,
                            hapticFeedback,
                            HapticFeedbackType.TextHandleMove
                        )
                        appSettings.setLyricsSourcePreference(preference)
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceContainer
                    ),
                    shape = RoundedCornerShape(18.dp),
                    border = if (isSelected) {
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    } else null,
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 2.dp else 0.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = RhythmIcons.CheckCircle,
                                contentDescription = stringResource(R.string.streaming_selected),
                                
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.lyricssourcesettingsscreen_online_api_options),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )

                val apiPriority by appSettings.lyricsApiPriority.collectAsState()
                val apiFallback by appSettings.lyricsApiFallbackRetry.collectAsState()

                Material3SettingsGroup(
                    items = listOf(
                        Material3SettingsItem(
                            icon = MaterialSymbolIcon("lyrics"),
                            title = { Text(stringResource(R.string.lyricssourcesettingsscreen_lyrics_api_priority)) },
                            description = {
                                Text(
                                    text = if (apiPriority == com.marvelspectrum.shared.data.model.LyricsApiPriority.APPLE_MUSIC_FIRST) "Apple Music First" else "LRCLib First",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = {
                                HapticUtils.performHapticFeedback(context, hapticFeedback, HapticFeedbackType.LongPress)
                                showPriorityBottomSheet = true
                            }
                        ),
                        toMaterial3SettingsItem(
                            context = context,
                            hapticFeedback = hapticFeedback,
                            item = SettingItem(
                                icon = MaterialSymbolIcon("compare_arrows"),
                                title = stringResource(R.string.lyricssourcesettingsscreen_retry_using_fallbacks),
                                description = "Attempt fallback APIs if the preferred API fails to return lyrics",
                                toggleState = apiFallback,
                                onToggleChange = { appSettings.setLyricsApiFallbackRetry(it) }
                            )
                        )
                    ),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            }

            // Info Card
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
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
                                text = stringResource(R.string.settings_about_lyrics_sources),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }

                        Text(
                            text = stringResource(R.string.lyricssourcesettingsscreen_embedded_lyrics_are_stored) +
                                    "• Online APIs provide high-quality synced lyrics\n" +
                                "• Rhythm offers word-by-word sync\n" +
                                    "• LRCLib provides free line-by-line sync",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }

    if (showPriorityBottomSheet) {
        LyricsApiPriorityBottomSheet(
            onDismiss = { showPriorityBottomSheet = false },
            appSettings = appSettings
        )
    }
}

