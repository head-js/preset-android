package com.lisitede.preset.preset

import android.os.Build

data class DeviceInfo(
    val brand: String,
    val model: String,
    val manufacturer: String,
    val device: String,
    val androidVersion: String,
    val sdkInt: Int,
    val board: String,
    val hardware: String,
    val product: String
)

class DeviceInfoHelper {

    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            brand = Build.BRAND ?: "unknown",
            model = Build.MODEL ?: "unknown",
            manufacturer = Build.MANUFACTURER ?: "unknown",
            device = Build.DEVICE ?: "unknown",
            androidVersion = Build.VERSION.RELEASE ?: "unknown",
            sdkInt = Build.VERSION.SDK_INT,
            board = Build.BOARD ?: "unknown",
            hardware = Build.HARDWARE ?: "unknown",
            product = Build.PRODUCT ?: "unknown"
        )
    }

    fun getDisplayString(): String {
        val info = getDeviceInfo()
        return buildString {
            appendLine("Brand: ${info.brand}")
            appendLine("Model: ${info.model}")
            appendLine("Manufacturer: ${info.manufacturer}")
            appendLine("Device: ${info.device}")
            appendLine("Android Version: ${info.androidVersion}")
            appendLine("SDK Int: ${info.sdkInt}")
            appendLine("Board: ${info.board}")
            appendLine("Hardware: ${info.hardware}")
            appendLine("Product: ${info.product}")
        }
    }
}
