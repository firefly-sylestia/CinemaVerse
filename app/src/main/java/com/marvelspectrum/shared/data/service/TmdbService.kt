package com.marvelspectrum.shared.data.service

import com.marvelspectrum.BuildConfig
import com.marvelspectrum.shared.data.viewing.TrailerSource
import com.marvelspectrum.shared.data.viewing.ViewingCastMember
import com.marvelspectrum.shared.data.viewing.ViewingCrewMember
import com.marvelspectrum.shared.data.viewing.ViewingItem
import com.marvelspectrum.shared.util.ViewingArtworkUtils
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

    suspend fun getTmdbMovieDetails(movieId: Int): Result<ViewingItem> = getTmdbDetails(movieId, false)

    suspend fun getTmdbViewingDetails(item: ViewingItem): Result<ViewingItem> = withContext(Dispatchers.IO) {
        if (!hasCredentials) return@withContext Result.failure(IllegalStateException("TMDB API key/token is missing; using local viewing-list data."))
        val isTv = item.type == com.marvelspectrum.shared.data.viewing.ViewingType.SERIES || item.type == com.marvelspectrum.shared.data.viewing.ViewingType.EPISODE
        runCatching {
            item.tmdbId?.let { id ->
                return@runCatching normalizeTmdbMovie(
                    request("/${if (isTv) "tv" else "movie"}/$id", "append_to_response=credits,videos,recommendations,images,external_ids"),
                    mediaType = if (isTv) "tv" else "movie"
                )
            }
            val params = StringBuilder("query=${item.title.urlEncode()}&include_adult=false&page=1")
            item.year?.let { params.append("&year=${it.urlEncode()}") }
            val results = request("/search/multi", params.toString()).optJSONArray("results")
            val best = (0 until (results?.length() ?: 0))
                .mapNotNull { results?.optJSONObject(it) }
                .firstOrNull { candidate ->
                    candidate.optString("media_type") in setOf("movie", "tv") &&
                        (candidate.optString("backdrop_path").takeUsable() != null || candidate.optString("poster_path").takeUsable() != null)
                } ?: throw IllegalStateException("No TMDB artwork match for ${item.title}")
            val mediaType = if (best.optString("media_type") == "tv") "tv" else "movie"
            val id = best.optInt("id")
            normalizeTmdbMovie(request("/$mediaType/$id", "append_to_response=credits,videos,recommendations,images,external_ids"), mediaType)
        }
    }

    private suspend fun getTmdbDetails(movieId: Int, isTv: Boolean): Result<ViewingItem> = withContext(Dispatchers.IO) {
        if (!hasCredentials) return@withContext Result.failure(IllegalStateException("TMDB API key/token is missing; using local viewing-list data."))
        runCatching { normalizeTmdbMovie(request("/${if (isTv) "tv" else "movie"}/$movieId", "append_to_response=credits,videos,recommendations,images,external_ids"), if (isTv) "tv" else "movie") }
    }

    suspend fun getTmdbMovieVideos(movieId: Int): Result<List<TmdbTrailer>> = withContext(Dispatchers.IO) {
        if (!hasCredentials) return@withContext Result.failure(IllegalStateException("TMDB API key/token is missing; using local viewing-list data."))
        runCatching { extractTrailers(request("/movie/$movieId/videos")) }
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

    fun normalizeTmdbMovie(json: JSONObject, mediaType: String = json.optString("media_type", "movie")): ViewingItem {
        val genres = json.optJSONArray("genres")?.let { array ->
            (0 until array.length()).mapNotNull { array.optJSONObject(it)?.optString("name")?.takeUsable() }
        } ?: emptyList()
        val credits = extractCredits(json.optJSONObject("credits"))
        val trailer = extractTrailers(json.optJSONObject("videos")).firstOrNull()
        val imdbId = json.optJSONObject("external_ids")?.optString("imdb_id")?.takeUsable()
        val releaseDate = json.optString("release_date").takeUsable()
        return ViewingItem(
            id = json.optInt("id").takeIf { it > 0 }?.toString() ?: json.optString("title").slug(),
            title = json.optString("title").takeUsable() ?: json.optString("name"),
            originalTitle = (json.optString("original_title").takeUsable() ?: json.optString("original_name").takeUsable()),
            type = if (mediaType == "tv") com.marvelspectrum.shared.data.viewing.ViewingType.SERIES else com.marvelspectrum.shared.data.viewing.ViewingType.MOVIE,
            year = (releaseDate ?: json.optString("first_air_date").takeUsable())?.take(4),
            releaseDate = releaseDate ?: json.optString("first_air_date").takeUsable(),
            imdbId = imdbId,
            tmdbId = json.optInt("id").takeIf { it > 0 },
            runtime = (json.optInt("runtime").takeIf { it > 0 } ?: json.optJSONArray("episode_run_time")?.optInt(0)?.takeIf { it > 0 })?.let { "$it min" },
            genres = genres,
            plot = json.optString("overview").takeUsable(),
            overview = json.optString("overview").takeUsable(),
            tmdbPoster = ViewingArtworkUtils.tmdbPoster(json.optString("poster_path").takeUsable()),
            poster = ViewingArtworkUtils.tmdbPoster(json.optString("poster_path").takeUsable()),
            tmdbBackdrop = ViewingArtworkUtils.tmdbBackdrop(json.optString("backdrop_path").takeUsable()),
            backdrop = ViewingArtworkUtils.tmdbBackdrop(json.optString("backdrop_path").takeUsable()),
            trailerUrl = trailer?.url,
            youtubeVideoId = trailer?.key,
            trailerSource = trailer?.let { TrailerSource.TMDB },
            cast = credits.first,
            crew = credits.second,
            director = credits.second.firstOrNull { it.job == "Director" }?.name,
            writer = credits.second.filter { it.job?.contains("Writer", true) == true || it.job?.contains("Screenplay", true) == true }.joinToString { it.name }.takeUsable(),
            actors = credits.first.take(8).map { it.name },
            tmdbRating = json.optDouble("vote_average").takeIf { it > 0.0 },
            metadataSource = com.marvelspectrum.shared.data.viewing.MetadataSource.TMDB
        )
    }

    fun extractTrailerUrls(json: JSONObject?): List<String> = extractTrailers(json).map { it.url }

    fun extractTrailers(json: JSONObject?): List<TmdbTrailer> {
        val results = json?.optJSONArray("results") ?: return emptyList()
        return (0 until results.length()).mapNotNull { index ->
            val video = results.optJSONObject(index) ?: return@mapNotNull null
            val site = video.optString("site")
            val key = video.optString("key").takeUsable()
            val type = video.optString("type").takeUsable()
            if (site.equals("YouTube", ignoreCase = true) && key != null && key.matches(Regex("^[A-Za-z0-9_-]{11}$"))) {
                TmdbTrailer(key = key, name = video.optString("name").takeUsable(), type = type, official = video.optBoolean("official"))
            } else {
                null
            }
        }.sortedWith(
            compareBy<TmdbTrailer> { trailer ->
                when {
                    trailer.official && trailer.type.equals("Trailer", true) -> 0
                    trailer.type.equals("Trailer", true) -> 1
                    trailer.official && trailer.type.equals("Teaser", true) -> 2
                    trailer.type.equals("Teaser", true) -> 3
                    else -> 4
                }
            }.thenBy { it.name ?: "" }
        )
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

data class TmdbTrailer(val key: String, val name: String? = null, val type: String? = null, val official: Boolean = false) {
    val url: String = "https://www.youtube.com/watch?v=$key"
}


private fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")
private fun String.takeUsable(): String? = takeIf { it.isNotBlank() && it != "N/A" && it != "null" }
private fun String.slug(): String = lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
