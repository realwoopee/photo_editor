package com.pokhuimand.photoeditor.ui.screens.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.ContrastAndBrightnessFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.DitheringFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.GrayscaleFilter
import com.pokhuimand.photoeditor.filters.impl.NothingFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.PixelSortingFilter
import com.pokhuimand.photoeditor.filters.impl.RotateFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.UnsharpMaskingFilter
import com.pokhuimand.photoeditor.ui.screens.edit.filters.colorcorrection.EditContrastAndBrightnessFilterScreen
import com.pokhuimand.photoeditor.ui.screens.edit.filters.colorcorrection.EditDitheringFilterScreen
import com.pokhuimand.photoeditor.ui.screens.edit.filters.EditGrayscaleFilterScreen
import com.pokhuimand.photoeditor.ui.screens.edit.filters.EditNothingFilterScreen
import com.pokhuimand.photoeditor.ui.screens.edit.filters.colorcorrection.EditPixelSortingFilterScreen
import com.pokhuimand.photoeditor.filters.impl.ResizeFilter
import com.pokhuimand.photoeditor.ui.screens.edit.filters.EditResizeFilterScreen
import com.pokhuimand.photoeditor.ui.screens.edit.filters.EditRotateFilterScreen
import com.pokhuimand.photoeditor.ui.screens.edit.filters.colorcorrection.EditUnsharpMaskingFilterScreen

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

        is DitheringFilter -> {
            EditDitheringFilterScreen(
                photoPreview = uiState.photo.asImageBitmap(),
                isProcessingRunning = uiState.isProcessingRunning,
                onBackPress = viewModel::onBackPress,
                onCancelPress = { viewModel.onFilterSelect(null) },
                onDonePress = viewModel::onFilterApply,
                onFilterSettingsUpdate = viewModel::onFilterSettingsUpdate
            )
        }

        is PixelSortingFilter -> {
            EditPixelSortingFilterScreen(
                photoPreview = uiState.photo.asImageBitmap(),
                isProcessingRunning = uiState.isProcessingRunning,
                onBackPress = viewModel::onBackPress,
                onCancelPress = { viewModel.onFilterSelect(null) },
                onDonePress = viewModel::onFilterApply,
                onFilterSettingsUpdate = viewModel::onFilterSettingsUpdate
            )
        }

        is ContrastAndBrightnessFilter -> {
            EditContrastAndBrightnessFilterScreen(
                photoPreview = uiState.photo.asImageBitmap(),
                isProcessingRunning = uiState.isProcessingRunning,
                onBackPress = viewModel::onBackPress,
                onCancelPress = { viewModel.onFilterSelect(null) },
                onDonePress = viewModel::onFilterApply,
                onFilterSettingsUpdate = viewModel::onFilterSettingsUpdate
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

        is ResizeFilter -> {
            EditResizeFilterScreen(
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