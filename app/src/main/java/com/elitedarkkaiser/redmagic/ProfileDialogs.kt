package com.elitedarkkaiser.redmagic

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast

internal object ProfileDialogs {

    fun showDeleteProfileDialog(
        context: Context,
        profileName: String,
        onConfirmDelete: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle("Delete Profile")
            .setMessage("Delete $profileName?")
            .setPositiveButton("Delete") { _, _ ->
                onConfirmDelete()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun saveProfile(
        context: Context,
        profileName: String,
        profile: HardwareProfile,
        onSaved: () -> Unit
    ) {
        ProfileManager.upsertProfile(context, profile)
        Toast.makeText(context, "Saved $profileName", Toast.LENGTH_SHORT).show()
        onSaved()
    }

    fun renderProfiles(
        context: Context,
        profileList: LinearLayout,
        profiles: List<HardwareProfile>,
        subtleLabel: (String) -> View,
        actionButton: (String, Boolean, () -> Unit) -> View,
        space: (Int) -> View,
        dp: (Int) -> Int,
        onApplyProfile: (HardwareProfile) -> Unit,
        onDeleteProfile: (HardwareProfile) -> Unit
    ) {
        profileList.removeAllViews()

        if (profiles.isEmpty()) {
            profileList.addView(subtleLabel("No saved profiles yet"))
            return
        }

        profiles.forEach { profile ->
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val applyBtn = actionButton(profile.name, false) {
                onApplyProfile(profile)
            }.apply {
                setPadding(dp(16), dp(10), dp(16), dp(10))
            }

            val deleteBtn = actionButton("DEL", true) {
                onDeleteProfile(profile)
            }.apply {
                setPadding(dp(14), dp(10), dp(14), dp(10))
            }

            row.addView(
                applyBtn,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            )
            row.addView(space(dp(8)))
            row.addView(deleteBtn)

            profileList.addView(row)
            profileList.addView(space(dp(10)))
        }
    }
}
