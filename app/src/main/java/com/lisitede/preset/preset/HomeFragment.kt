package com.lisitede.preset.preset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.lisitede.preset.preset.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        connectivityHelper = ConnectivityHelper(requireContext())
        deviceInfoHelper = DeviceInfoHelper()
        packageInfoHelper = PackageInfoHelper(requireContext())

        val webView = view.findViewById<WebView>(R.id.webView)
        webViewHelper = WebViewHelper(requireContext())
        webViewHelper.webView = webView
        webViewHelper.setup()
        logBuilder = StringBuilder()

        statusText = view.findViewById(R.id.statusText)
        deviceInfoText = view.findViewById(R.id.deviceInfoText)
        packageInfoText = view.findViewById(R.id.packageInfoText)
        logText = view.findViewById(R.id.logText)

        val info = deviceInfoHelper.getDeviceInfo()
        deviceInfoText.text = "Device: ${info.brand} ${info.model} / Android ${info.androidVersion}"

        val pkgInfo = packageInfoHelper.getAppPackageInfo()
        packageInfoText.text = "App: ${pkgInfo.packageName} ${pkgInfo.versionName}(${pkgInfo.versionCode})"

        view.findViewById<Button>(R.id.postButton).setOnClickListener { sendHttpPost() }
        view.findViewById<Button>(R.id.navigateButton).setOnClickListener {
            findNavController().navigate(R.id.action_home_to_detail)
        }

        webViewHelper.onPageStarted = { url -> appendLog("WebView loading: $url") }
        webViewHelper.onPageFinished = { url -> appendLog("WebView loaded: $url") }
        webViewHelper.onPageError = { error -> appendLog("WebView error: $error") }
        webViewHelper.loadUrl("https://www.baidu.com")

        updateStatus()
        connectivityHelper.registerCallback { type ->
            requireActivity().runOnUiThread {
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

    override fun onDestroyView() {
        super.onDestroyView()
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
