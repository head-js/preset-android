package com.lisitede.preset.deviceinfo.collectors

import com.lisitede.preset.deviceinfo.DeviceInfoEntry

/**
 * ROM 相关 system property 原始读取结果。
 *
 * Model 属性名与公开的 [DeviceInfoEntry.key] 保持一致。已有 ROM key 使用下划线映射实际
 * system property key（如 `ro_miui_ui_version_name` 对应 `ro.miui.ui.version.name`）；
 * `vb_meta_digest` 对应原有公开字段 `vbMetaDigest` 的 snake_case 形式，底层读取
 * `ro.boot.vbmeta.digest`。读取方式为反射调用 [SystemProperties.get]，不汇总、不归一化、
 * 不做 ROM 归类。
 *
 * - 声明：不需要。
 * - 弹窗：不触发。
 * - PII：否。ROM 版本字段无法定位个人。
 * - 稳定性：VBMeta 摘要在同一启动镜像下通常稳定，OTA、切换不同版本的 A/B Slot、刷机或
 *   修改 VBMeta 后可能变化；它不是单台设备唯一标识，也不表示 Verified Boot 是否成功或
 *   bootloader 是否锁定。
 */
internal data class RomInfo(
    /**
     * ro.boot.vbmeta.digest
     *
     * AVB 2.0 bootloader 提供的当前 VBMeta 元数据摘要原始值。旧设备、未使用 AVB 2.0 或
     * ROM 未导出该属性时为空；不转换大小写，不再次哈希。
     */
    val vb_meta_digest: DeviceInfoEntry,

    /** ro.miui.ui.version.name */
    val ro_miui_ui_version_name: DeviceInfoEntry,

    /** ro.miui.ui.version.code */
    val ro_miui_ui_version_code: DeviceInfoEntry,

    /** ro.mi.os.version.name */
    val ro_mi_os_version_name: DeviceInfoEntry,

    /** ro.mi.os.version.code */
    val ro_mi_os_version_code: DeviceInfoEntry,

    /** ro.mi.os.version.incremental */
    val ro_mi_os_version_incremental: DeviceInfoEntry,

    /** ro.build.version.emui */
    val ro_build_version_emui: DeviceInfoEntry,

    /** ro.build.version.magic */
    val ro_build_version_magic: DeviceInfoEntry,

    /** ro.build.version.opporom */
    val ro_build_version_opporom: DeviceInfoEntry,

    /** ro.build.version.oplusrom */
    val ro_build_version_oplusrom: DeviceInfoEntry,

    /** ro.build.version.realmeui */
    val ro_build_version_realmeui: DeviceInfoEntry,

    /** ro.vivo.os.name */
    val ro_vivo_os_name: DeviceInfoEntry,

    /** ro.vivo.os.version */
    val ro_vivo_os_version: DeviceInfoEntry,

    /** ro.vivo.rom */
    val ro_vivo_rom: DeviceInfoEntry,

    /** ro.vivo.rom.version */
    val ro_vivo_rom_version: DeviceInfoEntry,

    /** ro.build.version.oneui */
    val ro_build_version_oneui: DeviceInfoEntry,

    /** ro.flyme.published */
    val ro_flyme_published: DeviceInfoEntry,

    /** ro.meizu.setupwizard.flyme */
    val ro_meizu_setupwizard_flyme: DeviceInfoEntry,

    /** ro.smartisan.version */
    val ro_smartisan_version: DeviceInfoEntry,

    /** ro.letv.release.version */
    val ro_letv_release_version: DeviceInfoEntry,

    /** ro.lenovo.lvp.version */
    val ro_lenovo_lvp_version: DeviceInfoEntry,

    /** ro.build.nubia.rom.name */
    val ro_build_nubia_rom_name: DeviceInfoEntry,

    /** ro.build.nubia.rom.code */
    val ro_build_nubia_rom_code: DeviceInfoEntry,

    /** ro.build.version.oxygen */
    val ro_build_version_oxygen: DeviceInfoEntry,

    /** ro.build.version.harmony */
    val ro_build_version_harmony: DeviceInfoEntry,

    /** ro.build.version.harmony_type */
    val ro_build_version_harmony_type: DeviceInfoEntry,

    /** hw_sc.build.platform.version */
    val hw_sc_build_platform_version: DeviceInfoEntry
)

internal class RomInfoCollector {
    fun getRomInfo(): RomInfo {
        return RomInfo(
            vb_meta_digest = entry("vb_meta_digest", "ro.boot.vbmeta.digest", "VBMeta Digest"),
            ro_miui_ui_version_name = propertyEntry("ro_miui_ui_version_name", "ro.miui.ui.version.name"),
            ro_miui_ui_version_code = propertyEntry("ro_miui_ui_version_code", "ro.miui.ui.version.code"),
            ro_mi_os_version_name = propertyEntry("ro_mi_os_version_name", "ro.mi.os.version.name"),
            ro_mi_os_version_code = propertyEntry("ro_mi_os_version_code", "ro.mi.os.version.code"),
            ro_mi_os_version_incremental = propertyEntry(
                "ro_mi_os_version_incremental",
                "ro.mi.os.version.incremental"
            ),
            ro_build_version_emui = propertyEntry("ro_build_version_emui", "ro.build.version.emui"),
            ro_build_version_magic = propertyEntry("ro_build_version_magic", "ro.build.version.magic"),
            ro_build_version_opporom = propertyEntry("ro_build_version_opporom", "ro.build.version.opporom"),
            ro_build_version_oplusrom = propertyEntry("ro_build_version_oplusrom", "ro.build.version.oplusrom"),
            ro_build_version_realmeui = propertyEntry("ro_build_version_realmeui", "ro.build.version.realmeui"),
            ro_vivo_os_name = propertyEntry("ro_vivo_os_name", "ro.vivo.os.name"),
            ro_vivo_os_version = propertyEntry("ro_vivo_os_version", "ro.vivo.os.version"),
            ro_vivo_rom = propertyEntry("ro_vivo_rom", "ro.vivo.rom"),
            ro_vivo_rom_version = propertyEntry("ro_vivo_rom_version", "ro.vivo.rom.version"),
            ro_build_version_oneui = propertyEntry("ro_build_version_oneui", "ro.build.version.oneui"),
            ro_flyme_published = propertyEntry("ro_flyme_published", "ro.flyme.published"),
            ro_meizu_setupwizard_flyme = propertyEntry(
                "ro_meizu_setupwizard_flyme",
                "ro.meizu.setupwizard.flyme"
            ),
            ro_smartisan_version = propertyEntry("ro_smartisan_version", "ro.smartisan.version"),
            ro_letv_release_version = propertyEntry("ro_letv_release_version", "ro.letv.release.version"),
            ro_lenovo_lvp_version = propertyEntry("ro_lenovo_lvp_version", "ro.lenovo.lvp.version"),
            ro_build_nubia_rom_name = propertyEntry("ro_build_nubia_rom_name", "ro.build.nubia.rom.name"),
            ro_build_nubia_rom_code = propertyEntry("ro_build_nubia_rom_code", "ro.build.nubia.rom.code"),
            ro_build_version_oxygen = propertyEntry("ro_build_version_oxygen", "ro.build.version.oxygen"),
            ro_build_version_harmony = propertyEntry("ro_build_version_harmony", "ro.build.version.harmony"),
            ro_build_version_harmony_type = entry(
                "ro_build_version_harmony_type",
                "ro.build.version.harmony_type",
                "ro.build.version.harmony_type"
            ),
            hw_sc_build_platform_version = entry(
                "hw_sc_build_platform_version",
                "hw_sc.build.platform.version",
                "hw_sc.build.platform.version"
            )
        )
    }

    private fun propertyEntry(key: String, propertyKey: String): DeviceInfoEntry {
        return entry(key, propertyKey, propertyKey)
    }

    private fun entry(key: String, propertyKey: String, label: String): DeviceInfoEntry {
        return DeviceInfoEntry(key, getSystemProperty(propertyKey), label)
    }

    private fun getSystemProperty(key: String): String {
        return try {
            Class.forName("android.os.SystemProperties")
                .getMethod("get", String::class.java)
                .invoke(null, key) as? String
        } catch (_: Throwable) {
            null
        }.orEmpty()
    }
}
