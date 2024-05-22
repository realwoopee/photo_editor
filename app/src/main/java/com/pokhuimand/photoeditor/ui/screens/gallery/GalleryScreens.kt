package com.pokhuimand.photoeditor.ui.screens.gallery

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.leinardi.android.speeddial.compose.SpeedDial
import com.leinardi.android.speeddial.compose.SpeedDialOverlay
import com.leinardi.android.speeddial.compose.SpeedDialState
import com.pokhuimand.photoeditor.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryEmptyScreen(onImportPhoto: () -> Unit) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Photo Editor"
                    )
                },
            )
        }) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .clickable { onImportPhoto() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Press anywhere to add photos")
        }
    }
}

@OptIn(
    ExperimentalMaterialApi::class, ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun GalleryPhotosScreen(
    uiState: GalleryUiState.HasPhotos,
    deviceHasCamera: Boolean,
    onImportPhoto: () -> Unit,
    onSelectedDelete: () -> Unit,
    onPhotoLongPress: (photoId: String) -> Unit,
    onPhotoShortPress: (photoId: String) -> Unit
) {
    var speedDialState by rememberSaveable { mutableStateOf(SpeedDialState.Collapsed) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Photo Editor"
                    )
                },
            )
        },
        floatingActionButton = {
            AnimatedContent(
                targetState = uiState.toFabState(deviceHasCamera),
                label = "main_view_fab"
            ) {
                when (it) {
                    is GalleryPhotosFabState.PhotosSelected -> FloatingActionButton(
                        onClick = { onSelectedDelete() },
                        //modifier = Modifier.animateEnterExit(enter = fadeIn(), exit = fadeOut())
                    ) {
                        Icon(
                            ImageVector.vectorResource(id = R.drawable.delete_24dp_fill0_wght400_grad0_opsz24),
                            null
                        )
                    }

                    is GalleryPhotosFabState.ImportAvailable -> SpeedDial(
                        state = speedDialState,
                        onFabClick = { expanded ->
                            speedDialState =
                                if (expanded) SpeedDialState.Collapsed else SpeedDialState.Expanded
                        },
                        fabOpenedContent = {
                            Icon(
                                ImageVector.vectorResource(id = R.drawable.cancel_24dp_fill0_wght400_grad0_opsz24),
                                null
                            )
                        },
                        //modifier = Modifier.animateEnterExit(enter = fadeIn(), exit = fadeOut())

                    ) {
                        item {
                            FloatingActionButton(
                                onClick = {
                                    speedDialState = SpeedDialState.Collapsed
                                    onImportPhoto()
                                },
                            ) {
                                Icon(
                                    ImageVector.vectorResource(id = R.drawable.add_photo_alternate_24dp_fill0_wght400_grad0_opsz24),
                                    null
                                )
                            }
                        }

                        if (deviceHasCamera)
                            item {
                                FloatingActionButton(
                                    onClick = {
                                        speedDialState = SpeedDialState.Collapsed
                                    },
                                ) {
                                    Icon(
                                        ImageVector.vectorResource(id = R.drawable.add_a_photo_24dp_fill0_wght400_grad0_opsz24),
                                        null
                                    )
                                }
                            }
                    }
                }

            }
        }) { innerPadding ->

        val interactionSource = remember { MutableInteractionSource() }

        PhotoGrid(
            photos = uiState.photos,
            selectedPhotos = uiState.selectedPhotos,
            onPhotoShortPress = { onPhotoShortPress(it) },
            onPhotoLongPress = { onPhotoLongPress(it) },
            modifier = Modifier
                .padding(innerPadding)
                .wrapContentSize()
        )
        SpeedDialOverlay(
            visible = speedDialState == SpeedDialState.Expanded,
            onClick = { },
            modifier = Modifier.clickable(
                interactionSource = interactionSource,
                indication = null
            ) { speedDialState = speedDialState.toggle() }
        )
    }
}

private sealed class GalleryPhotosFabState {
    data object PhotosSelected : GalleryPhotosFabState()
    data class ImportAvailable(val deviceHasCamera: Boolean) : GalleryPhotosFabState()
}

private fun GalleryUiState.HasPhotos.toFabState(deviceHasCamera: Boolean) =
    if (this.selectedPhotos.isNotEmpty())
        GalleryPhotosFabState.PhotosSelected
    else
        GalleryPhotosFabState.ImportAvailable(deviceHasCamera)