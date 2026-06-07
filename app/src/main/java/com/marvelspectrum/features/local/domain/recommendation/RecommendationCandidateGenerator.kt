package com.marvelspectrum.features.local.domain.recommendation

class RecommendationCandidateGenerator {
    fun generate(
        seed: TrackFeatureVector,
        library: List<TrackFeatureVector>,
        queueContext: QueueContext,
        settings: SmartRecommendationSettings,
        nowMillis: Long = System.currentTimeMillis()
    ): List<RecommendationCandidate> {
        val normalized = settings.normalized()
        val excluded = buildSet {
            seed.songId.let(::add)
            addAll(queueContext.manualQueueSongIds)
            addAll(queueContext.recommendationQueueSongIds)
            addAll(queueContext.sessionPlayedSongIds)
        }
        val candidates = linkedMapOf<Long, RecommendationCandidate>()
        library.asSequence().filter { it.songId !in excluded }.forEach { track ->
            sourceFor(seed, track, normalized, nowMillis)?.let { (source, score, reason) ->
                val existing = candidates[track.songId]
                if (existing == null || score > existing.initialScore) {
                    candidates[track.songId] = RecommendationCandidate(track.songId, source, seed.songId, score, reason)
                }
            }
        }
        return candidates.values.sortedWith(compareByDescending<RecommendationCandidate> { it.initialScore }.thenBy { it.songId })
    }

    private fun sourceFor(
        seed: TrackFeatureVector,
        track: TrackFeatureVector,
        settings: SmartRecommendationSettings,
        nowMillis: Long
    ): Triple<CandidateSource, Double, RecommendationReason>? {
        val sameArtist = seed.artistTokens.isNotEmpty() && seed.artistTokens == track.artistTokens
        if (sameArtist && track.isFavorite) return Triple(CandidateSource.FAVORITES_NEIGHBOR, 8.5, RecommendationReason.FavoriteArtist)
        if (sameArtist) return Triple(CandidateSource.SAME_ARTIST, 8.0, RecommendationReason.SameArtist)
        if (seed.albumArtistTokens.isNotEmpty() && seed.albumArtistTokens.intersect(track.albumArtistTokens).isNotEmpty()) {
            return Triple(CandidateSource.ALBUM_ARTIST, 7.2, RecommendationReason.SameArtist)
        }
        if (seed.genreTokens.isNotEmpty() && seed.genreTokens.intersect(track.genreTokens).isNotEmpty()) {
            return Triple(CandidateSource.SAME_GENRE, 6.0, RecommendationReason.SameGenre)
        }
        if (seed.playlistIds.isNotEmpty() && seed.playlistIds.intersect(track.playlistIds).isNotEmpty()) {
            return Triple(CandidateSource.PLAYLIST_COOCCURRENCE, 5.8, RecommendationReason.PlaylistContinuation)
        }
        if (seed.folderTokens.isNotEmpty() && seed.folderTokens.intersect(track.folderTokens).isNotEmpty()) {
            return Triple(CandidateSource.FOLDER_COOCCURRENCE, 5.2, RecommendationReason.FolderContinuation)
        }
        if (track.isFavorite && track.lastPlayedAt?.let { nowMillis - it > 14L * 24 * 60 * 60 * 1000 } != false) {
            return Triple(CandidateSource.FORGOTTEN_FAVORITE, 5.1, RecommendationReason.ForgottenFavorite)
        }
        if (seed.language.normalizedFamily() == track.language.normalizedFamily()) {
            return Triple(CandidateSource.SAME_LANGUAGE, 4.8, RecommendationReason.SimilarToCurrentSong)
        }
        if (settings.languageMixMode == LanguageMixMode.MIX_ENGLISH_HINDI && seed.language.isHindiFamily() && track.language == TrackLanguage.ENGLISH) {
            return Triple(CandidateSource.COMPLEMENTARY_LANGUAGE, 4.2, RecommendationReason.EnglishMix)
        }
        if (settings.languageMixMode == LanguageMixMode.MIX_ENGLISH_HINDI && seed.language == TrackLanguage.ENGLISH && track.language.isHindiFamily()) {
            return Triple(CandidateSource.COMPLEMENTARY_LANGUAGE, 4.2, RecommendationReason.HindiMix)
        }
        if (seed.decade != null && seed.decade == track.decade) {
            return Triple(CandidateSource.SAME_DECADE, 3.2, RecommendationReason.SimilarToCurrentSong)
        }
        if (seed.durationBucket == track.durationBucket) {
            return Triple(CandidateSource.SIMILAR_DURATION, 2.0, RecommendationReason.SimilarToCurrentSong)
        }
        if (settings.discoveryLevel == DiscoveryLevel.HIGH && track.playCount <= 1) {
            return Triple(CandidateSource.UNDERPLAYED_SIMILAR, 2.8, RecommendationReason.NewDiscovery)
        }
        return null
    }
}
