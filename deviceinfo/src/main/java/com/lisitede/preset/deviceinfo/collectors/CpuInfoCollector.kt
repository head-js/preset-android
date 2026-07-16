package com.lisitede.preset.deviceinfo.collectors

import android.system.Os
import android.system.OsConstants
import com.lisitede.preset.deviceinfo.DeviceInfoEntry

internal data class CpuInfo(
    /** 系统配置的逻辑处理器数量，API 21+。 */
    val cores_count: DeviceInfoEntry
)

/**
 * Android/Linux 运行环境报告的 CPU 配置原始只读结果。
 *
 * [CpuInfo.cores_count] 直接读取 `Os.sysconf(OsConstants._SC_NPROCESSORS_CONF)`，表示系统配置的
 * 逻辑处理器数量。普通 ARM 设备上通常等于 CPU 核心数，但在支持 SMT 的设备或模拟器上
 * 不一定等于物理核心数。不读取当前在线处理器数，也不使用 `/proc` 或 `/sys` 文件回退。
 *
 * 读取异常或返回值非正数时由 Collector 转换为空字符串。
 *
 * - 声明：不需要权限。
 * - 弹窗：不触发。
 * - PII：否，但逻辑处理器数量可作为设备指纹的一部分。
 * - 稳定性：安装、卸载或后台运行 App 不会改变；内核或虚拟机 CPU 配置变化后可能变化。
 *
 * 已评估但暂不采集：
 * - `_SC_NPROCESSORS_ONLN`：当前在线处理器数，可能受 CPU hotplug 影响。
 * - `Runtime.availableProcessors()`：当前 JVM 可用处理器数，不代表系统配置总数。
 */
internal class CpuInfoCollector {
    fun getCpuInfo(): CpuInfo {
        val coresCount = try {
            Os.sysconf(OsConstants._SC_NPROCESSORS_CONF).takeIf { it > 0L }
        } catch (_: Throwable) {
            null
        }

        return CpuInfo(
            cores_count = DeviceInfoEntry(
                "cores_count",
                coresCount?.toString().orEmpty(),
                "CPU Cores Count"
            )
        )
    }
}
