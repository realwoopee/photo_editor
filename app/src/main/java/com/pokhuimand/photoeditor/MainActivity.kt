package com.pokhuimand.photoeditor

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import com.pokhuimand.photoeditor.ui.PhotoEditorNavGraph
import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()

        if (OpenCVLoader.initLocal()) {
            Log.i("OpenCV", "OpenCV successfully loaded.");
        }

        val appContainer = (application as PhotoEditorApplication).appContainer;

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }

        setContentView(ComposeView(this).apply {
            setContent {
                PhotoEditorNavGraph(appContainer)
            }
        })
    }
}