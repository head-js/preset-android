package com.lisitede.preset.preset

import android.content.SharedPreferences

class TokenStorage(private val prefs: SharedPreferences) {

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USERNAME = "auth_username"
    }

    fun saveToken(token: String, username: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun clear() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USERNAME)
            .apply()
    }

    fun isLoggedIn(): Boolean = getToken() != null
}
