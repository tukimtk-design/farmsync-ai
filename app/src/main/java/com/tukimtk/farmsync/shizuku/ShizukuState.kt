package com.tukimtk.farmsync.shizuku

sealed class ShizukuState {
    object NotInstalled : ShizukuState()
    object NotRunning : ShizukuState()
    object VersionTooOld : ShizukuState()
    object PermissionRequired : ShizukuState()
    object RequiresManualAuthorization : ShizukuState()
    object Ready : ShizukuState()
}

object ShizukuStateEvaluator {
    fun evaluate(
        isInstalled: Boolean,
        isBinderAlive: Boolean,
        isVersionSupported: Boolean,
        isPermissionGranted: Boolean
    ): ShizukuState {
        if (!isInstalled) return ShizukuState.NotInstalled
        if (!isBinderAlive) return ShizukuState.NotRunning
        if (!isVersionSupported) return ShizukuState.VersionTooOld
        if (isPermissionGranted) return ShizukuState.Ready
        return ShizukuState.PermissionRequired
    }
}
