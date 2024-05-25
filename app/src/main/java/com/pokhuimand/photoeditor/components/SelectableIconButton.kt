package com.pokhuimand.photoeditor.components

import android.content.res.Resources.Theme
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SelectableIconButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedModifier: Modifier = Modifier,
    icon: @Composable BoxScope.() -> Unit
) {
    IconButton(onClick = onClick, modifier = modifier) {
        AnimatedContent(targetState = selected, label = "SelectableIconButton") { state ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxSize()
                    .then(
                        when (state) {
                            true -> selectedModifier
                            false -> Modifier
                        }
                    )
            ) {
                icon()
            }
        }
    }
}