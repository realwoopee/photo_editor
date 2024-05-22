package com.pokhuimand.photoeditor.filters.impl.colorcorrection

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterDataCache
import com.pokhuimand.photoeditor.filters.FilterCategory
import com.pokhuimand.photoeditor.filters.FilterSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round
import kotlin.math.roundToInt

data class ContrastAndBrightnessFilterCache(var medianValue: Float) :
    FilterDataCache()

data class ContrastAndBrightnessFilterSettings(
    val contrast: Float = 0f,
    val brightness: Float = 0f
) :
    FilterSettings() {
    object Ranges {
        val contrast = 0f..2f
        val brightness = -1f..1f
    }

    companion object {
        val default = ContrastAndBrightnessFilterSettings(1f, 0f)
    }
}

class ContrastAndBrightnessFilter : Filter {
    override val id: String = "contrast-and-brightness"
    override val category: FilterCategory = FilterCategory.ColorCorrection

    override fun buildCache(): FilterDataCache = ContrastAndBrightnessFilterCache(-1.0f)

    override suspend fun applyDefaults(image: Bitmap, cache: FilterDataCache): Bitmap {
        return apply(image, ContrastAndBrightnessFilterSettings.default, cache)
    }

    override suspend fun apply(
        image: Bitmap,
        settings: FilterSettings,
        cache: FilterDataCache
    ): Bitmap {
        return withContext(Dispatchers.Default) {
            return@withContext changeContrastAndBrightness(
                image,
                settings as ContrastAndBrightnessFilterSettings,
                cache as ContrastAndBrightnessFilterCache
            )
        }
    }


    // Asynchronous image processing function
    private suspend fun changeContrastAndBrightness(
        bitmap: Bitmap,
        settings: ContrastAndBrightnessFilterSettings, cache: ContrastAndBrightnessFilterCache
    ): Bitmap =
        coroutineScope {
            val width = bitmap.width
            val height = bitmap.height
            val buffer = IntArray(bitmap.height * bitmap.width)
            bitmap.getPixels(buffer, 0, width, 0, 0, width, height)
            val result = Bitmap.createBitmap(width, height, bitmap.config)

            if (cache.medianValue == -1.0f)
                cache.medianValue = medianValue(buffer)

            val medianValue = cache.medianValue
            //if (buffer.size < 1_000_000) medianValue(buffer) else averageValue(buffer)

            val deferredPixels =
                (0 until bitmap.height * bitmap.width).chunked(
                    bitmap.height * bitmap.width / Runtime.getRuntime().availableProcessors()
                )
                    .map { chunk ->
                        async {
                            chunk.map { i ->
                                // java sucks. this is the optimized way
                                val pixel = buffer[i]
                                val r = pixel.red / 255.0
                                val g = pixel.green / 255.0
                                val b = pixel.blue / 255.0
                                val max = maxOf(r, g, b)
                                val min = minOf(r, g, b)
                                var hue = when {
                                    max == min -> 0.0
                                    max == r && g >= b -> 60 * ((g - b) / (max - min))
                                    max == r && g < b -> 60 * ((g - b) / (max - min)) + 360
                                    max == g -> 60 * ((b - r) / (max - min)) + 120
                                    max == b -> 60 * ((r - g) / (max - min)) + 240
                                    else -> 0.0
                                }
                                if (hue < 0) {
                                    hue += 360
                                }
                                val saturation = if (max == 0.0) 0.0 else 1 - (min / max)
                                var value = max

                                value =
                                    (value - medianValue) * settings.contrast + medianValue + settings.brightness

                                value = value.coerceIn(0.0, 1.0)

                                buffer[i] = run {
                                    val h1 = hue / 60
                                    val c = value * saturation
                                    val x = c * (1 - abs((h1 % 2) - 1))
                                    val m = value - c
                                    val r: Double
                                    val g: Double
                                    val b: Double
                                    when (h1) {
                                        in 0.0f..1.0f -> {
                                            r = c
                                            g = x
                                            b = 0.0
                                        }

                                        in 1.0f..2.0f -> {
                                            r = x
                                            g = c
                                            b = 0.0
                                        }

                                        in 2.0f..3.0f -> {
                                            r = 0.0
                                            g = c
                                            b = x
                                        }

                                        in 3.0f..4.0f -> {
                                            r = 0.0
                                            g = x
                                            b = c
                                        }

                                        in 4.0f..5.0f -> {
                                            r = x
                                            g = 0.0
                                            b = c
                                        }

                                        else -> {
                                            r = c
                                            g = 0.0
                                            b = x
                                        }
                                    }

                                    Color.rgb(
                                        ((r + m) * 255).coerceIn(0.0, 255.0).roundToInt(),
                                        ((g + m) * 255).coerceIn(0.0, 255.0).roundToInt(),
                                        ((b + m) * 255).coerceIn(0.0, 255.0).roundToInt()
                                    )
                                }
                            }
                        }
                    }

            deferredPixels.awaitAll()

            result.setPixels(buffer, 0, width, 0, 0, width, height)

            return@coroutineScope result
        }

    private suspend fun medianValue(buffer: IntArray): Float = coroutineScope {
        val histogram = ConcurrentHashMap<Float, Int>()
        val jobs = buffer.indices.chunked(buffer.size / Runtime.getRuntime().availableProcessors())
            .map { chunk ->
                async {
                    chunk.forEach {
                        val pixel = buffer[it]
                        val value =
                            max(Color.red(pixel), max(Color.green(pixel), Color.blue(pixel))) / 255f
                        histogram[round(value * 100) / 100] =
                            histogram.getOrDefault(round(value * 100) / 100, 0) + 1
                    }
                }
            }

        jobs.awaitAll()

        return@coroutineScope histogram.maxBy { it.value }.key
    }
}