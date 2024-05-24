package com.pokhuimand.photoeditor.filters.impl.colorcorrection

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Log
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterDataCache
import com.pokhuimand.photoeditor.filters.FilterCategory
import com.pokhuimand.photoeditor.filters.FilterSettings
import com.pokhuimand.photoeditor.filters.impl.RotateFilterSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.system.measureTimeMillis

data class GrayscaleFilterSettings(
    val amount: Float = 0f
) :
    FilterSettings() {
    object Ranges {
        val amount = 0f..0f
    }

    companion object {
        val default = GrayscaleFilterSettings(0f)
    }
}

class GrayscaleFilter : Filter {
    override val id: String = "grayscale"
    override val category: FilterCategory = FilterCategory.ColorCorrection

    override suspend fun applyDefaults(image: Bitmap, cache: FilterDataCache): Bitmap {
        return apply(image, RotateFilterSettings.default, cache)
    }

    override suspend fun apply(
        image: Bitmap,
        settings: FilterSettings,
        cache: FilterDataCache
    ): Bitmap {
        return withContext(Dispatchers.Default) {
            return@withContext asyncChunked(
                image
            ) { grayscale(it) }
        }
    }


    private suspend inline fun asyncChunked(
        bitmap: Bitmap,
        crossinline filter: (Int) -> Int
    ): Bitmap =
        coroutineScope {
            val width = bitmap.width
            val height = bitmap.height
            val buffer = IntArray(bitmap.height * bitmap.width)
            bitmap.getPixels(buffer, 0, width, 0, 0, width, height)
            val result = Bitmap.createBitmap(width, height, bitmap.config)

            val deferredPixels =
                (0 until bitmap.height * bitmap.width).chunked(
                    bitmap.height * bitmap.width / Runtime.getRuntime().availableProcessors()
                )
                    .map { chunk ->
                        async {
                            chunk.map { i ->
                                ensureActive()
                                buffer[i] = filter(buffer[i])
                            }
                        }
                    }

            deferredPixels.awaitAll()

            result.setPixels(buffer, 0, width, 0, 0, width, height)

            return@coroutineScope result
        }

    private fun grayscale(pixel: Int): Int {
        val alpha = pixel.alpha

        val gray = (0.3 * pixel.red + 0.59 * pixel.green + 0.11 * pixel.blue).toInt()
        return Color.argb(alpha, gray, gray, gray)
    }

}