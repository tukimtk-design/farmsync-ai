package com.tukimtk.farmsync

import com.tukimtk.farmsync.game.StardewProfile
import com.tukimtk.farmsync.game.TerrariaProfile
import org.junit.Test
import org.junit.Assert.assertEquals

class GameProfileTest {
    @Test
    fun testStardewProfile() {
        val profile = StardewProfile()
        assertEquals("Stardew Valley", profile.gameName)
        assertEquals("StardewValley", profile.expectedSaveDirectory)
    }

    @Test
    fun testTerrariaProfile() {
        val profile = TerrariaProfile()
        assertEquals("Terraria", profile.gameName)
    }
}
