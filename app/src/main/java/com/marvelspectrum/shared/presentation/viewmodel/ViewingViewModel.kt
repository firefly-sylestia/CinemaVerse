package com.marvelspectrum.shared.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.marvelspectrum.shared.data.service.ViewingMetadataStore
import com.marvelspectrum.shared.data.viewing.McuAssetDataSource
import com.marvelspectrum.shared.data.viewing.ViewingItem
import com.marvelspectrum.shared.data.viewing.ViewingList
import com.marvelspectrum.shared.data.viewing.ViewingListImportance
import com.marvelspectrum.shared.data.viewing.ViewingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

/**
 * Loads the Cinemaverse catalog and viewing preference cache outside composition.
 */
data class ViewingUiState(
    val isLoading: Boolean = false,
    val data: McuAssetDataSource.ViewingAssetData? = null,
    val errorMessage: String? = null,
    val homeFeaturedTitles: List<ViewingItem> = emptyList(),
    val homeMarvelItems: List<ViewingItem> = emptyList(),
    val homeDcItems: List<ViewingItem> = emptyList(),
    val homeTrailerItems: List<ViewingItem> = emptyList(),
    val homeUpcomingItems: List<ViewingItem> = emptyList(),
    val visibleManagedLists: List<ViewingList> = emptyList(),
    val libraryGenres: List<String> = emptyList(),
    val libraryMcuItems: List<ViewingItem> = emptyList(),
    val libraryDcItems: List<ViewingItem> = emptyList(),
    val libraryTimelineItems: List<ViewingItem> = emptyList()
)

class ViewingViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ViewingUiState())
    val uiState: StateFlow<ViewingUiState> = _uiState.asStateFlow()

    init {
        loadViewingData()
    }

    fun loadViewingData(forceRefresh: Boolean = false) {
        if (!forceRefresh && (_uiState.value.isLoading || _uiState.value.data != null)) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            runCatching {
                supervisorScope {
                    val context = getApplication<Application>()
                    val metadata = async(Dispatchers.IO) {
                        ViewingMetadataStore.initializeAsync(context)
                    }
                    val catalog = async {
                        McuAssetDataSource.loadAsync(context, Dispatchers.Default)
                    }

                    val data = catalog.await()
                    metadata.await()
                    buildLoadedState(data)
                }
            }.onSuccess { loadedState ->
                _uiState.value = loadedState
            }.onFailure { error ->
                _uiState.value = ViewingUiState(
                    isLoading = false,
                    data = _uiState.value.data,
                    errorMessage = error.message ?: "Unable to load Cinemaverse."
                )
            }
        }
    }

    private suspend fun buildLoadedState(data: McuAssetDataSource.ViewingAssetData): ViewingUiState =
        withContext(Dispatchers.Default) {
            val visibleManagedLists = data.allLists.visibleManagedLists()
            ViewingUiState(
                isLoading = false,
                data = data,
                homeFeaturedTitles = data.homeFeaturedTitles(),
                homeMarvelItems = data.allItems.filter { it.universe == "MCU" }.take(14),
                homeDcItems = data.allItems.filter { it.universe in dcUniverses }.take(14),
                homeTrailerItems = data.allItems.filter { it.hasAnyTrailer() }.take(16),
                homeUpcomingItems = data.allItems.filter { it.status == ViewingStatus.UPCOMING || it.status == ViewingStatus.ANNOUNCED }.take(14),
                visibleManagedLists = visibleManagedLists,
                libraryGenres = data.allItems.flatMap { it.genres }.distinct().sorted(),
                libraryMcuItems = data.allItems.filter { it.universe in setOf("MCU", "Marvel") },
                libraryDcItems = data.allItems.filter { it.universe in dcUniverses },
                libraryTimelineItems = data.allItems.sortedWith(compareBy<ViewingItem> { it.chronologicalOrder ?: Int.MAX_VALUE }.thenBy { it.releaseDate ?: "9999" })
            )
        }

    private fun McuAssetDataSource.ViewingAssetData.homeFeaturedTitles(): List<ViewingItem> {
        val essentials = allLists.firstOrNull { it.id == "mcu-release-order" }?.items.orEmpty().take(5)
        val dcHighlights = allItems.filter { it.universe in dcUniverses && it.status == ViewingStatus.RELEASED }.take(4)
        val trailerReady = allItems.filter { it.status == ViewingStatus.RELEASED && it.hasAnyTrailer() }.take(6)
        val upcoming = allItems.filter { it.status == ViewingStatus.UPCOMING || it.status == ViewingStatus.ANNOUNCED }.take(3)
        return (listOf(featuredItem) + trailerReady + essentials + dcHighlights + upcoming).distinctBy { it.id }.take(10)
    }

    private fun List<ViewingList>.visibleManagedLists(): List<ViewingList> = filter { list ->
        list.importance == ViewingListImportance.PRIMARY ||
            list.category in setOf("Character Journeys", "Specials", "Defenders Saga", "Marvel One-Shots", "Disney+ Series")
    }.distinctBy { it.title }.sortedWith(
        compareByDescending<ViewingList> { it.importance == ViewingListImportance.PRIMARY }
            .thenBy { it.category ?: "" }
            .thenBy { it.title }
    )

    private fun ViewingItem.hasAnyTrailer(): Boolean =
        trailers.any { !it.youtubeVideoId.isNullOrBlank() || !it.url.isNullOrBlank() } ||
            !youtubeVideoId.isNullOrBlank() ||
            !trailerUrl.isNullOrBlank()

    private companion object {
        val dcUniverses = setOf("DCU", "DCEU", "Elseworlds")
    }
}
