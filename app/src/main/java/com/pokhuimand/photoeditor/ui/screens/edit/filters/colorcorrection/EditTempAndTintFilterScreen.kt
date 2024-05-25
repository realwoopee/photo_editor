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
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.TempAndTintFilterSettings
import com.pokhuimand.photoeditor.ui.screens.edit.filters.EditFilterScreenBase
import kotlin.math.roundToInt


@Composable
fun EditTempAndTintFilterScreen(
    photoPreview: ImageBitmap,
    isProcessingRunning: Boolean,
    onBackPress: () -> Unit,
    onDonePress: () -> Unit,
    onCancelPress: () -> Unit,
    onFilterSettingsUpdate: (TempAndTintFilterSettings) -> Unit
) {
    var filterSettings by remember {
        mutableStateOf(TempAndTintFilterSettings.default)
    }

    EditFilterScreenBase(
        photoPreview = photoPreview,
        isProcessingRunning = isProcessingRunning,
        onBackPress = onBackPress,
        onDonePress = onDonePress,
        onCancelPress = onCancelPress,
        title = { Text(stringResource(R.string.TempAndTintFilter)) },
        controlsContent = {
            SliderWithLabelAndValue(
                value = (filterSettings.temp),
                onValueChange = {
                    filterSettings =
                        filterSettings.copy(temp = it)
                },
                onValueChangeFinished = {
                    onFilterSettingsUpdate(filterSettings)
                },
                valueRange = TempAndTintFilterSettings.Ranges.temp,
                label = stringResource(R.string.Temp),
                valueFormat = {
                    val value = (it * 100).roundToInt()
                    "${if (value > 0) "+" else ""}${value}%"
                }
            )
            SliderWithLabelAndValue(
                value = (filterSettings.tint),
                onValueChange = {
                    filterSettings =
                        filterSettings.copy(tint = it)
                },
                onValueChangeFinished = {
                    onFilterSettingsUpdate(filterSettings)
                },
                valueRange = TempAndTintFilterSettings.Ranges.tint,
                label = stringResource(R.string.Tint),
                valueFormat = {
                    val value = (it * 100).roundToInt()
                    "${if (value > 0) "+" else ""}${value}%"
                }
            )
        })


}

