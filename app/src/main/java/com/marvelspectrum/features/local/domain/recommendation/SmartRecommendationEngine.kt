package com.marvelspectrum.features.local.domain.recommendation

class SmartRecommendationEngine(
    private val candidateGenerator: RecommendationCandidateGenerator = RecommendationCandidateGenerator(),
    private val ranker: RecommendationRanker = RecommendationRanker(),
    private val diversifier: RecommendationDiversifier = RecommendationDiversifier()
) {
    fun recommend(
        seed: TrackFeatureVector,
        library: List<TrackFeatureVector>,
        queueContext: QueueContext = QueueContext(currentSongId = seed.songId),
        settings: SmartRecommendationSettings = SmartRecommendationSettings(),
        tasteProfile: UserTasteProfile = UserTasteProfile(),
        nowMillis: Long = System.currentTimeMillis()
    ): List<RankedRecommendation> {
        val normalized = settings.normalized()
        if (!normalized.enabled) return emptyList()
        val featureById = library.associateBy { it.songId }
        val candidates = candidateGenerator.generate(seed, library, queueContext, normalized, nowMillis)
        val ranked = ranker.rank(seed, candidates, featureById, queueContext, normalized, tasteProfile, nowMillis)
        return diversifier.diversify(ranked, featureById, queueContext, normalized)
    }
}
