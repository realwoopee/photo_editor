package com.pokhuimand.photoeditor.ui.screens.edit.filters.colorcorrection

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.stringResource
import com.pokhuimand.photoeditor.R
import com.pokhuimand.photoeditor.components.SliderWithLabelAndValue
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.ContrastAndBrightnessFilterSettings
import com.pokhuimand.photoeditor.ui.screens.edit.filters.EditFilterScreenBase
import kotlin.math.roundToInt


@Composable
fun EditContrastAndBrightnessFilterScreen(
    photoPreview: ImageBitmap,
    isProcessingRunning: Boolean,
    onBackPress: () -> Unit,
    onDonePress: () -> Unit,
    onCancelPress: () -> Unit,
    onFilterSettingsUpdate: (ContrastAndBrightnessFilterSettings) -> Unit
) {
    var filterSettings by remember {
        mutableStateOf(ContrastAndBrightnessFilterSettings.default)
    }

    EditFilterScreenBase(
        photoPreview = photoPreview,
        isProcessingRunning = isProcessingRunning,
        onBackPress = onBackPress,
        onDonePress = onDonePress,
        onCancelPress = onCancelPress,
        title = { Text(stringResource(R.string.ContrastAndBrightnessFilter)) },
        controlsContent = {
            SliderWithLabelAndValue(
                value = (filterSettings.contrast),
                onValueChange = {
                    filterSettings =
                        filterSettings.copy(contrast = it)
                },
                onValueChangeFinished = {
                    onFilterSettingsUpdate(filterSettings)
                },
                valueRange = ContrastAndBrightnessFilterSettings.Ranges.contrast,
                label = stringResource(R.string.Contrast),
                valueFormat = {
                    val value = ((it - 1) * 100).roundToInt()
                    "${if (value > 0) "+" else ""}${value}%"
                }
            )
            SliderWithLabelAndValue(
                value = (filterSettings.brightness),
                onValueChange = {
                    filterSettings =
                        filterSettings.copy(brightness = it)
                },
                onValueChangeFinished = {
                    onFilterSettingsUpdate(filterSettings)
                },
                valueRange = ContrastAndBrightnessFilterSettings.Ranges.brightness,
                label = stringResource(R.string.Brightness),
                valueFormat = {
                    val value = (it * 100).roundToInt()
                    "${if (value > 0) "+" else ""}${value}%"
                }
            )
        })


}

