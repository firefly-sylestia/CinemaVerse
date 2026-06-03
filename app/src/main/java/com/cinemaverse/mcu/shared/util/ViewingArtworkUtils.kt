package com.cinemaverse.mcu.shared.util

import com.cinemaverse.mcu.shared.data.viewing.ViewingItem
import com.cinemaverse.mcu.shared.data.viewing.ViewingList

object ViewingArtworkUtils {
    const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p"

    fun tmdbPoster(path: String?, size: String = "w500"): String? = path
        ?.takeIf { it.isNotBlank() }
        ?.let { path -> if (path.startsWith("http")) path else "$TMDB_IMAGE_BASE_URL/$size/${path.trimStart('/')}" }

    fun tmdbBackdrop(path: String?, size: String = "w1280"): String? = path
        ?.takeIf { it.isNotBlank() }
        ?.let { path -> if (path.startsWith("http")) path else "$TMDB_IMAGE_BASE_URL/$size/${path.trimStart('/')}" }

    fun resolvePoster(item: ViewingItem): String? = firstUsable(
        item.localPoster?.takeIf { isLocalAssetArtwork(it) },
        item.localPoster,
        item.localBackdrop?.takeIf { isLocalAssetArtwork(it) },
        item.tmdbPoster,
        item.poster,
        item.omdbPoster
    )

    fun resolvePoster(list: ViewingList): String? = firstUsable(
        list.localPoster,
        list.poster,
        list.items.firstOrNull()?.localPoster,
        list.items.firstOrNull()?.tmdbPoster,
        list.items.firstOrNull()?.poster,
        list.items.firstOrNull()?.omdbPoster
    )

    fun resolveBackdrop(item: ViewingItem): String? = firstUsable(
        item.localBackdrop?.takeIf { isLocalAssetArtwork(it) },
        item.localPoster?.takeIf { isLocalAssetArtwork(it) },
        item.localBackdrop,
        item.tmdbBackdrop,
        item.backdrop
    )

    fun resolveBackdrop(list: ViewingList): String? = firstUsable(
        list.localBackdrop,
        list.backdrop,
        list.items.firstOrNull()?.localBackdrop,
        list.items.firstOrNull()?.tmdbBackdrop,
        list.items.firstOrNull()?.backdrop
    )

    fun isLocalAssetArtwork(value: String): Boolean = value.startsWith("file:///android_asset/mcu_posters/") || value.startsWith("mcu_posters/") || value.startsWith("android_asset/mcu_posters/")

    fun isUsableArtwork(value: String?): Boolean = !value.isNullOrBlank() &&
        !value.contains("[I WILL PROVIDE POSTER FOLDER PATH LATER]") &&
        !value.contains("provide poster", ignoreCase = true) &&
        value != "N/A" &&
        value != "null"

    private fun firstUsable(vararg values: String?): String? = values.firstOrNull(::isUsableArtwork)?.normalizeArtworkUri()

    private fun String.normalizeArtworkUri(): String = when {
        startsWith("mcu_posters/") -> "file:///android_asset/$this"
        startsWith("android_asset/mcu_posters/") -> "file:///$this"
        else -> this
    }
}
