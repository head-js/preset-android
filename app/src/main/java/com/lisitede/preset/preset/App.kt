package com.lisitede.preset.preset

import android.app.Application
import com.lynx.tasm.LynxEnv

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        LynxEnv.inst().init(this, null, null, null)
    }
}
