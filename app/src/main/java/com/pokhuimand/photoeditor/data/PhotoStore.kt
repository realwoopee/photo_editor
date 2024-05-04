package com.pokhuimand.photoeditor.data

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.pokhuimand.photoeditor.models.Photo
import java.io.File
import java.nio.file.attribute.FileTime
import java.time.ZoneId
import java.util.UUID

class PhotoStore(private val contentResolver: ContentResolver, private val filesDir: File) {
    fun importContent(uri: Uri): Unit {
        val outputFile = File(
            filesDir,
            "${UUID.randomUUID().toString()}.${
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
    }

    fun delete(photo: Photo) {
        val file = photo.uri.toFile()
        file.delete()
    }

    fun getPhotoList(): List<Photo> {
        return filesDir.listFiles()?.map { f ->
            Photo(
                f.toUri(),

                (java.nio.file.Files.readAttributes(
                    f.toPath(),
                    "creationTime"
                )["creationTime"] as FileTime).toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            )
        }?.toList() ?: emptyList()
    }
}