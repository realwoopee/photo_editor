package com.pokhuimand.photoeditor.filters

import android.graphics.Bitmap
import java.io.Serializable

open class FilterSettings : Serializable

open class FilterDataCache : Serializable

interface Filter {
    val id: String
    val category: FilterCategory

    suspend fun apply(
        image: Bitmap,
        settings: FilterSettings,
        cache: FilterDataCache
    ): Bitmap

    suspend fun applyDefaults(
        image: Bitmap,
        cache: FilterDataCache
    ): Bitmap

    fun buildCache(): FilterDataCache = FilterDataCache()
}

enum class FilterCategory {
    CropResize,
    ColorCorrection,
    FaceRecognition,
    Retouch,
    TriPointTransform
}