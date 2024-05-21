package com.pokhuimand.photoeditor.filters.impl

import android.graphics.Bitmap
import android.graphics.Color
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterDataCache
import com.pokhuimand.photoeditor.filters.FilterCategory
import com.pokhuimand.photoeditor.filters.FilterSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
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

//TODO: implement proper threshold
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

    private fun rotate(source: Bitmap, degrees: Float): Bitmap {
        val radians = Math.toRadians(degrees.toDouble())
        val sin = sin(radians)
        val cos = cos(radians)

        val srcArray = bitmapTo2DArray(source)
        val srcWidth = source.width
        val srcHeight = source.height

        val newWidth = (srcWidth * abs(cos) + srcHeight * abs(sin)).toInt()
        val newHeight = (srcWidth * abs(sin) + srcHeight * abs(cos)).toInt()

        val rotatedArray = Array(newHeight) { IntArray(newWidth) { Color.TRANSPARENT } }

        val centerX = srcWidth / 2
        val centerY = srcHeight / 2
        val newCenterX = newWidth / 2
        val newCenterY = newHeight / 2

        for (x in 0 until newWidth) {
            for (y in 0 until newHeight) {
                val relativeX = x - newCenterX
                val relativeY = y - newCenterY

                val originalX = (relativeX * cos + relativeY * sin + centerX).toFloat()
                val originalY = (-relativeX * sin + relativeY * cos + centerY).toFloat()

                if (originalX >= 0 && originalX < srcWidth - 1 && originalY >= 0 && originalY < srcHeight - 1) {
                    rotatedArray[y][x] = bilinearInterpolation(srcArray, originalX, originalY)
                }
            }
        }

        return arrayToBitmap(rotatedArray)
    }

    private fun bilinearInterpolation(array: Array<IntArray>, x: Float, y: Float): Int {
        val x1 = floor(x).toInt()
        val y1 = floor(y).toInt()
        val x2 = x1 + 1
        val y2 = y1 + 1

        // [0, 1]
        val xFraction = x - x1
        val yFraction = y - y1

        val color11 = array[y1][x1]
        val color12 = array[y1][x2]
        val color21 = array[y2][x1]
        val color22 = array[y2][x2]

        val r = (1 - xFraction) * (1 - yFraction) * Color.red(color11) +
                xFraction * (1 - yFraction) * Color.red(color12) +
                (1 - xFraction) * yFraction * Color.red(color21) +
                xFraction * yFraction * Color.red(color22)

        val g = (1 - xFraction) * (1 - yFraction) * Color.green(color11) +
                xFraction * (1 - yFraction) * Color.green(color12) +
                (1 - xFraction) * yFraction * Color.green(color21) +
                xFraction * yFraction * Color.green(color22)

        val b = (1 - xFraction) * (1 - yFraction) * Color.blue(color11) +
                xFraction * (1 - yFraction) * Color.blue(color12) +
                (1 - xFraction) * yFraction * Color.blue(color21) +
                xFraction * yFraction * Color.blue(color22)

        val alpha = (1 - xFraction) * (1 - yFraction) * Color.alpha(color11) +
                xFraction * (1 - yFraction) * Color.alpha(color12) +
                (1 - xFraction) * yFraction * Color.alpha(color21) +
                xFraction * yFraction * Color.alpha(color22)

        return Color.argb(
            alpha.coerceIn(0f, 255f).toInt(),
            r.coerceIn(0f, 255f).toInt(),
            g.coerceIn(0f, 255f).toInt(),
            b.coerceIn(0f, 255f).toInt()
        )
    }

    private fun bitmapTo2DArray(source: Bitmap): Array<IntArray> {
        val width = source.width
        val height = source.height
        val result = Array(height) { IntArray(width) }
        for (y in 0 until height) {
            for (x in 0 until width) {
                result[y][x] = source.getPixel(x, y)
            }
        }
        return result
    }

    private fun arrayToBitmap(array: Array<IntArray>): Bitmap {
        val height = array.size
        val width = array[0].size
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (y in 0 until height) {
            for (x in 0 until width) {
                result.setPixel(x, y, array[y][x])
            }
        }
        return result
    }

}