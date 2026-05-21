package com.lisitede.preset.preset

import android.content.Context

object PrivacyConsentStorage {
    private const val PREFS_NAME = "privacy_prefs"
    private const val KEY_PRIVACY_AGREED = "privacy_agreed"

    fun isPrivacyAgreed(context: Context): Boolean {
        return context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_PRIVACY_AGREED, false)
    }

    fun setPrivacyAgreed(context: Context) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_PRIVACY_AGREED, true)
            .apply()
    }
}
