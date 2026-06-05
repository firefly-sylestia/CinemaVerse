package com.marvelspectrum.shared.data.service

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.marvelspectrum.shared.data.viewing.McuAssetDataSource
import com.marvelspectrum.shared.data.viewing.MetadataProviderMode
import com.marvelspectrum.shared.data.viewing.RemoteMetadataState
import com.marvelspectrum.shared.data.viewing.ViewingArtworkAttribution
import com.marvelspectrum.shared.data.viewing.ViewingItem
import com.marvelspectrum.shared.data.viewing.ViewingUserStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ViewingMetadataStore {
    private val service = MovieMetadataService()
    private val enriched = mutableStateMapOf<String, ViewingItem>()
    val isFetching = mutableStateOf(false)
    val providerMode = mutableStateOf(MetadataProviderMode.OMDB_PRIMARY_TMDB_FALLBACK)
    val statusMessage = mutableStateOf("Marvel Spectrum remains usable with bundled local posters and the offline Marvel/DC catalog.")
    val useLocalPosters = mutableStateOf(true)
    val useThirdPartyRemoteArtwork = mutableStateOf(false)
    val watchmodeQuotaMessage = mutableStateOf<String?>(null)
    val remoteMetadataState = mutableStateOf(RemoteMetadataState.IDLE)
    val watchmodeApiEnabled = mutableStateOf(false)
    val watchmodeApiKey = mutableStateOf("")
    val tmdbApiEnabled = mutableStateOf(false)
    val tmdbReadAccessToken = mutableStateOf("")
    val omdbApiEnabled = mutableStateOf(false)
    val omdbApiKey = mutableStateOf("")
    val cinemaAvailabilityRegion = mutableStateOf("US")
    private val userStatuses = mutableStateMapOf<String, Set<ViewingUserStatus>>()
    private val recentlyViewed = mutableStateMapOf<String, Long>()
    private var appContext: Context? = null

    fun initialize(context: Context) {
        val application = context.applicationContext
        if (appContext === application) return
        appContext = application
        val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        useLocalPosters.value = prefs.getBoolean(KEY_USE_LOCAL_POSTERS, true)
        useThirdPartyRemoteArtwork.value = prefs.getBoolean(KEY_USE_THIRD_PARTY_REMOTE_ARTWORK, false)
        watchmodeApiEnabled.value = prefs.getBoolean(KEY_WATCHMODE_ENABLED, false)
        watchmodeApiKey.value = prefs.getString(KEY_WATCHMODE_KEY, "").orEmpty()
        tmdbApiEnabled.value = prefs.getBoolean(KEY_TMDB_ENABLED, false)
        tmdbReadAccessToken.value = prefs.getString(KEY_TMDB_TOKEN, "").orEmpty()
        omdbApiEnabled.value = prefs.getBoolean(KEY_OMDB_ENABLED, false)
        omdbApiKey.value = prefs.getString(KEY_OMDB_KEY, "").orEmpty()
        cinemaAvailabilityRegion.value = prefs.getString(KEY_AVAILABILITY_REGION, "US").orEmpty().ifBlank { "US" }
        providerMode.value = runCatching {
            MetadataProviderMode.valueOf(prefs.getString(KEY_PROVIDER_MODE, MetadataProviderMode.OMDB_PRIMARY_TMDB_FALLBACK.name).orEmpty())
        }.getOrDefault(MetadataProviderMode.OMDB_PRIMARY_TMDB_FALLBACK)
        statusMessage.value = service.getConfigurationMessage(providerMode.value)
        enriched.clear()
        prefs.all.filterKeys { it.startsWith(KEY_ENRICHED_PREFIX + providerMode.value.name + ":") }.forEach { (key, value) ->
            (value as? String)?.let { json ->
                cachedItemFromJson(json)?.let { cached ->
                    val itemId = key.removePrefix(KEY_ENRICHED_PREFIX).substringAfter(':')
                    enriched[itemId] = cached
                }
            }
        }
        userStatuses.clear()
        prefs.all.filterKeys { it.startsWith(KEY_STATUS_PREFIX) }.forEach { (key, value) ->
            val statuses = value.toString().split(',').mapNotNull { runCatching { ViewingUserStatus.valueOf(it) }.getOrNull() }.toSet()
            if (statuses.isNotEmpty()) userStatuses[key.removePrefix(KEY_STATUS_PREFIX)] = statuses
        }
        recentlyViewed.clear()
        prefs.all.filterKeys { it.startsWith(KEY_RECENT_PREFIX) }.forEach { (key, value) ->
            (value as? Long)?.let { recentlyViewed[key.removePrefix(KEY_RECENT_PREFIX)] = it }
        }
    }

    fun setUseLocalPosters(enabled: Boolean) {
        useLocalPosters.value = enabled
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.putBoolean(KEY_USE_LOCAL_POSTERS, enabled)?.apply()
    }

    fun setUseThirdPartyRemoteArtwork(enabled: Boolean) {
        useThirdPartyRemoteArtwork.value = enabled
        enriched.clear()
        service.clearCache()
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.putBoolean(KEY_USE_THIRD_PARTY_REMOTE_ARTWORK, enabled)?.apply()
    }

    fun setWatchmodeQuotaMessage(message: String?) { watchmodeQuotaMessage.value = message }
    fun setRemoteMetadataState(state: RemoteMetadataState) { remoteMetadataState.value = state }

    fun setWatchmodeApiEnabled(enabled: Boolean) = putBoolean(KEY_WATCHMODE_ENABLED, enabled) { watchmodeApiEnabled.value = it }
    fun setWatchmodeApiKey(key: String) = putString(KEY_WATCHMODE_KEY, key.trim()) { watchmodeApiKey.value = it }
    fun setTmdbApiEnabled(enabled: Boolean) = putBoolean(KEY_TMDB_ENABLED, enabled) { tmdbApiEnabled.value = it }
    fun setTmdbReadAccessToken(token: String) = putString(KEY_TMDB_TOKEN, token.trim()) { tmdbReadAccessToken.value = it }
    fun setOmdbApiEnabled(enabled: Boolean) = putBoolean(KEY_OMDB_ENABLED, enabled) { omdbApiEnabled.value = it }
    fun setOmdbApiKey(key: String) = putString(KEY_OMDB_KEY, key.trim()) { omdbApiKey.value = it }
    fun setCinemaAvailabilityRegion(region: String) = putString(KEY_AVAILABILITY_REGION, region.uppercase()) { cinemaAvailabilityRegion.value = it.ifBlank { "US" } }

    private fun putBoolean(key: String, value: Boolean, update: (Boolean) -> Unit) {
        update(value)
        enriched.clear()
        service.clearCache()
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.putBoolean(key, value)?.apply()
    }

    private fun putString(key: String, value: String, update: (String) -> Unit) {
        update(value)
        enriched.clear()
        service.clearCache()
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.putString(key, value)?.apply()
    }

    fun setProviderMode(mode: MetadataProviderMode) {
        if (providerMode.value == mode) return
        providerMode.value = mode
        enriched.clear()
        service.clearCache()
        statusMessage.value = service.getConfigurationMessage(mode)
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.putString(KEY_PROVIDER_MODE, mode.name)?.apply()
    }

    fun statusesFor(item: ViewingItem): Set<ViewingUserStatus> = userStatuses[item.id] ?: buildSet {
        if (item.watched) add(ViewingUserStatus.WATCHED)
        if (item.watchlisted) add(ViewingUserStatus.WATCHLIST)
        if (item.favorite) add(ViewingUserStatus.FAVORITE)
        addAll(item.userStatuses)
    }

    fun toggleStatus(item: ViewingItem, status: ViewingUserStatus) {
        val next = statusesFor(item).toMutableSet().apply { if (!add(status)) remove(status) }
        if (status == ViewingUserStatus.WATCHED && ViewingUserStatus.WATCHED in next) next.remove(ViewingUserStatus.WATCHING)
        if (status == ViewingUserStatus.HIDDEN && ViewingUserStatus.HIDDEN in next) next.removeAll(setOf(ViewingUserStatus.WATCHLIST, ViewingUserStatus.WATCH_LATER, ViewingUserStatus.FAVORITE, ViewingUserStatus.WATCHING))
        if (status in setOf(ViewingUserStatus.WATCHLIST, ViewingUserStatus.WATCH_LATER, ViewingUserStatus.FAVORITE, ViewingUserStatus.WATCHING) && status in next) next.remove(ViewingUserStatus.HIDDEN)
        saveStatuses(item.id, next)
    }

    fun markViewed(item: ViewingItem) {
        recentlyViewed[item.id] = System.currentTimeMillis()
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.putLong(KEY_RECENT_PREFIX + item.id, recentlyViewed[item.id] ?: 0L)?.apply()
    }

    fun recentItems(data: McuAssetDataSource.ViewingAssetData, limit: Int = 6): List<ViewingItem> = recentlyViewed.entries.sortedByDescending { it.value }.mapNotNull { data.findItem(it.key) }.take(limit)

    fun itemFor(item: ViewingItem): ViewingItem = enriched[item.id]?.let { mergeKeepingIdentity(item, it) } ?: item

    suspend fun enrich(item: ViewingItem): ViewingItem {
        enriched[item.id]?.let { return mergeKeepingIdentity(item, it) }
        return withContext(Dispatchers.IO) {
            service.getEnrichedViewingItem(item, providerMode.value).item
        }.let { mergeKeepingIdentity(item, it) }.also { merged ->
            enriched[item.id] = merged
            saveEnriched(item.id, merged)
        }
    }

    suspend fun fetchAll(data: McuAssetDataSource.ViewingAssetData) {
        if (isFetching.value) return
        isFetching.value = true
        statusMessage.value = "Fetching ${providerMode.value.label} artwork and cinema metadata…"
        var loaded = 0
        try {
            data.allItems.forEach { item ->
                runCatching { enrich(item) }
                loaded += 1
                statusMessage.value = "Fetched $loaded of ${data.allItems.size} Cinemaverse titles."
            }
            statusMessage.value = "Database loaded: $loaded titles refreshed with ${providerMode.value.label}. Cached locally for future app launches."
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
        poster = remote.poster ?: remote.omdbPoster ?: remote.tmdbPoster ?: local.poster,
        tmdbPoster = remote.tmdbPoster ?: local.tmdbPoster,
        omdbPoster = remote.omdbPoster ?: local.omdbPoster,
        backdrop = remote.backdrop ?: local.backdrop,
        tmdbBackdrop = remote.tmdbBackdrop ?: local.tmdbBackdrop,
        remoteArtworkAttribution = remote.remoteArtworkAttribution ?: local.remoteArtworkAttribution,
        trailerUrl = remote.trailerUrl ?: local.trailerUrl,
        youtubeVideoId = remote.youtubeVideoId ?: local.youtubeVideoId,
        trailerSource = remote.trailerSource ?: local.trailerSource,
        trailers = (remote.trailers + local.trailers).distinctBy { listOf(it.label, it.youtubeVideoId, it.url).joinToString(":") },
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
        lastUpdated = providerMode.value.label
    )

    private fun saveEnriched(itemId: String, item: ViewingItem) {
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()
            ?.putString(KEY_ENRICHED_PREFIX + providerMode.value.name + ":" + itemId, item.toCacheJson().toString())
            ?.apply()
    }

    private fun cachedItemFromJson(value: String): ViewingItem? = runCatching {
        val json = JSONObject(value)
        ViewingItem(
            id = json.getString("id"),
            title = json.getString("title"),
            originalTitle = json.optStringOrNull("originalTitle"),
            universe = json.optStringOrNull("universe"),
            franchise = json.optStringOrNull("franchise"),
            studio = json.optStringOrNull("studio"),
            type = runCatching { com.marvelspectrum.shared.data.viewing.ViewingType.valueOf(json.optString("type")) }.getOrDefault(com.marvelspectrum.shared.data.viewing.ViewingType.MOVIE),
            phase = json.optStringOrNull("phase"),
            saga = json.optStringOrNull("saga"),
            category = json.optStringOrNull("category"),
            releaseDate = json.optStringOrNull("releaseDate"),
            year = json.optStringOrNull("year"),
            runtime = json.optStringOrNull("runtime"),
            genres = json.optStringList("genres"),
            language = json.optStringOrNull("language"),
            country = json.optStringOrNull("country"),
            imdbId = json.optStringOrNull("imdbId"),
            tmdbId = json.optIntOrNull("tmdbId"),
            imdbRating = json.optStringOrNull("imdbRating"),
            tmdbRating = json.optDoubleOrNull("tmdbRating"),
            director = json.optStringOrNull("director"),
            writer = json.optStringOrNull("writer"),
            actors = json.optStringList("actors"),
            description = json.optStringOrNull("description"),
            overview = json.optStringOrNull("overview"),
            plot = json.optStringOrNull("plot"),
            poster = json.optStringOrNull("poster"),
            tmdbPoster = json.optStringOrNull("tmdbPoster"),
            omdbPoster = json.optStringOrNull("omdbPoster"),
            localPoster = json.optStringOrNull("localPoster"),
            backdrop = json.optStringOrNull("backdrop"),
            tmdbBackdrop = json.optStringOrNull("tmdbBackdrop"),
            localBackdrop = json.optStringOrNull("localBackdrop"),
            remoteArtworkAttribution = json.optJSONObject("remoteArtworkAttribution")?.let { art ->
                ViewingArtworkAttribution(
                    provider = art.optStringOrNull("provider") ?: "Remote",
                    posterUrl = art.optStringOrNull("posterUrl"),
                    backdropUrl = art.optStringOrNull("backdropUrl"),
                    requiresAttribution = art.optBoolean("requiresAttribution", true)
                )
            },
            trailerUrl = json.optStringOrNull("trailerUrl"),
            youtubeVideoId = json.optStringOrNull("youtubeVideoId"),
            trailers = json.optJSONArray("trailers")?.let { array ->
                List(array.length()) { index -> array.optJSONObject(index) }.mapNotNull { trailer ->
                    trailer?.let {
                        com.marvelspectrum.shared.data.viewing.ViewingTrailer(
                            label = it.optStringOrNull("label") ?: "Trailer",
                            youtubeVideoId = it.optStringOrNull("youtubeVideoId"),
                            url = it.optStringOrNull("url"),
                            source = runCatching { com.marvelspectrum.shared.data.viewing.TrailerSource.valueOf(it.optString("source")) }.getOrNull()
                        )
                    }
                }
            } ?: emptyList(),
            releaseOrder = json.optIntOrNull("releaseOrder"),
            chronologicalOrder = json.optIntOrNull("chronologicalOrder"),
            phaseOrder = json.optIntOrNull("phaseOrder"),
            metadataSource = runCatching { com.marvelspectrum.shared.data.viewing.MetadataSource.valueOf(json.optString("metadataSource")) }.getOrDefault(com.marvelspectrum.shared.data.viewing.MetadataSource.LOCAL),
            lastUpdated = json.optStringOrNull("lastUpdated"),
            status = runCatching { com.marvelspectrum.shared.data.viewing.ViewingStatus.valueOf(json.optString("status")) }.getOrDefault(com.marvelspectrum.shared.data.viewing.ViewingStatus.RELEASED),
            awards = json.optStringOrNull("awards")
        )
    }.getOrNull()

    private fun ViewingItem.toCacheJson(): JSONObject = JSONObject().apply {
        put("id", id); put("title", title); putNullable("originalTitle", originalTitle); putNullable("universe", universe); putNullable("franchise", franchise); putNullable("studio", studio)
        put("type", type.name); putNullable("phase", phase); putNullable("saga", saga); putNullable("category", category); putNullable("releaseDate", releaseDate); putNullable("year", year); putNullable("runtime", runtime)
        put("genres", JSONArray(genres)); putNullable("language", language); putNullable("country", country); putNullable("imdbId", imdbId); tmdbId?.let { put("tmdbId", it) }
        putNullable("imdbRating", imdbRating); tmdbRating?.let { put("tmdbRating", it) }; putNullable("director", director); putNullable("writer", writer); put("actors", JSONArray(actors))
        putNullable("description", description); putNullable("overview", overview); putNullable("plot", plot); putNullable("poster", poster); putNullable("tmdbPoster", tmdbPoster); putNullable("omdbPoster", omdbPoster); putNullable("localPoster", localPoster)
        putNullable("backdrop", backdrop); putNullable("tmdbBackdrop", tmdbBackdrop); putNullable("localBackdrop", localBackdrop); putNullable("trailerUrl", trailerUrl); putNullable("youtubeVideoId", youtubeVideoId)
        remoteArtworkAttribution?.let { put("remoteArtworkAttribution", JSONObject().apply { put("provider", it.provider); putNullable("posterUrl", it.posterUrl); putNullable("backdropUrl", it.backdropUrl); put("requiresAttribution", it.requiresAttribution) }) }
        put("trailers", JSONArray(trailers.map { JSONObject().apply { put("label", it.label); putNullable("youtubeVideoId", it.youtubeVideoId); putNullable("url", it.url); it.source?.let { source -> put("source", source.name) } } }))
        releaseOrder?.let { put("releaseOrder", it) }; chronologicalOrder?.let { put("chronologicalOrder", it) }; phaseOrder?.let { put("phaseOrder", it) }
        put("metadataSource", metadataSource.name); putNullable("lastUpdated", lastUpdated); put("status", status.name); putNullable("awards", awards)
    }

    private fun JSONObject.putNullable(name: String, value: String?) { if (value != null) put(name, value) }
    private fun JSONObject.optStringOrNull(name: String): String? = optString(name).takeIf { it.isNotBlank() && it != "null" }
    private fun JSONObject.optIntOrNull(name: String): Int? = if (has(name) && !isNull(name)) optInt(name) else null
    private fun JSONObject.optDoubleOrNull(name: String): Double? = if (has(name) && !isNull(name)) optDouble(name) else null
    private fun JSONObject.optStringList(name: String): List<String> = optJSONArray(name)?.let { array -> List(array.length()) { array.optString(it) }.filter { it.isNotBlank() } } ?: emptyList()

    private fun saveStatuses(itemId: String, statuses: Set<ViewingUserStatus>) {
        if (statuses.isEmpty()) userStatuses.remove(itemId) else userStatuses[itemId] = statuses
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.apply {
            if (statuses.isEmpty()) remove(KEY_STATUS_PREFIX + itemId) else putString(KEY_STATUS_PREFIX + itemId, statuses.joinToString(",") { it.name })
        }?.apply()
    }

    private const val PREFS_NAME = "cinemaverse_user_state"
    private const val KEY_USE_LOCAL_POSTERS = "use_local_posters"
    private const val KEY_USE_THIRD_PARTY_REMOTE_ARTWORK = "use_third_party_remote_artwork"
    private const val KEY_PROVIDER_MODE = "metadata_provider_mode"
    private const val KEY_WATCHMODE_ENABLED = "watchmode_api_enabled"
    private const val KEY_WATCHMODE_KEY = "watchmode_api_key"
    private const val KEY_TMDB_ENABLED = "tmdb_api_enabled"
    private const val KEY_TMDB_TOKEN = "tmdb_read_access_token"
    private const val KEY_OMDB_ENABLED = "omdb_api_enabled"
    private const val KEY_OMDB_KEY = "omdb_api_key"
    private const val KEY_AVAILABILITY_REGION = "cinema_availability_region"
    private const val KEY_STATUS_PREFIX = "statuses:"
    private const val KEY_RECENT_PREFIX = "recent:"
    private const val KEY_ENRICHED_PREFIX = "enriched:"
}
