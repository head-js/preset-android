package com.lisitede.preset.preset

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var connectivityHelper: ConnectivityHelper
    private lateinit var deviceInfoHelper: DeviceInfoHelper
    private lateinit var statusText: TextView
    private lateinit var deviceInfoText: TextView
    private lateinit var logText: TextView
    private lateinit var logBuilder: StringBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectivityHelper = ConnectivityHelper(this)
        deviceInfoHelper = DeviceInfoHelper()
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

        logText = TextView(this).apply {
            textSize = 16f
            setPadding(48, 24, 48, 24)
        }

        val scrollView = ScrollView(this).apply { addView(logText) }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(statusText)
            addView(deviceInfoText)
            addView(scrollView)
        }

        setContentView(layout)

        updateStatus()
        connectivityHelper.registerCallback { type ->
            runOnUiThread {
                appendLog("Network changed: $type")
                updateStatus()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityHelper.unregisterCallback()
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
