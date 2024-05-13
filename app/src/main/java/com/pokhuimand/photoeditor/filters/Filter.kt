package com.pokhuimand.photoeditor.filters

import android.graphics.Bitmap
import java.io.Serializable

open class FilterSettings : Serializable {

}

interface Filter {
    val id: String
    val category: FilterCategory
    suspend fun apply(image: Bitmap, settings: FilterSettings): Bitmap
    suspend fun applyDefaults(image: Bitmap): Bitmap
}

enum class FilterCategory {
    CropResize,
    ColorCorrection,
    FaceDetect,
    Retouch,
    TriPointTransform
}