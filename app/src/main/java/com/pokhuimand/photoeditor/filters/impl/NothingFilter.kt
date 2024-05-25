package com.pokhuimand.photoeditor.filters.impl

import android.graphics.Bitmap
import com.pokhuimand.photoeditor.filters.Filter
import com.pokhuimand.photoeditor.filters.FilterCategory
import com.pokhuimand.photoeditor.filters.FilterDataCache
import com.pokhuimand.photoeditor.filters.FilterSettings

data class NothingFilterSettings(val settings: String = "") : FilterSettings()

class NothingFilter :
    Filter {

    override val category: FilterCategory = FilterCategory.ColorCorrection

    override val id: String = "nothing"

    override suspend fun apply(
        image: Bitmap,
        settings: FilterSettings,
        cache: FilterDataCache
    ): Bitmap {
        return image
    }

    override suspend fun applyDefaults(image: Bitmap, cache: FilterDataCache): Bitmap {
        return image
    }
}