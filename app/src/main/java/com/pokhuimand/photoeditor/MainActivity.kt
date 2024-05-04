package com.pokhuimand.photoeditor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.ViewCompat
import com.pokhuimand.photoeditor.screens.main.MainView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }

        setContentView(ComposeView(this).apply {
            setContent {
                MainView()
            }
        })
    }
}