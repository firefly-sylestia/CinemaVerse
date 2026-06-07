package com.marvelspectrum.features.local.domain.recommendation

import org.junit.Assert.*
import org.junit.Test

class RecommendationDiversifierTest {
    @Test fun removesDuplicatesAndAvoidsSameArtistBackToBack() {
        val a2 = feature(2, artist = "a")
        val a3 = feature(3, artist = "a")
        val b4 = feature(4, artist = "b")
        val ranked = listOf(2L, 2L, 3L, 4L).mapIndexed { i, id -> RankedRecommendation(id, 100.0 - i, RecommendationReason.SimilarToCurrentSong) }
        val result = RecommendationDiversifier().diversify(ranked, listOf(a2, a3, b4).associateBy { it.songId }, QueueContext(1), SmartRecommendationSettings(targetQueueSize = 10))
        assertEquals(listOf(2L, 4L), result.map { it.songId })
    }

    @Test fun languageMixingObeysAllowEnglishBetweenHindi() {
        val english = feature(2, language = TrackLanguage.ENGLISH)
        val hindi = feature(3, language = TrackLanguage.HINDI)
        val result = RecommendationDiversifier().diversify(
            listOf(RankedRecommendation(2, 10.0, RecommendationReason.EnglishMix), RankedRecommendation(3, 9.0, RecommendationReason.HindiMix)),
            listOf(english, hindi).associateBy { it.songId },
            QueueContext(1, upcomingLanguages = listOf(TrackLanguage.HINDI)),
            SmartRecommendationSettings(allowEnglishBetweenHindi = false)
        )
        assertFalse(result.map { it.songId }.contains(2L))
    }

    @Test fun outputIsDeterministic() {
        val tracks = (2L..10L).map { feature(it, artist = "a$it") }
        val ranked = tracks.map { RankedRecommendation(it.songId, 1.0, RecommendationReason.NewDiscovery) }
        val diversifier = RecommendationDiversifier()
        assertEquals(
            diversifier.diversify(ranked, tracks.associateBy { it.songId }, QueueContext(1), SmartRecommendationSettings()).map { it.songId },
            diversifier.diversify(ranked, tracks.associateBy { it.songId }, QueueContext(1), SmartRecommendationSettings()).map { it.songId }
        )
    }
}
