package com.elitedarkkaiser.redmagic

import android.content.Context

object DeviceScanActions {
    fun runBackgroundScan(context: Context) {
        Thread {
            val report = DeviceCapabilityScanner.scan()
            saveDeviceCapabilityReportStorage(context, report)
        }.start()
    }
}
