package com.pokhuimand.photoeditor.ui.screens.gallery

import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun GalleryRoute(galleryViewModel: GalleryViewModel) {
    val uiState by galleryViewModel.uiState.collectAsStateWithLifecycle()

    GalleryRoute(
        uiState = uiState,
        onImportPhoto = galleryViewModel::importPhoto,
        onSelectedDelete = galleryViewModel::onSelectedDelete,
        onPhotoShortPress = galleryViewModel::onPhotoShortPress,
        onPhotoLongPress = galleryViewModel::onPhotoLongPress,
        onCameraImport = galleryViewModel::importCamera,
        onExportPress = galleryViewModel::onExport,
        cameraBufferPath = galleryViewModel.cameraBufferUri
    )
}

@Composable
private fun GalleryRoute(
    uiState: GalleryUiState,
    onImportPhoto: (Uri) -> Unit,
    onCameraImport: () -> Unit,
    cameraBufferPath: Uri,
    onSelectedDelete: () -> Unit,
    onExportPress: () -> Unit,
    onPhotoLongPress: (String) -> Unit,
    onPhotoShortPress: (String) -> Unit
) {
    val context = LocalContext.current

    val pickMedia =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null)
                onImportPhoto(uri)
        }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                onCameraImport()
            }
        }
    )

    val importPhoto = {
        pickMedia.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }

    val deviceHasCamera = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

    when (uiState) {
        is GalleryUiState.NoPhotos ->
            GalleryEmptyScreen(
                uiState = GalleryUiState.NoPhotos,
                deviceHasCamera = deviceHasCamera,
                onImportPhoto = importPhoto,
                onSelectedDelete = onSelectedDelete,
                onLaunchCamera = { cameraLauncher.launch(cameraBufferPath) }
            )

        is GalleryUiState.HasPhotos ->
            GalleryPhotosScreen(
                uiState = uiState,
                deviceHasCamera = deviceHasCamera,
                onImportPhoto = importPhoto,
                onSelectedDelete = onSelectedDelete,
                onExportPress = onExportPress,
                onPhotoLongPress = onPhotoLongPress,
                onPhotoShortPress = onPhotoShortPress,
                onLaunchCamera = { cameraLauncher.launch(cameraBufferPath) }
            )

    }
}