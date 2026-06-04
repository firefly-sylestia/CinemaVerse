package com.cinemaverse.mcu.shared.util

import com.cinemaverse.mcu.shared.data.viewing.ViewingItem
import com.cinemaverse.mcu.shared.data.viewing.ViewingList

object ViewingArtworkUtils {
    const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p"

    fun tmdbPoster(path: String?, size: String = "w500"): String? = path
        ?.takeIf { it.isNotBlank() }
        ?.let { if (it.startsWith("http")) it else "$TMDB_IMAGE_BASE_URL/$size$it" }

    fun tmdbBackdrop(path: String?, size: String = "w1280"): String? = path
        ?.takeIf { it.isNotBlank() }
        ?.let { if (it.startsWith("http")) it else "$TMDB_IMAGE_BASE_URL/$size$it" }

    fun resolvePoster(item: ViewingItem): String? = firstUsable(
        item.localPoster?.takeIf { isLocalAssetArtwork(it) },
        item.localPoster,
        item.localBackdrop?.takeIf { isLocalAssetArtwork(it) },
        item.omdbPoster,
        item.poster,
        item.tmdbPoster
    )

    fun resolvePoster(list: ViewingList): String? = firstUsable(
        list.localPoster,
        list.poster,
        list.items.firstOrNull()?.localPoster,
        list.items.firstOrNull()?.omdbPoster,
        list.items.firstOrNull()?.poster,
        list.items.firstOrNull()?.tmdbPoster
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

    fun isLocalAssetArtwork(value: String): Boolean = value.startsWith("file:///android_asset/mcu_posters/")

    fun isUsableArtwork(value: String?): Boolean = !value.isNullOrBlank() &&
        !value.contains("[I WILL PROVIDE POSTER FOLDER PATH LATER]") &&
        value != "N/A"

    private fun firstUsable(vararg values: String?): String? = values.firstOrNull(::isUsableArtwork)
}
