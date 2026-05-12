package com.lisitede.preset.preset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var connectivityHelper: ConnectivityHelper
    private lateinit var webViewHelper: WebViewHelper
    private lateinit var statusText: TextView
    private lateinit var deviceInfoText: TextView
    private lateinit var packageInfoText: TextView
    private lateinit var logText: TextView
    private lateinit var postButton: Button
    private lateinit var countText: TextView
    private lateinit var logBuilder: StringBuilder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        connectivityHelper = ConnectivityHelper(requireContext())

        val webView = view.findViewById<WebView>(R.id.webView)
        webViewHelper = WebViewHelper(requireContext())
        webViewHelper.webView = webView
        webViewHelper.setup()
        logBuilder = StringBuilder()

        statusText = view.findViewById(R.id.statusText)
        deviceInfoText = view.findViewById(R.id.deviceInfoText)
        packageInfoText = view.findViewById(R.id.packageInfoText)
        logText = view.findViewById(R.id.logText)

        val info = DeviceInfoHelper().getDeviceInfo()
        deviceInfoText.text = "Device: ${info.brand} ${info.model} / Android ${info.androidVersion}"

        val pkgInfo = PackageInfoHelper(requireContext()).getAppPackageInfo()
        packageInfoText.text = "App: ${pkgInfo.packageName} ${pkgInfo.versionName}(${pkgInfo.versionCode})"

        view.findViewById<Button>(R.id.postButton).also { postButton = it }.setOnClickListener {
            appendLog("POST sending...")
            viewModel.sendPost(mapOf("key" to "value", "from" to "preset-android"))
        }
        countText = view.findViewById(R.id.countText)
        view.findViewById<Button>(R.id.incrementButton).setOnClickListener { viewModel.increment() }
        view.findViewById<Button>(R.id.decrementButton).setOnClickListener { viewModel.decrement() }
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.httpPostState.collect { state ->
                    postButton.isEnabled = !state.isLoading
                    if (!state.isLoading) {
                        if (state.error != null) {
                            appendLog("POST ERROR: ${state.error}")
                        } else if (state.response.isNotEmpty()) {
                            appendLog("POST OK: ${state.response}")
                        }
                    }
                }}
                launch { viewModel.count.collect { count ->
                    countText.text = count.toString()
                }}
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
}
