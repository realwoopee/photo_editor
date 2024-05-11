package com.pokhuimand.photoeditor.ui.screens.gallery

import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.leinardi.android.speeddial.compose.SpeedDial
import com.leinardi.android.speeddial.compose.SpeedDialOverlay
import com.leinardi.android.speeddial.compose.SpeedDialState

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

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
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
                        Icon(Icons.Default.DeleteForever, null)
                    }

                    is GalleryPhotosFabState.ImportAvailable -> SpeedDial(
                        state = speedDialState,
                        onFabClick = { expanded ->
                            speedDialState =
                                if (expanded) SpeedDialState.Collapsed else SpeedDialState.Expanded
                        },
                        fabOpenedContent = { Icon(Icons.Default.Cancel, null) },
                        //modifier = Modifier.animateEnterExit(enter = fadeIn(), exit = fadeOut())

                    ) {
                        item {
                            FloatingActionButton(
                                onClick = {
                                    speedDialState = SpeedDialState.Collapsed
                                    onImportPhoto()
                                },
                            ) {
                                Icon(Icons.Default.PhotoLibrary, null)
                            }
                        }

                        if (deviceHasCamera)
                            item {
                                FloatingActionButton(
                                    onClick = {
                                        speedDialState = SpeedDialState.Collapsed
                                    },
                                ) {
                                    Icon(Icons.Default.Camera, null)
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