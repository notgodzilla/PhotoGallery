package com.bignerdranch.android.photogallery

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PhotoGalleryViewModel : ViewModel() {

    private val photoRepository = PhotoRepository()
    private val preferencesRepository = PreferencesRepository.get()


    private val _uiState: MutableStateFlow<PhotoGalleryUiState> =
        MutableStateFlow(PhotoGalleryUiState())
    val uiState: StateFlow<PhotoGalleryUiState>
        get() = _uiState.asStateFlow()


    init {
        viewModelScope.launch {
            preferencesRepository.storedQuery.collectLatest { storedQuery ->
                try {
                    val items = fetchGalleryItems(storedQuery)
                    _uiState.update { oldState ->
                        oldState.copy(
                            images = items,
                            query = storedQuery
                        )
                    }
                } catch (ex: Exception) {
                    Log.e(TAG, "Failed to fetch gallery items", ex)
                }
            }
        }

        viewModelScope.launch {
            preferencesRepository.isPolling.collect { isPolling ->
                _uiState.update { it.copy(isPolling = isPolling) }
            }
        }
    }

    fun setQuery(query: String) {
        viewModelScope.launch { preferencesRepository.setStoredQuery(query) }
    }

    fun toggleIsPolling() {
        viewModelScope.launch {
            preferencesRepository.setPolling(!uiState.value.isPolling)
        }
    }
    private suspend fun fetchGalleryItems(query: String): List<GalleryItem> {
        return if (query.isNotEmpty()) {
            photoRepository.searchPhotos(query)
        } else {
            photoRepository.fetchPhotos()
        }
    }

}