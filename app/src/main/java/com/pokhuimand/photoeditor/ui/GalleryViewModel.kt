package com.pokhuimand.photoeditor.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pokhuimand.photoeditor.data.PhotoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GalleryViewModel(private val photoManager: PhotoManager) : ViewModel() {
    private val _state = MutableStateFlow(GalleryState(photos = photoManager.getPhotoList()))
    val state = _state.asStateFlow()

    fun importPhoto(uri: Uri?): Unit {
        photoManager.importContent(uri)
        _state.update { it.copy(photos = photoManager.getPhotoList()) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app =
                    checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val photoManager = PhotoManager(app.contentResolver, app.filesDir)
                GalleryViewModel(photoManager)
            }
        }

    }
}

data class GalleryState(val photos: List<String> = emptyList())