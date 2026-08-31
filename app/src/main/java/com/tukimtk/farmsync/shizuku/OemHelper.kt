package com.tukimtk.farmsync.shizuku

import android.os.Build

object OemHelper {
    fun isXiaomiDevice(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return manufacturer == "xiaomi" || manufacturer == "redmi" || manufacturer == "poco"
    }
}
