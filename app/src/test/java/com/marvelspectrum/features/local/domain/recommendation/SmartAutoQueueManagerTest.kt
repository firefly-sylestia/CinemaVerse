package com.marvelspectrum.features.local.domain.recommendation

import org.junit.Assert.*
import org.junit.Test

class SmartAutoQueueManagerTest {
    @Test fun maintainsTwentyRecommendationItemsAndRefillsBelowThreshold() {
        val seed = feature(1, artist = "seed")
        val library = listOf(seed) + (2L..40L).map { feature(it, genre = "pop", artist = "a$it") }
        val state = QueueLaneState(currentItem = 1, smartRecommendationItems = (2L..7L).toList())
        val update = SmartAutoQueueManager().ensureRecommendations(seed, library, state, SmartRecommendationSettings(enabled = true))
        assertEquals(20, update.laneState.smartRecommendationItems.size)
        assertTrue(update.recommendationsToAdd.isNotEmpty())
    }

    @Test fun doesNotRefillAboveTarget() {
        val seed = feature(1)
        val state = QueueLaneState(currentItem = 1, smartRecommendationItems = (2L..21L).toList())
        val update = SmartAutoQueueManager().ensureRecommendations(seed, listOf(seed), state, SmartRecommendationSettings(enabled = true))
        assertEquals(20, update.laneState.smartRecommendationItems.size)
        assertTrue(update.recommendationsToAdd.isEmpty())
    }

    @Test fun manualActionsStayBeforeRecommendations() {
        val manager = SmartAutoQueueManager()
        val state = QueueLaneState(currentItem = 1, smartRecommendationItems = listOf(2, 3, 4))
        val afterPlayNext = manager.playNext(5, state)
        val afterAdd = manager.addToQueue(6, afterPlayNext)
        assertEquals(listOf(1L, 5L, 6L, 2L, 3L, 4L), afterAdd.flattened())
    }

    @Test fun turningOffSmartQueueRemovesRecommendationLane() {
        val state = QueueLaneState(currentItem = 1, manualQueueItems = listOf(9), smartRecommendationItems = listOf(2, 3))
        val stopped = SmartAutoQueueManager().stopSmartQueue(state)
        assertEquals(listOf(1L, 9L), stopped.flattened())
    }
}
