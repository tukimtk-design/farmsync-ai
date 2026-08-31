package com.tukimtk.farmsync.mods

import com.tukimtk.farmsync.model.StardewMod

class ModManifestParser {
    fun parse(manifestJson: String, isEnabled: Boolean = true): StardewMod {
        // Stub implementation for verifying mod versions/reporting
        return StardewMod(
            uniqueId = "com.example.testmod",
            name = "Test Mod",
            version = "1.0.0",
            description = "A test mod for version verification.",
            isEnabled = isEnabled
        )
    }
}
