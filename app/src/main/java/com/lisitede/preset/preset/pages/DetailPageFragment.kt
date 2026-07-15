package com.lisitede.preset.preset.pages

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.lisitede.preset.deviceinfo.DeviceInfoRepository
import com.lisitede.preset.preset.App
import com.lisitede.preset.preset.AppPackageInfo
import com.lisitede.preset.preset.PackageInfoHelper
import com.lisitede.preset.preset.R
import com.therouter.router.Route

private data class DisplayItem(
    val title: String,
    val content: String,
    val isSection: Boolean = false
)

@Route(path = "/main/detail")
class DetailPageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.page_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val app = requireContext().applicationContext as App
        val repo = app.deviceInfoRepository
        val packageInfoHelper = PackageInfoHelper(requireContext())
        val pkgInfo = packageInfoHelper.getAppPackageInfo()

        val items = buildDisplayItems(repo, pkgInfo)

        val container = view.findViewById<LinearLayout>(R.id.detailList)
        items.forEach { item ->
            if (item.isSection) {
                container.addView(buildSectionTitle(item.title))
            } else {
                container.addView(buildTitle(item.title))
                container.addView(buildContent(item.content))
            }
        }

        view.findViewById<Button>(R.id.backButton).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun buildDisplayItems(
        repo: DeviceInfoRepository,
        pkgInfo: AppPackageInfo
    ): List<DisplayItem> {
        val groups = listOf(
            "Identity" to repo.getIdentity(),
            "Profile" to repo.getProfile(),
            "Fingerprint" to repo.getFingerprint(),
            "Risk" to repo.getRisk(),
            "App" to mapOf(
                "appName" to pkgInfo.appName,
                "packageName" to pkgInfo.packageName,
                "versionName" to (pkgInfo.versionName ?: "unknown"),
                "versionCode" to pkgInfo.versionCode.toString()
            )
        )
        return buildList {
            groups.forEach { (groupName, values) ->
                val visibleValues = values.filterValues { it.isNotEmpty() }
                if (visibleValues.isNotEmpty()) {
                    add(DisplayItem(groupName, "", isSection = true))
                    visibleValues.forEach { (key, value) ->
                        add(DisplayItem(displayLabel(key), value))
                    }
                }
            }
        }
    }

    private fun displayLabel(key: String): String {
        return when (key) {
            "brand" -> "Brand"
            "model" -> "Model"
            "manufacturer" -> "Manufacturer"
            "device" -> "Device"
            "product" -> "Product"
            "versionRelease" -> "Android Version"
            "versionSdkInt" -> "SDK Int"
            "board" -> "Board"
            "hardware" -> "Hardware"
            "display" -> "Display"
            "fingerprint" -> "Fingerprint"
            "id" -> "Build ID"
            "serial" -> "Serial"
            "androidId" -> "Android ID"
            "widevineDeviceId" -> "Widevine Device ID"
            "line1Number" -> "Line1Number"
            "simOperator" -> "SIM Operator"
            "networkOperator" -> "Network Operator"
            "simState" -> "SIM State"
            "simOperatorName" -> "SIM Operator Name"
            "oaid" -> "OAID"
            "vaid" -> "VAID"
            "aaid" -> "AAID"
            "gaid" -> "GAID"
            "imei" -> "IMEI"
            "imsi" -> "IMSI"
            "iccid" -> "ICCID"
            "device_memory" -> "Device Memory"
            "device_advertised_memory" -> "Device Advertised Memory"
            "appName" -> "App Name"
            "packageName" -> "Package Name"
            "versionName" -> "Version Name"
            "versionCode" -> "Version Code"
            "ro_build_version_harmony_type" -> "ro.build.version.harmony_type"
            "hw_sc_build_platform_version" -> "hw_sc.build.platform.version"
            else -> key.replace('_', '.')
        }
    }

    private fun buildTitle(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        }
    }

    private fun buildSectionTitle(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(20)
                bottomMargin = dp(4)
            }
        }
    }

    private fun buildContent(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun dp(value: Int): Int {
        val density = resources.displayMetrics.density
        return (value * density + 0.5f).toInt()
    }
}
