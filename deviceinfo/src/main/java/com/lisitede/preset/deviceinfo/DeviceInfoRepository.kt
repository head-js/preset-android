package com.lisitede.preset.deviceinfo

import android.content.Context
import com.lisitede.preset.deviceinfo.collectors.BatteryInfoCollector
import com.lisitede.preset.deviceinfo.collectors.BuildInfoCollector
import com.lisitede.preset.deviceinfo.collectors.ConnectivityInfoCollector
import com.lisitede.preset.deviceinfo.collectors.CpuInfoCollector
import com.lisitede.preset.deviceinfo.collectors.DeveloperInfoCollector
import com.lisitede.preset.deviceinfo.collectors.DisplayInfoCollector
import com.lisitede.preset.deviceinfo.collectors.LocaleInfoCollector
import com.lisitede.preset.deviceinfo.collectors.MemoryInfoCollector
import com.lisitede.preset.deviceinfo.collectors.PowerInfoCollector
import com.lisitede.preset.deviceinfo.collectors.RomInfoCollector
import com.lisitede.preset.deviceinfo.collectors.StorageInfoCollector
import com.lisitede.preset.deviceinfo.collectors.TelephonyInfoCollector

class DeviceInfoRepository(context: Context) {
    private val buildInfoCollector = BuildInfoCollector()
    private val connectivityInfoCollector = ConnectivityInfoCollector(context)
    private val telephonyInfoCollector = TelephonyInfoCollector(context)
    private val romInfoCollector = RomInfoCollector()
    private val localeInfoCollector = LocaleInfoCollector(context)
    private val cpuInfoCollector = CpuInfoCollector()
    private val developerInfoCollector = DeveloperInfoCollector(context)
    private val displayInfoCollector = DisplayInfoCollector(context)
    private val memoryInfoCollector = MemoryInfoCollector(context)
    private val storageInfoCollector = StorageInfoCollector()
    private val batteryInfoCollector = BatteryInfoCollector(context)
    private val powerInfoCollector = PowerInfoCollector(context)

    fun getProfile(): Array<DeviceInfoEntry> {
        val build = buildInfoCollector.getBuildInfo()
        return arrayOf(
            build.brand,
            build.model,
            build.manufacturer,
            build.device,
            build.product
        )
    }

    fun getFingerprint(): Array<DeviceInfoEntry> {
        val build = buildInfoCollector.getBuildInfo()
        val connectivityInfo = connectivityInfoCollector.getConnectivityInfo()
        val localeInfo = localeInfoCollector.getLocaleInfo()
        val telephony = telephonyInfoCollector.getTelephonyInfo()
        val rom = romInfoCollector.getRomInfo()
        val cpuInfo = cpuInfoCollector.getCpuInfo()
        val displayInfo = displayInfoCollector.getDisplayInfo()
        val memoryInfo = memoryInfoCollector.getMemoryInfo()
        val storageInfo = storageInfoCollector.getStorageInfo()
        return arrayOf(
            build.version_release,
            build.version_incremental,
            build.version_security_patch,
            build.version_sdk_int,
            build.build_time,
            build.board,
            build.hardware,
            build.bootloader,
            build.supported_abis,
            cpuInfo.cores_count,
            build.display,
            build.fingerprint,
            build.id,
            build.tags,
            build.host,
            build.serial,
            rom.vb_meta_digest,
            localeInfo.default_input_method,
            localeInfo.language,
            localeInfo.country,
            localeInfo.timezone,
            displayInfo.display_physical_width,
            displayInfo.display_physical_height,
            displayInfo.display_refresh_rate,
            displayInfo.display_rotation,
            displayInfo.display_state,
            displayInfo.display_is_hdr,
            displayInfo.display_is_wide_color_gamut,
            memoryInfo.device_memory,
            memoryInfo.device_advertised_memory,
            storageInfo.device_storage,
            connectivityInfo.connectivity_type,
            connectivityInfo.http_proxy,
            telephony.imsi,
            telephony.iccid,
            telephony.line1_number,
            telephony.sim_operator,
            telephony.network_operator,
            telephony.sim_country_iso,
            telephony.network_country_iso,
            telephony.data_roaming,
            telephony.data_roaming_enabled,
            telephony.subscription_data_roaming,
            telephony.data_network_type,
            telephony.sim_state,
            telephony.sim_operator_name,
            rom.ro_miui_ui_version_name,
            rom.ro_miui_ui_version_code,
            rom.ro_mi_os_version_name,
            rom.ro_mi_os_version_code,
            rom.ro_mi_os_version_incremental,
            rom.ro_build_version_emui,
            rom.ro_build_version_magic,
            rom.ro_build_version_opporom,
            rom.ro_build_version_oplusrom,
            rom.ro_build_version_realmeui,
            rom.ro_vivo_os_name,
            rom.ro_vivo_os_version,
            rom.ro_vivo_rom,
            rom.ro_vivo_rom_version,
            rom.ro_build_version_oneui,
            rom.ro_flyme_published,
            rom.ro_meizu_setupwizard_flyme,
            rom.ro_smartisan_version,
            rom.ro_letv_release_version,
            rom.ro_lenovo_lvp_version,
            rom.ro_build_nubia_rom_name,
            rom.ro_build_nubia_rom_code,
            rom.ro_build_version_oxygen,
            rom.ro_build_version_harmony,
            rom.ro_build_version_harmony_type,
            rom.hw_sc_build_platform_version
        )
    }

    fun getRisk(): Array<DeviceInfoEntry> {
        val batteryInfo = batteryInfoCollector.getBatteryInfo()
        val developerInfo = developerInfoCollector.getDeveloperInfo()
        val localeInfo = localeInfoCollector.getLocaleInfo()
        val powerInfo = powerInfoCollector.getPowerInfo()
        return arrayOf(
            localeInfo.accessibility_enabled,
            developerInfo.adb_enabled,
            developerInfo.development_setting_enabled,
            batteryInfo.battery_level,
            batteryInfo.battery_status,
            batteryInfo.battery_health_status,
            batteryInfo.battery_temperature,
            powerInfo.battery_low_power_mode
        )
    }
}
