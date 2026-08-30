package com.tukimtk.farmsync

import com.tukimtk.farmsync.game.stardew.StardewSaveParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StardewSaveParserTest {

    @Test
    fun testParse() {
        val parser = StardewSaveParser()
        val dummyXml = "<dummy></dummy>"
        val result = parser.parse(dummyXml)

        assertEquals("Test Farm", result.farmName)
        assertEquals("Player One", result.characterName)
        assertEquals("Spring", result.season)
        assertEquals(1, result.date)
        assertEquals(1, result.year)
        assertEquals(500, result.money)
    }
}
