package com.pokhuimand.photoeditor.ui.screens.edit.filters.colorcorrection

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import android.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.pokhuimand.photoeditor.R
import com.pokhuimand.photoeditor.ui.screens.edit.filters.EditFilterScreenBase


@Composable
fun EditDumbFilterScreen(
    photoPreview: ImageBitmap,
    isProcessingRunning: Boolean,
    onBackPress: () -> Unit,
    onDonePress: () -> Unit,
    onCancelPress: () -> Unit,
    title: @Composable () -> Unit
) {
    EditFilterScreenBase(
        photoPreview = photoPreview,
        isProcessingRunning = isProcessingRunning,
        onBackPress = onBackPress,
        onDonePress = onDonePress,
        onCancelPress = onCancelPress,
        title = title,
        controlsContent = { })

}

@Preview(
    showBackground = true, showSystemUi = true,
)
@Composable
fun EditGrayscaleFilterScreenPreview() {
    val width = 128 * 3
    val height = 128 * 3
    val bitmap = Bitmap.createBitmap(
        IntArray(width * height) { i -> Color.rgb(i % height, i / width, 0) },
        0,
        width,
        width,
        height,
        Bitmap.Config.ARGB_8888
    ).asImageBitmap()
    EditDumbFilterScreen(
        photoPreview = bitmap,
        isProcessingRunning = false,
        onBackPress = { },
        onDonePress = { }, onCancelPress = { }, title = {})
}
