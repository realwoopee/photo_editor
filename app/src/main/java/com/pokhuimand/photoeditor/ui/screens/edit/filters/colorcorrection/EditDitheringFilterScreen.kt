package com.pokhuimand.photoeditor.ui.screens.edit.filters.colorcorrection

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.stringResource
import com.pokhuimand.photoeditor.R
import com.pokhuimand.photoeditor.components.SliderWithLabelAndValue
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.DitheringFilterSettings
import com.pokhuimand.photoeditor.ui.screens.edit.filters.EditFilterScreenBase


@Composable
fun EditDitheringFilterScreen(
    photoPreview: ImageBitmap,
    isProcessingRunning: Boolean,
    onBackPress: () -> Unit,
    onDonePress: () -> Unit,
    onCancelPress: () -> Unit,
    onFilterSettingsUpdate: (DitheringFilterSettings) -> Unit
) {
    var filterSettings by remember {
        mutableStateOf(DitheringFilterSettings.default)
    }
    EditFilterScreenBase(
        photoPreview = photoPreview,
        isProcessingRunning = isProcessingRunning,
        onBackPress = onBackPress,
        onDonePress = onDonePress,
        onCancelPress = onCancelPress,
        title = { Text(stringResource(R.string.Dithering)) },
        controlsContent = {
            SliderWithLabelAndValue(
                value = (filterSettings.levels).toFloat(),
                onValueChange = {
                    filterSettings =
                        filterSettings.copy(levels = Math.round(it))
                },
                onValueChangeFinished = {
                    onFilterSettingsUpdate(filterSettings)
                },
                valueRange = DitheringFilterSettings.Ranges.levels,
                label = stringResource(R.string.BitDepth),
                valueFormat = { String.format("%d bits", Math.round(it)) }
            )
            SliderWithLabelAndValue(
                value = (filterSettings.errorMultiplier.toFloat()),
                onValueChange = {
                    filterSettings =
                        filterSettings.copy(errorMultiplier = it.toDouble())
                },
                onValueChangeFinished = {
                    onFilterSettingsUpdate(filterSettings)
                },
                valueRange = DitheringFilterSettings.Ranges.errorMultiplier,
                label = stringResource(R.string.ErrorMultiplier),
                valueFormat = { String.format("%d%%", Math.round(it * 100)) }
            )
        })
}

