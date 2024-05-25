package com.pokhuimand.photoeditor.ui.screens.edit.filters

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.pokhuimand.photoeditor.R
import com.pokhuimand.photoeditor.components.ProgressSpinner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFilterScreenBase(
    photoPreview: ImageBitmap,
    isProcessingRunning: Boolean,
    onBackPress: () -> Unit,
    onDonePress: () -> Unit,
    onCancelPress: () -> Unit,
    title: @Composable () -> Unit,
    controlsContent: @Composable ColumnScope.() -> Unit
) {
    BackHandler(onBack = onCancelPress)
    Scaffold(topBar = {
        TopAppBar(title = title,
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
                    width = Dimension.fillToConstraints
                }) {
                Image(
                    bitmap = photoPreview,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .aspectRatio(photoPreview.width.toFloat() / photoPreview.height)
                        .fillMaxSize()
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
                modifier = Modifier.constrainAs(controls) {
                    bottom.linkTo(anchor = parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            ) {
                controlsContent()
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