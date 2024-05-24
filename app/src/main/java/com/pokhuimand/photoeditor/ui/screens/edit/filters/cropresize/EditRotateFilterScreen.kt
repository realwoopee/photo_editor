package com.pokhuimand.photoeditor.ui.screens.edit.filters.cropresize

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.painterResource
import com.pokhuimand.photoeditor.R
import com.pokhuimand.photoeditor.components.SliderWithLabelAndValue
import com.pokhuimand.photoeditor.filters.impl.RotateFilterSettings
import com.pokhuimand.photoeditor.ui.screens.edit.filters.EditFilterScreenBase


@Composable
fun EditRotateFilterScreen(
    photoPreview: ImageBitmap,
    isProcessingRunning: Boolean,
    onBackPress: () -> Unit,
    onDonePress: () -> Unit,
    onCancelPress: () -> Unit,
    onFilterSettingsUpdate: (RotateFilterSettings) -> Unit
) {
    var filterSettings by remember {
        mutableStateOf(RotateFilterSettings.default)
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
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.CenterHorizontally)
            ) {
                IconButton(onClick = {
                    filterSettings =
                        filterSettings.copy(degrees = ((filterSettings.degrees).mod(360.0)).toFloat() - 90f)
                    onFilterSettingsUpdate(filterSettings)
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.rotate_90_degrees_ccw_24dp_fill0_wght400_grad0_opsz24),
                        null,
                    )
                }
                IconButton(onClick = {
                    filterSettings =
                        filterSettings.copy(degrees = 0f)
                    onFilterSettingsUpdate(filterSettings)
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.cancel_24dp_fill0_wght400_grad0_opsz24),
                        null,
                    )
                }
                IconButton(onClick = {
                    filterSettings =
                        filterSettings.copy(degrees = ((filterSettings.degrees + 180).mod(360.0)).toFloat() - 90f)
                    onFilterSettingsUpdate(filterSettings)
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.rotate_90_degrees_cw_24dp_fill0_wght400_grad0_opsz24),
                        null
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.CenterHorizontally)
            ) {
                SliderWithLabelAndValue(
                    value = (filterSettings.degrees),
                    onValueChange = {
                        filterSettings =
                            filterSettings.copy(degrees = it)
                    },
                    onValueChangeFinished = {
                        onFilterSettingsUpdate(filterSettings)
                    },
                    valueRange = RotateFilterSettings.Ranges.degrees,
                    label = "Degrees",
                    valueFormat = { String.format("%.2f°", it) }
                )
            }
        })
}

