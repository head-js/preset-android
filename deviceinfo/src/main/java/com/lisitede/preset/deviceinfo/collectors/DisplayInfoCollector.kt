package com.lisitede.preset.deviceinfo.collectors

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import com.lisitede.preset.deviceinfo.DeviceInfoEntry

internal data class DisplayInfo(
    /** 当前显示模式的物理宽度，API 23+。 */
    val display_physical_width: DeviceInfoEntry,
    /** 当前显示模式的物理高度，API 23+。 */
    val display_physical_height: DeviceInfoEntry,
    /** 当前显示模式的刷新率原始值，API 23+。 */
    val display_refresh_rate: DeviceInfoEntry,
    /** 当前显示器旋转常量原始值。 */
    val display_rotation: DeviceInfoEntry,
    /** 当前显示器状态常量原始值，API 20+。 */
    val display_state: DeviceInfoEntry,
    /** 当前显示器是否支持 HDR，API 26+。 */
    val display_is_hdr: DeviceInfoEntry,
    /** 当前显示器是否支持广色域，API 26+。 */
    val display_is_wide_color_gamut: DeviceInfoEntry
)

/**
 * 默认逻辑显示器及其当前模式的原始只读结果。
 *
 * 所有值都直接来自 [Display] 或 [Display.Mode] 的公开 getter；不拼接分辨率、不转换常量、
 * 不拆解 bitmask、不遍历或选择 supported modes。API 不可用、显示器不存在或读取异常时由
 * Collector 转换为空字符串。
 *
 * - 声明：不需要权限。
 * - 弹窗：不触发。
 * - PII：否，但显示能力组合可作为设备指纹的一部分。
 *
 * 已评估但暂不采集：
 * - `screen_resolution`：Android 没有对应的单值 getter，拼接宽高属于衍生。
 * - `modeId`：仅是当前 Display Mode 的 ID，不是稳定设备标识。
 * - `alternativeRefreshRates`、`supportedHdrTypes`、`supportedModes`：返回数组；当前
 *   `DeviceInfoEntry.value` 无法在不 join、不排序、不选择的前提下无损公开。
 * - `displayId`：默认显示器通常固定为 0，设备区分价值低。
 * - `name`：可能被用户重命名。
 * - `flags`：返回 bitmask；拆分能力位会引入自行计算。
 * - `isMinimalPostProcessingSupported`：主要服务外接显示器低延迟场景，当前不采集。
 * - `deviceProductInfo`：API 31+ 且常用于外接显示设备，内置屏可能返回 null；其 PnP ID、
 *   Product ID、名称、型号年份和连接类型暂不采集。
 * - `hdrCapabilities` 的 HDR 类型和亮度能力：用途较窄，且 HDR 类型为数组，当前暂不采集。
 * - `cutout`、`roundedCorner`、`shape`：返回复合对象，公开为 String 需要额外序列化。
 * - `DisplayMetrics.widthPixels`：受旋转、分屏、窗口尺寸和配置影响，数值不稳定，暂不读取。
 * - `DisplayMetrics.heightPixels`：受旋转、分屏、窗口尺寸和配置影响，数值不稳定，暂不读取。
 * - `DisplayMetrics.density`：属于逻辑显示密度，可能随显示大小、兼容模式或显示配置变化，数值不稳定，暂不读取。
 * - `getMetrics`、`getRealMetrics`、`getRealSize`：已废弃，且语义可能受窗口、旋转、分区或
 *   兼容模式影响。
 */
internal class DisplayInfoCollector(context: Context) {
    private val displayManager = context.applicationContext
        .getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager

    fun getDisplayInfo(): DisplayInfo {
        val display = readValue {
            displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
        }
        val physicalWidth: Int?
        val physicalHeight: Int?
        val refreshRate: Float?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val mode = readValue { display?.mode }
            physicalWidth = readValue { mode?.physicalWidth }
            physicalHeight = readValue { mode?.physicalHeight }
            refreshRate = readValue { mode?.refreshRate }
        } else {
            physicalWidth = null
            physicalHeight = null
            refreshRate = null
        }

        return DisplayInfo(
            display_physical_width = entry("display_physical_width", physicalWidth, "Display Physical Width"),
            display_physical_height = entry("display_physical_height", physicalHeight, "Display Physical Height"),
            display_refresh_rate = entry("display_refresh_rate", refreshRate, "Display Refresh Rate"),
            display_rotation = entry("display_rotation", readValue { display?.rotation }, "Display Rotation"),
            display_state = entry("display_state", readValue { display?.state }, "Display State"),
            display_is_hdr = entry(
                "display_is_hdr",
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) readValue { display?.isHdr } else null,
                "Display Is HDR"
            ),
            display_is_wide_color_gamut = entry(
                "display_is_wide_color_gamut",
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    readValue { display?.isWideColorGamut }
                } else null,
                "Display Is Wide Color Gamut"
            )
        )
    }

    private fun entry(key: String, value: Any?, label: String): DeviceInfoEntry {
        return DeviceInfoEntry(key, value?.toString().orEmpty(), label)
    }

    private inline fun <T> readValue(block: () -> T?): T? {
        return try {
            block()
        } catch (_: Throwable) {
            null
        }
    }
}
