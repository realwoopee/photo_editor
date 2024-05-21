package com.pokhuimand.photoeditor.ui.screens.edit

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pokhuimand.photoeditor.data.photos.PhotosRepository
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterDataCache
import com.pokhuimand.photoeditor.filters.FilterSettings
import com.pokhuimand.photoeditor.filters.Filters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditViewModel(
    private val photoId: String,
    private val photosRepository: PhotosRepository,
    private val navigateBack: () -> Unit,
    private val onPhotoSave: (Bitmap) -> Unit
) : ViewModel() {
    private val _state =
        MutableStateFlow(
            EditViewModelState(
                photoId, BitmapFactory.decodeFile(photosRepository.getPhoto(photoId).uri.path),
                null,
                null,
                false
            )
        )
    val uiState = _state.map(EditViewModelState::toUiState)
        .stateIn(viewModelScope, SharingStarted.Eagerly, _state.value.toUiState())

    init {
        viewModelScope.launch {
            photosRepository.observePhotos().collect() { it ->
                _state.update {
                    it.copy(
                        preview = BitmapFactory.decodeFile(
                            photosRepository.getPhoto(
                                photoId
                            ).uri.path
                        )
                    )
                }
            }
        }
    }

    fun onBackPress() {
        navigateBack()
    }

    fun onFilterApply() {
        onPhotoSave(_state.value.preview)
        onFilterSelect(null)
    }

    fun onFilterSelect(filter: Filter?) {
        _state.update {
            it.copy(
                filter = filter,
                filterCache = filter?.buildCache(),
                preview = BitmapFactory.decodeFile(photosRepository.getPhoto(photoId).uri.path)
            )
        }
        if (filter != null)
            viewModelScope.launch {
                _state.update { it.copy(isProcessingRunning = true) }
                _state.update {
                    if (it.filter != null)
                        it.copy(
                            preview = Filters.keyedImplementations[filter.id]!!.applyDefaults(
                                BitmapFactory.decodeFile(photosRepository.getPhoto(photoId).uri.path),
                                it.filterCache!!
                            ),
                            isProcessingRunning = false
                        )
                    else
                        it
                }
            }
    }

    fun onFilterSettingsUpdate(filterSettings: FilterSettings) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessingRunning = true) }
            _state.update {
                if (it.filter != null)
                    it.copy(
                        preview = Filters.keyedImplementations[it.filter.id]!!.apply(
                            BitmapFactory.decodeFile(photosRepository.getPhoto(photoId).uri.path),
                            filterSettings,
                            it.filterCache!!
                        ),
                        isProcessingRunning = false
                    )
                else
                    it
            }
        }
    }

    companion object {
        fun provideFactory(
            photoId: String,
            photosRepository: PhotosRepository,
            navigateBack: () -> Unit,
            onPhotoSave: (Bitmap) -> Unit,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EditViewModel(photoId, photosRepository, navigateBack, onPhotoSave) as T
                }
            }
    }
}

private data class EditViewModelState(
    val photoId: String,
    val preview: Bitmap,
    val filter: Filter?,
    val filterCache: FilterDataCache?,
    val isProcessingRunning: Boolean
) {
    fun toUiState(): EditUiState.HasPhoto =
        EditUiState.HasPhoto(
            photo = preview,
            filter = filter,
            isProcessingRunning = isProcessingRunning
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
        val isProcessingRunning: Boolean = false,
        val filter: Filter? = null
    ) :
        EditUiState()
}