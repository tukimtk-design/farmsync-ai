package com.tukimtk.farmsync.sync

import com.tukimtk.farmsync.model.SaveMetadata

class SaveDecisionEngine {
    fun evaluate(pcMetadata: SaveMetadata, mobileMetadata: SaveMetadata): SyncDecisionResult {
        // 1. MALFORMED CHECK
        if (!pcMetadata.isValidParse || !mobileMetadata.isValidParse) {
            return SyncDecisionResult(SyncAction.ERROR_MALFORMED, "MALFORMED", "One or both saves are invalid or corrupted.")
        }

        // 2. HASH CHECK
        if (pcMetadata.fileHash == mobileMetadata.fileHash) {
            return SyncDecisionResult(SyncAction.IDENTICAL, "HASH_MATCH", "Files are cryptographically identical.")
        }

        // 3. FARM IDENTITY
        if (pcMetadata.farmId != mobileMetadata.farmId) {
            return SyncDecisionResult(SyncAction.CONFLICT, "FARM_MISMATCH", "Saves belong to different farms.")
        }

        // 4. VERSION GATE
        val pcMajorMinor = parseMajorMinor(pcMetadata.gameVersion)
        val mobileMajorMinor = parseMajorMinor(mobileMetadata.gameVersion)

        if (pcMajorMinor > mobileMajorMinor) {
            return SyncDecisionResult(SyncAction.INCOMPATIBLE_VERSION, "MOBILE_OUTDATED", "PC version is newer. Mobile cannot load this save.")
        }
        if (mobileMajorMinor > pcMajorMinor) {
            return SyncDecisionResult(SyncAction.INCOMPATIBLE_VERSION, "PC_OUTDATED", "Mobile version is newer. PC cannot load this save.")
        }

        // 5. IN-GAME PROGRESSION MATRIX
        val pcTotalDays = calculateTotalDays(pcMetadata)
        val mobileTotalDays = calculateTotalDays(mobileMetadata)

        if (pcTotalDays > mobileTotalDays) {
            return SyncDecisionResult(SyncAction.PUSH_PC_TO_MOBILE, "PC_AHEAD_DAYS", "PC has played more in-game days.")
        }
        if (mobileTotalDays > pcTotalDays) {
            return SyncDecisionResult(SyncAction.PUSH_MOBILE_TO_PC, "MOBILE_AHEAD_DAYS", "Mobile has played more in-game days.")
        }

        // Total days are equal. Check money and playtime
        if (pcMetadata.money > mobileMetadata.money && pcMetadata.playTime >= mobileMetadata.playTime) {
             return SyncDecisionResult(SyncAction.PUSH_PC_TO_MOBILE, "PC_AHEAD_STATS", "PC has more money/playtime on the same day.")
        }
        if (mobileMetadata.money > pcMetadata.money && mobileMetadata.playTime >= pcMetadata.playTime) {
             return SyncDecisionResult(SyncAction.PUSH_MOBILE_TO_PC, "MOBILE_AHEAD_STATS", "Mobile has more money/playtime on the same day.")
        }
        if (pcMetadata.playTime > mobileMetadata.playTime && pcMetadata.money >= mobileMetadata.money) {
             return SyncDecisionResult(SyncAction.PUSH_PC_TO_MOBILE, "PC_AHEAD_STATS", "PC has more money/playtime on the same day.")
        }
        if (mobileMetadata.playTime > pcMetadata.playTime && mobileMetadata.money >= pcMetadata.money) {
             return SyncDecisionResult(SyncAction.PUSH_MOBILE_TO_PC, "MOBILE_AHEAD_STATS", "Mobile has more money/playtime on the same day.")
        }

        // If progress is diverging (e.g. PC more money, Mobile more playtime)
        if (pcMetadata.money != mobileMetadata.money || pcMetadata.playTime != mobileMetadata.playTime) {
            return SyncDecisionResult(SyncAction.CONFLICT, "DIVERGING_PROGRESS", "Progress diverges on the same day (one has more money, the other more playtime).")
        }

        // 6. TIMESTAMP TIE-BREAKER
        if (pcMetadata.lastModifiedTimestamp != mobileMetadata.lastModifiedTimestamp) {
            return SyncDecisionResult(SyncAction.MANUAL_REVIEW, "TIMESTAMP_DIFF", "In-game progress is identical but files differ. Manual review required.")
        }

        // Fallback (shouldn't reach here if hash check is robust, but just in case)
        return SyncDecisionResult(SyncAction.IDENTICAL, "FALLBACK_MATCH", "Saves appear identical.")
    }

    private fun calculateTotalDays(metadata: SaveMetadata): Int {
        return (metadata.inGameYear * 112) + (metadata.inGameSeason.index * 28) + metadata.inGameDay
    }

    private fun parseMajorMinor(version: String): Double {
        val parts = version.split(".")
        if (parts.size >= 2) {
            return "${parts[0]}.${parts[1]}".toDoubleOrNull() ?: 0.0
        }
        return version.toDoubleOrNull() ?: 0.0
    }
}
