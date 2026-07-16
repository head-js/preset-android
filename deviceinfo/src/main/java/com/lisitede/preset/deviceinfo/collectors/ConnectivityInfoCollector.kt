package com.lisitede.preset.deviceinfo.collectors

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.lisitede.preset.deviceinfo.DeviceInfoEntry

internal data class ConnectivityInfo(
    val connectivity_type: DeviceInfoEntry,
    val http_proxy: DeviceInfoEntry
)

private enum class ConnectivityType {
    WIFI, CELLULAR, ETHERNET, BLUETOOTH, VPN, NONE
}

internal class ConnectivityInfoCollector(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun getConnectivityInfo(): ConnectivityInfo {
        return ConnectivityInfo(
            connectivity_type = DeviceInfoEntry(
                "connectivity_type",
                getCurrentConnectivity().name,
                "Connectivity Type"
            ),
            http_proxy = DeviceInfoEntry(
                "http_proxy",
                getHttpProxy(),
                "HTTP Proxy"
            )
        )
    }

    private fun getCurrentConnectivity(): ConnectivityType {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return ConnectivityType.NONE
            val capabilities = connectivityManager.getNetworkCapabilities(network)
                ?: return ConnectivityType.NONE
            return transportToType(capabilities)
        } else {
            @Suppress("DEPRECATION")
            val info = connectivityManager.activeNetworkInfo
            if (info == null || !info.isConnectedOrConnecting) return ConnectivityType.NONE
            @Suppress("DEPRECATION")
            return when (info.type) {
                ConnectivityManager.TYPE_WIFI -> ConnectivityType.WIFI
                ConnectivityManager.TYPE_MOBILE -> ConnectivityType.CELLULAR
                ConnectivityManager.TYPE_ETHERNET -> ConnectivityType.ETHERNET
                ConnectivityManager.TYPE_BLUETOOTH -> ConnectivityType.BLUETOOTH
                ConnectivityManager.TYPE_VPN -> ConnectivityType.VPN
                else -> ConnectivityType.NONE
            }
        }
    }

    /**
     * 当前系统默认 HTTP 代理的规范化字符串。
     *
     * API 23+ 读取 [ConnectivityManager.getDefaultProxy]：PAC 代理返回原始 PAC URL，直接代理
     * 返回 `host:port`。API 21~22 使用 `http.proxyHost` 和 `http.proxyPort` Java 系统属性，
     * 无法表达 PAC 代理。无代理、字段不完整或读取异常时返回空字符串。
     *
     * - 声明：API 23+ 需要 ACCESS_NETWORK_STATE，不触发授权弹窗。
     * - PII：否，但代理地址属于当前网络配置，可能暴露企业或网络环境信息。
     * - 稳定性：会随 Wi-Fi、蜂窝网络、VPN 或系统代理设置变化，不是固定设备属性。
     * - 语义：不检测 App 自定义代理、VPN 内部转发或透明代理，也不代表流量实际经过代理。
     */
    private fun getHttpProxy(): String {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val proxyInfo = connectivityManager.defaultProxy ?: return ""
                val pacUrl = proxyInfo.pacFileUrl?.toString().orEmpty()
                if (pacUrl.isNotEmpty()) {
                    pacUrl
                } else {
                    formatDirectProxy(proxyInfo.host, proxyInfo.port)
                }
            } else {
                val host = System.getProperty("http.proxyHost")
                val port = System.getProperty("http.proxyPort")?.toIntOrNull()
                formatDirectProxy(host, port)
            }
        } catch (_: Throwable) {
            ""
        }
    }

    private fun transportToType(capabilities: NetworkCapabilities): ConnectivityType {
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectivityType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectivityType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectivityType.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> ConnectivityType.BLUETOOTH
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> ConnectivityType.VPN
            else -> ConnectivityType.NONE
        }
    }

    private fun formatDirectProxy(host: String?, port: Int?): String {
        return if (host.isNullOrEmpty() || port == null || port !in 1..65535) {
            ""
        } else {
            "$host:$port"
        }
    }
}
