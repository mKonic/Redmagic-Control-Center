package com.elitedarkkaiser.redmagic

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

internal object DeviceGateDialogs {
    data class Deps(
        val textPrimary: Int,
        val textSecondary: Int,
        val panelColor: Int,
        val borderColor: Int,
        val panelPressed: Int,
        val accent: Int,
        val typeface: Typeface?,
        val dp: (Int) -> Int,
        val roundedBg: (Int, Int, Int) -> Drawable,
        val roundedFill: (Int, Int) -> Drawable
    )

    fun showRootRequiredDialog(
        activity: MainActivity,
        onClose: () -> Unit,
        deps: Deps
    ) {
        val container = baseContainer(activity, deps)

        val titleView = title(activity, "Root Required", deps)
        val messageView = message(
            activity,
            "Root access is required for RedMagic hardware controls. Grant root access from Magisk, KernelSU, or APatch, then reopen the app.",
            deps
        )

        val closeButton = dialogButton(activity, "Close", deps)

        container.addView(titleView)
        container.addView(messageView)
        container.addView(closeButton)

        val dialog = AlertDialog.Builder(activity)
            .setView(container)
            .setCancelable(false)
            .create()

        closeButton.setOnClickListener {
            onClose()
            dialog.dismiss()
        }

        showTransparent(dialog)
    }

    private fun baseContainer(activity: MainActivity, deps: Deps): LinearLayout {
        return LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(deps.dp(22), deps.dp(18), deps.dp(22), deps.dp(14))
            background = deps.roundedBg(deps.panelColor, deps.borderColor, 22)
        }
    }

    private fun title(activity: MainActivity, textValue: String, deps: Deps): TextView {
        return TextView(activity).apply {
            text = textValue
            textSize = 20f
            setTextColor(deps.textPrimary)
            setTypeface(deps.typeface, Typeface.BOLD)
        }
    }

    private fun message(activity: MainActivity, textValue: String, deps: Deps): TextView {
        return TextView(activity).apply {
            text = textValue
            textSize = 13f
            setTextColor(deps.textSecondary)
            setPadding(0, deps.dp(10), 0, deps.dp(16))
        }
    }

    private fun dialogButton(activity: MainActivity, textValue: String, deps: Deps): Button {
        return Button(activity).apply {
            text = textValue
            textSize = 13f
            setAllCaps(false)
            setTextColor(deps.textPrimary)
            background = deps.roundedFill(deps.panelPressed, 14)
            setPadding(deps.dp(20), deps.dp(10), deps.dp(20), deps.dp(10))
        }
    }

    private fun showTransparent(dialog: AlertDialog) {
        dialog.show()
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.65f)
        }
    }
}
