package com.pokhuimand.photoeditor.ui.screens.edit

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pokhuimand.photoeditor.data.photos.PhotosRepository
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterSettings
import com.pokhuimand.photoeditor.filters.Filters
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

    fun onFilterSelect(filter: Filter?) {
        _state.update { it.copy(filter = filter) }
    }

    fun onFilterSettingsUpdate(filterSettings: FilterSettings) {
        _state.update {
            it.copy(
                photo = Filters.keyedImplementations[it.filter!!.id]!!.apply(
                    photo,
                    filterSettings
                )
            )
        }
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
    val photo: Bitmap,
    val filter: Filter?
) {
    fun toUiState(): EditUiState.HasPhoto =
        EditUiState.HasPhoto(
            photo,
            filter
        )

}

sealed class EditUiState {

    sealed class FiltersUiState {
        data object NoFilter : FiltersUiState()

        data class SelectedFilter(val filterId: String) :
            FiltersUiState()
    }

    data class HasPhoto(
        val photo: Bitmap,
        val filter: Filter? = null
    ) :
        EditUiState()
}