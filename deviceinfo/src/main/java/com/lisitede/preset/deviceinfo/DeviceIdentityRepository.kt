package com.lisitede.preset.deviceinfo

import android.content.Context
import com.lisitede.preset.deviceinfo.collectors.IdentityInfoCollector

class DeviceIdentityRepository(context: Context) {
    private val identityInfoCollector = IdentityInfoCollector(context)

    fun getIdentity(): Array<DeviceInfoEntry> {
        val info = identityInfoCollector.getIdentifierInfo()
        return arrayOf(
            info.oaid,
            info.vaid,
            info.aaid,
            info.gaid,
            info.android_id,
            info.widevine_device_id,
            info.imei
        )
    }
}
