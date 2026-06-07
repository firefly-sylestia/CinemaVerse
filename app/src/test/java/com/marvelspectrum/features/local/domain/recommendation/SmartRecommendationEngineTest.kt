package com.marvelspectrum.features.local.domain.recommendation

import org.junit.Assert.*
import org.junit.Test

class SmartRecommendationEngineTest {
    @Test fun sameArtistAndGenreCandidatesAppear() {
        val seed = feature(1, artist = "a", genre = "rock")
        val library = listOf(seed, feature(2, artist = "a", genre = "jazz"), feature(3, artist = "b", genre = "rock"))
        val result = SmartRecommendationEngine().recommend(seed, library, settings = SmartRecommendationSettings(enabled = true))
        assertTrue(result.map { it.songId }.containsAll(listOf(2L, 3L)))
    }

    @Test fun hindiEnglishSettingsAreRespected() {
        val seed = feature(1, language = TrackLanguage.HINDI)
        val english = feature(2, genre = "pop", language = TrackLanguage.ENGLISH)
        val hindi = feature(3, genre = "pop", language = TrackLanguage.HINDI)
        val result = SmartRecommendationEngine().recommend(
            seed,
            listOf(seed, english, hindi),
            settings = SmartRecommendationSettings(enabled = true, languageMixMode = LanguageMixMode.SAME_LANGUAGE, allowEnglishBetweenHindi = false)
        )
        assertFalse(result.map { it.songId }.contains(2L))
        assertTrue(result.map { it.songId }.contains(3L))
    }

    @Test fun recentCooldownExcludesRecentlyPlayedSongFromUsefulRanking() {
        val now = 10_000_000L
        val seed = feature(1, artist = "a")
        val recent = feature(2, artist = "a", lastPlayedAt = now - 10_000L)
        val old = feature(3, artist = "a", lastPlayedAt = now - 10_000_000L)
        val result = SmartRecommendationEngine().recommend(seed, listOf(seed, recent, old), settings = SmartRecommendationSettings(enabled = true), nowMillis = now)
        assertTrue(result.first().songId == 3L)
    }
}
