package com.elitedarkkaiser.redmagic

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.net.Uri
import android.os.Build
import android.provider.Settings

object FirstInstallPermissionsDialog {
    private const val POST_NOTIFICATIONS_REQUEST_CODE = 4101

    fun show(
        activity: Activity,
        onSetupComplete: () -> Unit
    ) {
        AlertDialog.Builder(activity)
            .setTitle("App permissions setup")
            .setMessage(
                "RedMagic Control needs a few permissions before first use:\n\n" +
                    "• Usage Access: detects selected Game Mode apps\n" +
                    "• Notifications: keeps foreground hardware services visible\n" +
                    "• Display over other apps: required for trigger touch setup overlay\n\n" +
                    "Grant these now, then return to the app. This dialog will continue showing until Usage Access is granted."
            )
            .setCancelable(false)
            .setPositiveButton("Open permissions") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    activity.requestPermissions(
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        POST_NOTIFICATIONS_REQUEST_CODE
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
                    activity.startActivity(
                        android.content.Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${activity.packageName}")
                        )
                    )
                } else {
                    PermissionActions.openUsageStatsAccessSettings(activity)
                }
            }
            .setNeutralButton("Grant with root") { _, _ ->
                val ok = RootShell.exec(
                    "appops set ${activity.packageName} GET_USAGE_STATS allow; " +
                        "appops set ${activity.packageName} SYSTEM_ALERT_WINDOW allow; " +
                        "pm grant ${activity.packageName} android.permission.POST_NOTIFICATIONS || true; " +
                        "pm grant ${activity.packageName} android.permission.READ_PHONE_STATE || true"
                )

                android.widget.Toast.makeText(
                    activity,
                    if (ok) "Root permissions applied. Reopen the app." else "Root permission grant failed.",
                    android.widget.Toast.LENGTH_LONG
                ).show()

                if (ok) {
                    setFirstInstallPermissionsPromptedStorage(activity, true)
                    onSetupComplete()
                }
            }
            .setNegativeButton("Continue") { _, _ ->
                if (PermissionActions.hasUsageStatsPermission(activity)) {
                    setFirstInstallPermissionsPromptedStorage(activity, true)
                    onSetupComplete()
                } else {
                    PermissionActions.openUsageStatsAccessSettings(activity)
                }
            }
            .show()
    }
}
