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
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.round
import kotlin.math.roundToInt


data class TempAndTintFilterSettings(
    val temp: Float = 0f,
    val tint: Float = 0f
) :
    FilterSettings() {
    object Ranges {
        val temp = -1f..1f
        val tint = -1f..1f
    }

    companion object {
        val default = TempAndTintFilterSettings(0f, 0f)
    }
}

class TempAndTintFilter : Filter {
    override val id: String = "temp-and-tint"
    override val category: FilterCategory = FilterCategory.ColorCorrection

    override suspend fun applyDefaults(image: Bitmap, cache: FilterDataCache): Bitmap {
        return apply(image, TempAndTintFilterSettings.default, cache)
    }

    override suspend fun apply(
        image: Bitmap,
        settings: FilterSettings,
        cache: FilterDataCache
    ): Bitmap {
        return withContext(Dispatchers.Default) {
            return@withContext changeTempAndTint(
                image,
                settings as TempAndTintFilterSettings
            )
        }
    }

    private suspend fun changeTempAndTint(
        bitmap: Bitmap,
        settings: TempAndTintFilterSettings,
    ): Bitmap =
        coroutineScope {
            val tempRed = 255 * settings.temp
            val tempBlue = 255 * (-settings.temp)

            val tintGreen = 255 * settings.tint
            val tintBlue = 255 * (-settings.tint)

            val width = bitmap.width
            val height = bitmap.height
            val buffer = IntArray(bitmap.height * bitmap.width)
            bitmap.getPixels(buffer, 0, width, 0, 0, width, height)

            val deferredPixels =
                (0 until bitmap.height * bitmap.width).chunked(
                    bitmap.height * bitmap.width / Runtime.getRuntime().availableProcessors()
                )
                    .map { chunk ->
                        async {
                            chunk.map { i ->
                                ensureActive()
                                val pixel = buffer[i]
                                val alpha = Color.alpha(pixel)
                                var red = Color.red(pixel)
                                var green = Color.green(pixel)
                                var blue = Color.blue(pixel)

                                red += tempRed.toInt()
                                blue += tempBlue.toInt()

                                green += tintGreen.toInt()
                                blue += tintBlue.toInt()

                                red = red.coerceIn(0, 255)
                                green = green.coerceIn(0, 255)
                                blue = blue.coerceIn(0, 255)

                                buffer[i] = Color.argb(alpha, red, green, blue)
                            }
                        }
                    }

            ensureActive()
            deferredPixels.awaitAll()

            ensureActive()
            val result = Bitmap.createBitmap(width, height, bitmap.config)
            result.setPixels(buffer, 0, width, 0, 0, width, height)

            return@coroutineScope result
        }
}