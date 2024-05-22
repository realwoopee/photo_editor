package com.pokhuimand.photoeditor.ui.screens.edit.filters

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import com.pokhuimand.photoeditor.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNothingFilterScreen(
    photoPreview: ImageBitmap,
    onBackPress: () -> Unit,
    onDonePress: () -> Unit,
    onCancelPress: () -> Unit
) {
    BackHandler(onBack = onCancelPress)
    Scaffold(topBar = {
        TopAppBar(title = { },
            navigationIcon = {
                IconButton(
                    onClick = onBackPress
                ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
            }
        )
    }, bottomBar = {
        BottomAppBar() {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onCancelPress) {
                    Icon(
                        ImageVector.vectorResource(id = R.drawable.cancel_24dp_fill0_wght400_grad0_opsz24),
                        null
                    )
                }

                IconButton(onClick = onDonePress) {
                    Icon(Icons.Default.Done, null)
                }
            }
        }
    }) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Image(
                bitmap = photoPreview,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.Fit,
            )
        }
    }

}

