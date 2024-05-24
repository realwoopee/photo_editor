package com.pokhuimand.photoeditor.filters.impl

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.core.graphics.red
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterCategory
import com.pokhuimand.photoeditor.filters.FilterDataCache
import com.pokhuimand.photoeditor.filters.FilterSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.floor

data class AffineTransformationSettings(
    var pointSet1: List<Offset>,
    var pointSet2: List<Offset>
) : FilterSettings()

class AffineTransformation : Filter {
    override val id: String = "affine"
    override val category: FilterCategory = FilterCategory.TriPointTransform

    override suspend fun apply(
        image: Bitmap,
        settings: FilterSettings,
        cache: FilterDataCache
    ): Bitmap {
        val sets = settings as AffineTransformationSettings
        return withContext(Dispatchers.Default) {
            applyAffineTransformation(
                image,
                sets.pointSet1,
                sets.pointSet2
            )
        }
    }

    override suspend fun applyDefaults(image: Bitmap, cache: FilterDataCache): Bitmap {
        return image
    }

    fun drawCircles(src: Bitmap, offsetList: List<Offset>): Bitmap {
        val srcArray = bitmapTo2DArray(src)
        var colorList: List<Color>

        colorList = listOf(
            Color.valueOf(1.0f, 0.0f, 0.0f),
            Color.valueOf(0.0f, 1.0f, 0.0f),
            Color.valueOf(0.0f, 0.0f, 1.0f)
        )
        for (i in 0..<offsetList.size) {
            var offset = offsetList[i]
            var color = colorList[i % 3]
            val centerX = (offset.x * (src.width - 1)).toInt()
            val centerY = (offset.y * (src.height - 1)).toInt()
            val radius = 20

            for (y in (centerY - radius) until (centerY + radius)) {
                for (x in (centerX - radius) until (centerX + radius)) {
                    if ((centerX - x) * (centerX - x) + (centerY - y) * (centerY - y) <= radius * radius) {
                        if (y >= 0 && y < src.height && x >= 0 && x < src.width) {
                            srcArray[y][x] = color.toArgb()
                        }
                        srcArray[y][x] = color.toArgb()
                    }
                }
            }
        }
        return arrayToBitmap(srcArray)
    }

    fun applyAffineTransformation(
        srcImage: Bitmap,
        srcPoints: List<Offset>,
        dstPoints: List<Offset>
    ): Bitmap {
        if (srcPoints.size + dstPoints.size < 6) {
            var offsetList: List<Offset> = listOf()

            for (i in 0..<srcPoints.size) {
                offsetList += srcPoints[i]
            }
            for (i in 0..<dstPoints.size) {
                offsetList += dstPoints[i]
            }
            return drawCircles(srcImage, offsetList)
        }

        val srcArray = bitmapTo2DArray(srcImage)
        val width = srcImage.width
        val height = srcImage.height
        val (srcX0, srcY0) = Pair(srcPoints[0].x * width, srcPoints[0].y * height)
        val (srcX1, srcY1) = Pair(srcPoints[1].x * width, srcPoints[1].y * height)
        val (srcX2, srcY2) = Pair(srcPoints[2].x * width, srcPoints[2].y * height)
        val (dstX0, dstY0) = Pair(dstPoints[0].x * width, dstPoints[0].y * height)
        val (dstX1, dstY1) = Pair(dstPoints[1].x * width, dstPoints[1].y * height)
        val (dstX2, dstY2) = Pair(dstPoints[2].x * width, dstPoints[2].y * height)

        val a = (dstX0 * (srcY1 - srcY2) + dstX1 * (srcY2 - srcY0) + dstX2 * (srcY0 - srcY1)) /
                (srcX0 * (srcY1 - srcY2) + srcX1 * (srcY2 - srcY0) + srcX2 * (srcY0 - srcY1))
        val b = (dstX0 * (srcX2 - srcX1) + dstX1 * (srcX0 - srcX2) + dstX2 * (srcX1 - srcX0)) /
                (srcY0 * (srcX2 - srcX1) + srcY1 * (srcX0 - srcX2) + srcY2 * (srcX1 - srcX0))
        val c = dstX0 - a * srcX0 - b * srcY0

        val d = (dstY0 * (srcY1 - srcY2) + dstY1 * (srcY2 - srcY0) + dstY2 * (srcY0 - srcY1)) /
                (srcX0 * (srcY1 - srcY2) + srcX1 * (srcY2 - srcY0) + srcX2 * (srcY0 - srcY1))
        val e = (dstY0 * (srcX2 - srcX1) + dstY1 * (srcX0 - srcX2) + dstY2 * (srcX1 - srcX0)) /
                (srcY0 * (srcX2 - srcX1) + srcY1 * (srcX0 - srcX2) + srcY2 * (srcX1 - srcX0))
        val f = dstY0 - d * srcX0 - e * srcY0

        val det = a * e - b * d
        val invA = e / det
        val invB = -b / det
        val invC = (b * f - e * c) / det
        val invD = -d / det
        val invE = a / det
        val invF = (d * c - a * f) / det

        val corners = listOf(
            Pair(0.0, 0.0),
            Pair(width - 1.0, 0.0),
            Pair(0.0, height - 1.0),
            Pair(width - 1.0, height - 1.0)
        )

        val transformedCorners = corners.map { (x, y) ->
            Pair(a * x + b * y + c, d * x + e * y + f)
        }

        val minX = transformedCorners.minOf { it.first }
        val minY = transformedCorners.minOf { it.second }
        val maxX = transformedCorners.maxOf { it.first }
        val maxY = transformedCorners.maxOf { it.second }

        val resultWidth = ceil(maxX - minX).toInt()
        val resultHeight = ceil(maxY - minY).toInt()

        val resultImage = Array(resultHeight) { IntArray(resultWidth) { Color.TRANSPARENT } }

        for (y in 0 until resultHeight) {
            for (x in 0 until resultWidth) {
                val srcX = invA * (x + minX) + invB * (y + minY) + invC
                val srcY = invD * (x + minX) + invE * (y + minY) + invF

                if (srcX in 0.0..<width.toDouble() && srcY in 0.0..<height.toDouble()) {
                    val rgb = bilinearInterpolation(srcArray, srcX, srcY)
                    resultImage[y][x] = rgb
                }
            }
        }

        return arrayToBitmap(resultImage)
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