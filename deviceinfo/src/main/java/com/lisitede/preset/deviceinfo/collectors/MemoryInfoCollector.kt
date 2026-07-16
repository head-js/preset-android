package com.lisitede.preset.deviceinfo.collectors

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.lisitede.preset.deviceinfo.DeviceInfoEntry

internal data class MemoryInfo(
    /** 内核可访问的总内存，单位为 byte，API 16+。 */
    val device_memory: DeviceInfoEntry,
    /** 面向消费者标称的系统内存，单位为 byte，API 34+；低版本为空。 */
    val device_advertised_memory: DeviceInfoEntry
)

/**
 * Android Framework 报告的设备内存原始只读结果，单位均为 byte。
 *
 * [MemoryInfo.device_memory] 直接读取 [ActivityManager.MemoryInfo.totalMem]，表示内核可访问的总内存。
 * [MemoryInfo.device_advertised_memory] 在 API 34+ 直接读取 [ActivityManager.MemoryInfo.advertisedMem]，表示面向
 * 消费者标称的系统内存。两者语义不同，不互相替代，也不换算为 KB、MB 或 GB。
 *
 * ActivityManager 不存在、读取异常或 API 不可用时由 Collector 转换为空字符串。
 *
 * - 声明：不需要权限。
 * - 弹窗：不触发。
 * - PII：否，但设备内存容量可作为设备指纹的一部分。
 *
 * 已评估但暂不采集：
 * - `availMem`：系统当前可用内存快照，不是设备总内存。
 * - `threshold`：系统进入低内存状态的可用内存阈值。
 * - `lowMemory`：系统当前是否处于低内存状态。
 * - `freeMem`：API 37+ 的当前未使用内存；当前 compileSdk 尚不支持，且不是总内存。
 * - `Runtime.maxMemory`：仅表示当前 App 的 Java Heap 上限，不是设备总内存。
 * - `StatFs`：表示文件系统存储空间，不属于 RAM。
 */
internal class MemoryInfoCollector(context: Context) {
    private val activityManager = context.applicationContext
        .getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

    fun getMemoryInfo(): MemoryInfo {
        val manager = activityManager ?: return emptyInfo()
        val frameworkInfo = ActivityManager.MemoryInfo()
        try {
            manager.getMemoryInfo(frameworkInfo)
        } catch (_: Throwable) {
            return emptyInfo()
        }

        return MemoryInfo(
            device_memory = DeviceInfoEntry("device_memory", frameworkInfo.totalMem.toString(), "Device Memory"),
            device_advertised_memory = DeviceInfoEntry(
                "device_advertised_memory",
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    frameworkInfo.advertisedMem.toString()
                } else "",
                "Device Advertised Memory"
            )
        )
    }

    private fun emptyInfo(): MemoryInfo {
        return MemoryInfo(
            DeviceInfoEntry("device_memory", "", "Device Memory"),
            DeviceInfoEntry("device_advertised_memory", "", "Device Advertised Memory")
        )
    }
}
