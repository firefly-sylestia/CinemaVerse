package com.marvelspectrum.shared.util

import com.marvelspectrum.shared.data.viewing.ViewingItem
import com.marvelspectrum.shared.data.viewing.ViewingList

object ViewingArtworkUtils {
    const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p"
    const val LOCAL_POSTER_BASE_URL = "file:///android_asset/mcu_posters/"
    private const val PLACEHOLDER_SENTINEL = "[I WILL PROVIDE POSTER FOLDER PATH LATER]"

    fun tmdbPoster(path: String?, size: String = "w500"): String? = normalizeTmdbImage(path, size)

    fun tmdbBackdrop(path: String?, size: String = "w1280"): String? = normalizeTmdbImage(path, size)

    fun tmdbProfile(path: String?, size: String = "w185"): String? = normalizeTmdbImage(path, size)

    fun localPoster(fileName: String?): String? = fileName
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?.let { if (it.startsWith("file:///android_asset/")) it else LOCAL_POSTER_BASE_URL + it.substringAfterLast('/') }
        ?.takeIf { it.startsWith("file:///android_asset/") }

    /** Resolves wide artwork intended for hero and detail backgrounds. */
    fun resolveHeroBackdrop(item: ViewingItem, preferLocalArtwork: Boolean = true): String? = firstUsable(
        item.localBackdrop.takeIfLocalArtwork(preferLocalArtwork),
        item.tmdbBackdrop?.let(::tmdbBackdrop),
        item.backdrop.takeIfRemoteArtwork()?.let(::normalizeRemoteBackdrop),
        item.tmdbPoster?.let(::tmdbPoster),
        item.omdbPoster.takeIfRemoteArtwork()
    )

    /** Resolves portrait-first artwork intended for poster cards. */
    fun resolveCardPoster(item: ViewingItem, preferLocalArtwork: Boolean = true): String? = firstUsable(
        item.localPoster.takeIfLocalArtwork(preferLocalArtwork),
        item.tmdbPoster?.let(::tmdbPoster),
        item.omdbPoster.takeIfRemoteArtwork(),
        item.poster.takeIfRemoteArtwork()?.let(::normalizeRemotePoster),
        item.localBackdrop.takeIfLocalArtwork(preferLocalArtwork),
        item.tmdbBackdrop?.let(::tmdbBackdrop),
        item.backdrop.takeIfRemoteArtwork()?.let(::normalizeRemoteBackdrop)
    )

    /** Resolves collection-level background artwork before falling back to artwork from its titles. */
    fun resolveCollectionBackdrop(list: ViewingList, preferLocalArtwork: Boolean = true): String? = firstUsable(
        list.localBackdrop.takeIfLocalArtwork(preferLocalArtwork),
        list.backdrop.takeIfRemoteArtwork()?.let(::normalizeRemoteBackdrop),
        list.artworkItems.ifEmpty { list.items }.firstNotNullOfOrNull { resolveHeroBackdrop(it, preferLocalArtwork) },
        list.localPoster.takeIfLocalArtwork(preferLocalArtwork),
        list.poster.takeIfRemoteArtwork()?.let(::normalizeRemotePoster),
        list.artworkItems.ifEmpty { list.items }.firstNotNullOfOrNull { resolveCardPoster(it, preferLocalArtwork) }
    )

    fun resolvePoster(item: ViewingItem, preferLocalArtwork: Boolean = true): String? = resolveCardPoster(item, preferLocalArtwork)

    fun resolvePoster(list: ViewingList, preferLocalArtwork: Boolean = true): String? = firstUsable(
        list.localPoster.takeIfLocalArtwork(preferLocalArtwork),
        list.poster.takeIfRemoteArtwork()?.let(::normalizeRemotePoster),
        list.artworkItems.ifEmpty { list.items }.firstNotNullOfOrNull { resolveCardPoster(it, preferLocalArtwork) }
    )

    fun resolveBackdrop(item: ViewingItem, preferLocalArtwork: Boolean = true): String? = resolveHeroBackdrop(item, preferLocalArtwork)

    fun resolveBackdrop(list: ViewingList, preferLocalArtwork: Boolean = true): String? = resolveCollectionBackdrop(list, preferLocalArtwork)

    fun isLocalAssetArtwork(value: String): Boolean = value.startsWith("file:///android_asset/")

    fun isUsableArtwork(value: String?): Boolean {
        val normalized = value?.trim() ?: return false
        return normalized.isNotEmpty() &&
            !normalized.contains(PLACEHOLDER_SENTINEL) &&
            !normalized.equals("N/A", ignoreCase = true) &&
            !normalized.equals("null", ignoreCase = true)
    }

    fun isRealImageUrl(value: String?): Boolean = isUsableArtwork(value) &&
        (value!!.startsWith("http://") || value.startsWith("https://") || value.startsWith("file:///android_asset/")) &&
        !value.contains("/movie/") &&
        !value.contains("/tv/") &&
        !looksLikeGeneratedTmdbIdUrl(value)

    private fun normalizeTmdbImage(path: String?, size: String): String? {
        val value = path?.trim()?.takeIf(::isUsableArtwork) ?: return null
        if (looksLikeGeneratedTmdbIdUrl(value)) return null
        if (value.startsWith(TMDB_IMAGE_BASE_URL)) return value
        if (value.contains("themoviedb.org/t/p/")) {
            val tail = value.substringAfter("/t/p/").substringAfter('/')
            return "$TMDB_IMAGE_BASE_URL/$size/${tail.trimStart('/')}"
        }
        if (value.startsWith("http://") || value.startsWith("https://")) return value.takeIf { it.contains("image.tmdb.org/t/p/") }
        return "$TMDB_IMAGE_BASE_URL/$size/${value.trimStart('/')}"
    }

    private fun String?.takeIfLocalArtwork(preferLocalArtwork: Boolean): String? =
        this?.takeIf { preferLocalArtwork && isLocalAssetArtwork(it) && isUsableArtwork(it) }

    private fun String?.takeIfRemoteArtwork(): String? =
        this?.takeIf { !isLocalAssetArtwork(it) && isRealImageUrl(it) }

    private fun normalizeRemotePoster(value: String): String? = if (looksLikeGeneratedTmdbIdUrl(value)) null else if (value.contains("themoviedb.org/t/p/")) tmdbPoster(value) else value
    private fun normalizeRemoteBackdrop(value: String): String? = if (looksLikeGeneratedTmdbIdUrl(value)) null else if (value.contains("themoviedb.org/t/p/")) tmdbBackdrop(value) else value

    private fun looksLikeGeneratedTmdbIdUrl(value: String): Boolean =
        value.contains("image.tmdb.org/t/p/") && Regex("/w\\d+/\\d+\\.(jpg|png|webp)$", RegexOption.IGNORE_CASE).containsMatchIn(value)

    private fun firstUsable(vararg values: String?): String? = values.firstOrNull(::isUsableArtwork)
}
