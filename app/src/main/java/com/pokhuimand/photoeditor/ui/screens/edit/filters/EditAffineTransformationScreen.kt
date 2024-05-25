package com.pokhuimand.photoeditor.ui.screens.edit.filters

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.pokhuimand.photoeditor.R
import com.pokhuimand.photoeditor.components.ProgressSpinner
import com.pokhuimand.photoeditor.filters.impl.AffineTransformationSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAffineTransformationScreen(
    photoPreview: ImageBitmap,
    isProcessingRunning: Boolean,
    onBackPress: () -> Unit,
    onDonePress: () -> Unit,
    onCancelPress: () -> Unit,
    onFilterSettingsUpdate: (AffineTransformationSettings) -> Unit
) {
    val runFilter = remember<(List<Offset>) -> Unit>(onFilterSettingsUpdate) {
        { filterSettings ->
            val settings =
                AffineTransformationSettings(
                    emptyList(), emptyList()
                )

            for (i in 0..<minOf(3, filterSettings.size)) {
                settings.pointSet1 += filterSettings[i]
            }

            for (i in 3..<filterSettings.size) {
                settings.pointSet2 += filterSettings[i]
            }
            onFilterSettingsUpdate(settings)
        }
    }

    var filterSettings by remember {
        mutableStateOf(emptyList<Offset>())
    }
    BackHandler(onBack = onCancelPress)
    Scaffold(topBar = {
        TopAppBar(title = { },
            navigationIcon = {
                IconButton(
                    onClick = onBackPress
                ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            },
            actions = {
                if (filterSettings.isNotEmpty())
                    IconButton(
                        onClick = {
                            filterSettings = filterSettings.dropLast(1)
                            runFilter(filterSettings)
                        }
                    ) {
                        Icon(
                            ImageVector.vectorResource(id = R.drawable.undo_24dp_fill0_wght400_grad0_opsz24),
                            null
                        )
                    }
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
                    width = Dimension.fillToConstraints
                }) {
                Image(
                    bitmap = photoPreview,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .aspectRatio(photoPreview.width.toFloat() / photoPreview.height)
                        .fillMaxSize()
                        .align(Alignment.Center)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    filterSettings = filterSettings + Offset(
                                        offset.x / size.width,
                                        offset.y / size.height
                                    )
                                    runFilter(filterSettings)
                                }
                            )
                        },
                    colorFilter = if (isProcessingRunning) ColorFilter.tint(
                        Color.LightGray.copy(alpha = 0.3f),
                        BlendMode.SrcOver
                    ) else null
                )
                if (isProcessingRunning)
                    ProgressSpinner(modifier = Modifier.align(Alignment.Center))
            }
            Column(
                modifier = Modifier.constrainAs(controls) {
                    bottom.linkTo(anchor = parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondary)
                        .align(Alignment.CenterHorizontally)
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
