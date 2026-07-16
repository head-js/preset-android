package com.lisitede.preset.deviceinfo

/**
 * 一条已采集的设备信息。
 *
 * @property key 稳定、机器可读的 snake_case 字段标识。
 * @property value 原始字段值；不可读取时为空字符串。
 * @property label 面向使用方展示的字段名称。
 */
data class DeviceInfoEntry(
    val key: String,
    val value: String,
    val label: String
)
