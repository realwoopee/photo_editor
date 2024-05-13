package com.pokhuimand.photoeditor.filters

import com.pokhuimand.photoeditor.filters.impl.NothingFilter
import com.pokhuimand.photoeditor.filters.impl.UnsharpMaskingFilter

object Filters {
    val implementations: Set<Filter> = setOf(NothingFilter(), UnsharpMaskingFilter())
    var keyedImplementations = implementations.associateBy { f -> f.id }
    val keys = implementations.map { f -> f.id }.toSet()
}