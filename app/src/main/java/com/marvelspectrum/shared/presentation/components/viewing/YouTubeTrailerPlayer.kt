package com.marvelspectrum.shared.presentation.components.viewing

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.marvelspectrum.shared.presentation.components.icons.Icon
import com.marvelspectrum.shared.presentation.components.icons.RhythmIcons

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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeTrailerWebPlayer(
    youtubeVideoId: String?,
    modifier: Modifier = Modifier,
    trailerUrl: String? = null,
    title: String = "Trailer",
    shape: Shape = RoundedCornerShape(28.dp)
) {
    val context = LocalContext.current
    val safeVideoId = remember(youtubeVideoId, trailerUrl) { parseYouTubeVideoId(youtubeVideoId) ?: parseYouTubeVideoId(trailerUrl) }
    if (safeVideoId == null) {
        TrailerFallback(
            title = "$title unavailable",
            body = if (trailerUrl.isNullOrBlank()) "Fetch trailers from TMDB or add a YouTube trailer ID later." else "Open this title on YouTube in your browser.",
            openAction = trailerUrl?.let { { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) } },
            refreshAction = null,
            posterAction = null,
            modifier = modifier,
            shape = shape
        )
        return
    }

    var playerFailed by remember(safeVideoId) { mutableStateOf(false) }
    val openYouTube = remember(safeVideoId) { { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$safeVideoId"))) } }
    if (playerFailed) {
        TrailerFallback(
            title = "Trailer unavailable",
            body = "This YouTube trailer may be unavailable, region-blocked, or not embeddable. You can open it in YouTube or return to the poster.",
            openAction = openYouTube,
            refreshAction = { playerFailed = false },
            posterAction = null,
            modifier = modifier,
            shape = shape
        )
        return
    }

    val embedUrl = remember(safeVideoId) {
        "https://www.youtube.com/embed/$safeVideoId?playsinline=1&rel=0&autoplay=1&enablejsapi=1&origin=https%3A%2F%2Fwww.youtube.com"
    }
    val requestHeaders = remember { mapOf("Referer" to "https://www.youtube.com/") }

    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        tonalElevation = 2.dp
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                WebView(viewContext).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    alpha = 0.99f
                    setBackgroundColor(android.graphics.Color.BLACK)
                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                            if (request?.isForMainFrame != false) playerFailed = true
                        }
                    }
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    settings.loadsImagesAutomatically = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                    loadUrl(embedUrl, requestHeaders)
                    tag = safeVideoId
                }
            },
            update = { view ->
                if (view.tag != safeVideoId) {
                    view.loadUrl(embedUrl, requestHeaders)
                    view.tag = safeVideoId
                }
            },
            onRelease = { view ->
                runCatching { view.evaluateJavascript("if (window.player) { player.pauseVideo(); }", null) }
                view.stopLoading()
                view.loadUrl("about:blank")
                view.removeAllViews()
                view.destroy()
            }
        )
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
                Icon(
                    RhythmIcons.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (openAction != null) Button(onClick = openAction) { Text("Open in YouTube") }
                    if (refreshAction != null) OutlinedButton(onClick = refreshAction) { Text("Refresh trailer") }
                    if (posterAction != null) TextButton(onClick = posterAction) { Text("Show poster") }
                }
            }
        }
    }
}
