package com.marvelspectrum.features.local.domain.recommendation

class RecommendationDiversifier {
    fun diversify(
        rankedCandidates: List<RankedRecommendation>,
        featureById: Map<Long, TrackFeatureVector>,
        currentQueueContext: QueueContext,
        settings: SmartRecommendationSettings
    ): List<RankedRecommendation> {
        val normalized = settings.normalized()
        val selected = mutableListOf<RankedRecommendation>()
        val seen = mutableSetOf<Long>()
        val languageTrail = currentQueueContext.upcomingLanguages.toMutableList()
        val artistTrail = currentQueueContext.upcomingArtistTokens.toMutableList()

        for (candidate in rankedCandidates) {
            if (selected.size >= normalized.targetQueueSize) break
            if (!seen.add(candidate.songId)) continue
            val feature = featureById[candidate.songId] ?: continue
            if (!languageAllowed(languageTrail, feature.language, normalized)) continue
            if (!artistAllowed(artistTrail, feature.artistTokens, normalized)) continue
            selected += candidate
            languageTrail += feature.language
            artistTrail += feature.artistTokens
        }
        return selected
    }

    private fun languageAllowed(trail: List<TrackLanguage>, candidate: TrackLanguage, settings: SmartRecommendationSettings): Boolean {
        val previous = trail.lastOrNull()
        if (!LanguagePreferenceModel.areCompatible(previous, candidate, settings)) return false
        if (previous == null || candidate == TrackLanguage.UNKNOWN) return true
        val previousFamily = previous.normalizedFamily()
        val candidateFamily = candidate.normalizedFamily()
        if (previousFamily == candidateFamily) return true
        val differentRun = trail.asReversed().takeWhile { it.normalizedFamily() != candidateFamily }.size
        return differentRun < settings.maxDifferentLanguageInARow
    }

    private fun artistAllowed(trail: List<Set<String>>, candidateArtist: Set<String>, settings: SmartRecommendationSettings): Boolean {
        if (candidateArtist.isEmpty()) return true
        if (settings.avoidSameArtistBackToBack && trail.lastOrNull() == candidateArtist) return false
        val window = trail.takeLast(settings.artistWindowSize)
        return window.count { it == candidateArtist } < settings.maxSameArtistInWindow
    }
}
