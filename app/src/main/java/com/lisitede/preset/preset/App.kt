package com.lisitede.preset.preset

import android.app.Application
import com.lynx.tasm.LynxEnv

class App : Application() {

    lateinit var deviceInfoRepository: DeviceInfoRepository
        private set

    override fun onCreate() {
        super.onCreate()

        deviceInfoRepository = DeviceInfoRepository(this)
        AppsFlyerHelper.initIfAllowed(this)
        LynxEnv.inst().init(this, null, null, null)
    }
}
