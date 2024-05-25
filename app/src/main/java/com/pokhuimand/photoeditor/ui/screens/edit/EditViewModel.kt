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
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class EditViewModel(
    private val photoId: String,
    private val photosRepository: PhotosRepository,
    private val navigateBack: () -> Unit,
    private val onPhotoSave: (Bitmap) -> Unit,
    val filters: Set<Filter>
) : ViewModel() {
    private val _state =
        MutableStateFlow(
            EditViewModelState(
                photoId, BitmapFactory.decodeFile(photosRepository.getPhoto(photoId).uri.path),
                BitmapFactory.decodeFile(
                    photosRepository.getPhoto(photoId).uri.path,
                    BitmapFactory.Options().apply {
                        inMutable = false
                    }),
                null,
                null,
                null
            )
        )

    val uiState = _state.map(EditViewModelState::toUiState)
        .stateIn(viewModelScope, SharingStarted.Eagerly, _state.value.toUiState())

    private val keyedFilters = filters.associateBy { it.id }

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
                preview = it.source
            )
        }
        if (filter != null)
            viewModelScope.launch {
                _state.value.runningJob?.cancelAndJoin()
                _state.update {
                    _state.value.copy(runningJob = launch {
                        _state.update {
                            if (it.filter != null)
                                it.copy(
                                    preview = keyedFilters[it.filter.id]!!.applyDefaults(
                                        it.source,
                                        it.filterCache!!
                                    ),
                                    runningJob = null
                                )
                            else
                                it.copy(runningJob = null)
                        }
                    })
                }
            }
    }

    fun onFilterSettingsUpdate(filterSettings: FilterSettings) {
        viewModelScope.launch {
            _state.value.runningJob?.cancelAndJoin()
            _state.update {
                _state.value.copy(runningJob = launch {
                    if (it.filter != null) {
                        if (!isActive) {
                            _state.update { it.copy(runningJob = null) }
                            return@launch
                        }
                        val newPhoto = keyedFilters[it.filter.id]!!.apply(
                            it.source,
                            filterSettings,
                            it.filterCache!!
                        )
                        if (isActive) {
                            _state.update { it.copy(preview = newPhoto) }
                        }
                    }
                    _state.update { it.copy(runningJob = null) }
                })
            }
        }
    }

    companion object {
        fun provideFactory(
            photoId: String,
            photosRepository: PhotosRepository,
            navigateBack: () -> Unit,
            onPhotoSave: (Bitmap) -> Unit,
            filters: Set<Filter>
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EditViewModel(
                        photoId,
                        photosRepository,
                        navigateBack,
                        onPhotoSave,
                        filters
                    ) as T
                }
            }
    }
}

private data class EditViewModelState(
    val photoId: String,
    val source: Bitmap,
    val preview: Bitmap,
    val filter: Filter?,
    val filterCache: FilterDataCache?,
    val runningJob: Job?
) {
    fun toUiState(): EditUiState.HasPhoto =
        EditUiState.HasPhoto(
            photo = preview,
            filter = filter,
            isProcessingRunning = runningJob != null,
            originalSize = Pair(source.width, source.height)
        )
}

sealed class EditUiState {
    data class HasPhoto(
        val photo: Bitmap,
        val isProcessingRunning: Boolean = false,
        val filter: Filter? = null,
        val originalSize: Pair<Int, Int>
    ) :
        EditUiState()
}