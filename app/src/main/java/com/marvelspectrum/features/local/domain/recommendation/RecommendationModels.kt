package com.marvelspectrum.features.local.domain.recommendation

sealed class RecommendationReason(val label: String) {
    data object SameArtist : RecommendationReason("Same artist")
    data object SameGenre : RecommendationReason("Same genre")
    data object SimilarToCurrentSong : RecommendationReason("Similar to this session")
    data object HindiMix : RecommendationReason("Hindi mix")
    data object EnglishMix : RecommendationReason("English mix")
    data object FavoriteArtist : RecommendationReason("Favorite artist")
    data object ForgottenFavorite : RecommendationReason("Forgotten favorite")
    data object NewDiscovery : RecommendationReason("New discovery")
    data object TimeOfDayMatch : RecommendationReason("Time-of-day match")
    data object PlaylistContinuation : RecommendationReason("Playlist continuation")
    data object FolderContinuation : RecommendationReason("Folder continuation")
}

enum class CandidateSource { SAME_ARTIST, ALBUM_ARTIST, SAME_ALBUM, SAME_GENRE, SAME_LANGUAGE, COMPLEMENTARY_LANGUAGE, SAME_DECADE, SIMILAR_DURATION, FAVORITES_NEIGHBOR, RECENT_ARTIST, UNDERPLAYED_SIMILAR, PLAYLIST_COOCCURRENCE, FOLDER_COOCCURRENCE, TIME_OF_DAY, FORGOTTEN_FAVORITE, NEW_LOCAL_ADDITION }

data class RecommendationCandidate(
    val songId: Long,
    val source: CandidateSource,
    val seedSongId: Long?,
    val initialScore: Double,
    val reason: RecommendationReason
)

data class RankedRecommendation(
    val songId: Long,
    val score: Double,
    val reason: RecommendationReason,
    val debugInfo: RecommendationDebugInfo = RecommendationDebugInfo()
)

data class RecommendationDebugInfo(
    val contentSimilarityScore: Double = 0.0,
    val userAffinityScore: Double = 0.0,
    val sessionContinuityScore: Double = 0.0,
    val languagePreferenceScore: Double = 0.0,
    val freshnessScore: Double = 0.0,
    val discoveryScore: Double = 0.0,
    val playlistCoOccurrenceScore: Double = 0.0,
    val repetitionPenalty: Double = 0.0,
    val recentCooldownPenalty: Double = 0.0,
    val skipPenalty: Double = 0.0,
    val sameArtistOverloadPenalty: Double = 0.0
)

data class UserTasteProfile(
    val favoriteSongIds: Set<Long> = emptySet(),
    val blacklistedSongIds: Set<Long> = emptySet(),
    val blacklistedArtistTokens: Set<String> = emptySet(),
    val blacklistedGenreTokens: Set<String> = emptySet(),
    val sessionPlayedSongIds: Set<Long> = emptySet(),
    val manuallyQueuedSongIds: Set<Long> = emptySet()
)

data class QueueContext(
    val currentSongId: Long?,
    val currentArtistTokens: Set<String> = emptySet(),
    val currentLanguage: TrackLanguage? = null,
    val manualQueueSongIds: Set<Long> = emptySet(),
    val recommendationQueueSongIds: Set<Long> = emptySet(),
    val upcomingLanguages: List<TrackLanguage> = emptyList(),
    val upcomingArtistTokens: List<Set<String>> = emptyList(),
    val sessionPlayedSongIds: Set<Long> = emptySet()
)
