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
import com.lisitede.preset.preset.App
import com.lisitede.preset.preset.PackageInfoHelper
import com.lisitede.preset.preset.R
import com.therouter.router.Route

private data class DisplayItem(
    val title: String,
    val content: String
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
            container.addView(buildTitle(item.title))
            container.addView(buildContent(item.content))
        }

        view.findViewById<Button>(R.id.backButton).setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun buildDisplayItems(
        repo: com.lisitede.preset.preset.DeviceInfoRepository,
        pkgInfo: com.lisitede.preset.preset.AppPackageInfo
    ): List<DisplayItem> {
        val buildInfo = repo.getBuildInfo()
        val romInfo = repo.getRomInfo()
        val identifierInfo = repo.getIdentifierInfo()
        val telephonyInfo = repo.getTelephonyInfo()
        return buildList {
            addIfNotEmpty("Brand", buildInfo.brand)
            addIfNotEmpty("Model", buildInfo.model)
            addIfNotEmpty("Manufacturer", buildInfo.manufacturer)
            addIfNotEmpty("Device", buildInfo.device)
            addIfNotEmpty("Product", buildInfo.product)
            addIfNotEmpty("Android Version", buildInfo.versionRelease)
            addIfNotEmpty("SDK Int", buildInfo.versionSdkInt.toString())
            addIfNotEmpty("Board", buildInfo.board)
            addIfNotEmpty("Hardware", buildInfo.hardware)
            addIfNotEmpty("Display", buildInfo.display)
            addIfNotEmpty("Fingerprint", buildInfo.fingerprint)
            addIfNotEmpty("Build ID", buildInfo.id)
            addIfNotEmpty("Serial", buildInfo.serial)
            addIfNotEmpty("ro.miui.ui.version.name", romInfo.ro_miui_ui_version_name)
            addIfNotEmpty("ro.miui.ui.version.code", romInfo.ro_miui_ui_version_code)
            addIfNotEmpty("ro.mi.os.version.name", romInfo.ro_mi_os_version_name)
            addIfNotEmpty("ro.mi.os.version.code", romInfo.ro_mi_os_version_code)
            addIfNotEmpty("ro.mi.os.version.incremental", romInfo.ro_mi_os_version_incremental)
            addIfNotEmpty("ro.build.version.emui", romInfo.ro_build_version_emui)
            addIfNotEmpty("ro.build.version.magic", romInfo.ro_build_version_magic)
            addIfNotEmpty("ro.build.version.opporom", romInfo.ro_build_version_opporom)
            addIfNotEmpty("ro.build.version.oplusrom", romInfo.ro_build_version_oplusrom)
            addIfNotEmpty("ro.build.version.realmeui", romInfo.ro_build_version_realmeui)
            addIfNotEmpty("ro.vivo.os.name", romInfo.ro_vivo_os_name)
            addIfNotEmpty("ro.vivo.os.version", romInfo.ro_vivo_os_version)
            addIfNotEmpty("ro.vivo.rom", romInfo.ro_vivo_rom)
            addIfNotEmpty("ro.vivo.rom.version", romInfo.ro_vivo_rom_version)
            addIfNotEmpty("ro.build.version.oneui", romInfo.ro_build_version_oneui)
            addIfNotEmpty("ro.flyme.published", romInfo.ro_flyme_published)
            addIfNotEmpty("ro.meizu.setupwizard.flyme", romInfo.ro_meizu_setupwizard_flyme)
            addIfNotEmpty("ro.smartisan.version", romInfo.ro_smartisan_version)
            addIfNotEmpty("ro.letv.release.version", romInfo.ro_letv_release_version)
            addIfNotEmpty("ro.lenovo.lvp.version", romInfo.ro_lenovo_lvp_version)
            addIfNotEmpty("ro.build.nubia.rom.name", romInfo.ro_build_nubia_rom_name)
            addIfNotEmpty("ro.build.nubia.rom.code", romInfo.ro_build_nubia_rom_code)
            addIfNotEmpty("ro.build.version.oxygen", romInfo.ro_build_version_oxygen)
            addIfNotEmpty("ro.build.version.harmony", romInfo.ro_build_version_harmony)
            addIfNotEmpty("ro.build.version.harmony_type", romInfo.ro_build_version_harmony_type)
            addIfNotEmpty("hw_sc.build.platform.version", romInfo.hw_sc_build_platform_version)
            addIfNotEmpty("OAID", identifierInfo.oaid)
            addIfNotEmpty("VAID", identifierInfo.vaid)
            addIfNotEmpty("AAID", identifierInfo.aaid)
            addIfNotEmpty("Android ID", identifierInfo.androidId)
            addIfNotEmpty("GAID", identifierInfo.gaid)
            addIfNotEmpty("IMEI", telephonyInfo.imei)
            addIfNotEmpty("IMSI", telephonyInfo.imsi)
            addIfNotEmpty("ICCID", telephonyInfo.iccid)
            addIfNotEmpty("Line1Number", telephonyInfo.line1Number)
            addIfNotEmpty("SIM Operator", telephonyInfo.simOperator)
            addIfNotEmpty("Network Operator", telephonyInfo.networkOperator)
            addIfNotEmpty("SIM State", telephonyInfo.simState)
            addIfNotEmpty("SIM Operator Name", telephonyInfo.simOperatorName)
            addIfNotEmpty("Widevine Device ID", identifierInfo.widevineDeviceId)
            addIfNotEmpty("App Name", pkgInfo.appName)
            addIfNotEmpty("Package Name", pkgInfo.packageName)
            addIfNotEmpty("Version Name", pkgInfo.versionName ?: "unknown")
            addIfNotEmpty("Version Code", pkgInfo.versionCode.toString())
        }
    }

    private fun MutableList<DisplayItem>.addIfNotEmpty(title: String, content: String) {
        if (content.isNotEmpty()) add(DisplayItem(title, content))
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
