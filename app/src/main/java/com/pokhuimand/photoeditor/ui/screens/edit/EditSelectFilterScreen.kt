package com.pokhuimand.photoeditor.ui.screens.edit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.pokhuimand.photoeditor.R
import com.pokhuimand.photoeditor.components.SelectableIconButton
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterCategory
import com.pokhuimand.photoeditor.filters.impl.ResizeFilter
import com.pokhuimand.photoeditor.filters.impl.RotateFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.ContrastAndBrightnessFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.DitheringFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.GrayscaleFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.PixelSortingFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.SepiaFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.TempAndTintFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.UnsharpMaskingFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSelectFilterScreen(
    photoPreview: ImageBitmap,
    onBackPress: () -> Unit,
    onFilterSelect: (Filter) -> Unit,
    filters: Set<Filter>
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
        ConstraintLayout(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            val (image, controls) = createRefs()
            BoxWithConstraints(modifier = Modifier
                .constrainAs(image) {
                    top.linkTo(parent.top)
                    bottom.linkTo(controls.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    height = Dimension.fillToConstraints
                }) {
                Image(
                    bitmap = photoPreview,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopCenter),
                    contentScale = ContentScale.Fit,
                )
            }
            Column(
                modifier = Modifier.constrainAs(controls) {
                    bottom.linkTo(anchor = parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
            ) {
                if (selectedFilterCategory != null && filters.any { filter -> filter.category == selectedFilterCategory })
                    Row(
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .height(48.dp)
                            .wrapContentWidth()
                            .align(Alignment.CenterHorizontally)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        filters.filter { filter -> filter.category == selectedFilterCategory }
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
                                                is SepiaFilter -> R.drawable.elderly_woman_24dp_fill0_wght400_grad0_opsz24
                                                is DitheringFilter -> R.drawable.transition_fade_24dp_fill0_wght400_grad0_opsz24
                                                is ContrastAndBrightnessFilter -> R.drawable.baseline_invert_colors_24
                                                is ResizeFilter -> R.drawable.resize_24dp_fill0_wght400_grad0_opsz24
                                                is TempAndTintFilter -> R.drawable.thermostat_24dp_fill0_wght400_grad0_opsz24
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
                        .align(Alignment.CenterHorizontally)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    FilterCategory.entries.map {
                        SelectableIconButton(
                            selected = selectedFilterCategory == it,
                            onClick = {
                                if (it != FilterCategory.FaceRecognition && it != FilterCategory.TriPointTransform)
                                    selectedFilterCategory = selectedFilterCategory.toggle(it)
                                else if (it == FilterCategory.TriPointTransform)
                                    onFilterSelect(filters.first { f -> f.category == FilterCategory.TriPointTransform })
                                else
                                    onFilterSelect(filters.first { f -> f.category == FilterCategory.FaceRecognition })
                            },
                            modifier = Modifier.fillMaxHeight(),
                            selectedModifier = Modifier.background(
                                MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(50)
                            )
                        ) {
                            Icon(
                                painterResource(
                                    id = when (it) {
                                        FilterCategory.CropResize -> R.drawable.baseline_crop_rotate_24
                                        FilterCategory.ColorCorrection -> R.drawable.baseline_invert_colors_24
                                        FilterCategory.FaceRecognition -> R.drawable.familiar_face_and_zone_24dp_fill0_wght400_grad0_opsz24
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