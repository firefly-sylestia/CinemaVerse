package com.cinemaverse.mcu.shared.util

import com.cinemaverse.mcu.shared.data.viewing.ViewingItem
import com.cinemaverse.mcu.shared.data.viewing.ViewingList

object ViewingArtworkUtils {
    const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p"
    const val LOCAL_POSTER_BASE_URL = "file:///android_asset/mcu_posters/"
    private const val PLACEHOLDER_SENTINEL = "[I WILL PROVIDE POSTER FOLDER PATH LATER]"

    fun tmdbPoster(path: String?, size: String = "w500"): String? = normalizeTmdbImage(path, size)

    fun tmdbBackdrop(path: String?, size: String = "w1280"): String? = normalizeTmdbImage(path, size)

    fun localPoster(fileName: String?): String? = fileName
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?.let { if (it.startsWith("file:///android_asset/")) it else LOCAL_POSTER_BASE_URL + it.substringAfterLast('/') }
        ?.takeIf(::isLocalAssetArtwork)

    fun resolvePoster(item: ViewingItem, preferLocalArtwork: Boolean = true): String? = firstUsable(
        item.localPoster?.takeIf { preferLocalArtwork && isLocalAssetArtwork(it) },
        item.omdbPoster?.takeIf(::isRealImageUrl),
        item.tmdbPoster?.let(::tmdbPoster),
        item.poster?.takeIf(::isRealImageUrl)?.let(::normalizeRemotePoster),
        item.localBackdrop?.takeIf { preferLocalArtwork && isLocalAssetArtwork(it) },
        item.tmdbBackdrop?.let(::tmdbBackdrop)
    )

    fun resolvePoster(list: ViewingList, preferLocalArtwork: Boolean = true): String? = firstUsable(
        list.localPoster?.takeIf { preferLocalArtwork && isLocalAssetArtwork(it) },
        list.items.firstOrNull { it.omdbPoster?.takeIf(::isRealImageUrl) != null }?.omdbPoster,
        list.poster?.takeIf { preferLocalArtwork || !isLocalAssetArtwork(it) }?.let(::normalizeRemotePoster),
        list.items.firstOrNull()?.let { resolvePoster(it, preferLocalArtwork) }
    )

    fun resolveBackdrop(item: ViewingItem, preferLocalArtwork: Boolean = true): String? = firstUsable(
        item.localBackdrop?.takeIf { preferLocalArtwork && isLocalAssetArtwork(it) },
        item.tmdbBackdrop?.let(::tmdbBackdrop),
        item.backdrop?.takeIf(::isRealImageUrl)?.let(::normalizeRemoteBackdrop),
        item.omdbPoster?.takeIf(::isRealImageUrl),
        item.tmdbPoster?.let(::tmdbPoster),
        item.poster?.takeIf(::isRealImageUrl)?.let(::normalizeRemotePoster)
    )

    fun resolveBackdrop(list: ViewingList, preferLocalArtwork: Boolean = true): String? = firstUsable(
        list.localBackdrop?.takeIf { preferLocalArtwork && isLocalAssetArtwork(it) },
        list.backdrop?.takeIf { preferLocalArtwork || !isLocalAssetArtwork(it) }?.let(::normalizeRemoteBackdrop),
        list.items.firstOrNull()?.let { resolveBackdrop(it, preferLocalArtwork) },
        list.items.firstOrNull()?.let { resolvePoster(it, preferLocalArtwork) }
    )

    fun isLocalAssetArtwork(value: String): Boolean = value.startsWith(LOCAL_POSTER_BASE_URL)

    fun isUsableArtwork(value: String?): Boolean = !value.isNullOrBlank() &&
        !value.contains(PLACEHOLDER_SENTINEL) &&
        value != "N/A" &&
        value != "null"

    fun isRealImageUrl(value: String?): Boolean = isUsableArtwork(value) &&
        (value!!.startsWith("http://") || value.startsWith("https://") || value.startsWith("file:///android_asset/")) &&
        !value.contains("/movie/") &&
        !value.contains("/tv/")

    private fun normalizeTmdbImage(path: String?, size: String): String? {
        val value = path?.trim()?.takeIf(::isUsableArtwork) ?: return null
        if (value.startsWith(TMDB_IMAGE_BASE_URL)) return value
        if (value.contains("themoviedb.org/t/p/")) {
            val tail = value.substringAfter("/t/p/").substringAfter('/')
            return "$TMDB_IMAGE_BASE_URL/$size/${tail.trimStart('/')}"
        }
        if (value.startsWith("http://") || value.startsWith("https://")) return value.takeIf { it.contains("image.tmdb.org/t/p/") }
        return "$TMDB_IMAGE_BASE_URL/$size/${value.trimStart('/')}"
    }

    private fun normalizeRemotePoster(value: String): String? = if (value.contains("themoviedb.org/t/p/")) tmdbPoster(value) else value
    private fun normalizeRemoteBackdrop(value: String): String? = if (value.contains("themoviedb.org/t/p/")) tmdbBackdrop(value) else value

    private fun firstUsable(vararg values: String?): String? = values.firstOrNull(::isUsableArtwork)
}
