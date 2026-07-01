package com.lisitede.preset.preset

import android.app.Application
import com.kongzue.dialogx.DialogX
import com.lynx.tasm.LynxEnv
import com.lisitede.preset.deviceinfo.DeviceIdentityRepository
import com.lisitede.preset.deviceinfo.DeviceInfoRepository

class App : Application() {

    lateinit var deviceIdentityRepository: DeviceIdentityRepository
        private set

    lateinit var deviceInfoRepository: DeviceInfoRepository
        private set

    override fun onCreate() {
        super.onCreate()

        deviceIdentityRepository = DeviceIdentityRepository(this)
        deviceInfoRepository = DeviceInfoRepository(this)
        AppsFlyerHelper.initIfAllowed(this)
        DialogX.init(this)
        LynxEnv.inst().init(this, null, null, null)
    }
}
