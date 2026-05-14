package com.lisitede.preset.preset.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lisitede.preset.preset.DeviceInfoHelper
import com.lisitede.preset.preset.PackageInfoHelper
import com.lisitede.preset.preset.R
import com.therouter.router.Route

@Route(path = "/main/detail")
class DetailPageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.page_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deviceInfoHelper = DeviceInfoHelper()
        val packageInfoHelper = PackageInfoHelper(requireContext())

        val info = deviceInfoHelper.getDeviceInfo()
        val pkgInfo = packageInfoHelper.getAppPackageInfo()

        view.findViewById<TextView>(R.id.detailInfo).text = buildString {
            appendLine("Device: ${info.brand} ${info.model}")
            appendLine("Android: ${info.androidVersion}")
            appendLine("App: ${pkgInfo.packageName}")
            appendLine("Version: ${pkgInfo.versionName}")
        }

        view.findViewById<Button>(R.id.backButton).setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
