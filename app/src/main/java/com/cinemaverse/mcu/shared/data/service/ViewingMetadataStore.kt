package com.cinemaverse.mcu.shared.data.service

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.cinemaverse.mcu.shared.data.viewing.McuAssetDataSource
import com.cinemaverse.mcu.shared.data.viewing.ViewingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ViewingMetadataStore {
    private val service = MovieMetadataService()
    private val enriched = mutableStateMapOf<String, ViewingItem>()
    val isFetching = mutableStateOf(false)
    val statusMessage = mutableStateOf(service.getConfigurationMessage())

    fun itemFor(item: ViewingItem): ViewingItem = enriched[item.id] ?: item

    suspend fun enrich(item: ViewingItem): ViewingItem {
        enriched[item.id]?.let { return it }
        return withContext(Dispatchers.IO) {
            service.getEnrichedViewingItem(item).item
        }.also { enriched[item.id] = mergeKeepingIdentity(item, it) }
    }

    suspend fun fetchAll(data: McuAssetDataSource.ViewingAssetData) {
        if (isFetching.value) return
        isFetching.value = true
        statusMessage.value = "Fetching OMDb-first posters, details, ratings, and TMDB fallback trailers…"
        var loaded = 0
        try {
            data.allItems.forEach { item ->
                runCatching { enrich(item) }
                loaded += 1
                statusMessage.value = "Fetched $loaded of ${data.allItems.size} Cinemaverse titles."
            }
            statusMessage.value = "Database loaded: $loaded titles refreshed from OMDb with TMDB fallback art/trailers. Cached for this app session."
        } catch (error: Throwable) {
            statusMessage.value = error.message ?: "Metadata fetch failed."
        } finally {
            isFetching.value = false
        }
    }

    private fun mergeKeepingIdentity(local: ViewingItem, remote: ViewingItem): ViewingItem = local.copy(
        originalTitle = remote.originalTitle ?: local.originalTitle,
        year = remote.year ?: local.year,
        releaseDate = remote.releaseDate ?: local.releaseDate,
        imdbId = remote.imdbId ?: local.imdbId,
        tmdbId = local.tmdbId ?: remote.tmdbId,
        runtime = remote.runtime ?: local.runtime,
        genres = remote.genres.ifEmpty { local.genres },
        plot = remote.plot ?: local.plot,
        overview = remote.overview ?: local.overview,
        poster = remote.omdbPoster ?: remote.poster ?: local.poster,
        tmdbPoster = local.tmdbPoster ?: remote.tmdbPoster,
        omdbPoster = remote.omdbPoster ?: local.omdbPoster,
        backdrop = remote.backdrop ?: local.backdrop,
        tmdbBackdrop = remote.tmdbBackdrop ?: local.tmdbBackdrop,
        trailerUrl = local.trailerUrl ?: remote.trailerUrl,
        trailerSource = local.trailerSource ?: remote.trailerSource,
        director = remote.director ?: local.director,
        writer = remote.writer ?: local.writer,
        actors = remote.actors.ifEmpty { local.actors },
        cast = remote.cast.ifEmpty { local.cast },
        crew = remote.crew.ifEmpty { local.crew },
        imdbRating = remote.imdbRating ?: local.imdbRating,
        tmdbRating = remote.tmdbRating ?: local.tmdbRating,
        ratings = remote.ratings.ifEmpty { local.ratings },
        awards = remote.awards ?: local.awards,
        language = remote.language ?: local.language,
        country = remote.country ?: local.country,
        metadataSource = remote.metadataSource,
        lastUpdated = "OMDb + TMDB fallback"
    )
}
