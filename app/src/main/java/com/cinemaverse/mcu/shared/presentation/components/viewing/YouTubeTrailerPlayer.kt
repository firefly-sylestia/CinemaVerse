package com.cinemaverse.mcu.shared.presentation.components.viewing

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

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
    title: String = "Trailer"
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
            modifier = modifier
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
            modifier = modifier
        )
        return
    }

    val html = remember(safeVideoId) { iframeHtml(safeVideoId) }
    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
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
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.evaluateJavascript("if (window.player) { player.pauseVideo(); }", null)
            webView.stopLoading()
            webView.loadUrl("about:blank")
            webView.removeAllViews()
            webView.destroy()
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        tonalElevation = 2.dp
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { webView },
            update = { it.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "utf-8", null) }
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
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        tonalElevation = 2.dp
    ) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

private fun iframeHtml(videoId: String): String = """
<!doctype html><html><head><meta name="viewport" content="width=device-width, initial-scale=1" />
<style>html,body,#player{margin:0;width:100%;height:100%;background:#000;overflow:hidden;}</style>
</head><body><div id="player"></div>
<script src="https://www.youtube.com/iframe_api"></script>
<script>
var player;
function onYouTubeIframeAPIReady(){
  player = new YT.Player('player', {
    width:'100%', height:'100%', videoId:'$videoId',
    playerVars:{playsinline:1,rel:0,modestbranding:1,autoplay:0,enablejsapi:1,origin:'https://www.youtube.com'},
    events:{'onReady':function(event){},'onError':function(event){document.body.setAttribute('data-error', event.data);}}
  });
}
</script></body></html>
""".trimIndent()
