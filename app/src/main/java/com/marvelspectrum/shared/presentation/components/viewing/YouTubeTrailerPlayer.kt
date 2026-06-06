package com.marvelspectrum.shared.presentation.components.viewing

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

private val YouTubeIdPattern = Regex("^[A-Za-z0-9_-]{11}$")

fun parseYouTubeVideoId(value: String?): String? {
    if (value.isNullOrBlank()) return null
    val trimmed = value.trim()
    if (YouTubeIdPattern.matches(trimmed)) return trimmed
    return Regex("(?:youtube\\.com/(?:watch\\?v=|embed/|shorts/|v/)|youtu\\.be/)([A-Za-z0-9_-]{11})")
        .find(trimmed)
        ?.groupValues
        ?.getOrNull(1)
}

private enum class TrailerPlayerState { Poster, Loading, Playing, Error }

@Composable
fun YouTubeTrailerWebPlayer(
    youtubeVideoId: String?,
    modifier: Modifier = Modifier,
    trailerUrl: String? = null,
    title: String = "Trailer",
    shape: Shape = RoundedCornerShape(28.dp)
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle
    val safeVideoId = remember(youtubeVideoId, trailerUrl) { parseYouTubeVideoId(youtubeVideoId) ?: parseYouTubeVideoId(trailerUrl) }
    val openYouTube = remember(safeVideoId, trailerUrl) {
        {
            val uri = safeVideoId?.let { "https://www.youtube.com/watch?v=$it" } ?: trailerUrl.orEmpty()
            if (uri.isNotBlank()) context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
        }
    }

    var playerState by remember(safeVideoId) { mutableStateOf(if (safeVideoId == null) TrailerPlayerState.Error else TrailerPlayerState.Poster) }
    var youtubePlayer by remember(safeVideoId) { mutableStateOf<YouTubePlayer?>(null) }
    var shouldStartWhenReady by remember(safeVideoId) { mutableStateOf(false) }
    var playRequestKey by remember(safeVideoId) { mutableStateOf(0) }

    fun requestPlayback() {
        if (safeVideoId == null) {
            playerState = TrailerPlayerState.Error
            return
        }
        shouldStartWhenReady = true
        playerState = TrailerPlayerState.Loading
        playRequestKey += 1
    }

    LaunchedEffect(playRequestKey, safeVideoId, youtubePlayer) {
        val player = youtubePlayer ?: return@LaunchedEffect
        val videoId = safeVideoId ?: return@LaunchedEffect
        if (shouldStartWhenReady && playerState == TrailerPlayerState.Loading) {
            player.loadVideo(videoId, 0f)
        }
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        tonalElevation = 2.dp
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (safeVideoId != null && playerState != TrailerPlayerState.Poster && playerState != TrailerPlayerState.Error) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { viewContext ->
                        YouTubePlayerView(viewContext).apply {
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            enableAutomaticInitialization = true
                            lifecycle.addObserver(this)
                            addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                override fun onReady(youTubePlayer: YouTubePlayer) {
                                    youtubePlayer = youTubePlayer
                                }

                                override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                                    when (state) {
                                        PlayerConstants.PlayerState.PLAYING,
                                        PlayerConstants.PlayerState.PAUSED,
                                        PlayerConstants.PlayerState.VIDEO_CUED -> playerState = TrailerPlayerState.Playing
                                        PlayerConstants.PlayerState.BUFFERING -> if (playerState != TrailerPlayerState.Playing) playerState = TrailerPlayerState.Loading
                                        PlayerConstants.PlayerState.ENDED -> playerState = TrailerPlayerState.Poster
                                        else -> Unit
                                    }
                                }

                                override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                                    playerState = TrailerPlayerState.Error
                                    shouldStartWhenReady = false
                                }
                            })
                        }
                    },
                    onRelease = { view ->
                        runCatching { youtubePlayer?.pause() }
                        youtubePlayer = null
                        lifecycle.removeObserver(view)
                        view.release()
                    }
                )
            }

            when (playerState) {
                TrailerPlayerState.Poster -> TrailerPosterState(
                    title = title,
                    onPlay = ::requestPlayback,
                    modifier = Modifier.fillMaxSize()
                )
                TrailerPlayerState.Loading -> TrailerLoadingState(title = title, modifier = Modifier.fillMaxSize())
                TrailerPlayerState.Playing -> Unit
                TrailerPlayerState.Error -> TrailerFallback(
                    title = if (safeVideoId == null) "$title unavailable" else "Trailer unavailable",
                    body = if (safeVideoId == null) {
                        "Fetch trailers from TMDB or add a YouTube trailer ID later."
                    } else {
                        "This YouTube trailer may be unavailable, region-blocked, or not playable here."
                    },
                    openAction = if (safeVideoId != null || !trailerUrl.isNullOrBlank()) openYouTube else null,
                    refreshAction = if (safeVideoId != null) ::requestPlayback else null,
                    posterAction = if (safeVideoId != null) { { playerState = TrailerPlayerState.Poster; shouldStartWhenReady = false } } else null,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(0.dp)
                )
            }
        }
    }

    DisposableEffect(lifecycle) {
        onDispose { runCatching { youtubePlayer?.pause() } }
    }
}

@Composable
private fun TrailerPosterState(title: String, onPlay: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("$title ready", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "Tap to start the YouTube player.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(onClick = onPlay) { Text("Watch trailer") }
        }
    }
}

@Composable
private fun TrailerLoadingState(title: String, modifier: Modifier = Modifier) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator()
            Text("Loading $title…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TrailerFallback(
    title: String,
    body: String,
    openAction: (() -> Unit)?,
    refreshAction: (() -> Unit)?,
    posterAction: (() -> Unit)?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp)
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        tonalElevation = 2.dp
    ) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    if (refreshAction != null) Button(onClick = refreshAction, modifier = Modifier.weight(1f)) { Text("Try again") }
                    if (openAction != null) OutlinedButton(onClick = openAction, modifier = Modifier.weight(1f)) { Text("Open in YouTube") }
                }
                if (posterAction != null) TextButton(onClick = posterAction) { Text("Show poster") }
            }
        }
    }
}
