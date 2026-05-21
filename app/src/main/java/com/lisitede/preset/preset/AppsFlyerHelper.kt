package com.lisitede.preset.preset

import android.app.Application
import android.content.Context
import com.appsflyer.AppsFlyerLib

object AppsFlyerHelper {
    private var initialized = false

    fun initIfAllowed(application: Application) {
        if (!PrivacyConsentStorage.isPrivacyAgreed(application)) return
        if (initialized) return
        AppsFlyerLib.getInstance().init(BuildConfig.VITE_APP_APPSFLYER_KEY, null, application)
        AppsFlyerLib.getInstance().start(application)
        initialized = true
    }

    fun getAfId(context: Context): String {
        if (!initialized) return ""
        return AppsFlyerLib.getInstance().getAppsFlyerUID(context).orEmpty()
    }
}
