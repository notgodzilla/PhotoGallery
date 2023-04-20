package com.bignerdranch.android.photogallery

data class PhotoGalleryUiState(
    val images: List<GalleryItem> = listOf(),
    val query: String = "",
    val isPolling: Boolean = false,
)