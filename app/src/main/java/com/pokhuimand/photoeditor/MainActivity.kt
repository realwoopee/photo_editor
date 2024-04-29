package com.pokhuimand.photoeditor

import android.os.Bundle
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import androidx.core.view.size
import com.pokhuimand.photoeditor.data.PhotoManager
import com.pokhuimand.photoeditor.ui.GalleryView
import com.pokhuimand.photoeditor.ui.GalleryViewModel

class MainActivity : AppCompatActivity() {

    private var photoManager: PhotoManager? = null;
    private val galleryViewModel: GalleryViewModel by viewModels { GalleryViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }


        photoManager = PhotoManager(contentResolver, filesDir);

        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                // Callback is invoked after the user selects media items or closes the
                // photo picker.
                galleryViewModel.importPhoto(uri)
            }

        val onPickMedia = {
            pickMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        };

        setContentView(ComposeView(this).apply {
            setContent {
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
                    val state by galleryViewModel.state.collectAsState()
                    Box(modifier = Modifier.clickable { onPickMedia() }) {

                        GalleryView(
                            state = state,
                            modifier = Modifier
                                .padding(innerPadding)
                                .wrapContentSize()
                        )
                    }
                }
            }
        })
    }
}