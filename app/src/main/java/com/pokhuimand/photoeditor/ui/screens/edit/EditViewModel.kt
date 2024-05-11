package com.pokhuimand.photoeditor.ui.screens.edit

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pokhuimand.photoeditor.data.photos.PhotosRepository
import com.pokhuimand.photoeditor.filters.FilterSettings
import com.pokhuimand.photoeditor.models.Photo
import com.pokhuimand.photoeditor.ui.screens.gallery.GalleryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditViewModel(
    private val photo: Bitmap,
    private val navigateBack: () -> Unit
) : ViewModel() {
    private val _state = MutableStateFlow(EditViewModelState(photo, null))
    val uiState = _state.map(EditViewModelState::toUiState)
        .stateIn(viewModelScope, SharingStarted.Eagerly, _state.value.toUiState())

    init {
    }

    fun onBackPress() {
        navigateBack()
    }

    companion object {
        fun provideFactory(
            photo: Bitmap,
            navigateBack: () -> Unit
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EditViewModel(photo, navigateBack) as T
                }
            }
    }
}

private data class EditViewModelState(
    val photo: Bitmap?,
    val filterId: String?
) {
    fun toUiState(): EditUiState =
        if (photo == null) {
            EditUiState.NoPhoto
        } else {
            EditUiState.HasPhoto(photo)
        }
}

sealed class EditUiState {

    sealed class FiltersUiState {
        data object NoFilters : FiltersUiState()

        data class SelectedFilter(val filterId: String, val filterSettings: FilterSettings) :
            FiltersUiState()
    }

    data object NoPhoto : EditUiState()

    data class HasPhoto(
        val photo: Bitmap,
        val filtersUiState: FiltersUiState = FiltersUiState.NoFilters
    ) :
        EditUiState()
}