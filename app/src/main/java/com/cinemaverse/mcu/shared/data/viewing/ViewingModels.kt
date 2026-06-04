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
    val userStatuses: Set<ViewingUserStatus> = emptySet(),
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
    val importance: ViewingListImportance = ViewingListImportance.SECONDARY,
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
enum class TrailerSource { TMDB, YOUTUBE, LOCAL, MANUAL, OMDB }
enum class ViewingListImportance { PRIMARY, SECONDARY, NICHE, HIDDEN }
enum class ViewingUserStatus(val activeLabel: String, val inactiveLabel: String, val libraryTitle: String) {
    BOOKMARKED("Bookmarked", "Bookmark", "Bookmarks"),
    WATCHLIST("In watchlist", "Add to watchlist", "Watchlist"),
    WATCH_LATER("Saved for later", "Watch later", "Watch later"),
    ON_HOLD("On hold", "Put on hold", "On hold"),
    WATCHING("Continue watching", "Start watching", "Continue"),
    WATCHED("Watched", "Mark watched", "Watched"),
    FAVORITE("Favorite", "Favorite", "Favorites"),
    HIDDEN("Not interested", "Hide", "Hidden")
}
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

enum class MetadataProviderMode(val label: String, val description: String) {
    OMDB_PRIMARY_TMDB_FALLBACK(
        "OMDb main • TMDB fallback",
        "Use OMDb posters/IMDb-style details first, then fill missing posters, backdrops, trailers, and cinema metadata from TMDB."
    ),
    TMDB_PRIMARY_OMDB_FALLBACK(
        "TMDB main • OMDb fallback",
        "Use TMDB posters/backdrops/trailers first, then fill missing posters and IMDb-style details from OMDb."
    );
}

enum class ViewingSearchCategory(val label: String) {
    ESSENTIAL("Essential"),
    RELEASE_ORDER("Release order"),
    TIMELINE("Timeline"),
    PHASES("Phases"),
    SAGAS("Sagas"),
    COLLECTIONS("Collections"),
    SPECIALS("Specials"),
    SERIES("Series"),
    UPCOMING("Upcoming"),
    SAVED("Saved")
}

enum class ViewingSearchSortMode(val label: String) {
    RELEVANCE("Relevance"),
    RELEASE_DATE("Release date"),
    CHRONOLOGICAL("Chronological"),
    RATING("Rating"),
    RUNTIME("Runtime"),
    TITLE("Title")
}

data class ViewingSearchFilters(
    val query: String = "",
    val universes: Set<String> = emptySet(),
    val types: Set<ViewingType> = emptySet(),
    val genres: Set<String> = emptySet(),
    val categories: Set<ViewingSearchCategory> = emptySet(),
    val statuses: Set<ViewingUserStatus> = emptySet(),
    val sortMode: ViewingSearchSortMode = ViewingSearchSortMode.RELEVANCE
)
