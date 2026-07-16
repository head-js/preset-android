package com.lisitede.preset.deviceinfo.collectors

import android.content.Context
import android.os.PowerManager
import com.lisitede.preset.deviceinfo.DeviceInfoEntry

internal data class PowerInfo(
    /** 当前是否启用省电模式。 */
    val battery_low_power_mode: DeviceInfoEntry
)

/**
 * Android 电源管理策略的原始只读结果。
 *
 * [PowerInfo.battery_low_power_mode] 直接读取 [PowerManager.isPowerSaveMode]，不转换为自定义状态或等级。
 * PowerManager 不存在或读取异常时由 Collector 转换为空字符串。
 *
 * - 声明：不需要权限。
 * - 弹窗：不触发。
 * - PII：否，但动态电源策略可作为风险判断材料。
 *
 * 已评估但暂不采集：
 * - `isDeviceIdleMode`、`isInteractive`、`locationPowerSaveMode`、`currentThermalStatus`：
 *   均有直接 getter，但当前没有对应业务字段。
 * - `isIgnoringBatteryOptimizations`：结果与传入的应用包名绑定，不是纯设备属性。
 * - `getBatteryDischargePrediction`、`getThermalHeadroom`：属于系统预测值，不是原始属性，禁止采集。
 */
internal class PowerInfoCollector(context: Context) {
    private val powerManager = context.applicationContext
        .getSystemService(Context.POWER_SERVICE) as? PowerManager

    fun getPowerInfo(): PowerInfo {
        return PowerInfo(
            battery_low_power_mode = DeviceInfoEntry(
                "battery_low_power_mode",
                try {
                    powerManager?.isPowerSaveMode?.toString().orEmpty()
                } catch (_: Throwable) {
                    ""
                },
                "Battery Low Power Mode"
            )
        )
    }
}
