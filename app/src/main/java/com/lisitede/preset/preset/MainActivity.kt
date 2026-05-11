package com.lisitede.preset.preset

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lisitede.preset.preset.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private val apiClient = ApiClient.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectivityHelper = ConnectivityHelper(this)
        deviceInfoHelper = DeviceInfoHelper()
        packageInfoHelper = PackageInfoHelper(this)
        webViewHelper = WebViewHelper(this)
        webViewHelper.setup()
        logBuilder = StringBuilder()

        statusText = TextView(this).apply {
            text = "Net: checking..."
            textSize = 14f
            setPadding(24, 24, 24, 4)
        }

        deviceInfoText = TextView(this).apply {
            val info = deviceInfoHelper.getDeviceInfo()
            text = "Device: ${info.brand} ${info.model} / Android ${info.androidVersion}"
            textSize = 14f
            setPadding(24, 4, 24, 4)
        }

        packageInfoText = TextView(this).apply {
            val info = packageInfoHelper.getAppPackageInfo()
            text = "App: ${info.packageName} ${info.versionName}(${info.versionCode})"
            textSize = 14f
            setPadding(24, 4, 24, 4)
        }

        logText = TextView(this).apply {
            textSize = 14f
            setPadding(24, 4, 24, 4)
        }

        val infoScrollView = ScrollView(this).apply { addView(logText) }

        val postButton = Button(this).apply {
            text = "Send POST"
            setOnClickListener { sendHttpPost() }
        }

        val infoLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(statusText)
            addView(deviceInfoText)
            addView(packageInfoText)
            addView(postButton)
            addView(infoScrollView)
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(infoLayout, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                2f
            ))
            addView(webViewHelper.webView, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
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

    private fun sendHttpPost() {
        appendLog("POST sending...")
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiClient.postTest(mapOf("key" to "value", "from" to "preset-android"))
                }
                appendLog("POST OK: origin=${response.origin}, url=${response.url}, data=${response.data}")
            } catch (e: Exception) {
                appendLog("POST ERROR: ${e.message}")
            }
        }
    }
}
