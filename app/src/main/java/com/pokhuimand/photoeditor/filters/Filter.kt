package com.pokhuimand.photoeditor.filters

import android.graphics.Bitmap
import java.io.Serializable

sealed class FilterSettings : Serializable {

}

interface Filter<T> where T : FilterSettings {
    val settings: T
    fun apply(image: Bitmap): Bitmap
}