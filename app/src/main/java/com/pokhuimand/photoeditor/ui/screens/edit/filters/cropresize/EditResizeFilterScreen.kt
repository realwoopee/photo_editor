package com.pokhuimand.photoeditor.ui.screens.edit.filters.cropresize

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pokhuimand.photoeditor.R
import com.pokhuimand.photoeditor.components.RangeSliderWithLabelAndValue
import com.pokhuimand.photoeditor.components.SliderWithLabelAndValue
import com.pokhuimand.photoeditor.filters.impl.ResizeFilterSettings
import com.pokhuimand.photoeditor.ui.screens.edit.filters.EditFilterScreenBase
import com.pokhuimand.photoeditor.ui.screens.edit.filters.colorcorrection.EditGrayscaleFilterScreen
import java.util.Timer
import kotlin.concurrent.schedule

@Composable
fun EditResizeFilterScreen(
    photoPreview: ImageBitmap,
    originalResolution: Pair<Int, Int>,
    isProcessingRunning: Boolean,
    onBackPress: () -> Unit,
    onDonePress: () -> Unit,
    onCancelPress: () -> Unit,
    onFilterSettingsUpdate: (ResizeFilterSettings) -> Unit
) {
    var filterSettings by remember {
        mutableStateOf(ResizeFilterSettings.default)
    }

    EditFilterScreenBase(
        photoPreview = photoPreview,
        isProcessingRunning = isProcessingRunning,
        onBackPress = onBackPress,
        onDonePress = onDonePress,
        onCancelPress = onCancelPress,
        title = { /*TODO*/ },
        controlsContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Original:")
                Text("${originalResolution.first}x${originalResolution.second}")
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("New:")
                Text("${photoPreview.width}x${photoPreview.height}")
            }
            SliderWithLabelAndValue(
                label = "Factor:",
                value = filterSettings.coefficient,
                onValueChange = { filterSettings = filterSettings.copy(coefficient = it) },
                valueRange = ResizeFilterSettings.Ranges.coefficient,
                onValueChangeFinished = {
                    onFilterSettingsUpdate(filterSettings)
                }
            )
        })

}

@Preview(
    showBackground = true, showSystemUi = true,
)
@Composable
fun EditResizeFilterScreenPreview() {
    val width = 128 * 3
    val height = 128 * 1
    val bitmap = Bitmap.createBitmap(
        IntArray(width * height) { i -> Color.rgb(i % height, i / width, 0) },
        0,
        width,
        width,
        height,
        Bitmap.Config.ARGB_8888
    ).asImageBitmap()

    var filterSettings by remember {
        mutableStateOf(ResizeFilterSettings.default)
    }

    EditResizeFilterScreen(
        photoPreview = bitmap,
        originalResolution = Pair(width, height),
        isProcessingRunning = false,
        onBackPress = { },
        onDonePress = { },
        onCancelPress = { },
        onFilterSettingsUpdate = { filterSettings = it })
}



