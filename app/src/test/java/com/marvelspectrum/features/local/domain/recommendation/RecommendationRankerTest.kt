package com.marvelspectrum.features.local.domain.recommendation

import org.junit.Assert.*
import org.junit.Test

class RecommendationRankerTest {
    @Test fun favoritesReceiveBoostAndSkippedSongsArePenalized() {
        val seed = feature(1, artist = "a")
        val favorite = feature(2, artist = "a", favorite = true)
        val skipped = feature(3, artist = "a", skipCount = 10)
        val candidates = RecommendationCandidateGenerator().generate(seed, listOf(seed, favorite, skipped), QueueContext(1), SmartRecommendationSettings(enabled = true))
        val ranked = RecommendationRanker().rank(seed, candidates, listOf(seed, favorite, skipped).associateBy { it.songId }, QueueContext(1), SmartRecommendationSettings(enabled = true))
        assertEquals(2L, ranked.first().songId)
        assertTrue(ranked.first { it.songId == 3L }.debugInfo.skipPenalty > 0.0)
    }

    @Test fun discoveryModeBoostsUnderplayedTracks() {
        val seed = feature(1, artist = "a")
        val familiar = feature(2, artist = "a", playCount = 50)
        val underplayed = feature(3, artist = "a", playCount = 0)
        val settings = SmartRecommendationSettings(enabled = true, discoveryLevel = DiscoveryLevel.HIGH)
        val candidates = RecommendationCandidateGenerator().generate(seed, listOf(seed, familiar, underplayed), QueueContext(1), settings)
        val ranked = RecommendationRanker().rank(seed, candidates, listOf(seed, familiar, underplayed).associateBy { it.songId }, QueueContext(1), settings)
        assertTrue(ranked.first { it.songId == 3L }.debugInfo.discoveryScore > ranked.first { it.songId == 2L }.debugInfo.discoveryScore)
    }
}
