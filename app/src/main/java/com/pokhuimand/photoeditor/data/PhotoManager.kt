package com.pokhuimand.photoeditor.data

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import java.io.File
import java.util.UUID
import kotlin.io.path.Path

class PhotoManager(private val contentResolver: ContentResolver, private val filesDir: File) {
    fun importContent(uri: Uri?): Unit {
        if (uri == null) return;
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

    fun getPhotoList(): List<String> {
        return filesDir.listFiles()?.map { f -> f.absolutePath }?.toList() ?: emptyList()
    }
}