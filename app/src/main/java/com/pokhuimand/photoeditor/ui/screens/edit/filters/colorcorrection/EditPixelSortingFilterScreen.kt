package com.pokhuimand.photoeditor.ui.screens.edit.filters.colorcorrection

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.pokhuimand.photoeditor.R
import com.pokhuimand.photoeditor.components.ProgressSpinner
import com.pokhuimand.photoeditor.components.RangeSliderWithLabelAndValue
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.PixelSortingFilterSettings
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.SortDirection
import com.pokhuimand.photoeditor.ui.screens.edit.filters.EditFilterScreenBase


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPixelSortingFilterScreen(
    photoPreview: ImageBitmap,
    isProcessingRunning: Boolean,
    onBackPress: () -> Unit,
    onDonePress: () -> Unit,
    onCancelPress: () -> Unit,
    onFilterSettingsUpdate: (PixelSortingFilterSettings) -> Unit
) {
    var filterSettings by remember {
        mutableStateOf(PixelSortingFilterSettings.default)
    }
    EditFilterScreenBase(
        photoPreview = photoPreview,
        isProcessingRunning = isProcessingRunning,
        onBackPress = onBackPress,
        onDonePress = onDonePress,
        onCancelPress = onCancelPress,
        title = { Text(stringResource(R.string.PSFilterName)) },
        controlsContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.Sort))
                MultiChoiceSegmentedButtonRow() {
                    SortDirection.entries.mapIndexed() { index, direction ->
                        SegmentedButton(
                            checked = filterSettings.direction == direction,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    filterSettings = filterSettings.copy(direction = direction)
                                    onFilterSettingsUpdate(filterSettings)
                                }
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = SortDirection.entries.size
                            ),
                            icon = {}
                        ) {
                            Icon(
                                ImageVector.vectorResource(
                                    id =
                                    when (direction) {
                                        SortDirection.Up -> R.drawable.arrow_upward_24dp_fill0_wght400_grad0_opsz24
                                        SortDirection.Right -> R.drawable.arrow_forward_24dp_fill0_wght400_grad0_opsz24
                                        SortDirection.Down -> R.drawable.arrow_downward_24dp_fill0_wght400_grad0_opsz24
                                        SortDirection.Left -> R.drawable.arrow_back_24dp_fill0_wght400_grad0_opsz24
                                    }
                                ), null

                            )
                        }
                    }
                }
            }
            RangeSliderWithLabelAndValue(
                modifier = Modifier.padding(horizontal = 16.dp),
                value = (filterSettings.threshold),
                onValueChange = {
                    filterSettings =
                        filterSettings.copy(threshold = it)
                },
                onValueChangeFinished = {
                    onFilterSettingsUpdate(filterSettings)
                },
                valueRange = PixelSortingFilterSettings.Ranges.threshold,
                label = stringResource(R.string.MakingThreshold),
            )
        })


}

