package com.pokhuimand.photoeditor.filters.impl.colorcorrection

import android.graphics.Bitmap
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterDataCache
import com.pokhuimand.photoeditor.filters.FilterCategory
import com.pokhuimand.photoeditor.filters.FilterSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt

data class UnsharpMaskingFilterSettings(
    val amount: Double,
    val radius: Double,
    val threshold: Double
) :
    FilterSettings() {
    object Ranges {
        val amount = 0f..5f
        val radius = 0f..5f
        val threshold = 0f..255f
    }

    companion object {
        val default = UnsharpMaskingFilterSettings(1.0, 1.0, 128.0)
    }
}

//TODO: implement proper threshold
class UnsharpMaskingFilter : Filter {
    override val id: String = "unsharp-masking"
    override val category: FilterCategory = FilterCategory.ColorCorrection

    override suspend fun applyDefaults(image: Bitmap, cache: FilterDataCache): Bitmap {
        return apply(image, UnsharpMaskingFilterSettings.default, cache)
    }

    override suspend fun apply(
        image: Bitmap,
        settings: FilterSettings,
        cache: FilterDataCache
    ): Bitmap {
        val sets = settings as UnsharpMaskingFilterSettings
        return withContext(Dispatchers.Default) {
            unsharpMaskingAsync(
                image,
                sets.amount,
                sets.radius.toFloat(),
                sets.threshold
            )
        }
    }

    private fun createKernelForGauss(radius: Float): Array<DoubleArray> = if (radius <= 0.001) {
        arrayOf(doubleArrayOf(1.0))
    } else {
        val sigma = radius / 3
        val integerRadius = ceil(radius).toInt()
        val size = 2 * integerRadius + 1
        val kernel = Array(size) { DoubleArray(size) }
        var sum = 0.0

        for (i in -integerRadius..integerRadius)
            for (j in -integerRadius..integerRadius) {
                val x = i.toDouble()
                val y = j.toDouble()
                val value =
                    exp(-(x * x + y * y) / (2 * sigma.pow(2))) / (2 * Math.PI * sigma.pow(2))
                kernel[i + integerRadius][j + integerRadius] = value
                sum += value
            }

        for (i in kernel.indices)
            for (j in kernel.indices)
                kernel[i][j] /= sum

        kernel
    }

    private suspend fun gaussianBlurAsync(
        imageData: IntArray, width: Int, height: Int, radius: Float
    ): IntArray = coroutineScope {

        val blurredData = IntArray(imageData.size)

        val kernel = createKernelForGauss(radius)
        val integerRadius = ceil(radius).toInt()

        val jobs =
            (0 until height).flatMap { y -> (0 until width).map { x -> Pair(x, y) } }
                .chunked(height * width / Runtime.getRuntime().availableProcessors()).map { chunk ->
                    async {
                        chunk.map { (x, y) ->
                            var a = 0.0
                            var r = 0.0
                            var g = 0.0
                            var b = 0.0

                            for (i in (-integerRadius)..integerRadius) {
                                for (j in (-integerRadius)..integerRadius) {
                                    val pixelX = x + i
                                    val pixelY = y + j

                                    if (pixelX in 0 until width && pixelY in 0 until height) {
                                        val rgb = imageData[pixelY * width + pixelX]
                                        val weight = kernel[i + integerRadius][j + integerRadius]

                                        a += weight * ((rgb shr 24) and 0xFF).toDouble()
                                        r += weight * ((rgb shr 16) and 0xFF).toDouble()
                                        g += weight * ((rgb shr 8) and 0xFF).toDouble()
                                        b += weight * (rgb and 0xFF).toDouble()
                                    }
                                }
                            }
                            a = a.coerceIn(0.0, 255.0)
                            r = r.coerceIn(0.0, 255.0)
                            g = g.coerceIn(0.0, 255.0)
                            b = b.coerceIn(0.0, 255.0)

                            val rgb =
                                (a.roundToInt() shl 24) or (r.roundToInt() shl 16) or (g.roundToInt() shl 8) or b.roundToInt()
                            blurredData[y * width + x] = rgb
                        }
                    }
                }

        jobs.awaitAll()

        return@coroutineScope blurredData
    }

    private suspend fun unsharpMaskingAsync(
        image: Bitmap, amount: Double,
        radius: Float, threshold: Double
    ): Bitmap = coroutineScope {
        val width = image.width
        val height = image.height
        val imageData = IntArray(width * height)
        image.getPixels(imageData, 0, width, 0, 0, width, height)
        val blurredImageData = gaussianBlurAsync(imageData, width, height, radius)
        val sharpenedImageData = IntArray(imageData.size)


        val jobs = (0 until height).flatMap { y -> (0 until width).map { x -> Pair(x, y) } }
            .chunked(width * height / Runtime.getRuntime().availableProcessors()).map { chunk ->
                launch {
                    chunk.forEach { (x, y) ->
                        val rgbOriginal = imageData[y * width + x]
                        val rgbBlurred = blurredImageData[y * width + x]

                        val rOriginal = (rgbOriginal shr 16) and 0xFF
                        val gOriginal = (rgbOriginal shr 8) and 0xFF
                        val bOriginal = rgbOriginal and 0xFF

                        val rBlurred = (rgbBlurred shr 16) and 0xFF
                        val gBlurred = (rgbBlurred shr 8) and 0xFF
                        val bBlurred = rgbBlurred and 0xFF

                        val rDiff = rOriginal - rBlurred
                        val gDiff = gOriginal - gBlurred
                        val bDiff = bOriginal - bBlurred

                        if (abs(rDiff) > threshold || abs(gDiff) > threshold || abs(bDiff) > threshold) {
                            val rNew =
                                (rOriginal + Math.round(amount * rDiff)).coerceIn(0, 255).toInt()
                            val gNew =
                                (gOriginal + Math.round(amount * gDiff)).coerceIn(0, 255).toInt()
                            val bNew =
                                (bOriginal + Math.round(amount * bDiff)).coerceIn(0, 255).toInt()

                            val rgbNew = (255 shl 24) or (rNew shl 16) or (gNew shl 8) or bNew
                            sharpenedImageData[y * width + x] = rgbNew
                        } else {
                            sharpenedImageData[y * width + x] = rgbOriginal
                        }
                    }
                }
            }

        jobs.forEach { it.join() }
        val sharpenedImage = Bitmap.createBitmap(width, height, image.config)
        sharpenedImage.setPixels(sharpenedImageData, 0, width, 0, 0, width, height)
        return@coroutineScope sharpenedImage
    }
}