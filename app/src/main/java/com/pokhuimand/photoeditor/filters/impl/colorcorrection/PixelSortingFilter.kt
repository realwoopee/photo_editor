package com.pokhuimand.photoeditor.filters.impl.colorcorrection

import android.graphics.Bitmap
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterCategory
import com.pokhuimand.photoeditor.filters.FilterDataCache
import com.pokhuimand.photoeditor.filters.FilterSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

enum class SortDirection {
    Up,
    Right,
    Down,
    Left
}

data class PixelSortingFilterCache(
    var mask: BooleanArray,
    var oldThreshold: ClosedFloatingPointRange<Float>
) :
    FilterDataCache() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PixelSortingFilterCache

        return mask.contentEquals(other.mask)
    }

    override fun hashCode(): Int {
        return mask.contentHashCode()
    }
}

data class PixelSortingFilterSettings(
    val threshold: ClosedFloatingPointRange<Float> = 0f..1f,
    val direction: SortDirection = SortDirection.Up
) :
    FilterSettings() {
    object Ranges {
        val threshold = 0f..1f
    }

    companion object {
        val default = PixelSortingFilterSettings()
    }
}

class PixelSortingFilter : Filter {
    override val id: String = "pixel-sorting"
    override val category: FilterCategory = FilterCategory.ColorCorrection

    override fun buildCache(): FilterDataCache = PixelSortingFilterCache(BooleanArray(0), -1f..0f)

    override suspend fun applyDefaults(image: Bitmap, cache: FilterDataCache): Bitmap {
        return apply(image, PixelSortingFilterSettings.default, cache)
    }

    override suspend fun apply(
        image: Bitmap,
        settings: FilterSettings,
        cache: FilterDataCache
    ): Bitmap {
        return withContext(Dispatchers.Default) {
            return@withContext pixelSorting(
                image,
                settings as PixelSortingFilterSettings,
                cache as PixelSortingFilterCache
            )
        }
    }


    // Asynchronous image processing function
    private suspend fun pixelSorting(
        bitmap: Bitmap,
        settings: PixelSortingFilterSettings, cache: PixelSortingFilterCache
    ): Bitmap =
        coroutineScope {
            val width = bitmap.width
            val height = bitmap.height
            val buffer = IntArray(bitmap.height * bitmap.width)
            bitmap.getPixels(buffer, 0, width, 0, 0, width, height)
            val result = Bitmap.createBitmap(width, height, bitmap.config)

            if (cache.mask.isEmpty() || settings.threshold != cache.oldThreshold)
                cache.mask = calculateMask(buffer, settings.threshold)

            when (settings.direction) {
                SortDirection.Up -> sortVertical(buffer, width, height, cache.mask)
                SortDirection.Right -> sortHorizontal(buffer, width, cache.mask, true)
                SortDirection.Down -> sortVertical(buffer, width, height, cache.mask, true)
                SortDirection.Left -> sortHorizontal(buffer, width, cache.mask)
            }

            ensureActive()
            result.setPixels(buffer, 0, width, 0, 0, width, height)

            return@coroutineScope result
        }

    private suspend fun sortVertical(
        buffer: IntArray,
        width: Int,
        height: Int,
        mask: BooleanArray,
        down: Boolean = false
    ) =
        coroutineScope {
            val jobs = (0 until width).flatMap { x ->
                (0 until height)
                    .fold(emptyList<MutableList<Int>>().toMutableList()) { acc, y ->
                        ensureActive()

                        if (mask[y * width + x]) {
                            if (acc.isEmpty())
                                acc.add(emptyList<Int>().toMutableList())
                            if (acc.last().isEmpty() || y * width + x - acc.last().last() == width)
                                acc.last().add(y * width + x)
                            else
                                acc.add(listOf(y * width + x).toMutableList())
                        }
                        return@fold acc
                    }.map { subLine ->
                        async {
                            if (down)
                                subLine
                                    .map { i -> i to buffer[i] }.sortedBy { (i, pixel) ->
                                        val r = pixel.red / 255.0
                                        val g = pixel.green / 255.0
                                        val b = pixel.blue / 255.0
                                        val max = maxOf(r, g, b)
                                        val value = max
                                        return@sortedBy value
                                    }.forEachIndexed { i, (_, pixel) ->
                                        ensureActive()

                                        buffer[subLine[i]] = pixel
                                    }
                            else
                                subLine
                                    .map { i -> i to buffer[i] }.sortedByDescending { (i, pixel) ->
                                        val r = pixel.red / 255.0
                                        val g = pixel.green / 255.0
                                        val b = pixel.blue / 255.0
                                        val max = maxOf(r, g, b)
                                        val value = max
                                        return@sortedByDescending value
                                    }.forEachIndexed { i, (_, pixel) ->
                                        ensureActive()

                                        buffer[subLine[i]] = pixel
                                    }
                        }
                    }
            }

            ensureActive()
            jobs.awaitAll()
        }

    private suspend fun sortHorizontal(
        buffer: IntArray,
        width: Int,
        mask: BooleanArray,
        right: Boolean = false
    ) =
        coroutineScope {
            val jobs = mask.indices.chunked(width).flatMapIndexed { y, line ->
                line
                    .fold(emptyList<MutableList<Int>>().toMutableList()) { acc, i ->
                        ensureActive()

                        if (mask[i]) {
                            if (acc.isEmpty())
                                acc.add(emptyList<Int>().toMutableList())
                            if (acc.last().isEmpty() || i % width - acc.last().last() == 1)
                                acc.last().add(i % width)
                            else
                                acc.add(listOf(i % width).toMutableList())
                        }
                        return@fold acc
                    }.map { subLine ->
                        async {
                            val startIndex = subLine[0]
                            if (right)
                                subLine
                                    .map { x -> buffer[y * width + x] }.sortedBy { pixel ->
                                        val r = pixel.red / 255.0
                                        val g = pixel.green / 255.0
                                        val b = pixel.blue / 255.0
                                        val max = maxOf(r, g, b)
                                        val value = max
                                        return@sortedBy value
                                    }.forEachIndexed { i, pixel ->
                                        ensureActive()

                                        buffer[y * width + startIndex + i] = pixel
                                    }
                            else
                                subLine
                                    .map { x -> buffer[y * width + x] }
                                    .sortedByDescending { pixel ->
                                        val r = pixel.red / 255.0
                                        val g = pixel.green / 255.0
                                        val b = pixel.blue / 255.0
                                        val max = maxOf(r, g, b)
                                        val value = max
                                        return@sortedByDescending value
                                    }.forEachIndexed { i, pixel ->
                                        ensureActive()

                                        buffer[y * width + startIndex + i] = pixel
                                    }
                        }
                    }
            }

            ensureActive()
            jobs.awaitAll()
        }

    private suspend fun calculateMask(
        image: IntArray,
        threshold: ClosedRange<Float>
    ): BooleanArray = coroutineScope {
        val mask = BooleanArray(image.size)

        image.indices.chunked(image.size / Runtime.getRuntime().availableProcessors())
            .map { chunk ->
                async {
                    chunk.forEach { i ->
                        ensureActive()

                        val pixel = image[i]
                        val r = pixel.red / 255.0
                        val g = pixel.green / 255.0
                        val b = pixel.blue / 255.0
                        val max = maxOf(r, g, b)
                        val value = max

                        mask[i] = value in threshold
                    }
                }
            }

        return@coroutineScope mask
    }
}