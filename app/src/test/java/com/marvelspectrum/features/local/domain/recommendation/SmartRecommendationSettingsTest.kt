package com.marvelspectrum.features.local.domain.recommendation

import org.junit.Assert.*
import org.junit.Test

class SmartRecommendationSettingsTest {
    @Test fun defaultsAreOfflineAndConservative() {
        val settings = SmartRecommendationSettings()
        assertFalse(settings.enabled)
        assertTrue(settings.offlineOnly)
        assertFalse(settings.onlineMetadataEnrichmentEnabled)
        assertEquals(20, settings.targetQueueSize)
        assertEquals(8, settings.refillThreshold)
        assertEquals(120, settings.recentCooldownMinutes)
    }

    @Test fun targetAndThresholdClampCorrectly() {
        val settings = SmartRecommendationSettings(targetQueueSize = 99, refillThreshold = 99).normalized()
        assertEquals(30, settings.targetQueueSize)
        assertEquals(29, settings.refillThreshold)
    }
}
