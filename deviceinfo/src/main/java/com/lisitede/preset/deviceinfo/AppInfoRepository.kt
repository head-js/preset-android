package com.lisitede.preset.deviceinfo

import android.content.Context
import com.lisitede.preset.deviceinfo.collectors.AppInfoCollector

class AppInfoRepository(context: Context) {
    private val appInfoCollector = AppInfoCollector(context)

    fun getAppInfo(): Array<DeviceInfoEntry> {
        val info = appInfoCollector.getAppInfo()
        return arrayOf(
            info.app_name,
            info.package_name,
            info.version_name,
            info.version_code,
            info.signing_cert_digest
        )
    }
}
