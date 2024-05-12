package com.pokhuimand.photoeditor.components

import android.content.res.Resources.Theme
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

@Composable
fun SelectableIconButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
                            true -> Modifier.background(
                                MaterialTheme.colors.secondary,
                                shape = RoundedCornerShape(15)
                            )

                            false -> Modifier
                        }
                    )
            ) {
                icon()
            }
        }
    }
}