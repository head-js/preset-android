package com.lisitede.preset.deviceinfo.collectors

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import com.lisitede.preset.deviceinfo.DeviceInfoEntry

/**
 * 运营商/归属地族。各字段的声明/弹窗/PII 详见对应 read* 方法的 KDoc。
 *
 * IMEI 已移动至 IdentifierInfo，由 IdentityInfoCollector.readImei() 采集；
 * TelephonyInfo 不再包含 IMEI。
 *
 * Android 没有统一的 `carrier` 原始字段。常见 `carrier` 语义最接近 [sim_operator_name]，
 * 表示 SIM 卡归属运营商的显示名称；[network_operator] 表示当前注册网络的 MCC-MNC，
 * 会随网络切换或漫游变化，两者不能互相替代。
 * [sim_country_iso] 表示 SIM 提供商国家代码，[network_country_iso] 表示当前注册网络国家代码；
 * 两者均保留 Android 返回的原始值，也不能用于确定用户实际地理位置。
 * [data_roaming]、[data_roaming_enabled] 和 [subscription_data_roaming] 分别保留系统全局设置、
 * TelephonyManager 布尔结果和 SubscriptionInfo 固定枚举结果，不合并、不互相回退或推断。
 *
 * 已评估但暂不采集：
 * - `networkOperatorName`：当前注册网络运营商名称，会随网络切换、漫游和注册状态变化，
 *   不是稳定设备属性。
 */
internal data class TelephonyInfo(
    /** SIM 卡用户身份标识。 */
    val imsi: DeviceInfoEntry,
    /** SIM 卡序列号。 */
    val iccid: DeviceInfoEntry,
    /** SIM 卡本机号码。 */
    val line1_number: DeviceInfoEntry,
    /** SIM 卡归属运营商 MCC-MNC。 */
    val sim_operator: DeviceInfoEntry,
    /** 当前注册网络运营商 MCC-MNC。 */
    val network_operator: DeviceInfoEntry,
    /** SIM 提供商国家 ISO 代码原始值。 */
    val sim_country_iso: DeviceInfoEntry,
    /** 当前注册网络国家 ISO 代码原始值。 */
    val network_country_iso: DeviceInfoEntry,
    /** 系统全局数据漫游开关，API 17+。 */
    val data_roaming: DeviceInfoEntry,
    /** 默认数据订阅数据漫游开关，API 29+。 */
    val data_roaming_enabled: DeviceInfoEntry,
    /** 默认数据订阅数据漫游固定枚举名称，API 22+。 */
    val subscription_data_roaming: DeviceInfoEntry,
    /** 当前数据订阅蜂窝制式的固定枚举名称。 */
    val data_network_type: DeviceInfoEntry,
    /** SIM 卡状态。 */
    val sim_state: DeviceInfoEntry,
    /** SIM 卡归属运营商显示名称，最接近常见 carrier 语义。 */
    val sim_operator_name: DeviceInfoEntry
)

internal class TelephonyInfoCollector(context: Context) {
    private val appContext = context.applicationContext

    fun getTelephonyInfo(): TelephonyInfo {
        val tm = appContext.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        return TelephonyInfo(
            imsi = DeviceInfoEntry("imsi", readImsi(tm), "IMSI"),
            iccid = DeviceInfoEntry("iccid", readIccid(tm), "ICCID"),
            line1_number = DeviceInfoEntry("line1_number", readLine1Number(tm), "Line1 Number"),
            sim_operator = DeviceInfoEntry("sim_operator", readSimOperator(tm), "SIM Operator"),
            network_operator = DeviceInfoEntry("network_operator", readNetworkOperator(tm), "Network Operator"),
            sim_country_iso = DeviceInfoEntry("sim_country_iso", readSimCountryIso(tm), "SIM Country ISO"),
            network_country_iso = DeviceInfoEntry(
                "network_country_iso",
                readNetworkCountryIso(tm),
                "Network Country ISO"
            ),
            data_roaming = DeviceInfoEntry(
                "data_roaming",
                readGlobalDataRoaming()?.toString().orEmpty(),
                "Data Roaming"
            ),
            data_roaming_enabled = DeviceInfoEntry(
                "data_roaming_enabled",
                readDataRoamingEnabled(tm)?.toString().orEmpty(),
                "Data Roaming Enabled"
            ),
            subscription_data_roaming = DeviceInfoEntry(
                "subscription_data_roaming",
                readSubscriptionDataRoaming(),
                "Subscription Data Roaming"
            ),
            data_network_type = DeviceInfoEntry(
                "data_network_type",
                readDataNetworkType(tm),
                "Data Network Type"
            ),
            sim_state = DeviceInfoEntry("sim_state", readSimState(tm), "SIM State"),
            sim_operator_name = DeviceInfoEntry(
                "sim_operator_name",
                readSimOperatorName(tm),
                "SIM Operator Name"
            )
        )
    }

    /**
     * IMSI（International Mobile Subscriber Identity），通过 [TelephonyManager.getSubscriberId] 读取。
     *
     * - 生命周期：SIM 卡级标识，换卡即变；双卡设备有两个独立 IMSI。
     * - Manifest 声明：需要 READ_PHONE_STATE。
     * - 用户授权弹窗：① Android ≤10 需 READ_PHONE_STATE 弹窗（用户拒绝则无法读取）。
     * - ② Android 11+：系统静默拒绝（需 `READ_PRIVILEGED_PHONE_STATE`，仅系统应用持有），不弹窗，直接返回空字符串。
     * - 返回值：正常时返回 IMSI 字符串；权限不足或系统拒绝时返回空字符串。
     * - PII：是。硬件级标识，可用于跨 session 关联用户。
     */
    @SuppressLint("MissingPermission")
    private fun readImsi(tm: TelephonyManager?): String {
        return try {
            @Suppress("DEPRECATION")
            tm?.subscriberId
        } catch (_: Throwable) {
            null
        }.orEmpty()
    }

    /**
     * ICCID（Integrated Circuit Card Identifier），即 SIM 卡序列号，通过 [TelephonyManager.getSimSerialNumber] 读取。
     *
     * - 生命周期：SIM 卡级唯一序列号，换卡即变。
     * - Manifest 声明：需要 READ_PHONE_STATE。
     * - 用户授权弹窗：① 全版本需 READ_PHONE_STATE 弹窗（用户拒绝则无法读取）。
     * - 返回值：正常时返回 ICCID 字符串；权限不足时返回空字符串。
     * - PII：是。硬件级标识，可用于跨 session 关联用户。
     */
    @SuppressLint("MissingPermission")
    private fun readIccid(tm: TelephonyManager?): String {
        return try {
            tm?.simSerialNumber
        } catch (_: Throwable) {
            null
        }.orEmpty()
    }

    /**
     * SIM Operator（SIM 卡归属运营商 MCC-MNC），通过 [TelephonyManager.getSimOperator] 读取。
     *
     * - 生命周期：换卡即变。
     * - Manifest 声明：不需要。
     * - 用户授权弹窗：不触发。
     * - PII：否。仅标识运营商，无法定位个人。
     */
    private fun readSimOperator(tm: TelephonyManager?): String {
        return tm?.simOperator.orEmpty()
    }

    /**
     * Network Operator（当前注册网络运营商 MCC-MNC），通过 [TelephonyManager.getNetworkOperator] 读取。
     *
     * - 生命周期：随网络切换变化。
     * - Manifest 声明：不需要。
     * - 用户授权弹窗：不触发。
     * - PII：否。仅标识运营商，无法定位个人。
     */
    private fun readNetworkOperator(tm: TelephonyManager?): String {
        return tm?.networkOperator.orEmpty()
    }

    /**
     * SIM Country ISO，通过 [TelephonyManager.getSimCountryIso] 读取 SIM 提供商 MCC 对应的
     * ISO 3166-1 alpha-2 国家代码原始值；无 SIM 或系统无法确定时返回空字符串。
     *
     * - 生命周期：通常随 SIM 卡变化，不表示当前网络或实际地理位置。
     * - Manifest 声明：不需要。
     * - 用户授权弹窗：不触发。
     * - PII：否。仅表示 SIM 提供商国家代码，无法定位个人。
     */
    private fun readSimCountryIso(tm: TelephonyManager?): String {
        return try {
            tm?.simCountryIso
        } catch (_: Throwable) {
            null
        }.orEmpty()
    }

    /**
     * Network Country ISO，通过 [TelephonyManager.getNetworkCountryIso] 读取当前注册网络 MCC
     * 对应的 ISO 3166-1 alpha-2 国家代码原始值；无蜂窝网络或系统无法确定时返回空字符串。
     *
     * - 生命周期：随网络注册、漫游和网络切换变化，不表示 SIM 归属国家或实际地理位置。
     * - Manifest 声明：不需要。
     * - 用户授权弹窗：不触发。
     * - PII：否。仅表示当前注册网络国家代码，无法定位个人。
     */
    private fun readNetworkCountryIso(tm: TelephonyManager?): String {
        return try {
            tm?.networkCountryIso
        } catch (_: Throwable) {
            null
        }.orEmpty()
    }

    /**
     * 系统全局 Data Roaming 设置，通过 [Settings.Global.DATA_ROAMING] 读取。
     *
     * 原始值 `0` 映射为 false，`1` 映射为 true；设置不存在、返回其他值或读取异常时返回
     * null。该旧式全局值不能精确表达多 SIM 设备中每个订阅的独立设置，也不表示当前网络
     * 正在漫游。
     *
     * - 生命周期：用户修改数据漫游设置或系统策略后变化。
     * - Manifest 声明：不需要。
     * - 用户授权弹窗：不触发。
     * - PII：否。
     */
    private fun readGlobalDataRoaming(): Boolean? {
        return try {
            when (Settings.Global.getInt(appContext.contentResolver, Settings.Global.DATA_ROAMING)) {
                0 -> false
                1 -> true
                else -> null
            }
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * 默认数据订阅 Data Roaming 开关，API 29+ 通过
     * [TelephonyManager.isDataRoamingEnabled] 读取。优先创建绑定默认数据订阅的
     * TelephonyManager；默认数据订阅无效时使用传入实例。API 21~28、权限不足、服务不存在
     * 或读取异常时返回 null。该值表示允许数据漫游，不表示当前网络正在漫游。
     *
     * - 生命周期：用户修改默认数据订阅或该订阅的数据漫游设置后变化。
     * - Manifest 声明：需要 ACCESS_NETWORK_STATE、READ_PHONE_STATE 或
     *   READ_BASIC_PHONE_STATE 之一；当前模块已声明 READ_PHONE_STATE，不新增权限。
     * - 用户授权弹窗：若依赖 READ_PHONE_STATE，则需要运行时授权；未授权时可能返回 null。
     * - PII：否。
     */
    @SuppressLint("MissingPermission")
    private fun readDataRoamingEnabled(tm: TelephonyManager?): Boolean? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null

        return try {
            val defaultDataSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId()
            val scopedManager = if (
                defaultDataSubscriptionId != SubscriptionManager.INVALID_SUBSCRIPTION_ID
            ) {
                tm?.createForSubscriptionId(defaultDataSubscriptionId)
            } else {
                tm
            }
            scopedManager?.isDataRoamingEnabled
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * SubscriptionInfo Data Roaming 固定枚举。
     *
     * API 24+ 读取默认数据订阅的 [SubscriptionInfo.getDataRoaming]；API 22~23 没有公开的
     * 默认数据订阅 ID，因此仅在恰好一个活动订阅时读取。原始固定枚举转换为 `ENABLE` 或
     * `DISABLE`；系统返回未来未知枚举时返回 `UNKNOWN`，API 不可用、订阅不明确、权限不足
     * 或读取异常时返回空字符串。该值不表示当前网络正在漫游。
     *
     * - 生命周期：用户修改订阅数据漫游设置、换卡或切换默认数据订阅后变化。
     * - Manifest 声明：需要 READ_PHONE_STATE；当前模块已声明，不新增权限。
     * - 用户授权弹窗：读取活动订阅依赖 READ_PHONE_STATE 运行时授权；未授权时返回空字符串。
     * - PII：否，但访问 SubscriptionInfo 本身受 Telephony 权限保护。
     */
    @SuppressLint("MissingPermission")
    private fun readSubscriptionDataRoaming(): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) return ""

        val subscriptionInfo = try {
            val subscriptionManager = appContext
                .getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                ?: return ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val defaultDataSubscriptionId = SubscriptionManager.getDefaultDataSubscriptionId()
                subscriptionManager.getActiveSubscriptionInfo(defaultDataSubscriptionId)
            } else {
                subscriptionManager.activeSubscriptionInfoList
                    ?.singleOrNull()
            }
        } catch (_: Throwable) {
            null
        } ?: return ""

        return when (subscriptionInfo.dataRoaming) {
            SubscriptionManager.DATA_ROAMING_ENABLE -> "ENABLE"
            SubscriptionManager.DATA_ROAMING_DISABLE -> "DISABLE"
            else -> "UNKNOWN"
        }
    }

    /**
     * 当前活动数据订阅使用的蜂窝无线制式。
     *
     * API 24+ 读取 [TelephonyManager.getDataNetworkType]，API 21~23 使用已废弃的
     * [TelephonyManager.getNetworkType]。Android `NETWORK_TYPE_*` 固定整数枚举统一转换为
     * `GPRS`、`LTE`、`NR` 等稳定字符串；系统明确返回 `NETWORK_TYPE_UNKNOWN` 或未来未知枚举
     * 时返回 `UNKNOWN`，权限不足、服务不存在或读取异常时返回空字符串。
     *
     * - 生命周期：随基站、信号、漫游、活动数据卡和 5G/LTE 切换动态变化。
     * - Manifest 声明：需要 READ_PHONE_STATE；当前模块已声明，不新增权限。
     * - 用户授权弹窗：读取依赖 READ_PHONE_STATE 运行时授权；未授权时返回空字符串。
     * - PII：否，仅表示当前蜂窝无线制式。
     * - 语义：不是 Wi-Fi、Cellular、VPN 等当前连接传输类型；5G NSA 通常仍返回 `LTE`，
     *   `NR` 主要表示 5G SA。
     */
    @SuppressLint("MissingPermission")
    private fun readDataNetworkType(tm: TelephonyManager?): String {
        val networkType = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tm?.dataNetworkType
            } else {
                @Suppress("DEPRECATION")
                tm?.networkType
            }
        } catch (_: Throwable) {
            null
        } ?: return ""

        return networkTypeToString(networkType)
    }

    @SuppressLint("InlinedApi")
    @Suppress("DEPRECATION")
    private fun networkTypeToString(networkType: Int): String {
        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_UNKNOWN -> "UNKNOWN"
            TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
            TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
            TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
            TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
            TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO_0"
            TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO_A"
            TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
            TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
            TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
            TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
            TelephonyManager.NETWORK_TYPE_IDEN -> "IDEN"
            TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO_B"
            TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
            TelephonyManager.NETWORK_TYPE_EHRPD -> "EHRPD"
            TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPAP"
            TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
            TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD_SCDMA"
            TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN"
            NETWORK_TYPE_LTE_CA -> "LTE_CA"
            TelephonyManager.NETWORK_TYPE_NR -> "NR"
            else -> "UNKNOWN"
        }
    }

    /**
     * SIM State（SIM 卡状态），通过 [TelephonyManager.getSimState] 读取。
     *
     * - 生命周期：随 SIM 插拔变化。
     * - Manifest 声明：不需要。
     * - 用户授权弹窗：不触发。
     * - PII：否。
     */
    private fun readSimState(tm: TelephonyManager?): String {
        return tm?.simState?.toString().orEmpty()
    }

    /**
     * SIM Operator Name（运营商显示名称），通过 [TelephonyManager.getSimOperatorName] 读取。
     *
     * - 生命周期：换卡即变。
     * - Manifest 声明：不需要。
     * - 用户授权弹窗：不触发。
     * - PII：否。仅运营商名称，无法定位个人。
     */
    private fun readSimOperatorName(tm: TelephonyManager?): String {
        return tm?.simOperatorName.orEmpty()
    }

    /**
     * Line1Number（MSISDN / 本机号码），通过 [TelephonyManager.getLine1Number] 读取。
     *
     * - 生命周期：SIM 卡上存储的号码，换卡即变；绝大多数 SIM 未写入，此时返回空字符串。
     * - Manifest 声明：需要 READ_PHONE_STATE（另需 READ_PHONE_NUMBERS）。
     * - 用户授权弹窗：① 需 READ_PHONE_STATE 弹窗（用户拒绝则无法读取）。
     * - ② Android 11+：系统限制非运营商应用读取，不弹窗，直接返回空字符串。
     * - 返回值：正常时返回号码字符串；权限不足、系统限制或 SIM 未写入时返回空字符串。
     * - PII：是。直接关联用户手机号。
     */
    @SuppressLint("MissingPermission")
    private fun readLine1Number(tm: TelephonyManager?): String {
        return try {
            @Suppress("DEPRECATION")
            tm?.line1Number
        } catch (_: Throwable) {
            null
        }.orEmpty()
    }

    private companion object {
        // NETWORK_TYPE_LTE_CA = 19 未在公开 SDK 暴露，但系统仍可能返回该固定值。
        const val NETWORK_TYPE_LTE_CA = 19
    }
}
