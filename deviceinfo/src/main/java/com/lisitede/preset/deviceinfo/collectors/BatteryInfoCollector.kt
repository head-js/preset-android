package com.lisitede.preset.deviceinfo.collectors

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.lisitede.preset.deviceinfo.DeviceInfoEntry

internal data class BatteryInfo(
    /** 当前电池电量百分比，API 21+。 */
    val battery_level: DeviceInfoEntry,
    /** 当前电池状态固定枚举名称，API 21+。 */
    val battery_status: DeviceInfoEntry,
    /** 当前电池健康状态固定枚举名称，API 21+。 */
    val battery_health_status: DeviceInfoEntry,
    /** 电池温度原始值，单位为 0.1°C，API 21+。 */
    val battery_temperature: DeviceInfoEntry
)

/**
 * 电池子系统的原始只读结果。
 *
 * [BatteryInfo.battery_level] 直接读取 [BatteryManager.BATTERY_PROPERTY_CAPACITY]，系统返回无小数的整数百分比。
 * 不使用 ACTION_BATTERY_CHANGED 的 level / scale 自行计算百分比。属性不受设备支持或读取异常时
 * 返回空字符串。[BatteryInfo.battery_status] 读取 [Intent.ACTION_BATTERY_CHANGED] sticky Intent 的
 * [BatteryManager.EXTRA_STATUS]，将 Android 固定枚举转换为 `UNKNOWN`、`CHARGING`、
 * `DISCHARGING`、`NOT_CHARGING` 或 `FULL`。[BatteryInfo.battery_health_status] 从同一 sticky Intent 的
 * [BatteryManager.EXTRA_HEALTH] 读取并转换为固定枚举名称；它表示过热、过压、失效等
 * 粗粒度状态，不表示电池容量衰减百分比。[BatteryInfo.battery_temperature] 读取同一 sticky Intent 的
 * [BatteryManager.EXTRA_TEMPERATURE] 原始整数，单位为 0.1°C，不换算或过滤范围。单项读取
 * 失败时对应字段返回空字符串。
 *
 * - 声明：不需要权限。
 * - 弹窗：不触发。
 * - PII：否，但动态电池状态可作为风险判断材料。
 *
 * 已评估但暂不采集：
 * - `BATTERY_PROPERTY_CHARGE_COUNTER`、`CURRENT_NOW`、`CURRENT_AVERAGE`、`ENERGY_COUNTER`：
 *   均为直接属性，但当前没有对应业务字段。
 * - `BATTERY_PROPERTY_STATUS`：仅 API 26+，当前统一使用 API 21+ 可用的 sticky Intent 状态。
 * - [BatteryManager.isCharging]：只能判断是否正在充电，无法保留完整状态枚举。
 * - ACTION_BATTERY_CHANGED 的 plugged、present、technology、voltage 和
 *   API 34+ cycle count：均可直接读取 sticky Intent extra，当前暂不采集。
 * - `computeChargeTimeRemaining`：返回系统估算值，不属于原始属性，禁止采集。
 */
internal class BatteryInfoCollector(context: Context) {
    private val appContext = context.applicationContext
    private val batteryManager = appContext
        .getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

    fun getBatteryInfo(): BatteryInfo {
        val level = try {
            batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (_: Throwable) {
            null
        }
        val batteryIntent = readBatteryIntent()

        return BatteryInfo(
            battery_level = DeviceInfoEntry(
                "battery_level",
                level?.takeUnless { it == Int.MIN_VALUE }?.toString().orEmpty(),
                "Battery Level"
            ),
            battery_status = DeviceInfoEntry(
                "battery_status",
                readStatus(batteryIntent),
                "Battery Status"
            ),
            battery_health_status = DeviceInfoEntry(
                "battery_health_status",
                readHealthStatus(batteryIntent),
                "Battery Health Status"
            ),
            battery_temperature = DeviceInfoEntry(
                "battery_temperature",
                readTemperature(batteryIntent)?.toString().orEmpty(),
                "Battery Temperature"
            )
        )
    }

    private fun readBatteryIntent(): Intent? {
        return try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.registerReceiver(null, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("DEPRECATION")
                appContext.registerReceiver(null, filter)
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun readStatus(batteryIntent: Intent?): String {
        val status = batteryIntent
            ?.getIntExtra(BatteryManager.EXTRA_STATUS, Int.MIN_VALUE)
            ?: return ""
        if (status == Int.MIN_VALUE) return ""

        return when (status) {
            BatteryManager.BATTERY_STATUS_UNKNOWN -> "UNKNOWN"
            BatteryManager.BATTERY_STATUS_CHARGING -> "CHARGING"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "DISCHARGING"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "NOT_CHARGING"
            BatteryManager.BATTERY_STATUS_FULL -> "FULL"
            else -> "UNKNOWN"
        }
    }

    private fun readHealthStatus(batteryIntent: Intent?): String {
        val healthStatus = batteryIntent
            ?.getIntExtra(BatteryManager.EXTRA_HEALTH, Int.MIN_VALUE)
            ?: return ""
        if (healthStatus == Int.MIN_VALUE) return ""

        return when (healthStatus) {
            BatteryManager.BATTERY_HEALTH_UNKNOWN -> "UNKNOWN"
            BatteryManager.BATTERY_HEALTH_GOOD -> "GOOD"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "OVERHEAT"
            BatteryManager.BATTERY_HEALTH_DEAD -> "DEAD"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "OVER_VOLTAGE"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "UNSPECIFIED_FAILURE"
            BatteryManager.BATTERY_HEALTH_COLD -> "COLD"
            else -> "UNKNOWN"
        }
    }

    private fun readTemperature(batteryIntent: Intent?): Int? {
        return batteryIntent
            ?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            ?.takeUnless { it == Int.MIN_VALUE }
    }
}
