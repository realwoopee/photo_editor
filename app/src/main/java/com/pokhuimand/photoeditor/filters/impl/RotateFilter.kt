package com.pokhuimand.photoeditor.filters.impl

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterCategory
import com.pokhuimand.photoeditor.filters.FilterDataCache
import com.pokhuimand.photoeditor.filters.FilterSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

data class RotateFilterSettings(
    val degrees: Float
) :
    FilterSettings() {
    object Ranges {
        val degrees = -90f..270f
    }

    companion object {
        val default = RotateFilterSettings(0f)
    }
}

class RotateFilter : Filter {
    override val id: String = "rotate"
    override val category: FilterCategory = FilterCategory.CropResize

    override suspend fun applyDefaults(image: Bitmap, cache: FilterDataCache): Bitmap {
        return apply(image, RotateFilterSettings.default, cache)
    }

    override suspend fun apply(
        image: Bitmap,
        settings: FilterSettings,
        cache: FilterDataCache
    ): Bitmap {
        val sets = settings as RotateFilterSettings
        return withContext(Dispatchers.Default) {
            rotate(
                image,
                sets.degrees
            )
        }
    }

    private suspend fun rotate(source: Bitmap, degrees: Float): Bitmap = coroutineScope {
        //if (degrees % 90 == 0f) return source
        val radians = Math.toRadians(degrees.toDouble())
        val sin = sin(radians)
        val cos = cos(radians)

        val srcArray = bitmapTo2DArray(source)
        val srcWidth = source.width
        val srcHeight = source.height

        val newWidth = (srcWidth * abs(cos) + srcHeight * abs(sin)).roundToInt()
        val newHeight = (srcWidth * abs(sin) + srcHeight * abs(cos)).roundToInt()

        val rotatedArray = Array(newHeight) { IntArray(newWidth) { Color.TRANSPARENT } }

        val centerX = srcWidth / 2.0
        val centerY = srcHeight / 2.0
        val newCenterX = newWidth / 2.0
        val newCenterY = newHeight / 2.0

        val processorCount = Runtime.getRuntime().availableProcessors()
        val jobs = List(processorCount) { n ->
            async {
                val start = n * (newHeight * newWidth / processorCount)
                val stop =
                    ((n + 1) * (newHeight * newWidth / processorCount)).coerceAtMost(
                        newWidth * newHeight
                    )
                for (i in start until stop) {
                    ensureActive()

                    val y = i / newWidth
                    val x = i - newWidth * y
                    val relativeX = x + 0.5 - newCenterX
                    val relativeY = y + 0.5 - newCenterY

                    val originalX = relativeX * cos + relativeY * sin + centerX - 0.5
                    val originalY = -relativeX * sin + relativeY * cos + centerY - 0.5

                    if (originalX > -1 && originalX < srcWidth && originalY > -1 && originalY < srcHeight) {
                        rotatedArray[y][x] =
                            bilinearInterpolation(srcArray, originalX, originalY)
                    }
                }
            }
        }

        ensureActive()
        jobs.awaitAll()

        return@coroutineScope arrayToBitmap(rotatedArray)
    }

    private fun bilinearInterpolation(array: Array<IntArray>, x: Double, y: Double): Int {
        val x1 = floor(x).toInt()
        val y1 = floor(y).toInt()

        val x2 = x1 + 1
        val y2 = y1 + 1

        // [0, 1]
        val dx = x - x1
        val dy = y - y1

        var color11 = Color.TRANSPARENT
        var color12 = Color.TRANSPARENT
        var color21 = Color.TRANSPARENT
        var color22 = Color.TRANSPARENT

        if (y1 in array.indices && x1 in array[y1].indices)
            color11 = array[y1][x1]
        if (y1 in array.indices && x2 in array[y1].indices)
            color12 = array[y1][x2]
        if (y2 in array.indices && x1 in array[y2].indices)
            color21 = array[y2][x1]
        if (y2 in array.indices && x2 in array[y2].indices)
            color22 = array[y2][x2]


        val r = (1 - dx) * (1 - dy) * Color.red(color11) +
                dx * (1 - dy) * Color.red(color12) +
                (1 - dx) * dy * Color.red(color21) +
                dx * dy * Color.red(color22)

        val g = (1 - dx) * (1 - dy) * Color.green(color11) +
                dx * (1 - dy) * Color.green(color12) +
                (1 - dx) * dy * Color.green(color21) +
                dx * dy * Color.green(color22)

        val b = (1 - dx) * (1 - dy) * Color.blue(color11) +
                dx * (1 - dy) * Color.blue(color12) +
                (1 - dx) * dy * Color.blue(color21) +
                dx * dy * Color.blue(color22)

        val alpha = (1 - dx) * (1 - dy) * Color.alpha(color11) +
                dx * (1 - dy) * Color.alpha(color12) +
                (1 - dx) * dy * Color.alpha(color21) +
                dx * dy * Color.alpha(color22)

        return Color.argb(
            alpha.coerceIn(0.0, 255.0).toInt(),
            r.coerceIn(0.0, 255.0).toInt(),
            g.coerceIn(0.0, 255.0).toInt(),
            b.coerceIn(0.0, 255.0).toInt()
        )
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