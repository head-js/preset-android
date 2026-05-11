package com.lisitede.preset.preset

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build

enum class ConnectivityType {
    WIFI, CELLULAR, ETHERNET, BLUETOOTH, VPN, NONE
}

class ConnectivityHelper(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var callback: ConnectivityManager.NetworkCallback? = null

    fun getCurrentConnectivity(): ConnectivityType {
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

    fun registerCallback(onChanged: (ConnectivityType) -> Unit) {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                onChanged.invoke(getCurrentConnectivity())
            }

            override fun onLost(network: Network) {
                onChanged.invoke(ConnectivityType.NONE)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                onChanged.invoke(transportToType(networkCapabilities))
            }
        }

        connectivityManager.registerNetworkCallback(request, networkCallback)
        callback = networkCallback
    }

    fun unregisterCallback() {
        callback?.let {
            connectivityManager.unregisterNetworkCallback(it)
            callback = null
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
}
