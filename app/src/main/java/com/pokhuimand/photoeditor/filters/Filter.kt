package com.pokhuimand.photoeditor.filters

import android.graphics.Bitmap
import java.io.Serializable

open class FilterSettings : Serializable {

}

interface Filter {
    val id: String
    val category: FilterCategory
    fun apply(image: Bitmap, settings: FilterSettings): Bitmap
}

enum class FilterCategory {
    CropResize,
    ColorCorrection,
    FaceDetect,
    Retouch,
    TriPointTransform
}