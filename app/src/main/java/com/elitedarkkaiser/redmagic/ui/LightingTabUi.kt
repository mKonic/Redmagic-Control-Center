package com.elitedarkkaiser.redmagic.ui

import android.app.Activity
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

object LightingTabUi {
    fun create(activity: Activity, deps: LightingTabDeps): LinearLayout {
        val container = deps.scrollTabContainer()

        val previewCard = deps.sectionPanel().apply {
            addView(deps.sectionHeader("⚡", "PREVIEW"))
            addView(TextView(activity).apply {
                text = "Apply LED color/effect changes instantly while selecting"
                textSize = 13f
                setTextColor(AppTheme.textSecondary)
                setPadding(0, 0, 0, deps.dp(10))
            })

            val previewSwitch = android.widget.Switch(activity).apply {
                isChecked = deps.getRealTimePreviewEnabled()
                setOnCheckedChangeListener { _, checked ->
                    deps.setRealTimePreviewEnabled(checked)
                    deps.saveRealTimePreviewEnabled(checked)
                }
            }

            val previewRow = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                addView(TextView(activity).apply {
                    text = "Real-time preview"
                    textSize = 14f
                    setTextColor(AppTheme.textPrimary)
                }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                addView(previewSwitch)
            }

            addView(previewRow)
        }

        val zonesCard = deps.sectionPanel().apply {
            addView(deps.sectionHeader("✦", "LED ZONES"))
            addView(deps.bodyText("Configure fan LEDs, logo lighting, and shoulder strip effects separately."))
            addView(deps.singleRow(deps.actionButton("FAN LED", false) {
                deps.showFanLedDialog()
            }))
            addView(deps.row(
                deps.actionButton("LOGO LED", false) { deps.showLogoLedDialog() },
                deps.actionButton("SHOULDER LEDS", false) { deps.showShoulderLedDialog() }
            ))
        }

        val gameModeCard = deps.sectionPanel().apply {
            addView(deps.sectionHeader("🎮", "GAME MODE"))
            addView(deps.bodyText("Pick apps and configure a full hardware profile to apply while a game is active."))
            addView(deps.infoRow("Apps", deps.subtleLabel(deps.gameModeAppsSummary())))
            addView(deps.row(
                deps.actionButton("SELECT APPS", false) { deps.showGameModeAppPicker() },
                deps.actionButton("GAME PROFILE", false) { deps.showGameModeProfileDialog() }
            ))
        }

        container.addView(previewCard)
        container.addView(zonesCard)
        container.addView(gameModeCard)

        return container
    }
}
