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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.BottomAppBar
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
import androidx.compose.ui.res.painterResource
import com.pokhuimand.photoeditor.R
import com.pokhuimand.photoeditor.components.ProgressSpinner
import com.pokhuimand.photoeditor.components.SliderWithLabelAndValue
import com.pokhuimand.photoeditor.filters.impl.RotateFilterSettings
import com.pokhuimand.photoeditor.filters.impl.UnsharpMaskingFilterSettings


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
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .wrapContentWidth()
                        .align(Alignment.CenterHorizontally)
                ) {
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
                            filterSettings.copy(degrees = ((filterSettings.degrees).mod(360.0)).toFloat() - 90f)
                        onFilterSettingsUpdate(filterSettings)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.rotate_90_degrees_ccw_24dp_fill0_wght400_grad0_opsz24),
                            null,
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

