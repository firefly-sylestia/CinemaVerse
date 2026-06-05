package com.marvelspectrum.shared.data.service

import com.marvelspectrum.BuildConfig
import com.marvelspectrum.shared.data.viewing.ViewingItem
import com.marvelspectrum.shared.data.viewing.ViewingRating
import com.marvelspectrum.shared.data.viewing.ViewingType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

class OmdbService(
    private val apiKey: String = BuildConfig.OMDB_API_KEY.ifBlank { DEFAULT_OMDB_API_KEY },
    private val fallbackApiKey: String = BuildConfig.OMDB_FALLBACK_API_KEY.ifBlank { DEFAULT_OMDB_FALLBACK_API_KEY }
) {
    private val apiKeys: List<String> = listOf(apiKey, fallbackApiKey).filter { it.isNotBlank() }.distinct()
    val hasApiKey: Boolean get() = apiKeys.isNotEmpty()

    suspend fun getMovieByTitle(title: String, year: String? = null): Result<ViewingItem> = withContext(Dispatchers.IO) {
        if (!hasApiKey) return@withContext Result.failure(IllegalStateException("OMDb API key is missing; using local viewing-list data."))
        val query = buildString {
            append("t=${title.urlEncode()}")
            if (!year.isNullOrBlank()) append("&y=${year.urlEncode()}")
        }
        fetchMovie(query)
    }

    suspend fun getMovieByImdbId(imdbId: String): Result<ViewingItem> = withContext(Dispatchers.IO) {
        if (!hasApiKey) return@withContext Result.failure(IllegalStateException("OMDb API key is missing; using local viewing-list data."))
        fetchMovie("i=${imdbId.urlEncode()}")
    }

    suspend fun searchOmdbMovies(query: String): Result<List<ViewingItem>> = withContext(Dispatchers.IO) {
        if (!hasApiKey) return@withContext Result.failure(IllegalStateException("OMDb API key is missing; using local viewing-list data."))
        runCatching {
            val json = request("s=${query.urlEncode()}&type=movie")
            if (json.optString("Response") == "False") return@runCatching emptyList()
            json.optJSONArray("Search")?.let { array ->
                (0 until array.length()).mapNotNull { index -> normalizeSearchResult(array.optJSONObject(index)) }
            } ?: emptyList()
        }
    }

    fun normalizeOmdbMovie(json: JSONObject): ViewingItem {
        val genres = json.optString("Genre").split(',').map { it.trim() }.filter { it.isNotBlank() && it != "N/A" }
        val actors = json.optString("Actors").split(',').map { it.trim() }.filter { it.isNotBlank() && it != "N/A" }
        val ratings = json.optJSONArray("Ratings")?.let { array ->
            (0 until array.length()).mapNotNull { index ->
                array.optJSONObject(index)?.let { ViewingRating(it.optString("Source"), it.optString("Value")) }
            }
        } ?: emptyList()
        return ViewingItem(
            id = json.optString("imdbID", json.optString("Title").slug()),
            title = json.optString("Title"),
            year = json.optString("Year").takeUsable(),
            releaseDate = json.optString("Released").takeUsable(),
            imdbId = json.optString("imdbID").takeUsable(),
            type = when (json.optString("Type")) {
                "series" -> ViewingType.SERIES
                "episode" -> ViewingType.EPISODE
                else -> ViewingType.MOVIE
            },
            runtime = json.optString("Runtime").takeUsable(),
            genres = genres,
            plot = json.optString("Plot").takeUsable(),
            overview = json.optString("Plot").takeUsable(),
            omdbPoster = json.optString("Poster").takeUsable(),
            poster = json.optString("Poster").takeUsable(),
            director = json.optString("Director").takeUsable(),
            writer = json.optString("Writer").takeUsable(),
            actors = actors,
            imdbRating = json.optString("imdbRating").takeUsable(),
            ratings = ratings,
            awards = json.optString("Awards").takeUsable(),
            language = json.optString("Language").takeUsable(),
            country = json.optString("Country").takeUsable()
        )
    }

    private fun fetchMovie(query: String): Result<ViewingItem> = runCatching {
        val json = request(query)
        if (json.optString("Response") == "False") {
            throw IllegalArgumentException(json.optString("Error", "OMDb did not return a movie."))
        }
        normalizeOmdbMovie(json)
    }

    private fun normalizeSearchResult(json: JSONObject?): ViewingItem? = json?.let {
        ViewingItem(
            id = it.optString("imdbID", it.optString("Title").slug()),
            title = it.optString("Title"),
            year = it.optString("Year").takeUsable(),
            imdbId = it.optString("imdbID").takeUsable(),
            omdbPoster = it.optString("Poster").takeUsable(),
            poster = it.optString("Poster").takeUsable()
        )
    }

    private fun request(query: String): JSONObject {
        var lastError: Throwable? = null
        apiKeys.forEach { key ->
            runCatching {
                val url = URL("https://www.omdbapi.com/?apikey=${key.urlEncode()}&$query")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8_000
                    readTimeout = 8_000
                }
                val json = connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
                val error = json.optString("Error")
                if (json.optString("Response") == "False" && error.contains(Regex("key|limit|quota", RegexOption.IGNORE_CASE))) {
                    lastError = IllegalArgumentException(error)
                } else {
                    return json
                }
            }.onFailure { lastError = it }
        }
        throw lastError ?: IllegalStateException("OMDb API key is missing; using local viewing-list data.")
    }
}

private const val DEFAULT_OMDB_API_KEY = "14596ed1"
private const val DEFAULT_OMDB_FALLBACK_API_KEY = "2c971c17"

private fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")
private fun String.takeUsable(): String? = takeIf { it.isNotBlank() && it != "N/A" }
private fun String.slug(): String = lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
