package com.cinemaverse.mcu.shared.data.service

import com.cinemaverse.mcu.shared.data.viewing.TrailerSource
import com.cinemaverse.mcu.shared.data.viewing.ViewingItem
import com.cinemaverse.mcu.shared.data.viewing.ViewingTrailer
import com.cinemaverse.mcu.shared.data.viewing.ViewingType
import com.cinemaverse.mcu.shared.data.viewing.WatchProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

class WatchmodeService(
    private val apiKeyProvider: () -> String = { ViewingMetadataStore.watchmodeApiKey.value },
    private val enabledProvider: () -> Boolean = { ViewingMetadataStore.watchmodeApiEnabled.value }
) {
    data class QuotaHeaders(
        val rateLimit: String? = null,
        val rateRemaining: String? = null,
        val accountQuota: String? = null,
        val accountQuotaUsed: String? = null
    )

    data class WatchmodeResponse<T>(val value: T, val quota: QuotaHeaders)

    suspend fun searchByImdbId(imdbId: String): Result<WatchmodeResponse<String?>> =
        search("imdb_id", imdbId)

    suspend fun searchByTmdbId(tmdbId: Int, type: ViewingType): Result<WatchmodeResponse<String?>> =
        search(if (type == ViewingType.SERIES) "tmdb_tv_id" else "tmdb_movie_id", tmdbId.toString())

    suspend fun getTitleDetails(titleId: String): Result<WatchmodeResponse<ViewingItem>> = withContext(Dispatchers.IO) {
        requestObject("title/${titleId.urlEncode()}/details/").map { response ->
            val json = response.value
            WatchmodeResponse(
                value = ViewingItem(
                    id = titleId,
                    title = json.optString("title"),
                    originalTitle = json.optString("original_title").takeUsable(),
                    year = json.optString("year").takeUsable(),
                    runtime = json.optInt("runtime_minutes").takeIf { it > 0 }?.let { "$it min" },
                    imdbId = json.optString("imdb_id").takeUsable(),
                    tmdbId = json.optInt("tmdb_id").takeIf { it > 0 },
                    tmdbRating = json.optDouble("user_rating").takeIf { it > 0.0 },
                    poster = json.optString("poster").takeUsable(),
                    backdrop = json.optString("backdrop").takeUsable(),
                    trailerUrl = json.optString("trailer").takeUsable(),
                    youtubeVideoId = json.optString("trailer").takeUsable()?.extractYoutubeVideoId(),
                    trailerSource = TrailerSource.WATCHMODE,
                    trailers = json.optString("trailer").takeUsable()?.let { url ->
                        listOf(ViewingTrailer("Watchmode trailer", url.extractYoutubeVideoId(), url, TrailerSource.WATCHMODE))
                    } ?: emptyList()
                ),
                quota = response.quota
            )
        }
    }

    suspend fun getTitleSources(titleId: String, region: String = ViewingMetadataStore.cinemaAvailabilityRegion.value): Result<WatchmodeResponse<List<WatchProvider>>> = withContext(Dispatchers.IO) {
        requestArray("title/${titleId.urlEncode()}/sources/", mapOf("regions" to region)).map { response ->
            WatchmodeResponse(response.value.mapNotNull { it.toWatchProvider(region) }, response.quota)
        }
    }

    suspend fun getSources(region: String = ViewingMetadataStore.cinemaAvailabilityRegion.value): Result<WatchmodeResponse<List<WatchProvider>>> = withContext(Dispatchers.IO) {
        requestArray("sources/", mapOf("regions" to region)).map { response ->
            WatchmodeResponse(response.value.mapNotNull { it.toWatchProvider(region) }, response.quota)
        }
    }

    private suspend fun search(field: String, value: String): Result<WatchmodeResponse<String?>> = withContext(Dispatchers.IO) {
        requestObject("search/", mapOf("search_field" to field, "search_value" to value)).map { response ->
            WatchmodeResponse(response.value.optJSONArray("title_results")?.optJSONObject(0)?.optString("id")?.takeUsable(), response.quota)
        }
    }

    private fun requestObject(path: String, params: Map<String, String> = emptyMap()): Result<WatchmodeResponse<JSONObject>> = runCatching {
        request(path, params) { JSONObject(it) }
    }

    private fun requestArray(path: String, params: Map<String, String> = emptyMap()): Result<WatchmodeResponse<List<JSONObject>>> = runCatching {
        request(path, params) { body ->
            val array = JSONArray(body)
            List(array.length()) { array.optJSONObject(it) }.filterNotNull()
        }
    }

    private fun <T> request(path: String, params: Map<String, String>, parse: (String) -> T): WatchmodeResponse<T> {
        if (!enabledProvider()) throw IllegalStateException("Watchmode provider is disabled.")
        val key = apiKeyProvider().trim()
        if (key.isBlank()) throw IllegalStateException("Watchmode API key is missing.")
        val query = (params + ("apiKey" to key)).entries.joinToString("&") { "${it.key.urlEncode()}=${it.value.urlEncode()}" }
        val connection = (URL("https://api.watchmode.com/v1/$path?$query").openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
        }
        val body = if (connection.responseCode in 200..299) connection.inputStream.bufferedReader().use { it.readText() } else connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (connection.responseCode == 401 || connection.responseCode == 403) throw IllegalArgumentException("Watchmode API key is invalid or unauthorized.")
        if (connection.responseCode == 429) throw IllegalStateException("Watchmode quota exceeded.")
        if (connection.responseCode !in 200..299) throw IllegalStateException("Watchmode request failed (${connection.responseCode}).")
        return WatchmodeResponse(parse(body), connection.quotaHeaders())
    }

    private fun JSONObject.toWatchProvider(region: String): WatchProvider? {
        val name = optString("name", optString("source_name")).takeUsable() ?: return null
        return WatchProvider(
            providerName = name,
            providerId = optInt("source_id").takeIf { it > 0 },
            displayPriority = optInt("display_priority").takeIf { it > 0 },
            region = optString("region").takeUsable() ?: region,
            type = optString("type").takeUsable(),
            webUrl = optString("web_url").takeUsable(),
            androidUrl = optString("android_url").takeUsable(),
            price = optString("price").takeUsable(),
            format = optString("format").takeUsable()
        )
    }

    private fun HttpURLConnection.quotaHeaders() = QuotaHeaders(
        rateLimit = getHeaderField("X-RateLimit-Limit"),
        rateRemaining = getHeaderField("X-RateLimit-Remaining"),
        accountQuota = getHeaderField("X-Account-Quota"),
        accountQuotaUsed = getHeaderField("X-Account-Quota-Used")
    )

    private fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")
    private fun String?.takeUsable(): String? = this?.takeIf { it.isNotBlank() && it != "null" && it != "N/A" }
    private fun String.extractYoutubeVideoId(): String? = Regex("(?:v=|youtu\\.be/|embed/)([A-Za-z0-9_-]{11})").find(this)?.groupValues?.getOrNull(1)
}
