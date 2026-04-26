package com.elitedarkkaiser.redmagic

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

internal object ModeCardUi {
    data class Deps(
        val textPrimary: Int,
        val textSecondary: Int,
        val panelColor: Int,
        val borderColor: Int,
        val typeface: Typeface?,
        val dp: (Int) -> Int,
        val roundedBg: (Int, Int, Int) -> Drawable,
        val applyPressEffect: (View) -> Unit
    )

    fun create(
        activity: MainActivity,
        title: String,
        subtitle: String,
        onClick: () -> Unit,
        deps: Deps
    ): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = deps.roundedBg(deps.panelColor, deps.borderColor, 18)
            setPadding(deps.dp(12), deps.dp(12), deps.dp(12), deps.dp(12))
            layoutParams = LinearLayout.LayoutParams(0, deps.dp(92), 1f).apply {
                setMargins(deps.dp(4), 0, deps.dp(4), 0)
            }

            addView(TextView(activity).apply {
                text = title
                textSize = 14f
                setTextColor(deps.textPrimary)
                setTypeface(deps.typeface, Typeface.BOLD)
                gravity = Gravity.CENTER
            })

            addView(TextView(activity).apply {
                text = subtitle
                textSize = 11f
                setTextColor(deps.textSecondary)
                gravity = Gravity.CENTER
                setPadding(0, deps.dp(6), 0, 0)
            })

            setOnClickListener { onClick() }
            deps.applyPressEffect(this)
        }
    }
}
