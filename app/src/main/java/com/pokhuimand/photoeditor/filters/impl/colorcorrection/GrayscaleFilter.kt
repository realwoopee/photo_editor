package com.pokhuimand.photoeditor.filters.impl.colorcorrection

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Log
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterDataCache
import com.pokhuimand.photoeditor.filters.FilterCategory
import com.pokhuimand.photoeditor.filters.FilterSettings
import com.pokhuimand.photoeditor.filters.impl.RotateFilterSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
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
            var bitmap: Bitmap
            val sync = measureTimeMillis {
                bitmap = sync(
                    image
                ) { grayscale(it) }
            }

            val asyncChunked = measureTimeMillis {
                bitmap = asyncChunked(
                    image
                ) { grayscale(it) }
            }

            val asyncPerRow = measureTimeMillis {
                bitmap = asyncPerRow(
                    image
                ) { grayscale(it) }
            }

            val colorMatrix = measureTimeMillis {
                bitmap = colorMatGrayscale(
                    image
                )
            }

            Log.i(
                "perftest",
                "sync: $sync asyncChunked: $asyncChunked asyncPerRow: $asyncPerRow colorMatrix: $colorMatrix"
            )
            return@withContext bitmap
        }
    }

    // Synchronous image processing function
    private fun sync(bitmap: Bitmap, filter: (Int) -> Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val result = Bitmap.createBitmap(width, height, bitmap.config)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val newPixel = filter(pixel)
                result.setPixel(x, y, newPixel)
            }
        }

        return result
    }


    // Asynchronous image processing function
    private suspend fun asyncChunked(bitmap: Bitmap, filter: suspend (Int) -> Int): Bitmap =
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
                                buffer[i] = filter(buffer[i])
                            }
                        }
                    }

            deferredPixels.awaitAll()

            result.setPixels(buffer, 0, width, 0, 0, width, height)

            return@coroutineScope result
        }

    private suspend fun asyncPerRow(bitmap: Bitmap, filter: suspend (Int) -> Int): Bitmap =
        coroutineScope {
            val width = bitmap.width
            val height = bitmap.height
            val buffer = IntArray(bitmap.height * bitmap.width)
            bitmap.getPixels(buffer, 0, width, 0, 0, width, height)
            val result = Bitmap.createBitmap(width, height, bitmap.config)

            val deferredPixels =
                (0 until bitmap.height * bitmap.width).chunked(bitmap.width)
                    .map { chunk ->
                        async {
                            chunk.map { i ->
                                buffer[i] = filter(buffer[i])
                            }
                        }
                    }

            deferredPixels.awaitAll()

            result.setPixels(buffer, 0, width, 0, 0, width, height)

            return@coroutineScope result
        }

    private suspend fun asyncPerPixel(bitmap: Bitmap, filter: suspend (Int) -> Int): Bitmap =
        coroutineScope {
            val width = bitmap.width
            val height = bitmap.height
            val buffer = IntArray(bitmap.height * bitmap.width)
            bitmap.getPixels(buffer, 0, width, 0, 0, width, height)
            val result = Bitmap.createBitmap(width, height, bitmap.config)

            val deferredPixels =
                (0 until bitmap.height * bitmap.width)
                    .map { i ->
                        async {
                            buffer[i] = filter(buffer[i])
                        }
                    }

            deferredPixels.awaitAll()

            result.setPixels(buffer, 0, width, 0, 0, width, height)

            return@coroutineScope result
        }

    private fun colorMatGrayscale(bitmap: Bitmap): Bitmap {
        val bitmapResult =
            Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config);
        val canvasResult = Canvas(bitmapResult);
        val paint = Paint();
        val colorMatrix = ColorMatrix(
            floatArrayOf(
                0.3f, 0.59f, 0.11f, 300f, 0f,
                0.3f, 0.59f, 0.11f, 0f, 0f,
                0.3f, 0.59f, 0.11f, 0f, 0f,
                0.0f, 0.0f, 0.0f, 1f, 0f,
            )
        );
        val filter = ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);
        canvasResult.drawBitmap(bitmap, 0f, 0f, paint);

        return bitmapResult;
    }

    private fun grayscale(pixel: Int): Int {
        val alpha = pixel and 0xff000000.toInt()
        val red = (pixel shr 16 and 0xff)
        val green = (pixel shr 8 and 0xff)
        val blue = (pixel and 0xff)

        val gray = (0.3 * red + 0.59 * green + 0.11 * blue).toInt()
        return alpha or (gray shl 16) or (gray shl 8) or gray
    }

}