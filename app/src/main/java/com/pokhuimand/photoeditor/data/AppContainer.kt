package com.pokhuimand.photoeditor.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.pokhuimand.photoeditor.data.haar.impl.FileSystemHaarModelLoader
import com.pokhuimand.photoeditor.data.photos.impl.FileSystemPhotoRepository
import java.io.File


class AppContainer(private val context: Context) {

    val photosRepository =
        FileSystemPhotoRepository(
            context,
            context.applicationContext.packageName.toString() + ".provider"
        )
    val modelLoader = FileSystemHaarModelLoader(context.resources, context.filesDir)
}