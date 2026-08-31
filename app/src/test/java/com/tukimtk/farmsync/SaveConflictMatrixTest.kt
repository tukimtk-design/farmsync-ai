package com.tukimtk.farmsync

import com.tukimtk.farmsync.model.GameSaveMetadata
import com.tukimtk.farmsync.sync.ConflictResult
import com.tukimtk.farmsync.sync.SaveConflictMatrix
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SaveConflictMatrixTest {

    @Test
    fun testKeepLocalWhenLocalIsNewer() {
        val matrix = SaveConflictMatrix()

        val localSave = GameSaveMetadata("Farm", "Player", "Spring", 1, 3, 500)
        val remoteSave = GameSaveMetadata("Farm", "Player", "Winter", 28, 2, 400)

        val result = matrix.resolveConflict(localSave, remoteSave)
        assertEquals(ConflictResult.KEEP_LOCAL, result)
    }
}
