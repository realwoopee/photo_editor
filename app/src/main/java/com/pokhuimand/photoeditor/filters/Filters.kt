package com.pokhuimand.photoeditor.filters

object Filters {
    val implementations: Set<Filter> = setOf(NothingFilter())
    var keyedImplementations = implementations.associateBy { f -> f.id }
    val keys = implementations.map { f -> f.id }.toSet()
}