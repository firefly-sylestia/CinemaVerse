package com.cinemaverse.mcu.shared.data.viewing

data class ViewingRating(
    val source: String,
    val value: String
)

data class ViewingCastMember(
    val id: String? = null,
    val name: String,
    val character: String? = null,
    val profilePath: String? = null
)

data class ViewingCrewMember(
    val id: String? = null,
    val name: String,
    val job: String? = null,
    val department: String? = null
)

data class ViewingItem(
    val id: String,
    val title: String,
    val originalTitle: String? = null,
    val year: String? = null,
    val releaseDate: String? = null,
    val imdbId: String? = null,
    val tmdbId: Int? = null,
    val type: ViewingType = ViewingType.MOVIE,
    val phase: String? = null,
    val saga: String? = null,
    val franchise: String? = null,
    val studio: String? = null,
    val order: Int? = null,
    val releaseOrder: Int? = null,
    val chronologicalOrder: Int? = null,
    val phaseOrder: Int? = null,
    val runtime: String? = null,
    val genres: List<String> = emptyList(),
    val plot: String? = null,
    val overview: String? = null,
    val poster: String? = null,
    val tmdbPoster: String? = null,
    val omdbPoster: String? = null,
    val localPoster: String? = null,
    val backdrop: String? = null,
    val tmdbBackdrop: String? = null,
    val localBackdrop: String? = null,
    val trailerUrl: String? = null,
    val trailerSource: TrailerSource? = null,
    val director: String? = null,
    val writer: String? = null,
    val actors: List<String> = emptyList(),
    val cast: List<ViewingCastMember> = emptyList(),
    val crew: List<ViewingCrewMember> = emptyList(),
    val imdbRating: String? = null,
    val tmdbRating: Double? = null,
    val ratings: List<ViewingRating> = emptyList(),
    val awards: String? = null,
    val language: String? = null,
    val country: String? = null,
    val watched: Boolean = false,
    val favorite: Boolean = false,
    val watchlisted: Boolean = false,
    val notes: String? = null
)

data class ViewingList(
    val id: String,
    val title: String,
    val description: String? = null,
    val poster: String? = null,
    val localPoster: String? = null,
    val backdrop: String? = null,
    val localBackdrop: String? = null,
    val phase: String? = null,
    val saga: String? = null,
    val franchise: String? = null,
    val items: List<ViewingItem>,
    val sortModes: List<ViewingSortMode> = listOf(
        ViewingSortMode.RELEASE,
        ViewingSortMode.CHRONOLOGICAL,
        ViewingSortMode.PHASE,
        ViewingSortMode.CUSTOM
    )
)

enum class ViewingType { MOVIE, SERIES, EPISODE, SPECIAL, SHORT }
enum class TrailerSource { TMDB, YOUTUBE, LOCAL, MANUAL }
enum class ViewingSortMode(val label: String) {
    RELEASE("Release order"),
    CHRONOLOGICAL("Chronological order"),
    PHASE("Phase order"),
    CUSTOM("Custom order")
}

data class MetadataResult(
    val item: ViewingItem,
    val source: MetadataSource = MetadataSource.LOCAL,
    val isFallback: Boolean = true,
    val message: String? = null
)

enum class MetadataSource { LOCAL, OMDB, TMDB, MERGED }
