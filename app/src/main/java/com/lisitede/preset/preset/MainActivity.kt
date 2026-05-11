package com.lisitede.preset.preset

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var connectivityHelper: ConnectivityHelper
    private lateinit var deviceInfoHelper: DeviceInfoHelper
    private lateinit var packageInfoHelper: PackageInfoHelper
    private lateinit var webViewHelper: WebViewHelper
    private lateinit var statusText: TextView
    private lateinit var deviceInfoText: TextView
    private lateinit var packageInfoText: TextView
    private lateinit var logText: TextView
    private lateinit var logBuilder: StringBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectivityHelper = ConnectivityHelper(this)
        deviceInfoHelper = DeviceInfoHelper()
        packageInfoHelper = PackageInfoHelper(this)
        webViewHelper = WebViewHelper(this)
        webViewHelper.setup()
        logBuilder = StringBuilder()

        statusText = TextView(this).apply {
            text = "Network: checking..."
            textSize = 24f
            setPadding(48, 48, 48, 24)
        }

        deviceInfoText = TextView(this).apply {
            text = deviceInfoHelper.getDisplayString()
            textSize = 14f
            setPadding(48, 24, 48, 24)
        }

        packageInfoText = TextView(this).apply {
            text = packageInfoHelper.getDisplayString()
            textSize = 14f
            setPadding(48, 24, 48, 24)
        }

        logText = TextView(this).apply {
            textSize = 16f
            setPadding(48, 24, 48, 24)
        }

        val infoScrollView = ScrollView(this).apply { addView(logText) }

        val infoLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(statusText)
            addView(deviceInfoText)
            addView(packageInfoText)
            addView(infoScrollView)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(infoLayout, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            ))
            addView(webViewHelper.webView, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                2f
            ))
        }

        setContentView(layout)

        webViewHelper.onPageStarted = { url -> appendLog("WebView loading: $url") }
        webViewHelper.onPageFinished = { url -> appendLog("WebView loaded: $url") }
        webViewHelper.onPageError = { error -> appendLog("WebView error: $error") }

        webViewHelper.loadUrl("https://www.baidu.com")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webViewHelper.canGoBack()) {
                    webViewHelper.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        updateStatus()
        connectivityHelper.registerCallback { type ->
            runOnUiThread {
                appendLog("Network changed: $type")
                updateStatus()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        webViewHelper.onResume()
    }

    override fun onPause() {
        super.onPause()
        webViewHelper.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityHelper.unregisterCallback()
        webViewHelper.destroy()
    }

    private fun updateStatus() {
        val type = connectivityHelper.getCurrentConnectivity()
        statusText.text = "Network: $type"
    }

    private fun appendLog(message: String) {
        logBuilder.insert(0, "$message\n")
        logText.text = logBuilder.toString()
    }
}
