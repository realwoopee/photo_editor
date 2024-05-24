package com.pokhuimand.photoeditor.filters.impl

import android.graphics.Bitmap
import android.graphics.Color
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterCategory
import com.pokhuimand.photoeditor.filters.FilterDataCache
import com.pokhuimand.photoeditor.filters.FilterSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.objdetect.CascadeClassifier
import kotlin.coroutines.cancellation.CancellationException


data class FaceRecognitionSettings(
    val rectBorderWidth: Int
) :
    FilterSettings() {
    object Ranges {
        val rectBorderWidth = 0..20
    }

    companion object {
        val default = FaceRecognitionSettings(5)
    }
}

class FaceRecognition(private val pathToModel: String) : Filter {

    override val id: String = "faceRecognition"
    override val category: FilterCategory = FilterCategory.FaceRecognition

    private fun drawRect(rect: Rect, srcArray: Array<IntArray>, borderWidth: Int) {
        val x = rect.x
        val y = rect.y
        val rectWidth = rect.width
        val rectHeight = rect.height
        val srcWidth = srcArray[0].size
        val srcHeight = srcArray.size
        for (j in x.coerceIn(0, srcWidth) until (x + rectWidth).coerceIn(0, srcWidth)) {
            for (addition in 0..borderWidth) {
                if (y + addition in srcArray.indices)
                    srcArray[y + addition][j] = Color.rgb(255, 255, 0)
                if (y + rectHeight + addition in srcArray.indices)
                    srcArray[y + rectHeight + addition][j] = Color.rgb(255, 255, 0)
            }
        }

        for (i in y.coerceIn(0, srcHeight) until (y + rectHeight).coerceIn(0, srcHeight)) {
            for (addition in 0..borderWidth) {
                if (x + addition in srcArray[0].indices)
                    srcArray[i][x + addition] = Color.rgb(255, 255, 0)
                if (x + rectWidth + addition in srcArray[0].indices)
                    srcArray[i][x + rectWidth + addition] = Color.rgb(255, 255, 0)
            }
        }
    }

    private suspend fun detectFaces(source: Bitmap, rectBorderWidth: Int): Bitmap = coroutineScope {
        val faceDetections = MatOfRect()

        val sourceMat = Mat()
        Utils.bitmapToMat(source, sourceMat)

        val cascadeClassifier = CascadeClassifier()

        ensureActive()

        cascadeClassifier.load(pathToModel)

        ensureActive()

        cascadeClassifier.detectMultiScale(
            sourceMat,
            faceDetections,
            1.1,
            5,
            0,
            Size(
                0.2 * minOf(source.width, source.height),
                0.2 * minOf(source.width, source.height)
            ),
            Size()
        )

        ensureActive()

        val srcArray = bitmapTo2DArray(source)

        for (rect in faceDetections.toList()) {
            ensureActive()

            drawRect(rect, srcArray, rectBorderWidth)
        }
        return@coroutineScope arrayToBitmap(srcArray)
    }

    override suspend fun apply(
        image: Bitmap,
        settings: FilterSettings,
        cache: FilterDataCache
    ): Bitmap {
        val sets = settings as FaceRecognitionSettings
        return withContext(Dispatchers.Default) {
            detectFaces(
                image,
                sets.rectBorderWidth
            )
        }
    }

    override suspend fun applyDefaults(image: Bitmap, cache: FilterDataCache): Bitmap {
        return apply(image, FaceRecognitionSettings.default, cache)
    }

    private fun bitmapTo2DArray(source: Bitmap): Array<IntArray> {
        val width = source.width
        val height = source.height
        val result = Array(height) { IntArray(width) }
        for (y in 0 until height) {
            source.getPixels(result[y], 0, width, 0, y, width, 1)
        }
        return result
    }

    private fun arrayToBitmap(array: Array<IntArray>): Bitmap {
        val height = array.size
        val width = array[0].size
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (y in 0 until height) {
            result.setPixels(array[y], 0, width, 0, y, width, 1)
        }
        return result
    }
}