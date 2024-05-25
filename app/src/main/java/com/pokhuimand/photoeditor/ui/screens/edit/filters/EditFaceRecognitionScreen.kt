package com.pokhuimand.photoeditor.ui.screens.edit.filters

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.stringResource
import com.pokhuimand.photoeditor.R
import com.pokhuimand.photoeditor.filters.impl.FaceRecognitionSettings
import com.pokhuimand.photoeditor.filters.impl.RotateFilterSettings


@Composable
fun EditFaceRecognitionScreen(
    photoPreview: ImageBitmap,
    isProcessingRunning: Boolean,
    onBackPress: () -> Unit,
    onDonePress: () -> Unit,
    onCancelPress: () -> Unit,
    onFilterSettingsUpdate: (FaceRecognitionSettings) -> Unit
) {
    EditFilterScreenBase(
        photoPreview = photoPreview,
        isProcessingRunning = isProcessingRunning,
        onBackPress = onBackPress,
        onDonePress = onDonePress,
        onCancelPress = onCancelPress,
        title = { Text(stringResource(R.string.FaceRecognitionFilter)) },
        controlsContent = { })
}



