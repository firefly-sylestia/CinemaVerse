package com.cinemaverse.mcu.shared.presentation.components.spectrum

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cinemaverse.mcu.shared.data.model.Album
import com.cinemaverse.mcu.shared.data.model.Artist
import com.cinemaverse.mcu.shared.data.model.Playlist
import com.cinemaverse.mcu.shared.data.model.Song
import com.cinemaverse.mcu.shared.presentation.components.icons.Icon
import com.cinemaverse.mcu.shared.presentation.components.icons.MaterialSymbolIcon
import com.cinemaverse.mcu.shared.presentation.components.icons.RhythmIcons
import com.cinemaverse.mcu.shared.presentation.components.player.formatDuration
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.SpectrumMusicUniverse
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.SpectrumRhythmTheme
import com.cinemaverse.mcu.shared.presentation.theme.spectrum.spectrumReadableOn

@Composable
fun SpectrumArtworkBackdrop(
    artworkUri: Uri?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val palette = SpectrumRhythmTheme.artwork
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                listOf(
                    palette.artworkPrimary.copy(alpha = 0.28f),
                    palette.artworkSecondary.copy(alpha = 0.16f),
                    MaterialTheme.colorScheme.background
                )
            )
        )
    ) {
        AsyncImage(
            model = artworkUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.14f,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(palette.artworkScrim.copy(alpha = 0.72f)))
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(palette.artworkGlow, radius = size.minDimension * 0.42f, center = Offset(size.width * .82f, size.height * .12f))
            drawCircle(palette.artworkAccent.copy(alpha = .10f), radius = size.minDimension * 0.36f, center = Offset(size.width * .05f, size.height * .72f), style = Stroke(3.dp.toPx()))
        }
        content()
    }
}

@Composable
fun SpectrumPanel(
    modifier: Modifier = Modifier,
    universe: SpectrumMusicUniverse = SpectrumMusicUniverse.Reactor,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable () -> Unit
) {
    val palette = SpectrumRhythmTheme.artwork
    Surface(
        modifier = modifier.border(1.dp, Brush.linearGradient(listOf(palette.artworkAccent.copy(.55f), Color.Transparent)), RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 1f)
    ) { Box(Modifier.background(Brush.linearGradient(listOf(palette.artworkContainer.copy(.35f), MaterialTheme.colorScheme.surfaceContainerHigh))).padding(contentPadding)) { content() } }
}

@Composable
fun SpectrumSectionHeader(title: String, subtitle: String? = null, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (action != null && onAction != null) Text(action, color = SpectrumRhythmTheme.artwork.artworkAccent, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onAction).padding(10.dp))
    }
}

@Composable
fun SpectrumArtworkTile(uri: Uri?, label: String, modifier: Modifier = Modifier, size: Dp = 58.dp, rounded: Dp = 18.dp) {
    Box(modifier.size(size).clip(RoundedCornerShape(rounded)).background(Brush.linearGradient(listOf(SpectrumRhythmTheme.artwork.artworkPrimary.copy(.55f), SpectrumRhythmTheme.artwork.artworkSecondary.copy(.45f)))), contentAlignment = Alignment.Center) {
        AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        if (uri == null) Text(label.take(1).uppercase(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = spectrumReadableOn(SpectrumRhythmTheme.artwork.artworkPrimary))
    }
}

@Composable
fun SpectrumSongRow(song: Song, isActive: Boolean, isPlaying: Boolean = false, onClick: () -> Unit, modifier: Modifier = Modifier, trailing: @Composable (() -> Unit)? = null) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale = if (pressed) .985f else 1f
    val container by animateColorAsState(if (isActive) SpectrumRhythmTheme.artwork.artworkContainer else MaterialTheme.colorScheme.surfaceContainer, label = "songRowColor")
    Row(modifier.scale(scale).clip(RoundedCornerShape(22.dp)).background(container).border(1.dp, if (isActive) SpectrumRhythmTheme.artwork.artworkAccent.copy(.65f) else MaterialTheme.colorScheme.outlineVariant.copy(.35f), RoundedCornerShape(22.dp)).clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick).padding(12.dp).then(modifier), verticalAlignment = Alignment.CenterVertically) {
        SpectrumArtworkTile(song.artworkUri, song.title, size = 56.dp, rounded = 16.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, style = MaterialTheme.typography.titleMedium, fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(listOf(song.artist, song.album).filter { it.isNotBlank() }.joinToString(" • "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (isActive) SpectrumEqualizerIndicator(isPlaying, Modifier.padding(horizontal = 8.dp))
        Text(formatDuration(song.duration), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (trailing != null) trailing()
    }
}

@Composable
fun SpectrumAlbumCard(album: Album, onClick: () -> Unit, modifier: Modifier = Modifier) { SpectrumMediaCard(album.artworkUri, album.title, album.artist, "${album.numberOfSongs.takeIf { it > 0 } ?: album.songs.size} songs", onClick, modifier) }
@Composable
fun SpectrumPlaylistCard(playlist: Playlist, onClick: () -> Unit, modifier: Modifier = Modifier) { SpectrumMediaCard(playlist.artworkUri ?: playlist.songs.firstOrNull()?.artworkUri, playlist.name, "Playlist", "${playlist.songs.size} songs", onClick, modifier) }
@Composable
fun SpectrumArtistCard(artist: Artist, onClick: () -> Unit, modifier: Modifier = Modifier) { SpectrumMediaCard(artist.artworkUri, artist.name, "Artist", "${artist.numberOfTracks.takeIf { it > 0 } ?: artist.songs.size} tracks", onClick, modifier) }

@Composable
private fun SpectrumMediaCard(uri: Uri?, title: String, subtitle: String, meta: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.width(170.dp), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(Modifier.padding(14.dp)) {
            SpectrumArtworkTile(uri, title, modifier = Modifier.fillMaxWidth().aspectRatio(1f), size = 142.dp, rounded = 24.dp)
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(meta, style = MaterialTheme.typography.labelSmall, color = SpectrumRhythmTheme.artwork.artworkAccent, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun SpectrumCommandBar(modifier: Modifier = Modifier, content: @Composable () -> Unit) { SpectrumPanel(modifier.fillMaxWidth().navigationBarsPadding(), contentPadding = PaddingValues(14.dp), content = content) }

@Composable
fun SpectrumIconButton(icon: MaterialSymbolIcon, contentDescription: String, selected: Boolean = false, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(52.dp).semantics { this.contentDescription = contentDescription; role = Role.Button }, colors = IconButtonDefaults.iconButtonColors(containerColor = if (selected) SpectrumRhythmTheme.artwork.artworkAccent else MaterialTheme.colorScheme.surfaceContainerHighest, contentColor = if (selected) spectrumReadableOn(SpectrumRhythmTheme.artwork.artworkAccent) else MaterialTheme.colorScheme.onSurface)) { Icon(icon, contentDescription = null) }
}

@Composable
fun SpectrumPrimaryButton(text: String, icon: MaterialSymbolIcon? = null, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier.height(54.dp), shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = SpectrumRhythmTheme.artwork.artworkAccent, contentColor = spectrumReadableOn(SpectrumRhythmTheme.artwork.artworkAccent))) { if (icon != null) { Icon(icon, null); Spacer(Modifier.width(8.dp)) }; Text(text, fontWeight = FontWeight.Bold) }
}

@Composable
fun SpectrumSeekBar(progress: Float, onSeek: (Float) -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.material3.Slider(value = progress.coerceIn(0f, 1f), onValueChange = onSeek, modifier = modifier.semantics { contentDescription = "Playback progress" })
}

@Composable
fun SpectrumEqualizerIndicator(active: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "eq")
    Row(modifier.size(width = 22.dp, height = 18.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
        repeat(3) { index ->
            val h by transition.animateFloat(if (active) .25f else .35f, if (active) (.75f + index * .08f) else .35f, infiniteRepeatable(tween(520 + index * 90), RepeatMode.Reverse), label = "eq$index")
            Box(Modifier.width(4.dp).height((18 * h).dp).clip(CircleShape).background(SpectrumRhythmTheme.artwork.artworkAccent))
        }
    }
}

@Composable
fun SpectrumProgressRing(progress: Float, modifier: Modifier = Modifier.size(156.dp)) {
    val palette = SpectrumRhythmTheme.artwork
    Canvas(modifier) {
        val stroke = Stroke(8.dp.toPx(), cap = StrokeCap.Round)
        drawArc(palette.artworkContainer, -90f, 360f, false, style = stroke, size = Size(size.width, size.height))
        drawArc(palette.artworkAccent, -90f, 360f * progress.coerceIn(0f, 1f), false, style = stroke, size = Size(size.width, size.height))
        drawArc(palette.artworkPrimary.copy(.45f), 24f, 72f, false, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round), size = Size(size.width * .82f, size.height * .82f), topLeft = Offset(size.width * .09f, size.height * .09f))
    }
}

@Composable
fun SpectrumEmptyState(title: String, message: String, action: String? = null, onAction: (() -> Unit)? = null) {
    SpectrumPanel(Modifier.fillMaxWidth()) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { SpectrumProgressRing(.72f, Modifier.size(88.dp)); Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); if (action != null && onAction != null) SpectrumPrimaryButton(action, RhythmIcons.Actions.Refresh, onAction) } }
}
