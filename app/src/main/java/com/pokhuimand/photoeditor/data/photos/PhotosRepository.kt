package com.pokhuimand.photoeditor.data.photos

import android.graphics.Bitmap
import android.net.Uri
import com.pokhuimand.photoeditor.models.Photo
import kotlinx.coroutines.flow.Flow

interface PhotosRepository {
    fun getPhoto(photoId: String): Photo
    fun getPhotos(): List<Photo>

    fun removePhoto(photoId: String)

    fun removePhotos(photoIds: Set<String>)

    fun importContent(uri: Uri)

    fun observePhotos(): Flow<List<Photo>>
    fun savePhoto(photoId: String, image: Bitmap)

    val photoBufferUri: Uri

    fun importPhotoBuffer()
}