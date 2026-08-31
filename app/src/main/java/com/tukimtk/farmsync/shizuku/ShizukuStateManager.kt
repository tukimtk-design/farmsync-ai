package com.tukimtk.farmsync.shizuku

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

object ShizukuStateManager {
    private val _state = MutableStateFlow<ShizukuState>(ShizukuState.NotInstalled)
    val state: StateFlow<ShizukuState> = _state.asStateFlow()

    private var isInitialized = false
    private var isRequestingPermission = false

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        refresh()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _state.value = ShizukuState.NotRunning
    }

    private val requestPermissionResultListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        isRequestingPermission = false
        refresh()
    }

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true

        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
            refresh(context)
        } catch (_: Exception) {}
    }

    fun refresh(context: Context? = null) {
        try {
            val isInstalled = context?.let {
                try {
                    it.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
                    true
                } catch (e: PackageManager.NameNotFoundException) {
                    false
                }
            } ?: true // Assume installed if no context and we reached here

            val isBinderAlive = Shizuku.pingBinder()
            val isVersionSupported = try { Shizuku.getVersion() >= 11 } catch (e: Exception) { false }
            val isPermissionGranted = if (isBinderAlive) {
                 Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            } else false

            _state.value = ShizukuStateEvaluator.evaluate(
                isInstalled = isInstalled,
                isBinderAlive = isBinderAlive,
                isVersionSupported = isVersionSupported,
                isPermissionGranted = isPermissionGranted
            )
        } catch (_: Exception) {
            _state.value = ShizukuState.NotInstalled // Fail closed
        }
    }

    fun requestPermission(requestCode: Int = 1001) {
        if (isRequestingPermission) return

        try {
            if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                isRequestingPermission = true
                Shizuku.requestPermission(requestCode)
            }
        } catch (e: Exception) {
            isRequestingPermission = false
        }
    }
}
