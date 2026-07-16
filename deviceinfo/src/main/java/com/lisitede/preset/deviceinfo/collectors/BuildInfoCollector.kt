package com.lisitede.preset.deviceinfo.collectors

import android.annotation.SuppressLint
import android.os.Build
import com.lisitede.preset.deviceinfo.DeviceInfoEntry

/**
 * 设备身份型号与 Android 构建系统版本类字段，全部读取自 [android.os.Build]。
 *
 * 未采集的 SoC 字段：
 * - `Build.SOC_MODEL`：SoC 型号，仅 API 31+。
 * - `Build.SOC_MANUFACTURER`：SoC 制造商，仅 API 31+。
 *
 * 两者无法覆盖本库支持的 API 21+，且不使用不稳定的 `/proc/cpuinfo` 作为低版本回退。
 *
 * - 声明：除 [serial] 需 READ_PHONE_STATE 外，其余字段无需权限声明。
 * - 弹窗：除 [serial] 在 26~28 需运行时权限弹窗外，其余字段不触发。
 * - PII：品牌/型号/序列号等可组合定位设备，需在隐私政策中声明。
 */
internal data class BuildInfo(
    /** 设备品牌。 */
    val brand: DeviceInfoEntry,
    /** 面向用户展示的设备型号。 */
    val model: DeviceInfoEntry,
    /** 设备制造商。 */
    val manufacturer: DeviceInfoEntry,
    /** 设备工业设计代号。 */
    val device: DeviceInfoEntry,
    /** 整体产品名称。 */
    val product: DeviceInfoEntry,
    /** Android 用户版本。 */
    val version_release: DeviceInfoEntry,
    /** 内部源码或构建增量标识。 */
    val version_incremental: DeviceInfoEntry,
    /** 原始安全补丁日期，API 23+；低版本为空。 */
    val version_security_patch: DeviceInfoEntry,
    /** Android SDK 整数版本转成的字符串。 */
    val version_sdk_int: DeviceInfoEntry,
    /** 系统镜像构建时间的 Unix 毫秒值。 */
    val build_time: DeviceInfoEntry,
    /** 底层主板名称。 */
    val board: DeviceInfoEntry,
    /** 硬件名称。 */
    val hardware: DeviceInfoEntry,
    /** 系统 bootloader 版本标识，不代表解锁状态，API 8+。 */
    val bootloader: DeviceInfoEntry,
    /** 支持的 ABI 列表，排序后逗号拼接，API 21+。 */
    val supported_abis: DeviceInfoEntry,
    /** 面向用户显示的构建标识。 */
    val display: DeviceInfoEntry,
    /** 系统构建指纹，不是单台设备唯一标识，API 1+。 */
    val fingerprint: DeviceInfoEntry,
    /** 系统构建 ID。 */
    val id: DeviceInfoEntry,
    /** 系统原始逗号分隔构建标签，不排序或解析，API 1+。 */
    val tags: DeviceInfoEntry,
    /** 系统构建服务器主机名，不是设备网络主机名，API 1+。 */
    val host: DeviceInfoEntry,
    /** Build.getSerial() / Build.SERIAL。 */
    val serial: DeviceInfoEntry
)

internal class BuildInfoCollector {
    fun getBuildInfo(): BuildInfo {
        return BuildInfo(
            brand = DeviceInfoEntry("brand", Build.BRAND, "Brand"),
            model = DeviceInfoEntry("model", Build.MODEL, "Model"),
            manufacturer = DeviceInfoEntry("manufacturer", Build.MANUFACTURER, "Manufacturer"),
            device = DeviceInfoEntry("device", Build.DEVICE, "Device"),
            product = DeviceInfoEntry("product", Build.PRODUCT, "Product"),
            version_release = DeviceInfoEntry(
                "version_release",
                Build.VERSION.RELEASE,
                "Android Version"
            ),
            version_incremental = DeviceInfoEntry(
                "version_incremental",
                Build.VERSION.INCREMENTAL,
                "Version Incremental"
            ),
            version_security_patch = DeviceInfoEntry(
                "version_security_patch",
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Build.VERSION.SECURITY_PATCH
                } else {
                    ""
                },
                "Security Patch"
            ),
            version_sdk_int = DeviceInfoEntry(
                "version_sdk_int",
                Build.VERSION.SDK_INT.toString(),
                "SDK Int"
            ),
            build_time = DeviceInfoEntry(
                "build_time",
                Build.TIME.toString(),
                "Build Time"
            ),
            board = DeviceInfoEntry("board", Build.BOARD, "Board"),
            hardware = DeviceInfoEntry("hardware", Build.HARDWARE, "Hardware"),
            bootloader = DeviceInfoEntry("bootloader", Build.BOOTLOADER, "Bootloader"),
            supported_abis = DeviceInfoEntry(
                "supported_abis",
                Build.SUPPORTED_ABIS.sorted().joinToString(","),
                "Supported ABIs"
            ),
            display = DeviceInfoEntry("display", Build.DISPLAY, "Display"),
            fingerprint = DeviceInfoEntry("fingerprint", Build.FINGERPRINT, "Fingerprint"),
            id = DeviceInfoEntry("id", Build.ID, "Build ID"),
            tags = DeviceInfoEntry("tags", Build.TAGS, "Tags"),
            host = DeviceInfoEntry("host", Build.HOST, "Host"),
            serial = DeviceInfoEntry("serial", readSerial(), "Serial")
        )
    }

    /**
     * Serial Number（设备序列号），通过 [Build.getSerial]（API 26+）或 [Build.SERIAL]（deprecated fallback）读取。
     *
     * - 生命周期：设备硬件级序列号，恢复出厂设置不变；部分定制 ROM 可能返回固定值或空。
     * - Manifest 声明：需要 READ_PHONE_STATE。
     * - 用户授权弹窗：① Android 26~28 需 READ_PHONE_STATE 弹窗（用户拒绝则无法读取）。
     * - ② Android 29+：系统静默拒绝，不弹窗，直接返回空字符串。
     * - 返回值：正常时返回序列号字符串；权限不足或系统拒绝时返回空字符串。
     * - PII：是。硬件级标识，可用于跨 session 关联设备。
     */
    @SuppressLint("MissingPermission")
    private fun readSerial(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Build.getSerial()
            } else {
                @Suppress("DEPRECATION")
                Build.SERIAL
            }
        } catch (_: Throwable) {
            null
        }.orEmpty()
    }
}
