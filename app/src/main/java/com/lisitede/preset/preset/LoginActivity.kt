package com.lisitede.preset.preset

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.therouter.router.Route

@Route(path = "/login")
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }
}
