package com.lisitede.preset.preset

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build

data class AppPackageInfo(
    val appName: String,
    val packageName: String,
    val versionName: String?,
    val versionCode: Long
)

class PackageInfoHelper(context: Context) {

    private val packageManager = context.packageManager
    private val packageInfo: PackageInfo =
        packageManager.getPackageInfo(context.packageName, 0)
    private val applicationInfo = packageInfo.applicationInfo!!

    fun getAppPackageInfo(): AppPackageInfo {
        return AppPackageInfo(
            appName = applicationInfo.loadLabel(packageManager).toString(),
            packageName = packageInfo.packageName ?: "unknown",
            versionName = packageInfo.versionName,
            versionCode = getVersionCode()
        )
    }

    @Suppress("DEPRECATION")
    private fun getVersionCode(): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            packageInfo.versionCode.toLong()
        }
    }

    fun getDisplayString(): String {
        val info = getAppPackageInfo()
        return buildString {
            appendLine("App Name: ${info.appName}")
            appendLine("Package Name: ${info.packageName}")
            appendLine("Version Name: ${info.versionName}")
            appendLine("Version Code: ${info.versionCode}")
        }
    }
}
