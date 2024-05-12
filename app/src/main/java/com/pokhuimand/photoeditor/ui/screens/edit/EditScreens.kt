package com.pokhuimand.photoeditor.ui.screens.edit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pokhuimand.photoeditor.R
import com.pokhuimand.photoeditor.components.SelectableIconButton
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterCategory
import com.pokhuimand.photoeditor.filters.Filters
import com.pokhuimand.photoeditor.filters.NothingFilter

@Composable
fun EditNothingFilterScreen(
    photoPreview: ImageBitmap,
    onBackPress: () -> Unit,
    onDonePress: () -> Unit,
    onCancelPress: () -> Unit
) {
    BackHandler(onBack = onCancelPress)
    Scaffold(topBar = {
        TopAppBar(title = { },
            navigationIcon = {
                IconButton(
                    onClick = onBackPress
                ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            }
        )
    }, bottomBar = {
        BottomAppBar() {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onCancelPress) {
                    Icon(Icons.Default.DeleteForever, null)
                }

                IconButton(onClick = onDonePress) {
                    Icon(Icons.Default.Done, null)
                }
            }
        }
    }) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Image(
                bitmap = photoPreview,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.FillWidth,
            )
        }
    }

}

@Composable
fun EditSelectFilterScreen(
    photoPreview: ImageBitmap,
    onBackPress: () -> Unit,
    onFilterSelect: (Filter) -> Unit
) {
    var selectedFilterCategory by remember { mutableStateOf(null as FilterCategory?) }
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
            Image(
                bitmap = photoPreview,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.FillWidth,
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {

                Row(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .height(48.dp)
                        .wrapContentWidth()
                        .background(
                            Color.DarkGray,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    FilterCategory.entries.mapNotNull {
                        when (it) {
                            FilterCategory.CropResize -> SelectableIconButton(
                                selected = selectedFilterCategory == FilterCategory.CropResize,
                                onClick = {
                                    selectedFilterCategory =
                                        selectedFilterCategory.toggle(FilterCategory.CropResize)
                                },
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.baseline_crop_rotate_24),
                                    null,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .fillMaxSize(0.7f)
                                )
                            }

                            FilterCategory.ColorCorrection -> SelectableIconButton(
                                selected = selectedFilterCategory == FilterCategory.ColorCorrection,
                                onClick = {
                                    selectedFilterCategory =
                                        selectedFilterCategory.toggle(FilterCategory.ColorCorrection)
                                },
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.baseline_invert_colors_24),
                                    null,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .fillMaxSize(0.7f)
                                )
                            }

                            FilterCategory.FaceDetect -> SelectableIconButton(
                                selected = selectedFilterCategory == FilterCategory.FaceDetect,
                                onClick = {
                                    selectedFilterCategory =
                                        selectedFilterCategory.toggle(FilterCategory.FaceDetect)
                                },
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.familiar_face_and_zone_24dp_fill0_wght400_grad0_opsz24),
                                    null,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .fillMaxSize(0.7f)
                                )
                            }

                            else -> null
                        }
                    }
                }
                if (selectedFilterCategory != null && Filters.implementations.any { filter -> filter.category == selectedFilterCategory })
                    Row(
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .height(48.dp)
                            .wrapContentWidth()
                            .background(
                                Color.DarkGray,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Filters.implementations.filter { filter -> filter.category == selectedFilterCategory }
                            .map { filter ->
                                SelectableIconButton(
                                    selected = false,
                                    onClick = {
                                        onFilterSelect(filter)
                                    },
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Icon(
                                        when (filter) {
                                            is NothingFilter -> Icons.AutoMirrored.Filled.Announcement
                                            else -> null!!
                                        },
                                        null,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .fillMaxSize(0.7f)
                                    )
                                }
                            }
                    }
            }
        }


    }
}

fun FilterCategory?.toggle(new: FilterCategory?) =
    if (this == new)
        null
    else
        new
