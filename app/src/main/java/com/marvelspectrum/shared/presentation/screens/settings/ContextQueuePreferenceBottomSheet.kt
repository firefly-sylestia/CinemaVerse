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


@Composable
fun ContextQueuePreferenceBottomSheet(
    currentPreference: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val options = listOf(
        "ARTIST_FIRST" to context.getString(R.string.settings_context_pref_artist_first),
        "GENRE_FIRST" to context.getString(R.string.settings_context_pref_genre_first)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.primary) },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        StandardBottomSheetHeader(
            title = context.getString(R.string.settings_context_queue_preference),
            subtitle = context.getString(R.string.settings_context_queue_preference_desc),
            visible = true
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            options.forEach { (key, label) ->
                val isSelected = currentPreference == key

                Card(
                    onClick = {
                        HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
                        onSelect(key)
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f)
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = RhythmIcons.CheckCircle,
                                contentDescription = context.getString(R.string.ui_selected),
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}