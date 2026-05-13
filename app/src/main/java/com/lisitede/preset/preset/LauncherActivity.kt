package com.lisitede.preset.preset

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.appcompat.app.AppCompatActivity

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("preset_prefs", 0)
        val tokenStorage = TokenStorage(prefs)
        val loggedIn = tokenStorage.isLoggedIn()

        var ready = false
        splashScreen.setKeepOnScreenCondition { !ready }

        Handler(Looper.getMainLooper()).postDelayed({
            ready = true
            if (loggedIn) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000)
    }
}
