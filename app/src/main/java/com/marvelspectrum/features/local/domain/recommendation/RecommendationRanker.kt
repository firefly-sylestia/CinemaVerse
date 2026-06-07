package com.marvelspectrum.features.local.domain.recommendation

import kotlin.math.ln

class RecommendationRanker {
    private companion object { const val MIN_RECOMMENDABLE_SCORE = -50.0 }
    fun rank(
        seed: TrackFeatureVector,
        candidates: List<RecommendationCandidate>,
        featureById: Map<Long, TrackFeatureVector>,
        queueContext: QueueContext,
        settings: SmartRecommendationSettings,
        tasteProfile: UserTasteProfile = UserTasteProfile(),
        nowMillis: Long = System.currentTimeMillis()
    ): List<RankedRecommendation> {
        val normalized = settings.normalized()
        return candidates.mapNotNull { candidate ->
            val track = featureById[candidate.songId] ?: return@mapNotNull null
            val debug = score(seed, track, candidate, queueContext, normalized, tasteProfile, nowMillis)
            val finalScore = debug.contentSimilarityScore + debug.userAffinityScore + debug.sessionContinuityScore +
                debug.languagePreferenceScore + debug.freshnessScore + debug.discoveryScore + debug.playlistCoOccurrenceScore -
                debug.repetitionPenalty - debug.recentCooldownPenalty - debug.skipPenalty - debug.sameArtistOverloadPenalty
            RankedRecommendation(candidate.songId, finalScore, candidate.reason, debug)
        }.filter { it.score > MIN_RECOMMENDABLE_SCORE }
            .sortedWith(compareByDescending<RankedRecommendation> { it.score }.thenBy { it.songId })
    }

    private fun score(
        seed: TrackFeatureVector,
        track: TrackFeatureVector,
        candidate: RecommendationCandidate,
        queueContext: QueueContext,
        settings: SmartRecommendationSettings,
        tasteProfile: UserTasteProfile,
        nowMillis: Long
    ): RecommendationDebugInfo {
        if (track.songId in tasteProfile.blacklistedSongIds) {
            return RecommendationDebugInfo(repetitionPenalty = Double.POSITIVE_INFINITY)
        }
        val recentCooldownPenalty = track.lastPlayedAt?.let { lastPlayed ->
            val cooldownMs = settings.recentCooldownMinutes * 60_000L
            if (cooldownMs > 0 && nowMillis - lastPlayed < cooldownMs) 1_000.0 else 0.0
        } ?: 0.0
        val content = candidate.initialScore + tokenOverlap(seed.artistTokens, track.artistTokens) * 4.0 +
            tokenOverlap(seed.genreTokens, track.genreTokens) * 2.5 + tokenOverlap(seed.albumTokens, track.albumTokens) * 1.2 +
            if (seed.decade != null && seed.decade == track.decade) 0.8 else 0.0
        val affinity = (if (track.isFavorite || track.songId in tasteProfile.favoriteSongIds) 3.0 else 0.0) +
            ln(track.playCount.coerceAtLeast(0) + 1.0) - (track.skipCount * 0.55)
        val continuity = if (track.songId !in queueContext.recommendationQueueSongIds) 0.4 else -10.0
        val language = languageScore(seed, track, settings)
        val freshness = track.addedAt?.let { if (nowMillis - it < 30L * 24 * 60 * 60 * 1000) 0.8 else 0.0 } ?: 0.0
        val discovery = when (settings.discoveryLevel) {
            DiscoveryLevel.LOW -> if (track.playCount > 2 || track.isFavorite) 0.9 else -0.8
            DiscoveryLevel.MEDIUM -> if (track.playCount <= 2) 0.5 else 0.2
            DiscoveryLevel.HIGH -> if (track.playCount <= 1) 1.8 else -0.2
        }
        val playlist = if (seed.playlistIds.intersect(track.playlistIds).isNotEmpty()) 1.6 else 0.0
        val repetition = if (track.songId == seed.songId || track.songId in queueContext.manualQueueSongIds) 1_000.0 else 0.0
        val skipPenalty = (track.skipCount * 0.7) + (track.lastSkippedAt?.let { if (nowMillis - it < 6L * 60 * 60 * 1000) 2.0 else 0.0 } ?: 0.0)
        val artistOverload = if (queueContext.upcomingArtistTokens.take(settings.artistWindowSize).count { it == track.artistTokens } >= settings.maxSameArtistInWindow) 4.0 else 0.0
        return RecommendationDebugInfo(content, affinity, continuity, language, freshness, discovery, playlist, repetition, recentCooldownPenalty, skipPenalty, artistOverload)
    }

    private fun languageScore(seed: TrackFeatureVector, track: TrackFeatureVector, settings: SmartRecommendationSettings): Double {
        val seedFamily = seed.language.normalizedFamily()
        val trackFamily = track.language.normalizedFamily()
        if (!settings.includeLowConfidenceLanguageTracks && track.languageConfidence < 0.45f) return -2.5
        if (seed.language.isHindiFamily() && track.language == TrackLanguage.ENGLISH && !settings.allowEnglishBetweenHindi) return -100.0
        if (seed.language == TrackLanguage.ENGLISH && track.language.isHindiFamily() && !settings.allowHindiBetweenEnglish) return -100.0
        return when (settings.languageMixMode) {
            LanguageMixMode.SAME_LANGUAGE -> if (seedFamily == trackFamily) 3.5 else -5.0
            LanguageMixMode.BALANCED -> if (settings.preferSameLanguageAsSeed && seedFamily == trackFamily) 1.8 else 0.4
            LanguageMixMode.MIX_ENGLISH_HINDI -> when {
                seed.language.isHindiFamily() && track.language == TrackLanguage.ENGLISH -> 1.2
                seed.language == TrackLanguage.ENGLISH && track.language.isHindiFamily() -> 1.2
                seedFamily == trackFamily -> 1.6
                else -> 0.0
            }
            LanguageMixMode.DISCOVERY -> if (seedFamily != trackFamily && track.language != TrackLanguage.UNKNOWN) 1.0 else 0.6
        } * track.languageConfidence.coerceIn(0.25f, 1f)
    }

    private fun tokenOverlap(left: Set<String>, right: Set<String>): Double {
        if (left.isEmpty() || right.isEmpty()) return 0.0
        return left.intersect(right).size.toDouble() / left.union(right).size.toDouble()
    }
}
