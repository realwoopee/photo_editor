package com.pokhuimand.photoeditor.filters

import android.graphics.Bitmap

data class NothingFilterSettings(val settings: String = "") : FilterSettings()

class NothingFilter :
    Filter {

    override val category: FilterCategory = FilterCategory.FaceDetect

    override val id: String = "nothing"

    override fun apply(image: Bitmap, settings: FilterSettings): Bitmap {
        return image
    }
}