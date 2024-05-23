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
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.vectorResource
import com.pokhuimand.photoeditor.R
import com.pokhuimand.photoeditor.components.ProgressSpinner
import com.pokhuimand.photoeditor.components.SliderWithLabelAndValue
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.ContrastAndBrightnessFilterSettings
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
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
                    value = (filterSettings.contrast),
                    onValueChange = {
                        filterSettings =
                            filterSettings.copy(contrast = it)
                    },
                    onValueChangeFinished = {
                        onFilterSettingsUpdate(filterSettings)
                    },
                    valueRange = ContrastAndBrightnessFilterSettings.Ranges.contrast,
                    label = "Contrast",
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
                    label = "Brightness",
                    valueFormat = {
                        val value = (it * 100).roundToInt()
                        "${if (value > 0) "+" else ""}${value}%"
                    }
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondary)
                ) {
                    IconButton(onClick = onCancelPress) {
                        Icon(
                            ImageVector.vectorResource(id = R.drawable.cancel_24dp_fill0_wght400_grad0_opsz24),
                            null
                        )
                    }

                    IconButton(onClick = onDonePress, enabled = !isProcessingRunning) {
                        Icon(Icons.Default.Done, null)
                    }
                }
            }
        }
    }

}
