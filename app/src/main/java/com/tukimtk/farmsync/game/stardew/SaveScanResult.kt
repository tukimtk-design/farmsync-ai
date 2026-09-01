package com.tukimtk.farmsync.game.stardew

sealed class SaveScanResult {
    object Idle : SaveScanResult()
    object Scanning : SaveScanResult()
    data class SavesFound(val saves: List<RealSaveSlot>) : SaveScanResult()
    object NoSavesFound : SaveScanResult()
    data class ScanFailed(val reason: ScanFailureReason) : SaveScanResult()
}

enum class ScanFailureReason {
    ShizukuNotReady,
    NoSupportedSaveRoot,
    AccessDenied,
    CommandFailed,
    MalformedResult,
    UnexpectedFailure
}
