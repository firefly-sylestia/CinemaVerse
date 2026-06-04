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
                    merged = mergeTmdbBackdropAndCinema(merged, tmdb)
                    source = MetadataSource.TMDB
                },
                onFailure = { messages += it.message ?: "TMDB lookup failed." }
            )
        } else {
            messages += "TMDB token missing; backdrops, trailers, and cinema metadata use local fallback fields while posters can still come from OMDb."
        }

        if (omdbService.hasApiKey) {
            val omdbResult = localItem.imdbId?.let { omdbService.getMovieByImdbId(it) }
                ?: omdbService.getMovieByTitle(localItem.title, localItem.year)
            omdbResult.fold(
                onSuccess = { omdb ->
                    merged = mergeOmdbPosterAndImdbDetails(merged, omdb)
                    source = if (source == MetadataSource.TMDB) MetadataSource.MERGED else MetadataSource.OMDB
                },
                onFailure = { messages += it.message ?: "OMDb lookup failed." }
            )
        } else {
            messages += "OMDb key not configured; poster artwork falls back to TMDB or local/catalog artwork."
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
        if (!tmdbService.hasCredentials) append("TMDB token missing. ")
        if (!omdbService.hasApiKey) append("OMDb key missing. ")
        if (isBlank()) append("OMDb powers primary posters and IMDb-style details. TMDB powers backdrops, trailers, and cinema metadata.")
        else append("Cinemaverse remains usable with local posters and the offline Marvel/DC catalog.")
    }

    private fun mergeOmdbPosterAndImdbDetails(local: ViewingItem, api: ViewingItem): ViewingItem = local.copy(
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

    private fun mergeTmdbBackdropAndCinema(local: ViewingItem, api: ViewingItem): ViewingItem = mergePreservingLocal(local, api).copy(
        poster = local.omdbPoster ?: local.poster ?: api.tmdbPoster ?: api.poster,
        tmdbPoster = api.tmdbPoster ?: local.tmdbPoster,
        backdrop = api.tmdbBackdrop ?: api.backdrop ?: local.backdrop,
        tmdbBackdrop = api.tmdbBackdrop ?: local.tmdbBackdrop,
        trailerUrl = api.trailerUrl ?: local.trailerUrl,
        youtubeVideoId = api.youtubeVideoId ?: local.youtubeVideoId,
        trailerSource = api.trailerSource ?: local.trailerSource
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
        poster = local.omdbPoster ?: local.poster ?: api.tmdbPoster ?: api.poster,
        tmdbPoster = api.tmdbPoster ?: local.tmdbPoster,
        omdbPoster = api.omdbPoster ?: local.omdbPoster,
        backdrop = api.backdrop ?: local.backdrop,
        tmdbBackdrop = api.tmdbBackdrop ?: local.tmdbBackdrop,
        trailerUrl = api.trailerUrl ?: local.trailerUrl,
        youtubeVideoId = api.youtubeVideoId ?: local.youtubeVideoId,
        trailerSource = api.trailerSource ?: local.trailerSource,
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
