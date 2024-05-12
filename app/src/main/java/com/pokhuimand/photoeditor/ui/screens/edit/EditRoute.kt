package com.pokhuimand.photoeditor.ui.screens.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pokhuimand.photoeditor.filters.NothingFilter

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

        is NothingFilter -> {
            EditNothingFilterScreen(
                photoPreview = uiState.photo.asImageBitmap(),
                onBackPress = viewModel::onBackPress,
                onCancelPress = { viewModel.onFilterSelect(null) },
                onDonePress = { viewModel.onFilterSelect(null) })
        }

        else -> {}
    }
}