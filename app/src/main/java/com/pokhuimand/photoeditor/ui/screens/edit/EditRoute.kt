package com.pokhuimand.photoeditor.ui.screens.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun EditRoute(viewModel: EditViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EditRoute(uiState = uiState, onBackPress = { viewModel.onBackPress() })
}

@Composable
fun EditRoute(uiState: EditUiState, onBackPress: () -> Unit) {
    EditScreen(uiState = uiState, onBackPress)
}