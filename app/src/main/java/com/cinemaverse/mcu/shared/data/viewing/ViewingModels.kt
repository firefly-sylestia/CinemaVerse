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

data class WatchProvider(
    val providerName: String,
    val displayPriority: Int? = null,
    val region: String? = null,
    val type: String? = null
)

data class ViewingItem(
    val id: String,
    val title: String,
    val originalTitle: String? = null,
    val universe: String? = null,
    val franchise: String? = null,
    val studio: String? = null,
    val type: ViewingType = ViewingType.MOVIE,
    val phase: String? = null,
    val saga: String? = null,
    val category: String? = null,
    val releaseDate: String? = null,
    val year: String? = releaseDate?.take(4),
    val runtime: String? = null,
    val genres: List<String> = emptyList(),
    val language: String? = null,
    val country: String? = null,
    val imdbId: String? = null,
    val tmdbId: Int? = null,
    val imdbRating: String? = null,
    val tmdbRating: Double? = null,
    val ratings: List<ViewingRating> = emptyList(),
    val director: String? = null,
    val writer: String? = null,
    val actors: List<String> = emptyList(),
    val cast: List<ViewingCastMember> = emptyList(),
    val crew: List<ViewingCrewMember> = emptyList(),
    val description: String? = null,
    val overview: String? = null,
    val plot: String? = null,
    val poster: String? = null,
    val tmdbPoster: String? = null,
    val omdbPoster: String? = null,
    val localPoster: String? = null,
    val backdrop: String? = null,
    val tmdbBackdrop: String? = null,
    val localBackdrop: String? = null,
    val trailerUrl: String? = null,
    val youtubeVideoId: String? = null,
    val trailerSource: TrailerSource? = null,
    val releaseOrder: Int? = null,
    val chronologicalOrder: Int? = null,
    val phaseOrder: Int? = null,
    val collectionOrder: Int? = null,
    val order: Int? = releaseOrder,
    val watchProviders: List<WatchProvider> = emptyList(),
    val metadataSource: MetadataSource = MetadataSource.LOCAL,
    val lastUpdated: String? = null,
    val status: ViewingStatus = ViewingStatus.RELEASED,
    val awards: String? = null,
    val watched: Boolean = false,
    val favorite: Boolean = false,
    val watchlisted: Boolean = false,
    val notes: String? = null
)

data class ViewingList(
    val id: String,
    val title: String,
    val description: String? = null,
    val universe: String? = null,
    val category: String? = null,
    val poster: String? = null,
    val localPoster: String? = null,
    val backdrop: String? = null,
    val localBackdrop: String? = null,
    val phase: String? = null,
    val saga: String? = null,
    val franchise: String? = null,
    val itemIds: List<String> = emptyList(),
    val items: List<ViewingItem>,
    val sortModes: List<ViewingSortMode> = listOf(
        ViewingSortMode.RELEASE,
        ViewingSortMode.CHRONOLOGICAL,
        ViewingSortMode.PHASE,
        ViewingSortMode.SAGA,
        ViewingSortMode.CUSTOM
    )
)

enum class ViewingType { MOVIE, SERIES, EPISODE, SPECIAL, SHORT, ONE_SHOT }
enum class ViewingStatus { RELEASED, UPCOMING, ANNOUNCED }
enum class TrailerSource { TMDB, YOUTUBE, LOCAL, MANUAL }
enum class ViewingSortMode(val label: String) {
    RELEASE("Release order"),
    CHRONOLOGICAL("Chronological order"),
    PHASE("Phase order"),
    SAGA("Saga order"),
    TITLE("Title"),
    RATING("Rating"),
    RUNTIME("Runtime"),
    CUSTOM("Custom order")
}

data class MetadataResult(
    val item: ViewingItem,
    val source: MetadataSource = MetadataSource.LOCAL,
    val isFallback: Boolean = true,
    val message: String? = null
)

enum class MetadataSource { LOCAL, OMDB, TMDB, MERGED, USER }
