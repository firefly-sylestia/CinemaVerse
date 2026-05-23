package chromahub.rhythm.app.features.local.presentation.components.bottomsheets

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chromahub.rhythm.app.R
import chromahub.rhythm.app.shared.presentation.components.Material3SettingsGroup
import chromahub.rhythm.app.shared.presentation.components.Material3SettingsItem
import chromahub.rhythm.app.shared.presentation.components.icons.Icon
import chromahub.rhythm.app.shared.presentation.components.icons.RhythmIcons
import chromahub.rhythm.app.util.HapticUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesBottomSheet(
    onDismiss: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val licenseItems = listOf(
        licenseItem(
            name = "AutoEQ",
            description = "Automatic headphone equalization from frequency responses",
            license = "MIT License",
            url = "https://github.com/jaakkopasanen/AutoEq",
            icon = RhythmIcons.Connectivity.OpenInNew,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "Jetpack Compose",
            description = "Android's modern toolkit for building native UI",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/compose",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "Material 3 Components",
            description = "Material Design 3 components for Android",
            license = "Apache License 2.0",
            url = "https://m3.material.io/",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "Media3 ExoPlayer",
            description = "Modern media playback library for Android",
            license = "Apache License 2.0",
            url = "https://github.com/androidx/media",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "Kotlin Coroutines",
            description = "Asynchronous programming framework for Kotlin",
            license = "Apache License 2.0",
            url = "https://github.com/Kotlin/kotlinx.coroutines",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "Coil",
            description = "Image loading library for Android backed by Kotlin Coroutines",
            license = "Apache License 2.0",
            url = "https://coil-kt.github.io/coil/",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "Retrofit",
            description = "Type-safe HTTP client for Android and Java",
            license = "Apache License 2.0",
            url = "https://square.github.io/retrofit/",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "OkHttp",
            description = "HTTP client for Android, Kotlin, and Java",
            license = "Apache License 2.0",
            url = "https://square.github.io/okhttp/",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "Gson",
            description = "Java serialization/deserialization library for JSON",
            license = "Apache License 2.0",
            url = "https://github.com/google/gson",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "AndroidX Navigation",
            description = "Navigation components for Android apps",
            license = "Apache License 2.0",
            url = "https://developer.android.com/guide/navigation",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "Accompanist Permissions",
            description = "Compose utilities for permissions handling",
            license = "Apache License 2.0",
            url = "https://google.github.io/accompanist/permissions/",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "AndroidX Palette",
            description = "Library to extract prominent colors from images",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/androidx/releases/palette",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "JAudioTagger",
            description = "Audio metadata editing library for Java",
            license = "LGPL v2.1",
            url = "https://github.com/Borewit/jaudiotagger",
            icon = RhythmIcons.Connectivity.OpenInNew,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "AndroidX Fragment",
            description = "Modular UI components for Android",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/androidx/releases/fragment",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "AndroidX MediaRouter",
            description = "Media routing support for Android",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/androidx/releases/mediarouter",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "Glance AppWidget",
            description = "Modern reactive widgets framework with Material 3",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/androidx/releases/glance",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "WorkManager",
            description = "Deferrable, asynchronous task management library",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/androidx/releases/work",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "Material Icons Extended",
            description = "Extended set of Material Design icons",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/compose/resources/material-icons",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "Ktor",
            description = "Asynchronous framework for creating microservices and web applications",
            license = "Apache License 2.0",
            url = "https://ktor.io/",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "LeakCanary",
            description = "Memory leak detection library for Android (debug builds only)",
            license = "Apache License 2.0",
            url = "https://square.github.io/leakcanary/",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "Desugar JDK Libs",
            description = "Allows using newer Java language APIs on older Android versions",
            license = "Apache License 2.0",
            url = "https://github.com/google/desugar_jdk_libs",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "Media3 FFmpeg Decoder",
            description = "FFmpeg-based decoder for Media3, enabling additional audio/video formats",
            license = "Apache License 2.0",
            url = "https://github.com/androidx/media",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "Room",
            description = "SQLite object mapping library that provides local data persistence",
            license = "Apache License 2.0",
            url = "https://developer.android.com/jetpack/androidx/releases/room",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        ),
        licenseItem(
            name = "Geom Font",
            description = "Modern, clean sans-serif typeface from Google Fonts",
            license = "SIL Open Font License 1.1",
            url = "https://fonts.google.com/specimen/Geom",
            icon = RhythmIcons.Actions.Info,
            context = context,
            haptic = haptic
        )
    )

    val licenseInfoItems = listOf(
        Material3SettingsItem(
            icon = RhythmIcons.Actions.Info,
            title = {
                Text(
                    text = context.getString(R.string.licenses_apache),
                    fontWeight = FontWeight.Bold
                )
            },
            description = {
                Text(context.getString(R.string.licenses_attribution))
            },
            enabled = false
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        dragHandle = { 
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.primary
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = context.getString(R.string.licenses_title),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = context.getString(R.string.licenses_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Material3SettingsGroup(
                title = "Open Source Libraries",
                items = licenseItems,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )

            Spacer(modifier = Modifier.height(16.dp))

            Material3SettingsGroup(
                title = "License Notes",
                items = licenseInfoItems,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun licenseItem(
    name: String,
    description: String,
    license: String,
    url: String,
    icon: Any,
    context: android.content.Context,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
): Material3SettingsItem {
    return Material3SettingsItem(
        icon = icon,
        title = {
            Text(
                text = name,
                fontWeight = FontWeight.SemiBold
            )
        },
        description = {
            Text("$description • $license")
        },
        trailingContent = {
            Icon(
                imageVector = RhythmIcons.Forward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        },
        onClick = {
            HapticUtils.performHapticFeedback(context, haptic, HapticFeedbackType.TextHandleMove)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    )
}
