package com.cinemaverse.mcu.shared.data.service

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.cinemaverse.mcu.shared.data.viewing.McuAssetDataSource
import com.cinemaverse.mcu.shared.data.viewing.MetadataProviderMode
import com.cinemaverse.mcu.shared.data.viewing.ViewingItem
import com.cinemaverse.mcu.shared.data.viewing.ViewingUserStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ViewingMetadataStore {
    private val service = MovieMetadataService()
    private val enriched = mutableStateMapOf<String, ViewingItem>()
    val isFetching = mutableStateOf(false)
    val providerMode = mutableStateOf(MetadataProviderMode.OMDB_PRIMARY_TMDB_FALLBACK)
    val statusMessage = mutableStateOf(service.getConfigurationMessage(providerMode.value))
    val useLocalPosters = mutableStateOf(true)
    private val userStatuses = mutableStateMapOf<String, Set<ViewingUserStatus>>()
    private val recentlyViewed = mutableStateMapOf<String, Long>()
    private var appContext: Context? = null

    fun initialize(context: Context) {
        val application = context.applicationContext
        if (appContext === application) return
        appContext = application
        val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        useLocalPosters.value = prefs.getBoolean(KEY_USE_LOCAL_POSTERS, true)
        providerMode.value = runCatching {
            MetadataProviderMode.valueOf(prefs.getString(KEY_PROVIDER_MODE, MetadataProviderMode.OMDB_PRIMARY_TMDB_FALLBACK.name).orEmpty())
        }.getOrDefault(MetadataProviderMode.OMDB_PRIMARY_TMDB_FALLBACK)
        statusMessage.value = service.getConfigurationMessage(providerMode.value)
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

    fun itemFor(item: ViewingItem): ViewingItem = enriched[item.id] ?: item

    suspend fun enrich(item: ViewingItem): ViewingItem {
        enriched[item.id]?.let { return it }
        return withContext(Dispatchers.IO) {
            service.getEnrichedViewingItem(item, providerMode.value).item
        }.also { enriched[item.id] = mergeKeepingIdentity(item, it) }
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
            statusMessage.value = "Database loaded: $loaded titles refreshed with ${providerMode.value.label}. Cached for this app session."
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
        trailerUrl = remote.trailerUrl ?: local.trailerUrl,
        youtubeVideoId = remote.youtubeVideoId ?: local.youtubeVideoId,
        trailerSource = remote.trailerSource ?: local.trailerSource,
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

    private fun saveStatuses(itemId: String, statuses: Set<ViewingUserStatus>) {
        if (statuses.isEmpty()) userStatuses.remove(itemId) else userStatuses[itemId] = statuses
        appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)?.edit()?.apply {
            if (statuses.isEmpty()) remove(KEY_STATUS_PREFIX + itemId) else putString(KEY_STATUS_PREFIX + itemId, statuses.joinToString(",") { it.name })
        }?.apply()
    }

    private const val PREFS_NAME = "cinemaverse_user_state"
    private const val KEY_USE_LOCAL_POSTERS = "use_local_posters"
    private const val KEY_PROVIDER_MODE = "metadata_provider_mode"
    private const val KEY_STATUS_PREFIX = "statuses:"
    private const val KEY_RECENT_PREFIX = "recent:"
}
