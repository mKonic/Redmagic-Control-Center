package com.elitedarkkaiser.redmagic.ui.components

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.elitedarkkaiser.redmagic.ui.AppTheme

object UiKit {
    fun titleText(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(AppTheme.textPrimary)
            textSize = 22f
            typeface = Typeface.create(AppTheme.appTypeface, Typeface.BOLD)
        }
    }

    fun subtitleText(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(AppTheme.textSecondary)
            textSize = 14f
            typeface = AppTheme.appTypeface
        }
    }

    fun bodyText(context: Context, text: String): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(AppTheme.textSecondary)
            textSize = 13f
            typeface = AppTheme.appTypeface
            setLineSpacing(0f, 1.08f)
        }
    }

    fun sectionPanel(context: Context): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                AppTheme.dp(context, 16),
                AppTheme.dp(context, 14),
                AppTheme.dp(context, 16),
                AppTheme.dp(context, 14)
            )
            background = AppTheme.roundedBg(
                AppTheme.panelColor,
                AppTheme.borderColor,
                AppTheme.dp(context, 18).toFloat()
            )
        }
    }

    fun actionButton(context: Context, text: String, isDanger: Boolean = false): Button {
        return Button(context).apply {
            this.text = text
            isAllCaps = false
            gravity = Gravity.CENTER
            setTextColor(AppTheme.textPrimary)
            textSize = 12f
            typeface = Typeface.create(AppTheme.appTypeface, Typeface.BOLD)
            background = AppTheme.roundedBg(
                if (isDanger) AppTheme.dangerColor else AppTheme.chipOnColor,
                AppTheme.borderColor,
                AppTheme.dp(context, 14).toFloat()
            )
        }
    }
}
