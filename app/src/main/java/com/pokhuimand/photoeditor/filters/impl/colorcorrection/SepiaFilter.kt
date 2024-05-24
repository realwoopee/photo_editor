package com.pokhuimand.photoeditor.filters.impl.colorcorrection

import android.graphics.Bitmap
import android.graphics.Color
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

class SepiaFilter : Filter {
    override val id: String = "sepia"
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
            ) { sepia(it) }
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

    private fun sepia(pixel: Int): Int {
        val alpha = pixel.alpha

        val l = (0.3 * pixel.red + 0.59 * pixel.green + 0.11 * pixel.blue)
        val r =
            (0.393 * pixel.red + 0.769 * pixel.green + 0.189 * pixel.blue)
        val g =
            (0.349 * pixel.red + 0.686 * pixel.green + 0.168 * pixel.blue)
        val b =
            (0.272 * pixel.red + 0.534 * pixel.green + 0.131 * pixel.blue)

        val rg = r - g
        val gb = g - b
        val ar = rg * 0.7 + gb * 0.11
        val ag = ar - rg
        val ab = ar - rg - gb

        return Color.argb(
            alpha,
            (l + ar).toInt().coerceIn(0, 255),
            (l + ag).toInt().coerceIn(0, 255),
            (l + ab).toInt().coerceIn(0, 255)
        )
    }

}