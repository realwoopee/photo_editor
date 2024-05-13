package com.pokhuimand.photoeditor.ui.screens.gallery

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pokhuimand.photoeditor.data.photos.PhotosRepository
import com.pokhuimand.photoeditor.data.photos.impl.FileSystemPhotoRepository
import com.pokhuimand.photoeditor.models.Photo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GalleryViewModel(
    private val photosRepository: PhotosRepository,
    private val onOpenEditor: (photoId: String) -> Unit
) : ViewModel() {
    private val _state = MutableStateFlow(GalleryViewModelState())
    val uiState = _state.map(GalleryViewModelState::toUiState)
        .stateIn(viewModelScope, SharingStarted.Eagerly, _state.value.toUiState())

    init {
        _state.update { it.copy(photos = photosRepository.getPhotos()) }

        viewModelScope.launch {
            photosRepository.observePhotos().collect() { photos ->
                val newSelectedPhotos =
                    _state.value.selectedPhotos - (_state.value.photos - photos.toSet()).toSet()
                _state.update { it.copy(photos = photos, selectedPhotos = newSelectedPhotos) }
            }
        }
    }

    fun importPhoto(uri: Uri): Unit {
        photosRepository.importContent(uri)
    }

    fun onPhotoLongPress(photoId: String) {
        if (_state.value.selectedPhotos.isEmpty()) {
            val photo = photosRepository.getPhoto(photoId)
            _state.update {
                it.copy(
                    selectedPhotos =
                    if (photo in it.selectedPhotos)
                        it.selectedPhotos - photo
                    else it.selectedPhotos + photo
                )
            }
        }
    }

    fun onPhotoShortPress(photoId: String) {
        if (_state.value.selectedPhotos.any()) {
            val photo = photosRepository.getPhoto(photoId)
            _state.update {
                it.copy(
                    selectedPhotos =
                    if (photo in it.selectedPhotos)
                        it.selectedPhotos - photo
                    else it.selectedPhotos + photo
                )
            }
        } else
            onOpenEditor(photoId)
    }

    fun onSelectedDelete() {
        photosRepository.removePhotos(_state.value.selectedPhotos.mapTo(HashSet()) { it.id })
    }

    companion object {
        fun provideFactory(
            photosRepository: PhotosRepository,
            onOpenEditor: (photoId: String) -> Unit,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GalleryViewModel(photosRepository, onOpenEditor) as T
                }
            }
    }
}

private data class GalleryViewModelState(
    val photos: List<Photo> = emptyList(),
    val selectedPhotos: List<Photo> = emptyList()
) {
    fun toUiState(): GalleryUiState =
        if (photos.isEmpty()) {
            GalleryUiState.NoPhotos
        } else {
            GalleryUiState.HasPhotos(photos, selectedPhotos)
        }
}

sealed class GalleryUiState {
    data object NoPhotos : GalleryUiState()

    data class HasPhotos(
        val photos: List<Photo>,
        val selectedPhotos: List<Photo>
    ) : GalleryUiState()
}