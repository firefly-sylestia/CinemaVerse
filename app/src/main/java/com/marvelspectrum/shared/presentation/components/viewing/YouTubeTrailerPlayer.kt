package com.marvelspectrum.shared.presentation.components.viewing

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.ui.viewinterop.AndroidView

private val YouTubeIdPattern = Regex("^[A-Za-z0-9_-]{11}$")
enum class YouTubeTrailerPlayerState { Poster, Loading, Playing, Failed }

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
    shape: Shape = RoundedCornerShape(28.dp),
    autoplay: Boolean = true,
    muted: Boolean = false,
    showControls: Boolean = true,
    loop: Boolean = false,
    onShowPoster: (() -> Unit)? = null
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

    var playerState by remember(safeVideoId) { mutableStateOf(YouTubeTrailerPlayerState.Loading) }
    val openYouTube = remember(safeVideoId) { { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$safeVideoId"))) } }
    if (playerState == YouTubeTrailerPlayerState.Failed) {
        TrailerFallback(
            title = "Open trailer on YouTube",
            body = "This trailer cannot be played in-app.",
            openAction = openYouTube,
            refreshAction = null,
            posterAction = onShowPoster,
            modifier = modifier,
            shape = shape
        )
        return
    }

    val playerHtml = remember(safeVideoId, autoplay, muted, showControls, loop) {
        buildYouTubePlayerHtml(
            videoId = safeVideoId,
            autoplay = autoplay,
            muted = muted,
            showControls = showControls,
            loop = loop
        )
    }
    val playerDescription = if (playerState == YouTubeTrailerPlayerState.Loading) "Loading trailer for $title" else "Playing trailer for $title"
    LaunchedEffect(safeVideoId) {
        playerState = YouTubeTrailerPlayerState.Loading
        delay(900)
        if (playerState == YouTubeTrailerPlayerState.Loading) playerState = YouTubeTrailerPlayerState.Playing
    }

    Surface(
        modifier = modifier.semantics { contentDescription = playerDescription },
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        tonalElevation = 2.dp
    ) {
        Box(Modifier.fillMaxSize()) {
            AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                WebView(viewContext).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    alpha = 0.99f
                    setBackgroundColor(android.graphics.Color.BLACK)
                    webChromeClient = FullscreenTrailerChromeClient(viewContext)
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) { playerState = YouTubeTrailerPlayerState.Playing }
                        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                            if (request?.isForMainFrame != false) playerState = YouTubeTrailerPlayerState.Failed
                        }
                    }
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    settings.loadsImagesAutomatically = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                    loadDataWithBaseURL("https://www.youtube.com", playerHtml, "text/html", "utf-8", null)
                    tag = safeVideoId
                }
            },
            update = { view ->
                if (view.tag != safeVideoId) {
                    view.loadDataWithBaseURL("https://www.youtube.com", playerHtml, "text/html", "utf-8", null)
                    view.tag = safeVideoId
                }
            },
            onRelease = { view ->
                runCatching { view.evaluateJavascript("if (window.player) { player.pauseVideo(); player.stopVideo(); }", null) }
                view.stopLoading()
                view.loadUrl("about:blank")
                view.removeAllViews()
                view.destroy()
            }
            )
            if (playerState == YouTubeTrailerPlayerState.Loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        }
    }
}

private fun buildYouTubePlayerHtml(
    videoId: String,
    autoplay: Boolean,
    muted: Boolean,
    showControls: Boolean,
    loop: Boolean
): String {
    val autoplayFlag = if (autoplay) 1 else 0
    val controlsFlag = if (showControls) 1 else 0
    val loopFlag = if (loop) 1 else 0
    val muteCall = if (muted) "player.mute();" else ""
    val playCall = if (autoplay) "player.playVideo();" else ""
    val playlist = if (loop) ", playlist: '$videoId'" else ""
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
            <style>
                html, body { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; background: #000; }
                #player { position: absolute; inset: 0; width: 100%; height: 100%; min-width: 100%; min-height: 100%; }
                iframe { position: absolute !important; inset: 0 !important; width: 100% !important; height: 100% !important; border: 0; }
            </style>
        </head>
        <body>
            <div id="player"></div>
            <script src="https://www.youtube.com/iframe_api"></script>
            <script>
                var player;
                function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                        width: '100%',
                        height: '100%',
                        videoId: '$videoId',
                        playerVars: {
                            autoplay: $autoplayFlag,
                            controls: $controlsFlag,
                            playsinline: 0,
                            fs: 1,
                            rel: 0,
                            enablejsapi: 1,
                            origin: 'https://www.youtube.com',
                            modestbranding: 1,
                            loop: $loopFlag$playlist
                        },
                        events: {
                            onReady: function(event) {
                                $muteCall
                                $playCall
                            },
                            onError: function(event) {
                                document.body.setAttribute('data-player-error', event.data);
                            }
                        }
                    });
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}

private class FullscreenTrailerChromeClient(context: Context) : WebChromeClient() {
    private val activity = context.findActivity()
    private var customView: View? = null
    private var customViewCallback: CustomViewCallback? = null

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        val host = activity ?: return super.onShowCustomView(view, callback)
        if (customView != null) {
            callback?.onCustomViewHidden()
            return
        }
        val fullscreenView = view ?: return
        customView = fullscreenView
        customViewCallback = callback
        host.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        (host.window.decorView as? ViewGroup)?.addView(
            fullscreenView,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
    }

    override fun onHideCustomView() {
        val host = activity ?: return
        customView?.let { view ->
            (host.window.decorView as? ViewGroup)?.removeView(view)
        }
        host.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        customView = null
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
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
        modifier = modifier.semantics { contentDescription = "$title. $body" },
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        tonalElevation = 2.dp
    ) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (openAction != null) Button(onClick = openAction) { Text("Open YouTube") }
                    if (refreshAction != null) OutlinedButton(onClick = refreshAction) { Text("Try again") }
                    if (posterAction != null) TextButton(onClick = posterAction) { Text("Show poster") }
                }
            }
        }
    }
}
