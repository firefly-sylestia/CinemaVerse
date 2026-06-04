package com.cinemaverse.mcu.shared.data.service

import com.cinemaverse.mcu.shared.data.viewing.MetadataProviderMode
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

    suspend fun getEnrichedViewingItem(
        localItem: ViewingItem,
        providerMode: MetadataProviderMode = MetadataProviderMode.OMDB_PRIMARY_TMDB_FALLBACK
    ): MetadataResult {
        val cacheKey = listOf(providerMode.name, localItem.imdbId ?: localItem.tmdbId?.toString() ?: localItem.id).joinToString(":")
        mutex.withLock { cache[cacheKey]?.let { return it } }

        var merged = localItem
        var source = MetadataSource.LOCAL
        val messages = mutableListOf<String>()

        suspend fun applyOmdb(primary: Boolean) {
            if (!omdbService.hasApiKey) {
                messages += "OMDb key not configured; OMDb poster/details step skipped."
                return
            }
            val omdbResult = localItem.imdbId?.let { omdbService.getMovieByImdbId(it) }
                ?: omdbService.getMovieByTitle(localItem.title, localItem.year)
            omdbResult.fold(
                onSuccess = { omdb ->
                    merged = if (primary) mergeOmdbPrimary(merged, omdb) else mergeOmdbFallback(merged, omdb)
                    source = source.combine(MetadataSource.OMDB)
                },
                onFailure = { messages += it.message ?: "OMDb lookup failed." }
            )
        }

        suspend fun applyTmdb(primary: Boolean) {
            if (!tmdbService.hasCredentials) {
                messages += "TMDB token missing; TMDB poster/backdrop/trailer step skipped."
                return
            }
            tmdbService.getTmdbViewingDetails(localItem).fold(
                onSuccess = { tmdb ->
                    merged = if (primary) mergeTmdbPrimary(merged, tmdb) else mergeTmdbFallback(merged, tmdb)
                    source = source.combine(MetadataSource.TMDB)
                },
                onFailure = { messages += it.message ?: "TMDB lookup failed." }
            )
        }

        when (providerMode) {
            MetadataProviderMode.OMDB_PRIMARY_TMDB_FALLBACK -> {
                applyOmdb(primary = true)
                applyTmdb(primary = false)
            }
            MetadataProviderMode.TMDB_PRIMARY_OMDB_FALLBACK -> {
                applyTmdb(primary = true)
                applyOmdb(primary = false)
            }
        }

        val result = MetadataResult(
            item = merged.copy(metadataSource = source),
            source = source,
            isFallback = source == MetadataSource.LOCAL,
            message = messages.joinToString(" ").takeIf { it.isNotBlank() }
        )
        mutex.withLock { cache[cacheKey] = result }
        return result
    }

    fun clearCache() = cache.clear()

    fun getConfigurationMessage(providerMode: MetadataProviderMode = MetadataProviderMode.OMDB_PRIMARY_TMDB_FALLBACK): String = buildString {
        append(providerMode.label).append(". ")
        if (!omdbService.hasApiKey) append("OMDb key missing. ")
        if (!tmdbService.hasCredentials) append("TMDB token missing. ")
        if (omdbService.hasApiKey || tmdbService.hasCredentials) append("Manual fetch refreshes posters, backdrops, trailers, and details; bundled local posters remain available.")
        else append("Cinemaverse remains usable with bundled local posters and the offline Marvel/DC catalog.")
    }

    private fun mergeOmdbPrimary(local: ViewingItem, api: ViewingItem): ViewingItem = local.copy(
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
        director = api.director ?: local.director,
        writer = api.writer ?: local.writer,
        actors = api.actors.ifEmpty { local.actors },
        imdbRating = api.imdbRating ?: local.imdbRating,
        ratings = api.ratings.ifEmpty { local.ratings },
        awards = api.awards ?: local.awards,
        language = api.language ?: local.language,
        country = api.country ?: local.country,
        metadataSource = MetadataSource.OMDB
    )

    private fun mergeOmdbFallback(local: ViewingItem, api: ViewingItem): ViewingItem = local.copy(
        originalTitle = local.originalTitle ?: api.originalTitle,
        year = local.year ?: api.year,
        releaseDate = local.releaseDate ?: api.releaseDate,
        imdbId = local.imdbId ?: api.imdbId,
        runtime = local.runtime ?: api.runtime,
        genres = local.genres.ifEmpty { api.genres },
        plot = local.plot ?: api.plot,
        overview = local.overview ?: api.overview,
        poster = local.poster ?: api.omdbPoster ?: api.poster,
        omdbPoster = local.omdbPoster ?: api.omdbPoster,
        director = local.director ?: api.director,
        writer = local.writer ?: api.writer,
        actors = local.actors.ifEmpty { api.actors },
        imdbRating = local.imdbRating ?: api.imdbRating,
        ratings = local.ratings.ifEmpty { api.ratings },
        awards = local.awards ?: api.awards,
        language = local.language ?: api.language,
        country = local.country ?: api.country
    )

    private fun mergeTmdbPrimary(local: ViewingItem, api: ViewingItem): ViewingItem = local.copy(
        originalTitle = api.originalTitle ?: local.originalTitle,
        year = api.year ?: local.year,
        releaseDate = api.releaseDate ?: local.releaseDate,
        imdbId = api.imdbId ?: local.imdbId,
        tmdbId = api.tmdbId ?: local.tmdbId,
        runtime = api.runtime ?: local.runtime,
        genres = api.genres.ifEmpty { local.genres },
        plot = api.plot ?: local.plot,
        overview = api.overview ?: local.overview,
        poster = api.tmdbPoster ?: api.poster ?: local.poster,
        tmdbPoster = api.tmdbPoster ?: local.tmdbPoster,
        backdrop = api.tmdbBackdrop ?: api.backdrop ?: local.backdrop,
        tmdbBackdrop = api.tmdbBackdrop ?: local.tmdbBackdrop,
        trailerUrl = api.trailerUrl ?: local.trailerUrl,
        youtubeVideoId = api.youtubeVideoId ?: local.youtubeVideoId,
        trailerSource = api.trailerSource ?: local.trailerSource,
        trailers = (api.trailers + local.trailers).distinctBy { listOf(it.label, it.youtubeVideoId, it.url).joinToString(":") },
        director = api.director ?: local.director,
        writer = api.writer ?: local.writer,
        actors = api.actors.ifEmpty { local.actors },
        cast = api.cast.ifEmpty { local.cast },
        crew = api.crew.ifEmpty { local.crew },
        tmdbRating = api.tmdbRating ?: local.tmdbRating,
        metadataSource = MetadataSource.TMDB
    )

    private fun mergeTmdbFallback(local: ViewingItem, api: ViewingItem): ViewingItem = local.copy(
        originalTitle = local.originalTitle ?: api.originalTitle,
        year = local.year ?: api.year,
        releaseDate = local.releaseDate ?: api.releaseDate,
        imdbId = local.imdbId ?: api.imdbId,
        tmdbId = local.tmdbId ?: api.tmdbId,
        runtime = local.runtime ?: api.runtime,
        genres = local.genres.ifEmpty { api.genres },
        plot = local.plot ?: api.plot,
        overview = local.overview ?: api.overview,
        poster = local.poster ?: api.tmdbPoster ?: api.poster,
        tmdbPoster = local.tmdbPoster ?: api.tmdbPoster,
        backdrop = local.backdrop ?: api.tmdbBackdrop ?: api.backdrop,
        tmdbBackdrop = local.tmdbBackdrop ?: api.tmdbBackdrop,
        trailerUrl = local.trailerUrl ?: api.trailerUrl,
        youtubeVideoId = local.youtubeVideoId ?: api.youtubeVideoId,
        trailerSource = local.trailerSource ?: api.trailerSource,
        trailers = (local.trailers + api.trailers).distinctBy { listOf(it.label, it.youtubeVideoId, it.url).joinToString(":") },
        director = local.director ?: api.director,
        writer = local.writer ?: api.writer,
        actors = local.actors.ifEmpty { api.actors },
        cast = local.cast.ifEmpty { api.cast },
        crew = local.crew.ifEmpty { api.crew },
        tmdbRating = local.tmdbRating ?: api.tmdbRating
    )

    private fun MetadataSource.combine(next: MetadataSource): MetadataSource = when {
        this == MetadataSource.LOCAL -> next
        this == next -> this
        this in setOf(MetadataSource.OMDB, MetadataSource.TMDB, MetadataSource.WATCHMODE) && next in setOf(MetadataSource.OMDB, MetadataSource.TMDB, MetadataSource.WATCHMODE) -> MetadataSource.MERGED
        this == MetadataSource.MERGED -> MetadataSource.MERGED
        else -> next
    }
}
