package com.pokhuimand.photoeditor.ui.screens.gallery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pokhuimand.photoeditor.models.Photo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoGrid(
    photos: List<Photo>,
    selectedPhotos: List<Photo>,
    onPhotoShortPress: (photoId: String) -> Unit,
    onPhotoLongPress: (photoId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    LazyVerticalGrid(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        columns = GridCells.Adaptive(96.dp),
    ) {
        photos.groupByTo(sortedMapOf(Comparator.reverseOrder())) { it.creationDate.toLocalDate() }
            .flatMap {
                listOf(item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Text(
                            text = (it.key as LocalDate).format(dateFormatter),
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }) + items(it.value.sortedBy { p -> p.creationDate }) { photo ->
                    key(photo) {
                        Box {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(photo.uri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                clipToBounds = true,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .combinedClickable(onClick = {
                                        onPhotoShortPress(photo.id)
                                    }, onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onPhotoLongPress(photo.id)
                                    })
                            )
                            if (photo in selectedPhotos)
                                Image(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(
                                            Alignment.BottomEnd
                                        )
                                        .padding(4.dp),
                                    colorFilter = ColorFilter.tint(Color.Cyan)
                                )
                        }
                    }
                }
            }
    }

}