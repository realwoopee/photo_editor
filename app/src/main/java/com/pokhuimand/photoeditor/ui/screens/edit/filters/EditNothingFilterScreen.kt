package com.pokhuimand.photoeditor.ui.screens.edit.filters

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap


@Composable
fun EditNothingFilterScreen(
    photoPreview: ImageBitmap,
    onBackPress: () -> Unit,
    onDonePress: () -> Unit,
    onCancelPress: () -> Unit
) {
    EditFilterScreenBase(
        photoPreview = photoPreview,
        isProcessingRunning = false,
        onBackPress = onBackPress,
        onDonePress = onDonePress,
        onCancelPress = onCancelPress,
        title = { /*TODO*/ },
        controlsContent = { })

}

