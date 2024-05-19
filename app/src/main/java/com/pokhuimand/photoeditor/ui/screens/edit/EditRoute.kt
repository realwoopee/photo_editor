package com.pokhuimand.photoeditor.ui.screens.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokhuimand.photoeditor.filters.impl.GrayscaleFilter
import com.pokhuimand.photoeditor.filters.impl.NothingFilter
import com.pokhuimand.photoeditor.filters.impl.RotateFilter
import com.pokhuimand.photoeditor.filters.impl.UnsharpMaskingFilter
import com.pokhuimand.photoeditor.ui.screens.edit.filters.EditGrayscaleFilterScreen
import com.pokhuimand.photoeditor.ui.screens.edit.filters.EditNothingFilterScreen
import com.pokhuimand.photoeditor.ui.screens.edit.filters.EditRotateFilterScreen
import com.pokhuimand.photoeditor.ui.screens.edit.filters.EditUnsharpMaskingFilterScreen

@Composable
fun EditRoute(viewModel: EditViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState.filter) {
        null -> {
            EditSelectFilterScreen(
                photoPreview = uiState.photo.asImageBitmap(),
                onBackPress = { viewModel.onBackPress() },
                onFilterSelect = viewModel::onFilterSelect
            )
        }

        is GrayscaleFilter -> {
            EditGrayscaleFilterScreen(
                photoPreview = uiState.photo.asImageBitmap(),
                isProcessingRunning = uiState.isProcessingRunning,
                onBackPress = viewModel::onBackPress,
                onCancelPress = { viewModel.onFilterSelect(null) },
                onDonePress = viewModel::onFilterApply
            )

        }

        is NothingFilter -> {
            EditNothingFilterScreen(
                photoPreview = uiState.photo.asImageBitmap(),
                onBackPress = viewModel::onBackPress,
                onCancelPress = { viewModel.onFilterSelect(null) },
                onDonePress = { viewModel.onFilterSelect(null) })
        }

        is UnsharpMaskingFilter -> {
            EditUnsharpMaskingFilterScreen(
                photoPreview = uiState.photo.asImageBitmap(),
                isProcessingRunning = uiState.isProcessingRunning,
                onBackPress = viewModel::onBackPress,
                onCancelPress = { viewModel.onFilterSelect(null) },
                onDonePress = viewModel::onFilterApply,
                onFilterSettingsUpdate = viewModel::onFilterSettingsUpdate
            )
        }

        is RotateFilter -> {
            EditRotateFilterScreen(
                photoPreview = uiState.photo.asImageBitmap(),
                isProcessingRunning = uiState.isProcessingRunning,
                onBackPress = viewModel::onBackPress,
                onCancelPress = { viewModel.onFilterSelect(null) },
                onDonePress = viewModel::onFilterApply,
                onFilterSettingsUpdate = viewModel::onFilterSettingsUpdate
            )
        }

        else -> {}
    }
}