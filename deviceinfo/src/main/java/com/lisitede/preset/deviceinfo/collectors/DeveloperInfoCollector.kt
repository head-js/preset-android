package com.lisitede.preset.deviceinfo.collectors

import android.content.Context
import android.provider.Settings
import com.lisitede.preset.deviceinfo.DeviceInfoEntry

internal data class DeveloperInfo(
    /** 系统 ADB 设置是否启用，API 17+。 */
    val adb_enabled: DeviceInfoEntry,
    /** 开发者选项总开关是否启用，API 17+。 */
    val development_setting_enabled: DeviceInfoEntry
)

/**
 * 当前设备开发者相关设置的原始只读采集器。
 *
 * [DeveloperInfo.adb_enabled] 直接读取 [Settings.Global.ADB_ENABLED]。原始值 `0` 映射为
 * false，`1` 映射为 true；设置不存在、返回其他值或读取异常时由 Collector 转换为空字符串。
 * 不读取非公开的无线调试设置。
 * [DeveloperInfo.development_setting_enabled] 以相同规则读取
 * [Settings.Global.DEVELOPMENT_SETTINGS_ENABLED]。两项分别读取，不互相推断。
 *
 * ADB 值只表示系统设置允许 ADB，不代表当前存在 USB 连接、主机已授权或进程正在被调试。
 *
 * - 声明：不需要权限；修改 Global Setting 需要系统级权限，但读取不需要。
 * - 弹窗：不触发。
 * - PII：否。
 * - 稳定性：用户修改开发者选项、设备管理策略或定制 ROM 行为变化后可能改变，不是固定
 *   设备属性。
 * - 风险语义：开发者正常开启 ADB 也会返回 true，不能单独作为风险结论。
 */
internal class DeveloperInfoCollector(context: Context) {
    private val contentResolver = context.applicationContext.contentResolver

    fun getDeveloperInfo(): DeveloperInfo {
        return DeveloperInfo(
            adb_enabled = DeviceInfoEntry(
                "adb_enabled",
                readBooleanGlobalSetting(Settings.Global.ADB_ENABLED)?.toString().orEmpty(),
                "ADB Enabled"
            ),
            development_setting_enabled = DeviceInfoEntry(
                "development_setting_enabled",
                readBooleanGlobalSetting(Settings.Global.DEVELOPMENT_SETTINGS_ENABLED)
                    ?.toString().orEmpty(),
                "Development Setting Enabled"
            )
        )
    }

    private fun readBooleanGlobalSetting(name: String): Boolean? {
        return try {
            when (Settings.Global.getInt(contentResolver, name)) {
                0 -> false
                1 -> true
                else -> null
            }
        } catch (_: Throwable) {
            null
        }
    }
}
