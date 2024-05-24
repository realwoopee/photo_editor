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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.input.KeyboardType
import com.pokhuimand.photoeditor.R
import com.pokhuimand.photoeditor.components.ProgressSpinner
import com.pokhuimand.photoeditor.filters.impl.ResizeFilterSettings
import java.util.Timer
import kotlin.concurrent.schedule


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditResizeFilterScreen(
    //подсчитать resolution, расситывать max.coefficient
    photoPreview: ImageBitmap,
    isProcessingRunning: Boolean,
    onBackPress: () -> Unit,
    onDonePress: () -> Unit,
    onCancelPress: () -> Unit,
    onFilterSettingsUpdate: (ResizeFilterSettings) -> Unit
) {
    var filterSettings by remember {
        mutableStateOf(ResizeFilterSettings.default)
    }
    var timer = Timer()

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
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.CenterHorizontally)
            ) {
                var text by remember { mutableStateOf("1.0") }
                TextField(
                    value = text,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    onValueChange = {
                        text = it.replace(",", ".", false)
                        timer.cancel()
                        timer = Timer()
                        try {
                            filterSettings = filterSettings.copy(coefficient = text.toFloat())
                            if (filterSettings.coefficient > 0)
                                timer.schedule(1000) {
                                    onFilterSettingsUpdate(filterSettings)
                                }
                        } catch (_: NumberFormatException) {
                        }
                    },
                    label = { Text("Enter coefficient:") }
                )

            }
        })

}




