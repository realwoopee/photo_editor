package com.pokhuimand.photoeditor.filters

import com.pokhuimand.photoeditor.data.AppContainer
import com.pokhuimand.photoeditor.filters.impl.ResizeFilter
import com.pokhuimand.photoeditor.filters.impl.RotateFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.ContrastAndBrightnessFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.DitheringFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.GrayscaleFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.PixelSortingFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.UnsharpMaskingFilter

class FilterFactory(appContainer: AppContainer) {
    fun buildSet(): Set<Filter> = setOf(
        UnsharpMaskingFilter(),
        RotateFilter(),
        GrayscaleFilter(),
        DitheringFilter(),
        ContrastAndBrightnessFilter(),
        PixelSortingFilter(),
        ResizeFilter()
    )
}