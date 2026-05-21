package com.lisitede.preset.preset

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var ready = false
        splashScreen.setKeepOnScreenCondition { !ready }

        Handler(Looper.getMainLooper()).postDelayed({
            ready = true
            PageRouter.navigate(this, "/main/home")
            finish()
        }, 2000)
    }
}
