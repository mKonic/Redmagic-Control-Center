package com.elitedarkkaiser.redmagic

import android.content.Context

private const val PREFS = "device_info_cache"
private const val ROM = "rom"
private const val CPU = "cpu"
private const val RAM = "ram"

data class DeviceInfoCache(
    val rom: String,
    val cpu: String,
    val ram: String
)

fun loadDeviceInfoCacheStorage(context: Context): DeviceInfoCache? {
    val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    val rom = prefs.getString(ROM, null)
    val cpu = prefs.getString(CPU, null)
    val ram = prefs.getString(RAM, null)

    return if (!rom.isNullOrBlank() && !cpu.isNullOrBlank() && !ram.isNullOrBlank()) {
        DeviceInfoCache(rom, cpu, ram)
    } else {
        null
    }
}

fun saveDeviceInfoCacheStorage(context: Context, info: DeviceInfoCache) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(ROM, info.rom)
        .putString(CPU, info.cpu)
        .putString(RAM, info.ram)
        .apply()
}
