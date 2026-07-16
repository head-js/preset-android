package com.lisitede.preset.deviceinfo.collectors

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import com.lisitede.preset.deviceinfo.DeviceInfoEntry
import java.security.MessageDigest

internal data class AppInfo(
    val app_name: DeviceInfoEntry,
    val package_name: DeviceInfoEntry,
    val version_name: DeviceInfoEntry,
    val version_code: DeviceInfoEntry,
    val signing_cert_digest: DeviceInfoEntry
)

internal class AppInfoCollector(context: Context) {

    private val packageManager = context.packageManager
    private val packageInfo: PackageInfo =
        packageManager.getPackageInfo(context.packageName, 0)
    private val applicationInfo = packageInfo.applicationInfo!!

    fun getAppInfo(): AppInfo {
        return AppInfo(
            app_name = DeviceInfoEntry(
                "app_name",
                applicationInfo.loadLabel(packageManager).toString(),
                "App Name"
            ),
            package_name = DeviceInfoEntry(
                "package_name",
                packageInfo.packageName ?: "unknown",
                "Package Name"
            ),
            version_name = DeviceInfoEntry(
                "version_name",
                packageInfo.versionName.orEmpty(),
                "Version Name"
            ),
            version_code = DeviceInfoEntry(
                "version_code",
                getVersionCode().toString(),
                "Version Code"
            ),
            signing_cert_digest = DeviceInfoEntry(
                "signing_cert_digest",
                getSigningCertDigest(),
                "Signing Cert Digest"
            )
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

    /**
     * 当前已安装 APK 签名证书的 SHA-256 摘要。
     *
     * API 28+ 读取 [android.content.pm.SigningInfo.getApkContentsSigners]，API 21~27 使用
     * [PackageInfo.signatures]。每个证书摘要编码为小写十六进制；存在多个当前签名者时，
     * 按摘要排序后使用英文逗号拼接，确保返回顺序稳定。读取失败时返回空字符串。
     *
     * - 声明：读取当前 App 不需要权限。
     * - 弹窗：不触发。
     * - PII：否，属于 App 身份信息。
     * - 稳定性：使用同一签名证书的 App 更新期间稳定；debug/release 切换、签名轮换或
     *   不同分发渠道使用不同签名时会变化。Google Play App Signing 场景读取设备上 APK
     *   的 App Signing Key，不是开发者上传密钥。
     */
    private fun getSigningCertDigest(): String {
        return try {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            getCurrentSignatures()
                .map { signature ->
                    messageDigest.digest(signature.toByteArray()).toLowerHexString()
                }
                .sorted()
                .joinToString(",")
        } catch (_: Throwable) {
            ""
        }
    }

    @Suppress("DEPRECATION")
    private fun getCurrentSignatures(): Array<out Signature> {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            PackageManager.GET_SIGNATURES
        }
        val signingPackageInfo = packageManager.getPackageInfo(packageInfo.packageName, flags)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            signingPackageInfo.signingInfo?.apkContentsSigners.orEmpty()
        } else {
            signingPackageInfo.signatures.orEmpty()
        }
    }

    private fun ByteArray.toLowerHexString(): String {
        val chars = CharArray(size * 2)
        forEachIndexed { index, byte ->
            val value = byte.toInt() and 0xff
            chars[index * 2] = HEX_DIGITS[value ushr 4]
            chars[index * 2 + 1] = HEX_DIGITS[value and 0x0f]
        }
        return String(chars)
    }

    private companion object {
        const val HEX_DIGITS = "0123456789abcdef"
    }
}
