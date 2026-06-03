package com.cinemaverse.mcu.features.local.presentation.viewmodel

import com.cinemaverse.mcu.features.local.data.database.entity.MCUTitleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the "Continue Watching" viewing queue for MCU titles.
 * Adapted from QueueStateHolder to work with MCU viewing instead of music playback.
 * 
 * Maintains viewing order and resume positions for titles being watched.
 */
class ViewingQueueHolder {

    /**
     * The original viewing order before reordering.
     * Preserved so users can restore the default viewing order.
     */
    private val _originalViewingOrder = MutableStateFlow<List<MCUTitleEntity>>(emptyList())
    val originalViewingOrder: List<MCUTitleEntity>
        get() = _originalViewingOrder.value

    /**
     * The source of the current viewing queue (e.g., "Saga: Infinity Saga", "Phase: Phase Three").
     * Used for display and persistence.
     */
    private val _currentQueueSourceName = MutableStateFlow<String?>(null)
    val currentQueueSourceName: StateFlow<String?> = _currentQueueSourceName.asStateFlow()

    /**
     * Current watching position in the queue (0-based index).
     */
    private val _currentWatchingIndex = MutableStateFlow<Int>(0)
    val currentWatchingIndex: StateFlow<Int> = _currentWatchingIndex.asStateFlow()

    /**
     * Whether an original viewing order is saved.
     */
    fun hasOriginalQueue(): Boolean = _originalViewingOrder.value.isNotEmpty()

    /**
     * Sets the original viewing order before any user reordering.
     */
    fun setOriginalQueueOrder(titles: List<MCUTitleEntity>) {
        _originalViewingOrder.value = titles.toList()
    }

    /**
     * Saves complete viewing queue state including source and order.
     */
    fun saveOriginalQueueState(titles: List<MCUTitleEntity>, sourceName: String?) {
        setOriginalQueueOrder(titles)
        _currentQueueSourceName.value = sourceName
    }

    /**
     * Clears the saved viewing order and restores defaults.
     */
    fun clearOriginalQueue() {
        _originalViewingOrder.value = emptyList()
        _currentQueueSourceName.value = null
        _currentWatchingIndex.value = 0
    }

    /**
     * Gets filtered viewing order, removing any titles no longer in queue.
     */
    fun getFilteredOriginalQueue(currentTitles: List<MCUTitleEntity>): List<MCUTitleEntity> {
        if (_originalViewingOrder.value.isEmpty()) return currentTitles

        val currentIds = currentTitles.map { it.id }.toSet()
        return _originalViewingOrder.value.filter { it.id in currentIds }
    }

    /**
     * Updates the source name (saga, phase, custom list, etc.)
     */
    fun setQueueSourceName(sourceName: String?) {
        _currentQueueSourceName.value = sourceName
    }

    /**
     * Sets current position in the continue watching queue.
     */
    fun setCurrentWatchingIndex(index: Int) {
        _currentWatchingIndex.value = index.coerceIn(0, Int.MAX_VALUE)
    }

    /**
     * Move to next title in queue.
     */
    fun nextTitle() {
        _currentWatchingIndex.value++
    }

    /**
     * Move to previous title in queue.
     */
    fun previousTitle() {
        if (_currentWatchingIndex.value > 0) {
            _currentWatchingIndex.value--
        }
    }
}
