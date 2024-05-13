package com.pokhuimand.photoeditor.data

import android.content.Context
import com.pokhuimand.photoeditor.data.photos.impl.FileSystemPhotoRepository

class AppContainer(private val context: Context) {
    val photosRepository = FileSystemPhotoRepository(context.contentResolver, context.filesDir)
}