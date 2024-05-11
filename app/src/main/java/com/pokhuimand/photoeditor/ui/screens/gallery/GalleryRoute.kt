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

    GalleryRoute(uiState = uiState,
        onImportPhoto = { galleryViewModel.importPhoto(it) },
        onSelectedDelete = { galleryViewModel.onSelectedDelete() },
        onPhotoShortPress = { galleryViewModel.onPhotoShortPress(it) },
        onPhotoLongPress = { galleryViewModel.onPhotoLongPress(it) })
}

@Composable
private fun GalleryRoute(
    uiState: GalleryUiState,
    onImportPhoto: (Uri) -> Unit,
    onSelectedDelete: () -> Unit,
    onPhotoLongPress: (String) -> Unit,
    onPhotoShortPress: (String) -> Unit
) {
    val pickMedia =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null)
                onImportPhoto(uri)
        }

    val importPhoto = {
        pickMedia.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    };

    val context = LocalContext.current

    val deviceHasCamera = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

    when (uiState) {
        is GalleryUiState.NoPhotos -> GalleryEmptyScreen(onImportPhoto = importPhoto)
        is GalleryUiState.HasPhotos ->
            GalleryPhotosScreen(
                uiState = uiState,
                deviceHasCamera = deviceHasCamera,
                onImportPhoto = importPhoto,
                onSelectedDelete = onSelectedDelete,
                onPhotoLongPress = onPhotoLongPress,
                onPhotoShortPress = onPhotoShortPress
            )

    }
}