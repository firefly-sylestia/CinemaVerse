package com.marvelspectrum.features.local.domain.recommendation

import org.junit.Assert.*
import org.junit.Test

class LanguagePreferenceModelTest {
    @Test fun detectsDevanagariAsHindi() {
        assertEquals(TrackLanguage.HINDI, LanguagePreferenceModel.predictText("दिल से").language)
    }

    @Test fun detectsHindiTransliterationWithoutOverconfidence() {
        val prediction = LanguagePreferenceModel.predictText("dil mera ishq")
        assertEquals(TrackLanguage.HINDI_TRANSLITERATED, prediction.language)
        assertTrue(prediction.confidence < 0.8f)
    }

    @Test fun languageMixFlagsBlockEnglishBetweenHindi() {
        val settings = SmartRecommendationSettings(allowEnglishBetweenHindi = false)
        assertFalse(LanguagePreferenceModel.areCompatible(TrackLanguage.HINDI, TrackLanguage.ENGLISH, settings))
    }
}
