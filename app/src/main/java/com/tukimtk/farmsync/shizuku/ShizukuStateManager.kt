package com.tukimtk.farmsync.shizuku

import androidx.compose.runtime.mutableStateOf
import rikka.shizuku.Shizuku

object ShizukuStateManager {
    val isAvailable = mutableStateOf(false)

    fun checkShizuku() {
        try {
            if (Shizuku.pingBinder()) {
                isAvailable.value = Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                isAvailable.value = false
            }
        } catch (e: Exception) {
            isAvailable.value = false
        }
    }
}
