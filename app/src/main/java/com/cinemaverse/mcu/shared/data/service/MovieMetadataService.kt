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

        if (omdbService.hasApiKey) {
            val omdbResult = localItem.imdbId?.let { omdbService.getMovieByImdbId(it) }
                ?: omdbService.getMovieByTitle(localItem.title, localItem.year)
            omdbResult.fold(
                onSuccess = { omdb ->
                    merged = mergePreferOmdbPoster(merged, omdb)
                    source = MetadataSource.OMDB
                },
                onFailure = { messages += it.message ?: "OMDb lookup failed." }
            )
        } else {
            messages += "OMDb key not configured; using local fallback fields."
        }

        if (tmdbService.hasCredentials) {
            tmdbService.getTmdbViewingDetails(localItem).fold(
                onSuccess = { tmdb ->
                    merged = mergeTmdbFallbacks(merged, tmdb)
                    source = if (source == MetadataSource.OMDB) MetadataSource.MERGED else MetadataSource.TMDB
                },
                onFailure = { messages += it.message ?: "TMDB lookup failed." }
            )
        } else {
            messages += "TMDB token not configured; using OMDb/local fallback fields."
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
        if (isBlank()) append("OMDb metadata is primary; TMDB only fills missing posters/backdrops/trailers.")
        else append("Cinemaverse remains usable with the offline Marvel/DC viewing catalog.")
    }

    private fun mergePreferOmdbPoster(local: ViewingItem, api: ViewingItem): ViewingItem = local.copy(
        originalTitle = api.originalTitle ?: local.originalTitle,
        year = api.year ?: local.year,
        releaseDate = api.releaseDate ?: local.releaseDate,
        imdbId = api.imdbId ?: local.imdbId,
        runtime = api.runtime ?: local.runtime,
        genres = api.genres.ifEmpty { local.genres },
        plot = api.plot ?: local.plot,
        overview = api.overview ?: local.overview,
        poster = api.omdbPoster ?: api.poster ?: local.poster,
        omdbPoster = api.omdbPoster ?: local.omdbPoster,
        tmdbPoster = local.tmdbPoster,
        backdrop = local.backdrop,
        tmdbBackdrop = local.tmdbBackdrop,
        trailerUrl = local.trailerUrl,
        trailerSource = local.trailerSource,
        director = api.director ?: local.director,
        writer = api.writer ?: local.writer,
        actors = api.actors.ifEmpty { local.actors },
        imdbRating = api.imdbRating ?: local.imdbRating,
        ratings = api.ratings.ifEmpty { local.ratings },
        awards = api.awards ?: local.awards,
        language = api.language ?: local.language,
        country = api.country ?: local.country
    )

    private fun mergeTmdbFallbacks(local: ViewingItem, api: ViewingItem): ViewingItem = mergePreservingLocal(local, api).copy(
        poster = local.omdbPoster ?: local.poster ?: api.poster,
        omdbPoster = local.omdbPoster,
        tmdbPoster = local.tmdbPoster ?: api.tmdbPoster,
        backdrop = local.backdrop ?: api.backdrop,
        tmdbBackdrop = local.tmdbBackdrop ?: api.tmdbBackdrop,
        trailerUrl = local.trailerUrl ?: api.trailerUrl,
        trailerSource = local.trailerSource ?: api.trailerSource
    )

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
