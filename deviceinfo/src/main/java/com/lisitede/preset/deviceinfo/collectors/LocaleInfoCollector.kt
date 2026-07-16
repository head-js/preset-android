package com.lisitede.preset.deviceinfo.collectors

import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.lisitede.preset.deviceinfo.DeviceInfoEntry
import java.util.Locale
import java.util.TimeZone

internal data class LocaleInfo(
    /** 当前进程默认语言代码原始值，API 1+。 */
    val language: DeviceInfoEntry,
    /** 当前进程默认 Locale 地区代码原始值，API 1+。 */
    val country: DeviceInfoEntry,
    /** 当前系统时区 ID 原始值，API 1+。 */
    val timezone: DeviceInfoEntry,
    /** 当前用户默认输入法服务组件原始值，API 3+。 */
    val default_input_method: DeviceInfoEntry,
    /** 当前用户是否启用无障碍功能，API 4+。 */
    val accessibility_enabled: DeviceInfoEntry
)

/**
 * 当前系统区域环境、默认输入法和无障碍状态的原始只读结果。
 *
 * [LocaleInfo.language] 直接读取 [Locale.getDefault] 的 `language`，例如 `zh` 或 `en`。
 * 只返回语言代码，不补充地区或文字体系；支持 App 单独语言的环境中，可能反映当前 App/进程
 * 生效的默认语言。[LocaleInfo.country] 直接读取同一默认 Locale 的 `country`，例如 `CN` 或
 * `US`。保留原始地区代码，不转换大小写，也不代表 SIM 归属国家、当前网络国家或用户实际
 * 地理位置。[LocaleInfo.timezone] 直接读取 [TimeZone.getDefault] 的 `id`，例如
 * `Asia/Shanghai`。不转换为 UTC offset、不解析地区。
 *
 * [LocaleInfo.default_input_method] 直接读取 [Settings.Secure.DEFAULT_INPUT_METHOD]，返回输入法
 * 服务的 flattened component 字符串，例如 `com.example.ime/.ImeService`。不提取包名、不查询
 * 显示名称，也不解析当前输入法 subtype。设置不存在、ROM 限制访问或读取异常时返回空字符串。
 *
 * [LocaleInfo.accessibility_enabled] 直接读取 [AccessibilityManager.isEnabled]。该值只表示系统
 * 当前启用无障碍能力，不标识具体服务，也不能用于判断服务是否恶意。服务不存在或读取异常时
 * 由 Collector 转换为空字符串。
 *
 * 所有字段均不缓存，确保用户或系统修改相应设置后再次调用可以读取最新值。
 *
 * - 声明：不需要权限；`WRITE_SECURE_SETTINGS` 仅与修改 Secure Setting 有关。
 * - 弹窗：不触发。
 * - PII：否，但语言、地区、时区、默认输入法和无障碍状态会暴露部分用户偏好或使用环境。
 * - 稳定性：会随系统/App 语言、地区、时区、输入法和无障碍设置变化，不是固定设备属性。
 * - 风险语义：TalkBack、开关控制等正常辅助功能也会返回 true，不能单独作为风险结论。
 *
 * 已评估但暂不采集：
 * - `rawOffset`：基础 UTC 偏移不能唯一表示时区，也不能表达夏令时规则。
 * - `displayName`：结果受语言和地区配置影响，不是稳定原始标识。
 */
internal class LocaleInfoCollector(context: Context) {
    private val appContext = context.applicationContext
    private val contentResolver = appContext.contentResolver
    private val accessibilityManager = appContext
        .getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager

    fun getLocaleInfo(): LocaleInfo {
        val locale = try {
            Locale.getDefault()
        } catch (_: Throwable) {
            null
        }
        val language = locale?.language.orEmpty()
        val country = locale?.country.orEmpty()
        val timezone = try {
            TimeZone.getDefault().id
        } catch (_: Throwable) {
            ""
        }

        return LocaleInfo(
            language = DeviceInfoEntry("language", language, "Language"),
            country = DeviceInfoEntry("country", country, "Country"),
            timezone = DeviceInfoEntry("timezone", timezone, "Timezone"),
            default_input_method = DeviceInfoEntry(
                "default_input_method",
                readDefaultInputMethod(),
                "Default Input Method"
            ),
            accessibility_enabled = DeviceInfoEntry(
                "accessibility_enabled",
                readAccessibilityEnabled()?.toString().orEmpty(),
                "Accessibility Enabled"
            )
        )
    }

    private fun readDefaultInputMethod(): String {
        return try {
            Settings.Secure.getString(
                contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD
            )
        } catch (_: Throwable) {
            null
        }.orEmpty()
    }

    private fun readAccessibilityEnabled(): Boolean? {
        return try {
            accessibilityManager?.isEnabled
        } catch (_: Throwable) {
            null
        }
    }
}
