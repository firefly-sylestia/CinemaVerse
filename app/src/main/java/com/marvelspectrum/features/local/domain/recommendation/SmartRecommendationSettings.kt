package com.marvelspectrum.features.local.domain.recommendation

data class SmartRecommendationSettings(
    val enabled: Boolean = false,
    val offlineOnly: Boolean = true,
    val onlineMetadataEnrichmentEnabled: Boolean = false,
    val targetQueueSize: Int = DEFAULT_TARGET_QUEUE_SIZE,
    val refillThreshold: Int = DEFAULT_REFILL_THRESHOLD,
    val recentCooldownMinutes: Int = DEFAULT_RECENT_COOLDOWN_MINUTES,
    val resetCooldownOnSessionEnd: Boolean = true,
    val languageMixMode: LanguageMixMode = LanguageMixMode.BALANCED,
    val allowEnglishBetweenHindi: Boolean = true,
    val allowHindiBetweenEnglish: Boolean = true,
    val preferSameLanguageAsSeed: Boolean = true,
    val maxDifferentLanguageInARow: Int = 2,
    val discoveryLevel: DiscoveryLevel = DiscoveryLevel.MEDIUM,
    val familiarityLevel: FamiliarityLevel = FamiliarityLevel.BALANCED,
    val diversityStrength: DiversityStrength = DiversityStrength.MEDIUM,
    val avoidSameArtistBackToBack: Boolean = true,
    val maxSameArtistInWindow: Int = 2,
    val artistWindowSize: Int = 6,
    val explicitContentAllowed: Boolean = true,
    val includeLowConfidenceLanguageTracks: Boolean = true
) {
    fun normalized(): SmartRecommendationSettings {
        val safeTarget = targetQueueSize.coerceIn(MIN_TARGET_QUEUE_SIZE, MAX_TARGET_QUEUE_SIZE)
        return copy(
            targetQueueSize = safeTarget,
            refillThreshold = refillThreshold.coerceIn(1, (safeTarget - 1).coerceAtLeast(1)),
            recentCooldownMinutes = recentCooldownMinutes.coerceAtLeast(0),
            maxDifferentLanguageInARow = maxDifferentLanguageInARow.coerceAtLeast(1),
            maxSameArtistInWindow = maxSameArtistInWindow.coerceAtLeast(1),
            artistWindowSize = artistWindowSize.coerceAtLeast(2)
        )
    }

    companion object {
        const val DEFAULT_TARGET_QUEUE_SIZE = 20
        const val MIN_TARGET_QUEUE_SIZE = 5
        const val MAX_TARGET_QUEUE_SIZE = 30
        const val DEFAULT_REFILL_THRESHOLD = 8
        const val DEFAULT_RECENT_COOLDOWN_MINUTES = 120
    }
}

enum class LanguageMixMode { SAME_LANGUAGE, BALANCED, MIX_ENGLISH_HINDI, DISCOVERY }
enum class DiscoveryLevel { LOW, MEDIUM, HIGH }
enum class FamiliarityLevel { FAMILIAR, BALANCED, DISCOVERY }
enum class DiversityStrength { LOW, MEDIUM, HIGH }
