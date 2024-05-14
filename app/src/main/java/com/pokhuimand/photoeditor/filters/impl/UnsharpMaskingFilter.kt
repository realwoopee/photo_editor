package com.pokhuimand.photoeditor.filters.impl

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.graphics.get
import androidx.core.graphics.set
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterCategory
import com.pokhuimand.photoeditor.filters.FilterSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.system.measureTimeMillis

data class UnsharpMaskingFilterSettings(
    val amount: Double,
    val radius: Int,
    val threshold: Double
) :
    FilterSettings() {
    object Ranges {
        val amount = 0f..5f
        val radius = 0f..15f
        val threshold = 0f..2f
    }

    companion object {
        val default = UnsharpMaskingFilterSettings(1.0, 1, 1.3)
    }
}

//TODO: implement proper threshold
class UnsharpMaskingFilter : Filter {
    override val id: String = "unsharp-masking"
    override val category: FilterCategory = FilterCategory.ColorCorrection

    override suspend fun applyDefaults(image: Bitmap): Bitmap {
        return apply(image, UnsharpMaskingFilterSettings.default)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun apply(image: Bitmap, settings: FilterSettings): Bitmap {
        val sets = settings as UnsharpMaskingFilterSettings
        return withContext(Dispatchers.Default) {
            unsharpMaskingAsync(
                image,
                sets.amount,
                sets.radius,
                sets.threshold
            )
        }
    }

    private fun gaussianBlur(
        imageData: IntArray, width: Int, height: Int, radius: Int
    ): IntArray {
        val blurredData = IntArray(imageData.size)

        val kernel = createKernelForGauss(radius)

        (0 until height).flatMap { y -> (0 until width).map { x -> Pair(x, y) } }.map { (x, y) ->
            var a = 0
            var r = 0
            var g = 0
            var b = 0

            for (i in (-radius)..radius) {
                for (j in (-radius)..radius) {
                    val pixelX = x + i
                    val pixelY = y + j

                    if (pixelX in 0 until width && pixelY in 0 until height) {
                        val rgb = imageData[pixelY * width + pixelX]
                        val weight = kernel[i + radius][j + radius]

                        a = rgb shr 24 and 0xFF
                        r = rgb shr 16 and 0xFF
                        g = rgb shr 8 and 0xFF
                        b = rgb and 0xFF

                        a += Math.round(weight * ((rgb shr 24) and 0xFF)).toInt()
                        r += Math.round(weight * ((rgb shr 16) and 0xFF)).toInt()
                        g += Math.round(weight * ((rgb shr 8) and 0xFF)).toInt()
                        b += Math.round(weight * (rgb and 0xFF)).toInt()
                    }
                }
            }
            a = a.coerceIn(0, 255)
            r = r.coerceIn(0, 255)
            g = g.coerceIn(0, 255)
            b = b.coerceIn(0, 255)

            val rgb = (a shl 24) or (r shl 16) or (g shl 8) or b
            blurredData[y * width + x] = rgb
        }

        return blurredData
    }

    private fun createKernelForGauss(radius: Int): Array<DoubleArray> {
        val kernel = Array(2 * radius + 1) { DoubleArray(2 * radius + 1) }
        val sigma = radius / 3.0

        for (i in -radius..radius) {
            for (j in -radius..radius) {
                val x = i.toDouble()
                val y = j.toDouble()
                kernel[i + radius][j + radius] =
                    exp(-(x * x + y * y) / (2 * sigma * sigma)) / (2 * Math.PI * sigma * sigma)
            }
        }
        return kernel
    }

    private fun unsharpMasking(
        image: Bitmap, amount: Double,
        radius: Int, threshold: Double
    ): Bitmap {
        val width = image.width
        val height = image.height
        val imageData = IntArray(width * height)
        image.getPixels(imageData, 0, width, 0, 0, width, height)
        val blurredImageData = gaussianBlur(imageData, width, height, radius)
        val sharpenedImageData = IntArray(imageData.size)

        (0 until height).flatMap { y -> (0 until width).map { x -> Pair(x, y) } }
            .chunked(width * height / 3) { chunk ->
                chunk
                    .forEach { (x, y) ->
                        val rgbOriginal = imageData[y * width + x]
                        val rgbBlurred = blurredImageData[y * width + x]

                        val aOriginal = (rgbOriginal shr 24) and 0xFF
                        val rOriginal = (rgbOriginal shr 16) and 0xFF
                        val gOriginal = (rgbOriginal shr 8) and 0xFF
                        val bOriginal = rgbOriginal and 0xFF

                        val aBlurred = (rgbBlurred shr 24) and 0xFF
                        val rBlurred = (rgbBlurred shr 16) and 0xFF
                        val gBlurred = (rgbBlurred shr 8) and 0xFF
                        val bBlurred = rgbBlurred and 0xFF

                        val aDiff = aOriginal - aBlurred
                        val rDiff = rOriginal - rBlurred
                        val gDiff = gOriginal - gBlurred
                        val bDiff = bOriginal - bBlurred

                        val alphaUpperBorder = Math.min((aOriginal * threshold).toLong(), 255)
                        val redUpperBorder = Math.min((rOriginal * threshold).toLong(), 255)
                        val greenUpperBorder = Math.min((gOriginal * threshold).toLong(), 255)
                        val blueUpperBorder = Math.min((bOriginal * threshold).toLong(), 255)

                        val lowerThreshold = 1 / threshold
                        val alphaLowerBorder = Math.max((aOriginal * lowerThreshold).toLong(), 0)
                        val redLowerBorder = Math.max((rOriginal * lowerThreshold).toLong(), 0)
                        val greenLowerBorder = Math.max((gOriginal * lowerThreshold).toLong(), 0)
                        val blueLowerBorder = Math.max((bOriginal * lowerThreshold).toLong(), 0)

                        val aNew = (aOriginal + Math.round(amount * aDiff)).coerceIn(0, 255).toInt()
                        val rNew = (rOriginal + Math.round(amount * rDiff)).coerceIn(0, 255).toInt()
                        val gNew = (gOriginal + Math.round(amount * gDiff)).coerceIn(0, 255).toInt()
                        val bNew = (bOriginal + Math.round(amount * bDiff)).coerceIn(0, 255).toInt()

//                val aNew = Math.max(
//                    alphaLowerBorder,
//                    Math.min(alphaUpperBorder, aOriginal + Math.round(amount * aDiff))
//                ).toInt()
//                val rNew = Math.max(
//                    redLowerBorder,
//                    Math.min(redUpperBorder, rOriginal + Math.round(amount * rDiff))
//                ).toInt()
//                val gNew = Math.max(
//                    greenLowerBorder,
//                    Math.min(greenUpperBorder, gOriginal + Math.round(amount * gDiff))
//                ).toInt()
//                val bNew = Math.max(
//                    blueLowerBorder,
//                    Math.min(blueUpperBorder, bOriginal + Math.round(amount * bDiff))
//                ).toInt()


                        val rgbNew = (aNew shl 24) or (rNew shl 16) or (gNew shl 8) or bNew
                        sharpenedImageData[y * width + x] = rgbNew
                    }
            }

        val sharpenedImage = Bitmap.createBitmap(width, height, image.config)
        sharpenedImage.setPixels(sharpenedImageData, 0, width, 0, 0, width, height)
        return sharpenedImage
    }


    private suspend fun gaussianBlurAsync(
        imageData: IntArray, width: Int, height: Int, radius: Int
    ): IntArray = coroutineScope {

        val blurredData = IntArray(imageData.size)

        val kernel = createKernelForGauss(radius)

        val jobs =
            (0 until height).flatMap { y -> (0 until width).map { x -> Pair(x, y) } }
                .chunked(height * width / Runtime.getRuntime().availableProcessors()).map { chunk ->
                    async {
                        chunk.map { (x, y) ->
                            var a = 0
                            var r = 0
                            var g = 0
                            var b = 0

                            for (i in -radius..radius) {
                                for (j in -radius..radius) {
                                    val pixelX = x + i
                                    val pixelY = y + j

                                    if (pixelX in 0 until width && pixelY in 0 until height) {
                                        val rgb = imageData[pixelY * width + pixelX]
                                        val weight = kernel[i + radius][j + radius]

                                        a = rgb shr 24 and 0xFF
                                        r = rgb shr 16 and 0xFF
                                        g = rgb shr 8 and 0xFF
                                        b = rgb and 0xFF

                                        a += Math.round(weight * ((rgb shr 24) and 0xFF)).toInt()
                                        r += Math.round(weight * ((rgb shr 16) and 0xFF)).toInt()
                                        g += Math.round(weight * ((rgb shr 8) and 0xFF)).toInt()
                                        b += Math.round(weight * (rgb and 0xFF)).toInt()
                                    }
                                }
                            }
                            a = a.coerceIn(0, 255)
                            r = r.coerceIn(0, 255)
                            g = g.coerceIn(0, 255)
                            b = b.coerceIn(0, 255)

                            val rgb = (a shl 24) or (r shl 16) or (g shl 8) or b
                            blurredData[y * width + x] = rgb
                        }
                    }
                }

        jobs.awaitAll()

        return@coroutineScope blurredData
    }

    private suspend fun unsharpMaskingAsync(
        image: Bitmap, amount: Double,
        radius: Int, threshold: Double
    ): Bitmap = coroutineScope {
        val width = image.width
        val height = image.height
        val imageData = IntArray(width * height)
        image.getPixels(imageData, 0, width, 0, 0, width, height)
        val blurredImageData = gaussianBlur(imageData, width, height, radius)
        val sharpenedImageData = IntArray(imageData.size)


        val jobs = (0 until height).flatMap { y -> (0 until width).map { x -> Pair(x, y) } }
            .chunked(width * height / Runtime.getRuntime().availableProcessors()).map { chunk ->
                launch {
                    chunk.forEach { (x, y) ->
                        val rgbOriginal = imageData[y * width + x]
                        val rgbBlurred = blurredImageData[y * width + x]

                        val aOriginal = (rgbOriginal shr 24) and 0xFF
                        val rOriginal = (rgbOriginal shr 16) and 0xFF
                        val gOriginal = (rgbOriginal shr 8) and 0xFF
                        val bOriginal = rgbOriginal and 0xFF

                        val aBlurred = (rgbBlurred shr 24) and 0xFF
                        val rBlurred = (rgbBlurred shr 16) and 0xFF
                        val gBlurred = (rgbBlurred shr 8) and 0xFF
                        val bBlurred = rgbBlurred and 0xFF

                        val aDiff = aOriginal - aBlurred
                        val rDiff = rOriginal - rBlurred
                        val gDiff = gOriginal - gBlurred
                        val bDiff = bOriginal - bBlurred

                        val alphaUpperBorder = Math.min((aOriginal * threshold).toLong(), 255)
                        val redUpperBorder = Math.min((rOriginal * threshold).toLong(), 255)
                        val greenUpperBorder = Math.min((gOriginal * threshold).toLong(), 255)
                        val blueUpperBorder = Math.min((bOriginal * threshold).toLong(), 255)

                        val lowerThreshold = 1 / threshold
                        val alphaLowerBorder =
                            Math.max((aOriginal * lowerThreshold).toLong(), 0)
                        val redLowerBorder = Math.max((rOriginal * lowerThreshold).toLong(), 0)
                        val greenLowerBorder =
                            Math.max((gOriginal * lowerThreshold).toLong(), 0)
                        val blueLowerBorder = Math.max((bOriginal * lowerThreshold).toLong(), 0)

                        val aNew =
                            (aOriginal + Math.round(amount * aDiff)).coerceIn(0, 255).toInt()
                        val rNew =
                            (rOriginal + Math.round(amount * rDiff)).coerceIn(0, 255).toInt()
                        val gNew =
                            (gOriginal + Math.round(amount * gDiff)).coerceIn(0, 255).toInt()
                        val bNew =
                            (bOriginal + Math.round(amount * bDiff)).coerceIn(0, 255).toInt()

//                val aNew = Math.max(
//                    alphaLowerBorder,
//                    Math.min(alphaUpperBorder, aOriginal + Math.round(amount * aDiff))
//                ).toInt()
//                val rNew = Math.max(
//                    redLowerBorder,
//                    Math.min(redUpperBorder, rOriginal + Math.round(amount * rDiff))
//                ).toInt()
//                val gNew = Math.max(
//                    greenLowerBorder,
//                    Math.min(greenUpperBorder, gOriginal + Math.round(amount * gDiff))
//                ).toInt()
//                val bNew = Math.max(
//                    blueLowerBorder,
//                    Math.min(blueUpperBorder, bOriginal + Math.round(amount * bDiff))
//                ).toInt()


                        val rgbNew = (aNew shl 24) or (rNew shl 16) or (gNew shl 8) or bNew
                        sharpenedImageData[y * width + x] = rgbNew
                    }
                }
            }

        jobs.forEach { it.join() }
        val sharpenedImage = Bitmap.createBitmap(width, height, image.config)
        sharpenedImage.setPixels(sharpenedImageData, 0, width, 0, 0, width, height)
        return@coroutineScope sharpenedImage
    }

}