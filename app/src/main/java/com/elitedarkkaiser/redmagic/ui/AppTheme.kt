package com.elitedarkkaiser.redmagic.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import kotlin.math.roundToInt

object AppTheme {
    val bgColor: Int = Color.parseColor("#0A0D12")
    val panelColor: Int = Color.parseColor("#121720")
    val panelPressed: Int = Color.parseColor("#1A2230")
    val borderColor: Int = Color.parseColor("#232C3B")
    val accentColor: Int = Color.parseColor("#8FA3BF")
    val chipOnColor: Int = Color.parseColor("#202B38")
    val chipActiveColor: Int = Color.parseColor("#2B3A4F")
    val dangerColor: Int = Color.parseColor("#5B2C33")
    val textPrimary: Int = Color.parseColor("#E8EEF7")
    val textSecondary: Int = Color.parseColor("#9AA8BA")
    val highlightBorder: Int = Color.parseColor("#7F8EA3")
    val appTypeface: Typeface? = Typeface.SANS_SERIF

    fun dp(context: Context, value: Int): Int {
        return (value * context.resources.displayMetrics.density).roundToInt()
    }

    fun roundedBg(fill: Int, stroke: Int, radiusPx: Float): GradientDrawable {
        return GradientDrawable().apply {
            setColor(fill)
            cornerRadius = radiusPx
            setStroke(1, stroke)
        }
    }
}
