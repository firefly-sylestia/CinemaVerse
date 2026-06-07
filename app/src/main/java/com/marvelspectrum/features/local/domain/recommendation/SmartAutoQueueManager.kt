package com.marvelspectrum.features.local.domain.recommendation

data class QueueLaneState(
    val currentItem: Long? = null,
    val manualPlayNextItems: List<Long> = emptyList(),
    val manualQueueItems: List<Long> = emptyList(),
    val smartRecommendationItems: List<Long> = emptyList(),
    val contextQueueItems: List<Long> = emptyList()
) {
    fun flattened(): List<Long> = buildList {
        currentItem?.let(::add)
        addAll(manualPlayNextItems)
        addAll(manualQueueItems)
        addAll(smartRecommendationItems)
        addAll(contextQueueItems)
    }
}

data class SmartQueueUpdate(
    val laneState: QueueLaneState,
    val recommendationsToAdd: List<Long>,
    val shouldMutatePlayerQueue: Boolean = recommendationsToAdd.isNotEmpty()
)

class SmartAutoQueueManager(
    private val engine: SmartRecommendationEngine = SmartRecommendationEngine()
) {
    private var sessionCooldownSongIds: Set<Long> = emptySet()

    fun playNext(songId: Long, state: QueueLaneState): QueueLaneState {
        val smartWithoutDuplicate = state.smartRecommendationItems.filterNot { it == songId }
        return state.copy(
            manualPlayNextItems = state.manualPlayNextItems + songId,
            smartRecommendationItems = smartWithoutDuplicate
        )
    }

    fun addToQueue(songId: Long, state: QueueLaneState): QueueLaneState {
        val smartWithoutDuplicate = state.smartRecommendationItems.filterNot { it == songId }
        return state.copy(
            manualQueueItems = state.manualQueueItems + songId,
            smartRecommendationItems = smartWithoutDuplicate
        )
    }

    fun onSessionEnded(settings: SmartRecommendationSettings) {
        if (settings.resetCooldownOnSessionEnd) sessionCooldownSongIds = emptySet()
    }

    fun markPlayed(songId: Long) {
        sessionCooldownSongIds = sessionCooldownSongIds + songId
    }

    fun stopSmartQueue(state: QueueLaneState, removeExistingRecommendations: Boolean = true): QueueLaneState =
        if (removeExistingRecommendations) state.copy(smartRecommendationItems = emptyList()) else state

    fun ensureRecommendations(
        seed: TrackFeatureVector,
        library: List<TrackFeatureVector>,
        state: QueueLaneState,
        settings: SmartRecommendationSettings,
        tasteProfile: UserTasteProfile = UserTasteProfile(),
        nowMillis: Long = System.currentTimeMillis()
    ): SmartQueueUpdate {
        val normalized = settings.normalized()
        if (!normalized.enabled) return SmartQueueUpdate(stopSmartQueue(state), emptyList(), false)
        if (state.smartRecommendationItems.size > normalized.refillThreshold) {
            return SmartQueueUpdate(state.copy(smartRecommendationItems = state.smartRecommendationItems.take(normalized.targetQueueSize)), emptyList(), false)
        }
        val needed = normalized.targetQueueSize - state.smartRecommendationItems.size
        if (needed <= 0) return SmartQueueUpdate(state, emptyList(), false)

        val manualIds = state.manualPlayNextItems.toSet() + state.manualQueueItems.toSet()
        val context = QueueContext(
            currentSongId = state.currentItem ?: seed.songId,
            currentArtistTokens = seed.artistTokens,
            currentLanguage = seed.language,
            manualQueueSongIds = manualIds,
            recommendationQueueSongIds = state.smartRecommendationItems.toSet(),
            upcomingLanguages = state.smartRecommendationItems.mapNotNull { id -> library.firstOrNull { it.songId == id }?.language },
            upcomingArtistTokens = state.smartRecommendationItems.mapNotNull { id -> library.firstOrNull { it.songId == id }?.artistTokens },
            sessionPlayedSongIds = sessionCooldownSongIds
        )
        val recommendations = engine.recommend(
            seed = seed,
            library = library,
            queueContext = context,
            settings = normalized,
            tasteProfile = tasteProfile.copy(manuallyQueuedSongIds = manualIds, sessionPlayedSongIds = sessionCooldownSongIds),
            nowMillis = nowMillis
        ).map { it.songId }.filterNot { it in manualIds || it in state.smartRecommendationItems || it == seed.songId }.take(needed)
        val nextState = state.copy(smartRecommendationItems = (state.smartRecommendationItems + recommendations).take(normalized.targetQueueSize))
        return SmartQueueUpdate(nextState, recommendations)
    }
}
