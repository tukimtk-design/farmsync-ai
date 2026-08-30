package com.tukimtk.farmsync.game.stardew

import com.tukimtk.farmsync.model.GameSaveMetadata

class StardewSaveParser {
    fun parse(xmlContent: String): GameSaveMetadata {
        // Stub implementation
        return GameSaveMetadata(
            farmName = "Test Farm",
            characterName = "Player One",
            season = "Spring",
            date = 1,
            year = 1,
            money = 500
        )
    }
}
