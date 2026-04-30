package com.elitedarkkaiser.redmagic

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.os.Build

object FirstInstallPermissionsDialog {
    private const val POST_NOTIFICATIONS_REQUEST_CODE = 4101

    fun show(
        activity: Activity,
        onSetupComplete: () -> Unit
    ) {
        AlertDialog.Builder(activity)
            .setTitle("App permissions setup")
            .setMessage(
                "RedMagic Control needs notification permission for foreground services and Usage Access for Game Mode detection. " +
                    "These are requested now so permission prompts do not appear later throughout the UI. " +
                    "\n\nAfter granting Usage Access, return to the app."
            )
            .setCancelable(false)
            .setPositiveButton("Start setup") { _, _ ->
                setFirstInstallPermissionsPromptedStorage(activity, true)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    activity.requestPermissions(
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        POST_NOTIFICATIONS_REQUEST_CODE
                    )
                }

                PermissionActions.openUsageStatsAccessSettings(activity)
                onSetupComplete()
            }
            .show()
    }
}
