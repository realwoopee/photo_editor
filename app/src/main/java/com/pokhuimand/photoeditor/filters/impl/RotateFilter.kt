package com.pokhuimand.photoeditor.filters.impl

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.core.graphics.get
import androidx.core.graphics.set
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterCategory
import com.pokhuimand.photoeditor.filters.FilterSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.exp

data class RotateFilterSettings(
    val amountOf90Deg: Int
) :
    FilterSettings() {
    object Ranges {
        val amountOf90Deg = 0f..4f
    }

    companion object {
        val default = RotateFilterSettings(0)
    }
}

//TODO: implement proper threshold
class RotateFilter : Filter {
    override val id: String = "rotate"
    override val category: FilterCategory = FilterCategory.CropResize

    override suspend fun applyDefaults(image: Bitmap): Bitmap {
        return apply(image, RotateFilterSettings.default)
    }

    override suspend fun apply(image: Bitmap, settings: FilterSettings): Bitmap {
        val sets = settings as RotateFilterSettings
        return withContext(Dispatchers.Default) {
            rotate(
                image,
                sets.amountOf90Deg
            )
        }
    }

    private fun rotate(bitmap: Bitmap, turns: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate((turns * 90).toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

}