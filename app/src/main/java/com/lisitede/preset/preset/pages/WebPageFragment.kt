package com.lisitede.preset.preset.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.lisitede.preset.preset.R
import com.lisitede.preset.preset.WebViewHelper
import com.therouter.router.Route

@Route(path = "/main/web")
class WebPageFragment : Fragment() {

    private lateinit var webViewHelper: WebViewHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.page_web, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val webView = view.findViewById<WebView>(R.id.webView)
        webViewHelper = WebViewHelper(requireContext())
        webViewHelper.webView = webView
        webViewHelper.setup()
        webViewHelper.loadUrl("https://www.baidu.com")
    }

    override fun onResume() {
        super.onResume()
        if (::webViewHelper.isInitialized) webViewHelper.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (::webViewHelper.isInitialized) webViewHelper.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::webViewHelper.isInitialized) webViewHelper.destroy()
    }
}
