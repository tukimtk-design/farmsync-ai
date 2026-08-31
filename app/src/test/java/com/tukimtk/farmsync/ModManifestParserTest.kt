package com.tukimtk.farmsync

import com.tukimtk.farmsync.mods.ModManifestParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModManifestParserTest {

    @Test
    fun testParseManifest() {
        val parser = ModManifestParser()
        val dummyJson = "{}"
        val result = parser.parse(dummyJson)

        assertEquals("com.example.testmod", result.uniqueId)
        assertEquals("Test Mod", result.name)
        assertEquals("1.0.0", result.version)
        assertTrue(result.isEnabled)
    }
}
