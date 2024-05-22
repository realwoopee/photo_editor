package com.pokhuimand.photoeditor.ui.screens.edit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.pokhuimand.photoeditor.R
import com.pokhuimand.photoeditor.components.SelectableIconButton
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterCategory
import com.pokhuimand.photoeditor.filters.Filters
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.ContrastAndBrightnessFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.DitheringFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.GrayscaleFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.PixelSortingFilter
import com.pokhuimand.photoeditor.filters.impl.RotateFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.UnsharpMaskingFilter

@OptIn(ExperimentalMaterial3Api::class)
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
                .fillMaxSize(),
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
                                        ImageVector.vectorResource(
                                            id =
                                            when (filter) {
                                                is UnsharpMaskingFilter -> R.drawable.deblur_24dp_fill0_wght400_grad0_opsz24
                                                is RotateFilter -> R.drawable.autorenew_24dp_fill0_wght400_grad0_opsz24
                                                is GrayscaleFilter -> R.drawable.monochrome_photos_24dp_fill0_wght400_grad0_opsz24
                                                is DitheringFilter -> R.drawable.transition_fade_24dp_fill0_wght400_grad0_opsz24
                                                is ContrastAndBrightnessFilter -> R.drawable.baseline_invert_colors_24


                                                is PixelSortingFilter -> R.drawable.filter_list_24dp_fill0_wght400_grad0_opsz24


                                                else -> R.drawable.sentiment_very_dissatisfied_24dp_fill0_wght400_grad0_opsz24

                                            }
                                        ),
                                        null,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .fillMaxSize(0.7f)
                                    )
                                }
                            }
                    }

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
                    FilterCategory.entries.map {
                        SelectableIconButton(
                            selected = selectedFilterCategory == it,
                            onClick = {
                                selectedFilterCategory =
                                    selectedFilterCategory.toggle(it)
                            },
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Icon(
                                painterResource(
                                    id = when (it) {
                                        FilterCategory.CropResize -> R.drawable.baseline_crop_rotate_24
                                        FilterCategory.ColorCorrection -> R.drawable.baseline_invert_colors_24
                                        FilterCategory.FaceDetect -> R.drawable.familiar_face_and_zone_24dp_fill0_wght400_grad0_opsz24
                                        FilterCategory.Retouch -> R.drawable.point_scan_24dp_fill0_wght400_grad0_opsz24
                                        FilterCategory.TriPointTransform -> R.drawable.workspaces_24dp_fill0_wght400_grad0_opsz24
                                    }
                                ),
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