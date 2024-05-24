package com.pokhuimand.photoeditor.utils

import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
@ReadOnlyComposable
fun colorAttribute(attrColor: Int) = colorResource(TypedValue().apply {
    LocalContext.current.theme.resolveAttribute(
        attrColor,
        this,
        true
    )
}.resourceId)

fun rgbToSrgbComponent(rgb: Double): Double {
    return if (rgb <= 0.0031308) {
        12.92 * rgb
    } else {
        1.055 * rgb.pow(1.0 / 2.4) - 0.055
    }
}

fun srgbToRgbComponent(srgb: Double): Double {
    return if (srgb <= 0.04045) {
        srgb / 12.92
    } else {
        ((srgb + 0.055) / 1.055).pow(2.4)
    }
}

fun rgbToSrgb(argb: Int): Int {
    val alpha = Color.alpha(argb)
    val red = Color.red(argb)
    val green = Color.green(argb)
    val blue = Color.blue(argb)

    val srgbRed = (rgbToSrgbComponent(red / 255.0) * 255).toInt()
    val srgbGreen = (rgbToSrgbComponent(green / 255.0) * 255).toInt()
    val srgbBlue = (rgbToSrgbComponent(blue / 255.0) * 255).toInt()

    return Color.argb(alpha, srgbRed, srgbGreen, srgbBlue)
}

fun srgbToRgb(argb: Int): Int {
    val alpha = Color.alpha(argb)
    val red = Color.red(argb)
    val green = Color.green(argb)
    val blue = Color.blue(argb)

    val rgbRed = (srgbToRgbComponent(red / 255.0) * 255).toInt()
    val rgbGreen = (srgbToRgbComponent(green / 255.0) * 255).toInt()
    val rgbBlue = (srgbToRgbComponent(blue / 255.0) * 255).toInt()

    return Color.argb(alpha, rgbRed, rgbGreen, rgbBlue)
}

inline val @receiver:ColorInt Int.hue: Double
    get() {
        val r = this.red / 255.0
        val g = this.green / 255.0
        val b = this.blue / 255.0
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        var hue = when {
            max == min -> 0.0
            max == r && g >= b -> 60 * ((g - b) / (max - min))
            max == r && g < b -> 60 * ((g - b) / (max - min)) + 360
            max == g -> 60 * ((b - r) / (max - min)) + 120
            max == b -> 60 * ((r - g) / (max - min)) + 240
            else -> 0.0
        }
        if (hue < 0) {
            hue += 360
        }
        return hue
    }

inline val @receiver:ColorInt Int.saturation: Double
    get() {
        val r = this.red / 255.0
        val g = this.green / 255.0
        val b = this.blue / 255.0
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        return if (max == 0.0) 0.0 else 1 - (min / max)
    }

inline val @receiver:ColorInt Int.value: Double
    get() {
        val r = this.red / 255.0
        val g = this.green / 255.0
        val b = this.blue / 255.0
        val max = maxOf(r, g, b)
        return max
    }

inline fun hsvToArgb(hue: Double, saturation: Double, value: Double, alpha: Double): Int {
    val h1 = hue / 60
    val c = value * saturation
    val x = c * (1 - abs((h1 % 2) - 1))
    val m = value - c
    val r: Double
    val g: Double
    val b: Double
    when (h1) {
        in 0.0f..1.0f -> {
            r = c
            g = x
            b = 0.0
        }

        in 1.0f..2.0f -> {
            r = x
            g = c
            b = 0.0
        }

        in 2.0f..3.0f -> {
            r = 0.0
            g = c
            b = x
        }

        in 3.0f..4.0f -> {
            r = 0.0
            g = x
            b = c
        }

        in 4.0f..5.0f -> {
            r = x
            g = 0.0
            b = c
        }

        else -> {
            r = c
            g = 0.0
            b = x
        }
    }

    return Color.argb(
        alpha.coerceIn(0.0, 255.0).roundToInt(),
        ((r + m) * 255).coerceIn(0.0, 255.0).roundToInt(),
        ((g + m) * 255).coerceIn(0.0, 255.0).roundToInt(),
        ((b + m) * 255).coerceIn(0.0, 255.0).roundToInt()
    )
}