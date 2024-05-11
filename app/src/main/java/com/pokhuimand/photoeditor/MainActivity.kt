package com.pokhuimand.photoeditor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import com.pokhuimand.photoeditor.ui.PhotoEditorNavGraph

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()

        val appContainer = (application as PhotoEditorApplication).appContainer;

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }

        setContentView(ComposeView(this).apply {
            setContent {
                PhotoEditorNavGraph(appContainer)
            }
        })
    }
}