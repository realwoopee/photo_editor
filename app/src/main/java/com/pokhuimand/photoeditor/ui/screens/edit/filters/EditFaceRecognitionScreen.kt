package com.pokhuimand.photoeditor.ui.screens.edit.filters

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import com.pokhuimand.photoeditor.filters.impl.RotateFilterSettings


@Composable
fun EditFaceRecognitionScreen(
    photoPreview: ImageBitmap,
    isProcessingRunning: Boolean,
    onBackPress: () -> Unit,
    onDonePress: () -> Unit,
    onCancelPress: () -> Unit,
    onFilterSettingsUpdate: (RotateFilterSettings) -> Unit
) {
    EditFilterScreenBase(
        photoPreview = photoPreview,
        isProcessingRunning = isProcessingRunning,
        onBackPress = onBackPress,
        onDonePress = onDonePress,
        onCancelPress = onCancelPress,
        title = { /*TODO*/ },
        controlsContent = { })
}



