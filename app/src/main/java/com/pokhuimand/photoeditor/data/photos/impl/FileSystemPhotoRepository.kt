package com.pokhuimand.photoeditor.data.photos.impl

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.pokhuimand.photoeditor.data.photos.PhotosRepository
import com.pokhuimand.photoeditor.models.Photo
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.nio.file.attribute.FileTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

private fun getFileCreationDate(file: File): LocalDateTime {
    return (java.nio.file.Files.readAttributes(
        file.toPath(),
        "creationTime"
    )["creationTime"] as FileTime).toInstant().atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}

class FileSystemPhotoRepository(
    private val contentResolver: ContentResolver,
    private val filesDir: File
) : PhotosRepository {
    private val photos = MutableStateFlow<List<Photo>>(emptyList())

    init {
        photos.update {
            filesDir.listFiles()?.map { f ->
                Photo(
                    f.nameWithoutExtension,
                    f.toUri(),
                    getFileCreationDate(f)
                )
            }?.toList() ?: emptyList()
        }
    }

    override fun getPhoto(photoId: String): Photo {
        return checkNotNull(photos.value.find { it.id == photoId })
    }

    override fun removePhoto(photoId: String) {
        removePhotos(setOf(photoId))
    }

    override fun removePhotos(photoIds: Set<String>) {
        val files =
            checkNotNull(filesDir.listFiles()?.filter { it.nameWithoutExtension in photoIds })

        val photosToDelete = files.mapTo(HashSet()) {
            Photo(
                it.nameWithoutExtension,
                it.toUri(),
                getFileCreationDate(it)
            )
        }

        photos.update {
            it - photosToDelete
        }

        files.forEach { it.delete() }
    }

    override fun importContent(uri: Uri) {
        val photoId = UUID.randomUUID().toString()

        val outputFile = File(
            filesDir,
            "${photoId}.${
                MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(contentResolver.getType(uri))
            }"
        )

        Log.i(
            "debugInfo",
            "Image is of type ${
                MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(contentResolver.getType(uri))
            }"
        )
        contentResolver.openInputStream(uri).use {
            outputFile.outputStream().use { of ->
                Log.i("debugInfo", "Written ${it?.copyTo(of)} bytes")
                of.flush()
            }
        }
        Log.i("debugInfo", outputFile.absolutePath)

        photos.update {
            it + Photo(
                outputFile.nameWithoutExtension,
                outputFile.toUri(),
                getFileCreationDate(outputFile)
            )
        }
    }

    override fun observePhotos(): Flow<List<Photo>> = photos
    override fun getPhotos(): List<Photo> {
        return photos.value;
    }
}