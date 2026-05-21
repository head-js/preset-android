package com.lisitede.preset.preset

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.therouter.router.Route

@Route(path = "/login")
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        AppsFlyerHelper.initIfAllowed(application)

        if (!PrivacyConsentStorage.isPrivacyAgreed(this)) {
            showPrivacyDialog()
        }
    }

    private fun showPrivacyDialog() {
        AlertDialog.Builder(this)
            .setTitle("隐私协议")
            .setMessage("请先同意隐私协议以继续使用")
            .setPositiveButton("同意") { _, _ ->
                PrivacyConsentStorage.setPrivacyAgreed(this)
                AppsFlyerHelper.initIfAllowed(application)
            }
            .setNegativeButton("拒绝", null)
            .setCancelable(false)
            .show()
    }
}
