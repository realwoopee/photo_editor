package com.pokhuimand.photoeditor.ui.screens.edit.filters

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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Done
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
import androidx.compose.ui.layout.ContentScale
import com.pokhuimand.photoeditor.components.ProgressSpinner
import com.pokhuimand.photoeditor.components.SliderWithLabelAndValue
import com.pokhuimand.photoeditor.filters.impl.UnsharpMaskingFilterSettings

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
    BackHandler(onBack = onCancelPress)
    Scaffold(topBar = {
        TopAppBar(title = { },
            navigationIcon = {
                IconButton(
                    onClick = onBackPress
                ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            }
        )
    }) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize()
            ) {
                Image(
                    bitmap = photoPreview,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    colorFilter = if (isProcessingRunning) ColorFilter.tint(
                        Color.LightGray.copy(alpha = 0.3f),
                        BlendMode.SrcOver
                    ) else null
                )
                if (isProcessingRunning)
                    ProgressSpinner(modifier = Modifier.align(Alignment.Center))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
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

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.secondary)
                ) {
                    IconButton(onClick = onCancelPress) {
                        Icon(Icons.Default.DeleteForever, null)
                    }

                    IconButton(onClick = onDonePress, enabled = !isProcessingRunning) {
                        Icon(Icons.Default.Done, null)
                    }
                }
            }
        }
    }

}