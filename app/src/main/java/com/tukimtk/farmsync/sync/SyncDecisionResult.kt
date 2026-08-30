package com.tukimtk.farmsync.sync

enum class SyncAction {
    IDENTICAL,
    PUSH_PC_TO_MOBILE,
    PUSH_MOBILE_TO_PC,
    CONFLICT,
    INCOMPATIBLE_VERSION,
    MANUAL_REVIEW,
    ERROR_MALFORMED
}

data class SyncDecisionResult(
    val action: SyncAction,
    val reasonCode: String,
    val description: String
)
