package com.pokhuimand.photoeditor.filters.impl

import android.graphics.Bitmap
import android.graphics.Color
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterCategory
import com.pokhuimand.photoeditor.filters.FilterSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.round

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

    override suspend fun applyDefaults(image: Bitmap): Bitmap {
        return apply(image, ResizeFilterSettings.default)
    }

    override suspend fun apply(image: Bitmap, settings: FilterSettings): Bitmap {
        val sets = settings as ResizeFilterSettings
        return withContext(Dispatchers.Default) {
            resize(
                image,
                sets.coefficient
            )
        }
    }

    private fun nearestNeighbourResize(image: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = image.width
        val height = image.height
        val initialArray = bitmapTo2DArray(image)
        val resultArray = Array(newHeight) { IntArray(newWidth) { Color.TRANSPARENT } }
        for (y in 0 until newHeight) {
            for (x in 0 until newWidth) {
                var u = x / (newWidth * 1.0);
                var v = y / (newHeight * 1.0);
                u *= width;
                v *= height;
                val newX = round(u - 0.5).toInt()
                val newY = round(v - 0.5).toInt()

                resultArray[y][x] = initialArray[newY][newX]
            }
        }
        return arrayToBitmap(resultArray)
    }

    private fun interpolateColor(pixel11: Int, pixel12: Int, pixel21: Int, pixel22: Int, u: Double, v: Double): Int {
        val r = (1 - u) * (1 - v) * Color.red(pixel11) +
                u * (1 - v) * Color.red(pixel12) +
                (1 - u) * v * Color.red(pixel21) +
                u * v * Color.red(pixel22)

        val g = (1 - u) * (1 - v) * Color.green(pixel11) +
                u * (1 - v) * Color.green(pixel12) +
                (1 - u) * v * Color.green(pixel21) +
                u * v * Color.green(pixel22)

        val b = (1 - u) * (1 - v) * Color.blue(pixel11) +
                u * (1 - v) * Color.blue(pixel12) +
                (1 - u) * v * Color.blue(pixel21) +
                u * v * Color.blue(pixel22)

        val alpha = (1 - u) * (1 - v) * Color.alpha(pixel11) +
                u * (1 - v) * Color.alpha(pixel12) +
                (1 - u) * v * Color.alpha(pixel21) +
                u * v * Color.alpha(pixel22)

        return Color.argb(
            alpha.coerceIn(0.0, 255.0).toInt(),
            r.coerceIn(0.0, 255.0).toInt(),
            g.coerceIn(0.0, 255.0).toInt(),
            b.coerceIn(0.0, 255.0).toInt()
        )
    }
    private fun bilinearResize(image: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = image.width
        val height = image.height
        val initialArray = bitmapTo2DArray(image)
        val resultArray = Array(newHeight) { IntArray(newWidth) { Color.TRANSPARENT } }


        for (x in 0 until newWidth) {
            for (y in 0 until newHeight) {
                var u = x / (newWidth * 1.0);
                var v = y / (newHeight * 1.0);
                u *= width;
                v *= height;
                val newX = round(u - 0.5).toInt()
                val newY = round(v - 0.5).toInt()
                val uRatio = u - newX;
                val vRatio = v - newY;
                val uOpposite = 1 - uRatio;
                val vOpposite = 1 - vRatio;
                var neighbourPixels = IntArray(4)
                if (newX in 0..<width && newY in 0..<height)
                    neighbourPixels[0] += initialArray[newY][newX]

                if (newX + 1 in 0..<width && newY in 0..<height)
                    neighbourPixels[1] += initialArray[newY][newX + 1]

                if (newX in 0..<width && newY + 1 in 0..<height)
                    neighbourPixels[2] += initialArray[newY + 1][newX]

                if (newX + 1 in 0..<width && newY + 1 in 0..<height)
                    neighbourPixels[3] += initialArray[newY + 1][newX + 1]

                while (neighbourPixels.size < 4)
                    neighbourPixels += 0

                resultArray[y][x] = interpolateColor(neighbourPixels[0],neighbourPixels[1],neighbourPixels[2],neighbourPixels[3], uRatio, vRatio)

            }
        }
        return arrayToBitmap(resultArray)
    }

    private fun upScaling(image: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        return bilinearResize(image, newWidth, newHeight)
    }

    private fun trilinearResize(image: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = image.width
        val height = image.height
        val imageResizedByNN = nearestNeighbourResize(image, width / 2, height / 2)
        val firstDonorImage: Bitmap = upScaling(image, newWidth, newHeight)
        val secondDonorImage = upScaling(imageResizedByNN, newWidth, newHeight)
        val firstDonorArray = bitmapTo2DArray(firstDonorImage)
        val secondDonorArray = bitmapTo2DArray(secondDonorImage)
        val resultArray = Array(newHeight) { IntArray(newWidth) { Color.TRANSPARENT } }

        for (y in 0 until newHeight) {
            for (x in 0 until newWidth) {
                val r = (Color.red(firstDonorArray[y][x]) + Color.red(secondDonorArray[y][x])) / 2
                val g = (Color.green(firstDonorArray[y][x]) + Color.green(secondDonorArray[y][x])) / 2
                val b = (Color.blue(firstDonorArray[y][x]) + Color.blue(secondDonorArray[y][x])) / 2
                val a = (Color.alpha(firstDonorArray[y][x]) + Color.alpha(secondDonorArray[y][x])) / 2

                resultArray[y][x] = Color.argb(
                    a.coerceIn(0, 255),
                    r.coerceIn(0, 255),
                    g.coerceIn(0, 255),
                    b.coerceIn(0, 255)
                )
            }
        }
        return arrayToBitmap(resultArray)
    }

    private fun downScaling(image: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        return trilinearResize(image, newWidth, newHeight)
    }

    private fun resize(source: Bitmap, coefficient: Float): Bitmap {
        val width = source.width
        val height = source.height
        val newWidth = Math.round(width * coefficient)
        val newHeight = Math.round(height * coefficient)
        lateinit var resultImage: Bitmap
        if (coefficient == 1.0f)
            return source
        else if (coefficient < 1.0f) {
            resultImage = downScaling(source, newWidth, newHeight)
        } else {
            resultImage = upScaling(source, newWidth, newHeight)
        }
        return resultImage
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