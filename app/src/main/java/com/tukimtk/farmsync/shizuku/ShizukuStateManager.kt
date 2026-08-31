package com.tukimtk.farmsync.shizuku

import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateOf
import rikka.shizuku.Shizuku

object ShizukuStateManager {
    val isAvailable = mutableStateOf(false)
    val isBinderAlive = mutableStateOf(false)

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkShizuku()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        isBinderAlive.value = false
        isAvailable.value = false
    }

    private val requestPermissionResultListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        isAvailable.value = (grantResult == PackageManager.PERMISSION_GRANTED)
    }

    init {
        try {
            Shizuku.addBinderReceivedListener(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
            checkShizuku()
        } catch (_: Exception) {}
    }

    fun checkShizuku() {
        try {
            val alive = Shizuku.pingBinder()
            isBinderAlive.value = alive
            if (alive) {
                isAvailable.value = (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED)
            } else {
                isAvailable.value = false
            }
        } catch (_: Exception) {
            isBinderAlive.value = false
            isAvailable.value = false
        }
    }

    fun requestPermission(requestCode: Int = 1001) {
        try {
            if (isBinderAlive.value && !isAvailable.value) {
                Shizuku.requestPermission(requestCode)
            }
        } catch (_: Exception) {}
    }
}

