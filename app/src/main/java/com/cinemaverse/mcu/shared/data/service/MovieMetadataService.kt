package com.cinemaverse.mcu.shared.data.service

import com.cinemaverse.mcu.shared.data.viewing.MetadataResult
import com.cinemaverse.mcu.shared.data.viewing.MetadataSource
import com.cinemaverse.mcu.shared.data.viewing.ViewingItem
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MovieMetadataService(
    private val omdbService: OmdbService = OmdbService(),
    private val tmdbService: TmdbService = TmdbService()
) {
    private val cache = mutableMapOf<String, MetadataResult>()
    private val mutex = Mutex()

    suspend fun getEnrichedViewingItem(localItem: ViewingItem): MetadataResult {
        val cacheKey = localItem.imdbId ?: localItem.tmdbId?.toString() ?: localItem.id
        mutex.withLock { cache[cacheKey]?.let { return it } }

        var merged = localItem
        var source = MetadataSource.LOCAL
        val messages = mutableListOf<String>()

        if (tmdbService.hasCredentials) {
            tmdbService.getTmdbViewingDetails(localItem).fold(
                onSuccess = { tmdb ->
                    merged = mergePreservingLocal(merged, tmdb)
                    source = MetadataSource.TMDB
                },
                onFailure = { messages += it.message ?: "TMDB lookup failed." }
            )
        } else {
            messages += "TMDB token not configured; using local fallback fields."
        }

        if (omdbService.hasApiKey) {
            val omdbResult = localItem.imdbId?.let { omdbService.getMovieByImdbId(it) }
                ?: omdbService.getMovieByTitle(localItem.title, localItem.year)
            omdbResult.fold(
                onSuccess = { omdb ->
                    merged = mergePreservingLocal(merged, omdb)
                    source = if (source == MetadataSource.TMDB) MetadataSource.MERGED else MetadataSource.OMDB
                },
                onFailure = { messages += it.message ?: "OMDb lookup failed." }
            )
        } else {
            messages += "OMDb key not configured; using local/TMDB fallback fields."
        }

        val result = MetadataResult(
            item = merged,
            source = source,
            isFallback = source == MetadataSource.LOCAL,
            message = messages.joinToString(" ").takeIf { it.isNotBlank() }
        )
        mutex.withLock { cache[cacheKey] = result }
        return result
    }

    fun getConfigurationMessage(): String = buildString {
        if (!omdbService.hasApiKey) append("OMDb key missing. ")
        if (!tmdbService.hasCredentials) append("TMDB token missing. ")
        if (isBlank()) append("OMDb and TMDB metadata enrichment configured; YouTube trailers use stored IDs or optional Data API discovery.")
        else append("Cinemaverse remains usable with the offline Marvel/DC viewing catalog.")
    }

    private fun mergePreservingLocal(local: ViewingItem, api: ViewingItem): ViewingItem = local.copy(
        originalTitle = local.originalTitle ?: api.originalTitle,
        year = local.year ?: api.year,
        releaseDate = local.releaseDate ?: api.releaseDate,
        imdbId = local.imdbId ?: api.imdbId,
        tmdbId = local.tmdbId ?: api.tmdbId,
        runtime = local.runtime ?: api.runtime,
        genres = local.genres.ifEmpty { api.genres },
        plot = api.plot ?: local.plot,
        overview = api.overview ?: local.overview,
        poster = api.poster ?: local.poster,
        tmdbPoster = api.tmdbPoster ?: local.tmdbPoster,
        omdbPoster = api.omdbPoster ?: local.omdbPoster,
        backdrop = api.backdrop ?: local.backdrop,
        tmdbBackdrop = api.tmdbBackdrop ?: local.tmdbBackdrop,
        trailerUrl = local.trailerUrl ?: api.trailerUrl,
        trailerSource = local.trailerSource ?: api.trailerSource,
        director = local.director ?: api.director,
        writer = local.writer ?: api.writer,
        actors = api.actors.ifEmpty { local.actors },
        cast = api.cast.ifEmpty { local.cast },
        crew = api.crew.ifEmpty { local.crew },
        imdbRating = local.imdbRating ?: api.imdbRating,
        tmdbRating = api.tmdbRating ?: local.tmdbRating,
        ratings = local.ratings.ifEmpty { api.ratings },
        awards = local.awards ?: api.awards,
        language = local.language ?: api.language,
        country = local.country ?: api.country
    )
}
