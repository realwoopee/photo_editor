package com.pokhuimand.photoeditor.screens.main

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pokhuimand.photoeditor.data.PhotoStore
import com.pokhuimand.photoeditor.models.Photo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel(private val photoStore: PhotoStore) : ViewModel() {
    private val _state = MutableStateFlow(MainViewState(photos = photoStore.getPhotoList()))
    val state = _state.asStateFlow()

    private fun refreshPhotoList() {
        _state.update { it.copy(photos = photoStore.getPhotoList()) }
    }

    fun importPhoto(uri: Uri): Unit {
        photoStore.importContent(uri)
        refreshPhotoList()
    }

    fun onPhotoLongPress(photo: Photo) {
        if (_state.value.selectedPhotos.isEmpty())
            _state.update {
                it.copy(selectedPhotos =
                if (photo in it.selectedPhotos)
                    it.selectedPhotos.filter { p -> p != photo }
                else it.selectedPhotos + listOf(photo)
                )
            }
    }

    fun onPhotoShortPress(photo: Photo) {
        if (_state.value.selectedPhotos.any())
            _state.update {
                it.copy(selectedPhotos =
                if (photo in it.selectedPhotos)
                    it.selectedPhotos.filter { p -> p != photo }
                else it.selectedPhotos + listOf(photo)
                )
            }
    }

    fun onSelectedDelete() {
        for (photo in _state.value.selectedPhotos)
            photoStore.delete(photo)
        _state.update { it.copy(selectedPhotos = emptyList()) }
        refreshPhotoList()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val photoStore = PhotoStore(app.contentResolver, app.filesDir)
                MainViewModel(photoStore)
            }
        }

    }
}

data class MainViewState(
    val photos: List<Photo> = emptyList(),
    val selectedPhotos: List<Photo> = emptyList()
)