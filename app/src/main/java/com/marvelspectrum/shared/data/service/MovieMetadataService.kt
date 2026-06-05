package com.marvelspectrum.shared.data.service

import com.marvelspectrum.shared.data.viewing.MetadataProviderMode
import com.marvelspectrum.shared.data.viewing.MetadataResult
import com.marvelspectrum.shared.data.viewing.MetadataSource
import com.marvelspectrum.shared.data.viewing.RemoteMetadataState
import com.marvelspectrum.shared.data.viewing.ViewingItem
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MovieMetadataService(
    private val omdbService: OmdbService = OmdbService(),
    private val tmdbService: TmdbService = TmdbService(),
    private val watchmodeService: WatchmodeService = WatchmodeService()
) {
    private val cache = mutableMapOf<String, MetadataResult>()
    private val watchmodeTitleIdCache = mutableMapOf<String, String>()
    private val mutex = Mutex()

    suspend fun getEnrichedViewingItem(
        localItem: ViewingItem,
        providerMode: MetadataProviderMode = MetadataProviderMode.OMDB_PRIMARY_TMDB_FALLBACK
    ): MetadataResult {
        val cacheKey = listOf(
            providerMode.name,
            localItem.imdbId ?: localItem.tmdbId?.toString() ?: localItem.id,
            "wm=${ViewingMetadataStore.watchmodeApiEnabled.value && ViewingMetadataStore.watchmodeApiKey.value.isNotBlank()}",
            "region=${ViewingMetadataStore.cinemaAvailabilityRegion.value}",
            "remoteArt=${ViewingMetadataStore.useThirdPartyRemoteArtwork.value}",
            "omdbUser=${ViewingMetadataStore.omdbApiEnabled.value && ViewingMetadataStore.omdbApiKey.value.isNotBlank()}",
            "tmdbUser=${ViewingMetadataStore.tmdbApiEnabled.value && ViewingMetadataStore.tmdbReadAccessToken.value.isNotBlank()}"
        ).joinToString(":")
        mutex.withLock { cache[cacheKey]?.let { return it } }

        var merged = localItem
        var source = MetadataSource.LOCAL
        val messages = mutableListOf<String>()
        var remoteState = RemoteMetadataState.IDLE
        var watchmodeAttempted = false

        suspend fun applyOmdb(primary: Boolean) {
            val activeOmdb = activeOmdbService()
            if (!activeOmdb.hasApiKey) {
                messages += "OMDb key not configured; OMDb poster/details step skipped."
                return
            }
            val omdbResult = localItem.imdbId?.let { activeOmdb.getMovieByImdbId(it) }
                ?: activeOmdb.getMovieByTitle(localItem.title, localItem.year)
            omdbResult.fold(
                onSuccess = { omdb ->
                    merged = if (primary) mergeOmdbPrimary(merged, omdb) else mergeOmdbFallback(merged, omdb)
                    source = source.combine(MetadataSource.OMDB)
                },
                onFailure = { messages += it.message ?: "OMDb lookup failed." }
            )
        }

        suspend fun applyTmdb(primary: Boolean) {
            val activeTmdb = activeTmdbService()
            if (!activeTmdb.hasCredentials) {
                messages += "TMDB token missing; TMDB poster/backdrop/trailer step skipped."
                return
            }
            activeTmdb.getTmdbViewingDetails(localItem).fold(
                onSuccess = { tmdb ->
                    merged = if (primary) mergeTmdbPrimary(merged, tmdb) else mergeTmdbFallback(merged, tmdb)
                    source = source.combine(MetadataSource.TMDB)
                },
                onFailure = { messages += it.message ?: "TMDB lookup failed." }
            )
        }


        suspend fun applyWatchmodeIfUseful(primary: Boolean = false) {
            val needsStreaming = merged.watchProviders.isEmpty()
            val needsTrailer = merged.trailers.isEmpty() && merged.youtubeVideoId.isNullOrBlank() && merged.trailerUrl.isNullOrBlank()
            if (!primary && !needsStreaming && !needsTrailer) return
            val lookupKey = merged.imdbId ?: merged.tmdbId?.let { "tmdb:$it" } ?: return
            if (watchmodeAttempted) return
            watchmodeAttempted = true
            val titleId = watchmodeTitleIdCache[lookupKey] ?: run {
                val searchResult = merged.imdbId?.let { watchmodeService.searchByImdbId(it) }
                    ?: merged.tmdbId?.let { watchmodeService.searchByTmdbId(it, merged.type) }
                searchResult?.fold(
                    onSuccess = { response ->
                        recordWatchmodeQuota(response.quota)
                        response.value?.also { watchmodeTitleIdCache[lookupKey] = it }
                    },
                    onFailure = { error ->
                        remoteState = error.toRemoteState()
                        if (ViewingMetadataStore.watchmodeApiEnabled.value || ViewingMetadataStore.watchmodeApiKey.value.isNotBlank()) {
                            messages += error.message ?: "Watchmode lookup failed."
                        }
                        null
                    }
                )
            }
            if (titleId.isNullOrBlank()) {
                if (remoteState == RemoteMetadataState.IDLE) remoteState = RemoteMetadataState.NOT_FOUND
                return
            }
            watchmodeService.getTitleDetails(titleId).fold(
                onSuccess = { response ->
                    recordWatchmodeQuota(response.quota)
                    merged = if (primary) {
                        mergeWatchmodePrimary(merged, response.value, ViewingMetadataStore.useThirdPartyRemoteArtwork.value)
                    } else {
                        mergeWatchmodeFallback(merged, response.value, ViewingMetadataStore.useThirdPartyRemoteArtwork.value)
                    }
                    source = source.combine(MetadataSource.WATCHMODE)
                    remoteState = RemoteMetadataState.SUCCESS
                },
                onFailure = { error ->
                    remoteState = error.toRemoteState()
                    messages += error.message ?: "Watchmode title details failed."
                }
            )
            watchmodeService.getTitleSources(titleId, ViewingMetadataStore.cinemaAvailabilityRegion.value).fold(
                onSuccess = { response ->
                    recordWatchmodeQuota(response.quota)
                    if (response.value.isNotEmpty()) {
                        merged = merged.copy(watchProviders = response.value)
                        source = source.combine(MetadataSource.WATCHMODE)
                        remoteState = RemoteMetadataState.SUCCESS
                    }
                },
                onFailure = { error ->
                    remoteState = error.toRemoteState()
                    messages += error.message ?: "Watchmode streaming sources failed."
                }
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
            MetadataProviderMode.WATCHMODE_PRIMARY_OMDB_TMDB_FALLBACK -> {
                applyWatchmodeIfUseful(primary = true)
                applyOmdb(primary = false)
                applyTmdb(primary = false)
            }
        }

        applyWatchmodeIfUseful()
        ViewingMetadataStore.setRemoteMetadataState(remoteState)

        val result = MetadataResult(
            item = merged.copy(metadataSource = source),
            source = source,
            isFallback = source == MetadataSource.LOCAL,
            message = messages.joinToString(" ").takeIf { it.isNotBlank() },
            remoteState = remoteState
        )
        mutex.withLock { cache[cacheKey] = result }
        return result
    }

    fun clearCache() {
        cache.clear()
        watchmodeTitleIdCache.clear()
    }

    fun getConfigurationMessage(providerMode: MetadataProviderMode = MetadataProviderMode.OMDB_PRIMARY_TMDB_FALLBACK): String = buildString {
        append(providerMode.label).append(". ")
        if (!activeOmdbService().hasApiKey) append("OMDb key missing. ")
        if (!activeTmdbService().hasCredentials) append("TMDB token missing. ")
        if (activeOmdbService().hasApiKey || activeTmdbService().hasCredentials) append("Manual fetch refreshes posters, backdrops, trailers, and details; bundled local posters remain available.")
        else append("Cinemaverse remains usable with bundled local posters and the offline Marvel/DC catalog.")
        if (ViewingMetadataStore.watchmodeApiEnabled.value && ViewingMetadataStore.watchmodeApiKey.value.isNotBlank()) append(" Watchmode streaming availability is enabled.")
    }

    private fun activeOmdbService(): OmdbService =
        if (ViewingMetadataStore.omdbApiEnabled.value && ViewingMetadataStore.omdbApiKey.value.isNotBlank()) {
            OmdbService(apiKey = ViewingMetadataStore.omdbApiKey.value, fallbackApiKey = "")
        } else {
            omdbService
        }

    private fun activeTmdbService(): TmdbService =
        if (ViewingMetadataStore.tmdbApiEnabled.value && ViewingMetadataStore.tmdbReadAccessToken.value.isNotBlank()) {
            val credential = ViewingMetadataStore.tmdbReadAccessToken.value
            if (credential.startsWith("eyJ")) TmdbService(readAccessToken = credential) else TmdbService(apiKey = credential, readAccessToken = "")
        } else {
            tmdbService
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


    private fun mergeWatchmodePrimary(local: ViewingItem, api: ViewingItem, allowThirdPartyArtwork: Boolean): ViewingItem {
        val attribution = api.remoteArtworkAttribution ?: local.remoteArtworkAttribution
        return local.copy(
            year = api.year ?: local.year,
            imdbId = api.imdbId ?: local.imdbId,
            tmdbId = api.tmdbId ?: local.tmdbId,
            runtime = api.runtime ?: local.runtime,
            genres = api.genres.ifEmpty { local.genres },
            tmdbRating = api.tmdbRating ?: local.tmdbRating,
            poster = if (allowThirdPartyArtwork) attribution?.posterUrl ?: local.poster else local.poster,
            backdrop = if (allowThirdPartyArtwork) attribution?.backdropUrl ?: local.backdrop else local.backdrop,
            remoteArtworkAttribution = attribution,
            trailerUrl = api.trailerUrl ?: local.trailerUrl,
            youtubeVideoId = api.youtubeVideoId ?: local.youtubeVideoId,
            trailerSource = api.trailerSource ?: local.trailerSource,
            trailers = (api.trailers + local.trailers).distinctBy { listOf(it.label, it.youtubeVideoId, it.url).joinToString(":") },
            metadataSource = MetadataSource.WATCHMODE
        )
    }

    private fun mergeWatchmodeFallback(local: ViewingItem, api: ViewingItem, allowThirdPartyArtwork: Boolean): ViewingItem {
        val attribution = api.remoteArtworkAttribution ?: local.remoteArtworkAttribution
        return local.copy(
            year = local.year ?: api.year,
            imdbId = local.imdbId ?: api.imdbId,
            tmdbId = local.tmdbId ?: api.tmdbId,
            runtime = local.runtime ?: api.runtime,
            genres = local.genres.ifEmpty { api.genres },
            tmdbRating = local.tmdbRating ?: api.tmdbRating,
            poster = local.poster ?: if (allowThirdPartyArtwork) attribution?.posterUrl else null,
            backdrop = local.backdrop ?: if (allowThirdPartyArtwork) attribution?.backdropUrl else null,
            remoteArtworkAttribution = attribution,
            trailerUrl = local.trailerUrl ?: api.trailerUrl,
            youtubeVideoId = local.youtubeVideoId ?: api.youtubeVideoId,
            trailerSource = local.trailerSource ?: api.trailerSource,
            trailers = (local.trailers + api.trailers).distinctBy { listOf(it.label, it.youtubeVideoId, it.url).joinToString(":") },
            metadataSource = MetadataSource.WATCHMODE
        )
    }

    private fun recordWatchmodeQuota(quota: WatchmodeService.QuotaHeaders) {
        ViewingMetadataStore.setWatchmodeQuotaMessage(
            listOfNotNull(
                quota.rateRemaining?.let { "Rate remaining $it" },
                quota.rateLimit?.let { "limit $it" },
                quota.accountQuotaUsed?.let { "quota used $it" },
                quota.accountQuota?.let { "of $it" }
            ).joinToString(" • ").takeIf { it.isNotBlank() }
        )
    }

    private fun Throwable.toRemoteState(): RemoteMetadataState {
        val text = message.orEmpty()
        return when {
            text.contains("disabled", ignoreCase = true) || text.contains("missing", ignoreCase = true) -> RemoteMetadataState.NOT_CONFIGURED
            text.contains("unauthorized", ignoreCase = true) || text.contains("invalid", ignoreCase = true) -> RemoteMetadataState.UNAUTHORIZED
            text.contains("quota", ignoreCase = true) || text.contains("429") -> RemoteMetadataState.QUOTA_LIMITED
            text.contains("not found", ignoreCase = true) || text.contains("404") -> RemoteMetadataState.NOT_FOUND
            else -> RemoteMetadataState.NETWORK_ERROR
        }
    }

    private fun MetadataSource.combine(next: MetadataSource): MetadataSource = when {
        this == MetadataSource.LOCAL -> next
        this == next -> this
        this in setOf(MetadataSource.OMDB, MetadataSource.TMDB, MetadataSource.WATCHMODE) && next in setOf(MetadataSource.OMDB, MetadataSource.TMDB, MetadataSource.WATCHMODE) -> MetadataSource.MERGED
        this == MetadataSource.MERGED -> MetadataSource.MERGED
        else -> next
    }
}
