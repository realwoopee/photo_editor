package com.pokhuimand.photoeditor.filters.impl

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Log
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterCategory
import com.pokhuimand.photoeditor.filters.FilterSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

data class DitheringFilterSettings(val levels: Int, val errorMultiplier: Double) :
    FilterSettings() {
    object Ranges {
        val levels = 2f..8f
        val errorMultiplier = 0f..2f
    }

    companion object {
        val default = DitheringFilterSettings(8, 1.0)
    }
}


class DitheringFilter : Filter {
    override val id: String = "dithering"
    override val category: FilterCategory = FilterCategory.ColorCorrection

    override suspend fun applyDefaults(image: Bitmap): Bitmap {
        return apply(image, DitheringFilterSettings.default)
    }

    override suspend fun apply(image: Bitmap, settings: FilterSettings): Bitmap {
        val sets = settings as DitheringFilterSettings
        return withContext(Dispatchers.Default) {
            return@withContext applyColorDitheringFilter(
                image,
                sierraFilter,
                sets.levels,
                sets.errorMultiplier
            )
        }
    }

    private val floydSteinbergFilter = arrayOf(
        doubleArrayOf(0.0, 0.0, 7 / 16.0),
        doubleArrayOf(3 / 16.0, 5 / 16.0, 1 / 16.0)
    )

    private val sierraFilter = arrayOf(
        doubleArrayOf(0.0, 0.0, 0.0, 5.0 / 32.0, 3.0 / 32.0),
        doubleArrayOf(2.0 / 32.0, 4.0 / 32.0, 5.0 / 32.0, 4.0 / 32.0, 2.0 / 32.0),
        doubleArrayOf(0.0, 2.0 / 32.0, 3.0 / 32.0, 2.0 / 32.0, 0.0)
    )

    private fun applyColorDitheringFilter(
        source: Bitmap,
        ditherFilter: Array<DoubleArray>,
        levels: Int,
        errorMultiplier: Double = 1.0
    ): Bitmap {
        val width = source.width
        val height = source.height
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val step = 256 / (levels - 1)

        val redErrors = Array(height) { DoubleArray(width) { 0.0 } }
        val greenErrors = Array(height) { DoubleArray(width) { 0.0 } }
        val blueErrors = Array(height) { DoubleArray(width) { 0.0 } }


        for (y in 0 until height) {
            for (x in 0 until width) {
                var r = Color.red(pixels[y * width + x]).toDouble()
                var g = Color.green(pixels[y * width + x]).toDouble()
                var b = Color.blue(pixels[y * width + x]).toDouble()

                r -= redErrors[y][x] * errorMultiplier
                g -= greenErrors[y][x] * errorMultiplier
                b -= blueErrors[y][x] * errorMultiplier

                val newR = (Math.round(r / step).toDouble() * step).coerceIn(0.0, 255.0)
                val newG = (Math.round(g / step).toDouble() * step).coerceIn(0.0, 255.0)
                val newB = (Math.round(b / step).toDouble() * step).coerceIn(0.0, 255.0)

                pixels[y * width + x] = Color.rgb(newR.toInt(), newG.toInt(), newB.toInt())

                val errR = newR - r
                val errG = newG - g
                val errB = newB - b

                for (dy in ditherFilter.indices)
                    for (dx in (-(ditherFilter[dy].size / 2) until (ditherFilter[dy].size + ditherFilter[dy].size % 2) / 2)) {
                        if (y + dy in 0 until height && x + dx in 0 until width) {
                            redErrors[y + dy][x + dx] += errR * ditherFilter[dy][ditherFilter[dy].size / 2 + dx]
                            greenErrors[y + dy][x + dx] += errG * ditherFilter[dy][ditherFilter[dy].size / 2 + dx]
                            blueErrors[y + dy][x + dx] += errB * ditherFilter[dy][ditherFilter[dy].size / 2 + dx]
                        }
                    }
            }
        }


        resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        return resultBitmap
    }

}