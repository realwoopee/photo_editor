package com.pokhuimand.photoeditor.screens.main

import android.content.Context
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.leinardi.android.speeddial.compose.BuildConfig
import com.leinardi.android.speeddial.compose.SpeedDial
import com.leinardi.android.speeddial.compose.SpeedDialOverlay
import com.leinardi.android.speeddial.compose.SpeedDialState
import java.io.File
import java.util.Objects
import java.util.UUID

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
@Composable
fun MainView(mainViewModel: MainViewModel = viewModel(factory = MainViewModel.Factory)) {
    val state: MainViewState by mainViewModel.state.collectAsState()

    val context = LocalContext.current

    val deviceHasCamera = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

    var speedDialState by rememberSaveable { mutableStateOf(SpeedDialState.Collapsed) }

    val pickMedia =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null)
                mainViewModel.importPhoto(uri)
        }

    val importPhoto = {
        pickMedia.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    };


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
            AnimatedContent(targetState = state, label = "main_view_fab") { targetState ->
                if (targetState.selectedPhotos.any())
                    FloatingActionButton(
                        onClick = { mainViewModel.onSelectedDelete() },
                        //modifier = Modifier.animateEnterExit(enter = fadeIn(), exit = fadeOut())
                    ) {
                        Icon(Icons.Default.DeleteForever, null)
                    }
                else if (targetState.photos.any())
                    SpeedDial(
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
                                    importPhoto()
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
        }) { innerPadding ->
        if (state.photos.isEmpty())
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .clickable { importPhoto() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Press anywhere to add photos")
            }
        else {
            val interactionSource = remember { MutableInteractionSource() }

            PhotoGrid(
                photos = state.photos,
                selectedPhotos = state.selectedPhotos,
                onPhotoShortPress = { mainViewModel.onPhotoShortPress(it) },
                onPhotoLongPress = { mainViewModel.onPhotoLongPress(it) },
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
}

fun Context.createImageFile(): File {
    // Create an image file name
    val imageFileName = "JPEG_" + UUID.randomUUID() + "_"
    return File.createTempFile(
        imageFileName, /* prefix */
        ".jpg", /* suffix */
        externalCacheDir /* directory */
    )
}