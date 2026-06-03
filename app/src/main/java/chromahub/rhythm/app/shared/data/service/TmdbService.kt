package com.cinemaverse.mcu.shared.data.service

import com.cinemaverse.mcu.BuildConfig
import com.cinemaverse.mcu.shared.data.viewing.TrailerSource
import com.cinemaverse.mcu.shared.data.viewing.ViewingCastMember
import com.cinemaverse.mcu.shared.data.viewing.ViewingCrewMember
import com.cinemaverse.mcu.shared.data.viewing.ViewingItem
import com.cinemaverse.mcu.shared.util.ViewingArtworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

class TmdbService(
    private val apiKey: String = BuildConfig.TMDB_API_KEY,
    private val readAccessToken: String = BuildConfig.TMDB_READ_ACCESS_TOKEN
) {
    val hasCredentials: Boolean get() = apiKey.isNotBlank() || readAccessToken.isNotBlank()

    suspend fun searchTmdbMovies(query: String): Result<List<ViewingItem>> = withContext(Dispatchers.IO) {
        if (!hasCredentials) return@withContext Result.failure(IllegalStateException("TMDB API key/token is missing; using local viewing-list data."))
        runCatching {
            val json = request("/search/movie", "query=${query.urlEncode()}")
            json.optJSONArray("results")?.let { array ->
                (0 until array.length()).mapNotNull { index -> normalizeTmdbMovie(array.optJSONObject(index)) }
            } ?: emptyList()
        }
    }

    suspend fun getTmdbMovieDetails(movieId: Int): Result<ViewingItem> = withContext(Dispatchers.IO) {
        if (!hasCredentials) return@withContext Result.failure(IllegalStateException("TMDB API key/token is missing; using local viewing-list data."))
        runCatching { normalizeTmdbMovie(request("/movie/$movieId", "append_to_response=credits,videos,recommendations,images,external_ids")) }
    }

    suspend fun getTmdbMovieVideos(movieId: Int): Result<List<String>> = withContext(Dispatchers.IO) {
        if (!hasCredentials) return@withContext Result.failure(IllegalStateException("TMDB API key/token is missing; using local viewing-list data."))
        runCatching { extractTrailerUrls(request("/movie/$movieId/videos")) }
    }

    suspend fun getTmdbCredits(movieId: Int): Result<Pair<List<ViewingCastMember>, List<ViewingCrewMember>>> = withContext(Dispatchers.IO) {
        if (!hasCredentials) return@withContext Result.failure(IllegalStateException("TMDB API key/token is missing; using local viewing-list data."))
        runCatching { extractCredits(request("/movie/$movieId/credits")) }
    }

    suspend fun getTmdbMovieImages(movieId: Int): Result<JSONObject> = withContext(Dispatchers.IO) {
        if (!hasCredentials) return@withContext Result.failure(IllegalStateException("TMDB API key/token is missing; using local viewing-list data."))
        runCatching { request("/movie/$movieId/images") }
    }

    suspend fun getTmdbRecommendations(movieId: Int): Result<List<ViewingItem>> = withContext(Dispatchers.IO) {
        if (!hasCredentials) return@withContext Result.failure(IllegalStateException("TMDB API key/token is missing; using local viewing-list data."))
        runCatching {
            request("/movie/$movieId/recommendations").optJSONArray("results")?.let { array ->
                (0 until array.length()).mapNotNull { index -> normalizeTmdbMovie(array.optJSONObject(index)) }
            } ?: emptyList()
        }
    }

    suspend fun getTmdbCollection(collectionId: Int): Result<JSONObject> = withContext(Dispatchers.IO) {
        if (!hasCredentials) return@withContext Result.failure(IllegalStateException("TMDB API key/token is missing; using local viewing-list data."))
        runCatching { request("/collection/$collectionId") }
    }

    fun normalizeTmdbMovie(json: JSONObject): ViewingItem {
        val genres = json.optJSONArray("genres")?.let { array ->
            (0 until array.length()).mapNotNull { array.optJSONObject(it)?.optString("name")?.takeUsable() }
        } ?: emptyList()
        val credits = extractCredits(json.optJSONObject("credits"))
        val trailer = extractTrailerUrls(json.optJSONObject("videos")).firstOrNull()
        val imdbId = json.optJSONObject("external_ids")?.optString("imdb_id")?.takeUsable()
        val releaseDate = json.optString("release_date").takeUsable()
        return ViewingItem(
            id = json.optInt("id").takeIf { it > 0 }?.toString() ?: json.optString("title").slug(),
            title = json.optString("title", json.optString("name")),
            originalTitle = json.optString("original_title").takeUsable(),
            year = releaseDate?.take(4),
            releaseDate = releaseDate,
            imdbId = imdbId,
            tmdbId = json.optInt("id").takeIf { it > 0 },
            runtime = json.optInt("runtime").takeIf { it > 0 }?.let { "$it min" },
            genres = genres,
            plot = json.optString("overview").takeUsable(),
            overview = json.optString("overview").takeUsable(),
            tmdbPoster = ViewingArtworkUtils.tmdbPoster(json.optString("poster_path").takeUsable()),
            poster = ViewingArtworkUtils.tmdbPoster(json.optString("poster_path").takeUsable()),
            tmdbBackdrop = ViewingArtworkUtils.tmdbBackdrop(json.optString("backdrop_path").takeUsable()),
            backdrop = ViewingArtworkUtils.tmdbBackdrop(json.optString("backdrop_path").takeUsable()),
            trailerUrl = trailer,
            trailerSource = trailer?.let { TrailerSource.TMDB },
            cast = credits.first,
            crew = credits.second,
            director = credits.second.firstOrNull { it.job == "Director" }?.name,
            writer = credits.second.filter { it.job?.contains("Writer", true) == true || it.job?.contains("Screenplay", true) == true }.joinToString { it.name }.takeUsable(),
            actors = credits.first.take(8).map { it.name },
            tmdbRating = json.optDouble("vote_average").takeIf { it > 0.0 }
        )
    }

    private fun extractTrailerUrls(json: JSONObject?): List<String> {
        val results = json?.optJSONArray("results") ?: return emptyList()
        return (0 until results.length()).mapNotNull { index ->
            val video = results.optJSONObject(index)
            val site = video?.optString("site")
            val key = video?.optString("key")
            val type = video?.optString("type")
            if (site == "YouTube" && !key.isNullOrBlank() && (type == "Trailer" || type == "Teaser")) "https://www.youtube.com/watch?v=$key" else null
        }
    }

    private fun extractCredits(json: JSONObject?): Pair<List<ViewingCastMember>, List<ViewingCrewMember>> {
        if (json == null) return emptyList<ViewingCastMember>() to emptyList()
        val cast = json.optJSONArray("cast")?.let { array ->
            (0 until minOf(array.length(), 12)).mapNotNull { index ->
                array.optJSONObject(index)?.let {
                    ViewingCastMember(it.optInt("id").takeIf { id -> id > 0 }?.toString(), it.optString("name"), it.optString("character").takeUsable(), it.optString("profile_path").takeUsable())
                }
            }
        } ?: emptyList()
        val crew = json.optJSONArray("crew")?.let { array ->
            (0 until array.length()).mapNotNull { index ->
                array.optJSONObject(index)?.let {
                    ViewingCrewMember(it.optInt("id").takeIf { id -> id > 0 }?.toString(), it.optString("name"), it.optString("job").takeUsable(), it.optString("department").takeUsable())
                }
            }
        } ?: emptyList()
        return cast to crew
    }

    private fun request(path: String, query: String = ""): JSONObject {
        val apiQuery = if (apiKey.isNotBlank()) "api_key=${apiKey.urlEncode()}" else ""
        val fullQuery = listOf(apiQuery, query).filter { it.isNotBlank() }.joinToString("&")
        val url = URL("https://api.themoviedb.org/3$path?$fullQuery")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
            if (readAccessToken.isNotBlank()) setRequestProperty("Authorization", "Bearer $readAccessToken")
        }
        return connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
    }
}

private fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")
private fun String.takeUsable(): String? = takeIf { it.isNotBlank() && it != "N/A" && it != "null" }
private fun String.slug(): String = lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
