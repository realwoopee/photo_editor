package com.pokhuimand.photoeditor.ui.screens.edit.filters.colorcorrection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import com.pokhuimand.photoeditor.components.SliderWithLabelAndValue
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.UnsharpMaskingFilterSettings
import com.pokhuimand.photoeditor.ui.screens.edit.filters.EditFilterScreenBase

@Composable
fun EditUnsharpMaskingFilterScreen(
    photoPreview: ImageBitmap,
    isProcessingRunning: Boolean,
    onBackPress: () -> Unit,
    onDonePress: () -> Unit,
    onCancelPress: () -> Unit,
    onFilterSettingsUpdate: (UnsharpMaskingFilterSettings) -> Unit
) {
    var filterSettings by remember {
        mutableStateOf(UnsharpMaskingFilterSettings.default)
    }
    EditFilterScreenBase(
        photoPreview = photoPreview,
        isProcessingRunning = isProcessingRunning,
        onBackPress = onBackPress,
        onDonePress = onDonePress,
        onCancelPress = onCancelPress,
        title = { /*TODO*/ },
        controlsContent = {
            SliderWithLabelAndValue(
                value = (filterSettings.amount).toFloat(),
                onValueChange = {
                    filterSettings =
                        filterSettings.copy(amount = (it).toDouble())
                },
                onValueChangeFinished = {
                    onFilterSettingsUpdate(filterSettings)
                },
                valueRange = UnsharpMaskingFilterSettings.Ranges.amount,
                label = "Amount",
                valueFormat = { String.format("%d%%", (it * 100).toInt()) }
            )
            SliderWithLabelAndValue(
                value = (filterSettings.radius.toFloat()),
                onValueChange = {
                    filterSettings =
                        filterSettings.copy(radius = it.toDouble())
                },
                onValueChangeFinished = {
                    onFilterSettingsUpdate(filterSettings)
                },
                valueRange = UnsharpMaskingFilterSettings.Ranges.radius,
                label = "Radius",
                valueFormat = { String.format("%.2f", it) }
            )
            SliderWithLabelAndValue(
                value = (filterSettings.threshold).toFloat(),
                onValueChange = {
                    filterSettings =
                        filterSettings.copy(threshold = (it).toDouble())
                },
                onValueChangeFinished = {
                    onFilterSettingsUpdate(filterSettings)
                },
                valueRange = UnsharpMaskingFilterSettings.Ranges.threshold,
                label = "Threshold",
                valueFormat = { it.toInt().toString() }
            )
        })

}