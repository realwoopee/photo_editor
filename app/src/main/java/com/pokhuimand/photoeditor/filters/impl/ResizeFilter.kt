package com.pokhuimand.photoeditor.filters.impl

import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterCategory
import com.pokhuimand.photoeditor.filters.FilterDataCache
import com.pokhuimand.photoeditor.filters.FilterSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

data class ResizeFilterSettings(
    val coefficient: Float
) :
    FilterSettings() {
    companion object {
        val default = ResizeFilterSettings(1.0f)
    }
}

class ResizeFilter : Filter {
    override val id: String = "resize"
    override val category: FilterCategory = FilterCategory.CropResize

    override suspend fun applyDefaults(image: Bitmap, cache: FilterDataCache): Bitmap {
        return apply(image, ResizeFilterSettings.default, cache)
    }

    override suspend fun apply(
        image: Bitmap,
        settings: FilterSettings,
        cache: FilterDataCache
    ): Bitmap {
        val sets = settings as ResizeFilterSettings
        return withContext(Dispatchers.Default) {
            resize(
                image,
                sets.coefficient
            )
        }
    }

    private suspend fun resize(source: Bitmap, coefficient: Float): Bitmap {
        val width = source.width
        val height = source.height
        val newWidth = Math.round(width * coefficient)
        val newHeight = Math.round(height * coefficient)
        return if (abs(coefficient - 1.0f) < 0.01f)
            source
        else if (coefficient <= 0.5f)
            trilinearResize(source, newWidth, newHeight)
        else
            bilinearResize(source, newWidth, newHeight)
    }

    private suspend fun bilinearResize(image: Bitmap, newWidth: Int, newHeight: Int): Bitmap =
        coroutineScope {
            val width = image.width
            val height = image.height
            val initialArray = bitmapTo2DArray(image)
            val resultArray = Array(newHeight) { IntArray(newWidth) { Color.TRANSPARENT } }


            val processorCount = Runtime.getRuntime().availableProcessors()
            val jobs = List(processorCount) { n ->
                async {
                    val start = n * (newHeight * newWidth / processorCount)
                    val stop =
                        ((n + 1) * (newHeight * newWidth / processorCount)).coerceAtMost(
                            newWidth * newHeight
                        )
                    for (i in start until stop) {
                        val y = i / newWidth
                        val x = i - newWidth * y


                        val u = (x.toDouble()) / (newWidth - 1) * (width - 1)
                        val v = (y.toDouble()) / (newHeight - 1) * (height - 1)

                        resultArray[y][x] = bilinearInterpolation(initialArray, u, v)
                    }
                }
            }

            jobs.awaitAll()
            return@coroutineScope arrayToBitmap(resultArray)
        }

    private fun bilinearInterpolation(array: Array<IntArray>, x: Double, y: Double): Int =
        bilinearInterpolation({ u, v -> array[v][u] }, array.size, array[0].size, x, y)

    private inline fun bilinearInterpolation(
        getter: (x: Int, y: Int) -> Int,
        height: Int,
        width: Int,
        x: Double,
        y: Double
    ): Int {
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

        if (y1 in 0 until height && x1 in 0 until width)
            color11 = getter(x1, y1)
        if (y1 in 0 until height && x2 in 0 until width)
            color12 = getter(x2, y1)
        if (y2 in 0 until height && x1 in 0 until width)
            color21 = getter(x1, y2)
        if (y2 in 0 until height && x2 in 0 until width)
            color22 = getter(x2, y2)


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

    private fun colorLerp(@ColorInt alpha: Int, @ColorInt beta: Int, x: Double): Int {
        val r = (alpha.red * (1 - x) + beta.red * x).roundToInt()
        val b = (alpha.blue * (1 - x) + beta.blue * x).roundToInt()
        val g = (alpha.green * (1 - x) + beta.green * x).roundToInt()
        val a = (alpha.alpha * (1 - x) + beta.alpha * x).roundToInt()

        return Color.argb(a, r, g, b)
    }

    private suspend fun trilinearResize(image: Bitmap, newWidth: Int, newHeight: Int): Bitmap =
        coroutineScope {
            val width = image.width
            val height = image.height
            val source = bitmapTo2DArray(image)
            val resultArray = Array(newHeight) { IntArray(newWidth) { Color.TRANSPARENT } }

            val processorCount = Runtime.getRuntime().availableProcessors()
            val jobs = List(processorCount) { n ->
                async {
                    val start = n * (newHeight * newWidth / processorCount)
                    val stop =
                        ((n + 1) * (newHeight * newWidth / processorCount)).coerceAtMost(
                            newWidth * newHeight
                        )
                    for (i in start until stop) {
                        val y = i / newWidth
                        val x = i - newWidth * y
                        val u = (x.toDouble()) / (newWidth - 1)
                        val v = (y.toDouble()) / (newHeight - 1)

                        val scale = (width - 1) / (newWidth - 1).toDouble()
                        val alphaScale = ceil(scale)
                        val betaScale = floor(scale)
                        val between = 1.0 - (scale - floor(scale))


                        val alphaRadius = (alphaScale / 2).toInt()
                        val betaRadius = (betaScale / 2).toInt()

                        val alphaHeight = source.size / (alphaRadius)
                        val alphaWidth = source[0].size / (alphaRadius)

                        val betaHeight = source.size / (betaRadius)
                        val betaWidth = source[0].size / (betaRadius)

                        val alpha = bilinearInterpolation(
                            { x0, y0 ->
                                boxDownSample(
                                    source,
                                    (x0 * alphaRadius),
                                    (y0 * alphaRadius),
                                    (alphaRadius)
                                )
                            },
                            alphaHeight,
                            alphaWidth,
                            u * (alphaWidth - 1),
                            v * (alphaHeight - 1)
                        )

                        val beta = bilinearInterpolation(
                            { x0, y0 ->
                                boxDownSample(
                                    source,
                                    (x0 * betaRadius),
                                    (y0 * betaRadius),
                                    (betaRadius)
                                )

                            },
                            betaHeight,
                            betaWidth,
                            u * (betaWidth - 1),
                            v * (betaHeight - 1)
                        )

                        resultArray[y][x] = colorLerp(alpha, beta, between)
                    }
                }

            }

            jobs.awaitAll()
            return@coroutineScope arrayToBitmap(resultArray)
        }

    private fun boxDownSample(image: Array<IntArray>, x: Int, y: Int, radius: Int): Int {
        var rSum = 0
        var gSum = 0
        var bSum = 0
        var aSum = 0
        var count = 0

        for (dx in (-radius..radius))
            for (dy in (-radius..radius))
                if ((y + dy) in image.indices && (x + dx) in image[y + dy].indices) {
                    val pixel = image[y + dy][x + dx]
                    rSum += pixel.red
                    gSum += pixel.green
                    bSum += pixel.blue
                    aSum += pixel.alpha
                    count++
                }

        if (count > 0)
            return Color.argb(aSum / count, rSum / count, gSum / count, bSum / count)
        return Color.TRANSPARENT
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