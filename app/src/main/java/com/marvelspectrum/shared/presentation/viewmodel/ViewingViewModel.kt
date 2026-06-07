package com.marvelspectrum.shared.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.marvelspectrum.shared.data.service.ViewingMetadataStore
import com.marvelspectrum.shared.data.viewing.McuAssetDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * Loads the Cinemaverse catalog and viewing preference cache outside composition.
 */
data class ViewingUiState(
    val isLoading: Boolean = false,
    val data: McuAssetDataSource.ViewingAssetData? = null,
    val errorMessage: String? = null
)

class ViewingViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ViewingUiState())
    val uiState: StateFlow<ViewingUiState> = _uiState.asStateFlow()

    init {
        loadViewingData()
    }

    fun loadViewingData() {
        if (_uiState.value.isLoading) return

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

                    metadata.await()
                    catalog.await()
                }
            }.onSuccess { data ->
                _uiState.value = ViewingUiState(isLoading = false, data = data)
            }.onFailure { error ->
                _uiState.value = ViewingUiState(
                    isLoading = false,
                    data = _uiState.value.data,
                    errorMessage = error.message ?: "Unable to load Cinemaverse."
                )
            }
        }
    }
}
