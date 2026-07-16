package com.lisitede.preset.deviceinfo.collectors

import android.os.Environment
import android.os.StatFs
import com.lisitede.preset.deviceinfo.DeviceInfoEntry

internal data class StorageInfo(
    /** 内部数据分区总容量，单位为 byte，API 18+。 */
    val device_storage: DeviceInfoEntry
)

/**
 * Android Framework 报告的内部数据分区原始只读结果，单位为 byte。
 *
 * [StorageInfo.device_storage] 直接读取 [StatFs.getTotalBytes]，统计 [Environment.getDataDirectory] 所在
 * 文件系统的总容量。不分桶、不换算为 KB、MB 或 GB，也不代表整块物理闪存或所有分区之和。
 * 读取异常时由 Collector 转换为空字符串。
 *
 * - 声明：不需要权限。
 * - 弹窗：不触发。
 * - PII：否，但内部存储容量可作为设备指纹的一部分。
 * - 稳定性：正常安装、卸载或运行 App 不会改变总容量；OTA 或分区布局调整后可能变化。
 *
 * 已评估但暂不采集：
 * - `freeBytes`、`availableBytes`：随文件写入、删除和系统空间回收动态变化，不是稳定设备属性。
 * - 外部存储和可采纳存储：可能插拔或迁移，不能作为唯一稳定的设备内部存储容量。
 */
internal class StorageInfoCollector {
    fun getStorageInfo(): StorageInfo {
        val totalStorage = try {
            StatFs(Environment.getDataDirectory().absolutePath).totalBytes
        } catch (_: Throwable) {
            null
        }

        return StorageInfo(
            device_storage = DeviceInfoEntry(
                "device_storage",
                totalStorage?.toString().orEmpty(),
                "Device Storage"
            )
        )
    }
}
