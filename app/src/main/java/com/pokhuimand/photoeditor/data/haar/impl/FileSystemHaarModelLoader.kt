package com.pokhuimand.photoeditor.data.haar.impl

import android.content.res.Resources
import com.pokhuimand.photoeditor.R
import com.pokhuimand.photoeditor.data.haar.HaarModelLoader
import java.io.File

class FileSystemHaarModelLoader
    (resources: Resources, private val filesDir: File) : HaarModelLoader {
    private val modelFileName: String = "haarcascade_frontalface_default.xml"
    private val modelsDirectory = "cascade"
    private val cascadeFile: File

    init {
        val inputStream = resources.openRawResource(R.raw.haarcascade_frontalface_default)
        File(filesDir, "$modelsDirectory/").mkdir()
        cascadeFile = File(filesDir, "$modelsDirectory/$modelFileName")
        inputStream.use { input ->
            cascadeFile.outputStream().use { output -> input.copyTo(output) }
        }
    }

    override val modelFilePath: String
        get() = cascadeFile.path
}