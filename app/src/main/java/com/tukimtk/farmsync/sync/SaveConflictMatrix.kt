package com.tukimtk.farmsync.sync

import com.tukimtk.farmsync.model.GameSaveMetadata

class SaveConflictMatrix {
    fun resolveConflict(localSave: GameSaveMetadata, remoteSave: GameSaveMetadata): ConflictResult {
        // Multi-layer verification stub based on in-game timeline
        if (localSave.year > remoteSave.year) {
            return ConflictResult.KEEP_LOCAL
        }
        return ConflictResult.SYNC_REMOTE
    }
}

enum class ConflictResult {
    KEEP_LOCAL, SYNC_REMOTE, MANUAL_RESOLUTION
}
