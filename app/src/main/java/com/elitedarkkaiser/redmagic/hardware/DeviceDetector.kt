package com.elitedarkkaiser.redmagic.hardware

import android.os.Build
import com.elitedarkkaiser.redmagic.HardwareController
import com.elitedarkkaiser.redmagic.state.DeviceState

object DeviceDetector {
    const val REQUIRED_MODEL = "NX809J"

    fun readState(): DeviceState {
        val buildModel = Build.MODEL ?: "Unknown"
        val productModel = prop("ro.product.model", "Unknown")
        val vendorModel = prop("ro.product.vendor.model", "Unknown")
        val marketName = prop("ro.product.marketname", "Unknown")

        return DeviceState(
            supported = isSupported(buildModel, productModel, vendorModel, marketName),
            rooted = RootProvider.hasRoot(),
            model = buildModel,
            productModel = productModel,
            vendorModel = vendorModel,
            marketName = marketName,
            fingerprint = Build.FINGERPRINT ?: "Unknown",
            cpuModel = HardwareController.readCpuModel(),
            ramText = HardwareController.readRamInfo()
        )
    }

    fun isSupportedDevice(): Boolean {
        val buildModel = Build.MODEL ?: ""
        val productModel = prop("ro.product.model", "")
        val vendorModel = prop("ro.product.vendor.model", "")
        val marketName = prop("ro.product.marketname", "")

        return isSupported(buildModel, productModel, vendorModel, marketName)
    }

    private fun isSupported(
        buildModel: String,
        productModel: String,
        vendorModel: String,
        marketName: String
    ): Boolean {
        return buildModel.contains(REQUIRED_MODEL, ignoreCase = true) ||
            productModel.contains(REQUIRED_MODEL, ignoreCase = true) ||
            vendorModel.contains(REQUIRED_MODEL, ignoreCase = true) ||
            marketName.contains(REQUIRED_MODEL, ignoreCase = true)
    }

    private fun prop(name: String, fallback: String): String {
        return RootProvider.output("getprop $name")?.trim().orEmpty().ifBlank { fallback }
    }
}
