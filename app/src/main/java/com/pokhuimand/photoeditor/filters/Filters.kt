package com.pokhuimand.photoeditor.filters

import com.pokhuimand.photoeditor.filters.impl.colorcorrection.ContrastAndBrightnessFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.DitheringFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.GrayscaleFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.PixelSortingFilter
import com.pokhuimand.photoeditor.filters.impl.RotateFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.UnsharpMaskingFilter

object Filters {
    val implementations: Set<Filter> =
        setOf(
            UnsharpMaskingFilter(),
            RotateFilter(),
            GrayscaleFilter(),
            DitheringFilter(),
            ContrastAndBrightnessFilter(),
            PixelSortingFilter()
        )
    var keyedImplementations = implementations.associateBy { f -> f.id }
    val keys = implementations.map { f -> f.id }.toSet()
}