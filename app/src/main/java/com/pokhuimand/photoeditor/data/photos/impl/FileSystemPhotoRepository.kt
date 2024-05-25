package com.pokhuimand.photoeditor.data.photos.impl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.pokhuimand.photoeditor.data.photos.PhotosRepository
import com.pokhuimand.photoeditor.models.Photo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

private fun getFileCreationDate(file: File): LocalDateTime {
    return (Files.readAttributes(
        file.toPath(),
        "creationTime"
    )["creationTime"] as FileTime).toInstant().atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}


const val tempDir = "temp"
const val photoBufferName = "photo.png"
const val photoBufferPath = "$tempDir/$photoBufferName"

class FileSystemPhotoRepository(
    context: Context,
    providerAuthority: String,
) : PhotosRepository {
    private val photos = MutableStateFlow<List<Photo>>(emptyList())
    private val contentResolver = context.contentResolver
    private val filesDir = context.filesDir
    override val photoBufferUri: Uri

    init {
        File(filesDir, tempDir).mkdirs()
        File(filesDir, photoBufferPath).createNewFile()
        photoBufferUri = FileProvider.getUriForFile(
            context,
            providerAuthority,
            File(filesDir, photoBufferPath)
        )

        photos.update {
            filesDir.listFiles()?.filter { f -> f.isFile && f.extension == "png" }?.map { f ->
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
            "${photoId}.png"
        )

        contentResolver.openInputStream(uri).use {
            outputFile.outputStream().use { of ->
                val image = BitmapFactory.decodeStream(it)
                image.compress(Bitmap.CompressFormat.PNG, 100, of)
                of.flush()
            }
        }

        photos.update {
            it + Photo(
                outputFile.nameWithoutExtension,
                outputFile.toUri(),
                getFileCreationDate(outputFile)
            )
        }
    }

    override fun savePhoto(photoId: String, image: Bitmap) {
        val photo = getPhoto(photoId)
        val file = photo.uri.toFile()
        file.outputStream().use {
            image.compress(Bitmap.CompressFormat.PNG, 100, it)
            it.flush()
        }
        photos.update { it - photo + photo.copy(creationDate = getFileCreationDate(file)) }
    }

    override fun observePhotos(): Flow<List<Photo>> = photos
    override fun getPhotos(): List<Photo> {
        return photos.value;
    }

    override fun importPhotoBuffer() {
        importContent(photoBufferUri)
    }
}