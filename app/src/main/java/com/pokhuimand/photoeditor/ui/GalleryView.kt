package com.pokhuimand.photoeditor.ui

import android.graphics.BitmapFactory
import android.media.ImageReader
import android.widget.GridView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import coil.transform.Transformation
import javax.xml.transform.Transformer

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryView(state: GalleryState, modifier: Modifier) {
    if (state.photos.isEmpty())
        Text(text = "No photos")
    else
    /*LazyColumn(modifier, verticalArrangement = Arrangement.Top) {
        item { Text(text = state.photos.count().toString()) }
        state.photos.groupByTo(sortedMapOf()) { p -> p.firstOrNull()?.toString() ?: "Null" }
            .forEach()
            { (date, photos) ->

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(Color.Green)
                    ) {
                        Text(text = date.toString())
                    }
                }

                item {

                }
            }
    }*/
        LazyVerticalGrid(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth(),
            //userScrollEnabled = false,
            columns = GridCells.Adaptive(96.dp),
        ) {
            state.photos.groupByTo(sortedMapOf()) { p ->
                p.slice(p.count() - 5 until p.count())
            }.flatMap { it ->
                listOf(item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Text(text = it.key.toString())
                    }
                }) + items(it.value) { photo ->
                    key(photo) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(photo)
                                .crossfade(true)
                                .build(),
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            clipToBounds = true,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(2.dp)
                        )
                    }
                }
            }
        }

}