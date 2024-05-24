package com.pokhuimand.photoeditor.filters

import com.pokhuimand.photoeditor.data.AppContainer
import com.pokhuimand.photoeditor.filters.impl.ResizeFilter
import com.pokhuimand.photoeditor.filters.impl.RotateFilter
import com.pokhuimand.photoeditor.filters.impl.FaceRecognition
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.ContrastAndBrightnessFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.DitheringFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.GrayscaleFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.PixelSortingFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.TempAndTintFilter
import com.pokhuimand.photoeditor.filters.impl.colorcorrection.UnsharpMaskingFilter

class FilterFactory(private val appContainer: AppContainer) {
    fun buildSet(): Set<Filter> = setOf(
        UnsharpMaskingFilter(),
        RotateFilter(),
        GrayscaleFilter(),
        DitheringFilter(),
        ContrastAndBrightnessFilter(),
        TempAndTintFilter(),
        PixelSortingFilter(),
        ResizeFilter(),
        FaceRecognition(appContainer.modelLoader.modelFilePath)
    )
}