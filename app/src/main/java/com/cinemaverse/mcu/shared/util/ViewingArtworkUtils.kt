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
        item.localPoster,
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

    private fun firstUsable(vararg values: String?): String? = values.firstOrNull { value ->
        !value.isNullOrBlank() && !value.contains("[I WILL PROVIDE POSTER FOLDER PATH LATER]") && value != "N/A"
    }
}
