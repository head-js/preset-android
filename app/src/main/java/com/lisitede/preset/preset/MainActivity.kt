package com.lisitede.preset.preset

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.therouter.router.Route

@Route(path = "/main")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        NavigationUI.setupWithNavController(bottomNav, navController)

        PageRouter.navigateToPage(this)

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
