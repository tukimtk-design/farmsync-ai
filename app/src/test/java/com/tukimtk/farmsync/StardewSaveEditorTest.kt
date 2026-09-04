package com.tukimtk.farmsync

import com.tukimtk.farmsync.game.stardew.EditableSaveData
import com.tukimtk.farmsync.game.stardew.StardewSaveEditor
import org.junit.Assert.*
import org.junit.Test

class StardewSaveEditorTest {

    private val editor = StardewSaveEditor()

    private val sampleSaveXml = """
        <?xml version="1.0" encoding="utf-8"?>
        <SaveGame xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
            <player>
                <name>OriginalFarmer</name>
                <farmName>OldFarm</farmName>
                <money>500</money>
                <totalMoneyEarned>1000</totalMoneyEarned>
                <maxHealth>100</maxHealth>
                <maxStamina>270</maxStamina>
            </player>
            <locations>
                <GameLocation xsi:type="Farm">
                    <characters>
                        <NPC>
                            <name>Abigail</name>
                        </NPC>
                        <NPC>
                            <name>Pierre</name>
                        </NPC>
                    </characters>
                    <animals>
                        <FarmAnimal>
                            <name>DaisyTheCow</name>
                        </FarmAnimal>
                    </animals>
                </GameLocation>
            </locations>
            <currentSeason>spring</currentSeason>
            <dayOfMonth>1</dayOfMonth>
            <year>1</year>
        </SaveGame>
    """.trimIndent()

    @Test
    fun `test that player is modified but all NPCs and Animals are 100 percent preserved`() {
        val edits = EditableSaveData(
            characterName = "SuperFarmer",
            farmName = "Golden Valley",
            money = 999999,
            season = "Winter",
            dayOfMonth = 25,
            year = 3,
            maxHealth = 180,
            maxStamina = 500
        )

        val modified = editor.applyEditsToXml(sampleSaveXml, edits)

        // 1. Verify Player edits
        assertTrue("Player name must be updated", modified.contains("<name>SuperFarmer</name>"))
        assertTrue("Farm name must be updated", modified.contains("<farmName>Golden Valley</farmName>"))
        assertTrue("Money must be updated", modified.contains("<money>999999</money>"))
        assertTrue("Season must be updated", modified.contains("<currentSeason>winter</currentSeason>"))
        assertTrue("Day must be updated", modified.contains("<dayOfMonth>25</dayOfMonth>"))
        assertTrue("Year must be updated", modified.contains("<year>3</year>"))
        assertTrue("Max health must be updated", modified.contains("<maxHealth>180</maxHealth>"))
        assertTrue("Max stamina must be updated", modified.contains("<maxStamina>500</maxStamina>"))

        // 2. CRITICAL: Verify NPCs and Animals are NOT corrupted
        assertTrue("NPC Abigail must NOT be renamed", modified.contains("<name>Abigail</name>"))
        assertTrue("NPC Pierre must NOT be renamed", modified.contains("<name>Pierre</name>"))
        assertTrue("Animal DaisyTheCow must NOT be renamed", modified.contains("<name>DaisyTheCow</name>"))
        assertFalse("OriginalFarmer must no longer exist", modified.contains("<name>OriginalFarmer</name>"))
    }

    @Test
    fun `test that farmerTeam money and totalMoneyEarned are synchronized in Stardew 1_6 saves`() {
        val saveXmlWithTeam = """
            <?xml version="1.0" encoding="utf-8"?>
            <SaveGame xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                <player>
                    <name>FarmerTuki</name>
                    <farmName>TukiFarm</farmName>
                    <money>500</money>
                    <totalMoneyEarned>500</totalMoneyEarned>
                </player>
                <farmerTeam>
                    <money>500</money>
                    <totalMoneyEarned>500</totalMoneyEarned>
                    <useSeparateWallets>false</useSeparateWallets>
                </farmerTeam>
            </SaveGame>
        """.trimIndent()

        val edits = EditableSaveData(
            characterName = "FarmerTuki",
            farmName = "TukiFarm",
            money = 999999
        )

        val modified = editor.applyEditsToXml(saveXmlWithTeam, edits)

        // Both player and farmerTeam blocks must have money updated to 999999
        val playerBlock = modified.substringAfter("<player>").substringBefore("</player>")
        val teamBlock = modified.substringAfter("<farmerTeam>").substringBefore("</farmerTeam>")

        assertTrue("Player block must have 999999", playerBlock.contains("<money>999999</money>"))
        assertTrue("farmerTeam block must have 999999", teamBlock.contains("<money>999999</money>"))
        assertTrue("farmerTeam totalMoneyEarned must be updated", teamBlock.contains("<totalMoneyEarned>1049999</totalMoneyEarned>"))
    }

    @Test
    fun `test that individualMoney is also updated in farmerTeam`() {
        val saveXml = """
            <SaveGame>
                <player>
                    <name>FarmerTuki</name>
                    <money>500</money>
                </player>
                <farmerTeam>
                    <money>500</money>
                    <individualMoney>
                        <item>
                            <key><long>12345678</long></key>
                            <value><int>500</int></value>
                        </item>
                    </individualMoney>
                </farmerTeam>
            </SaveGame>
        """.trimIndent()

        val edits = EditableSaveData(money = 999999)
        val modified = editor.applyEditsToXml(saveXml, edits)

        assertTrue(modified.contains("<money>999999</money>"))
        assertTrue(modified.contains("<value><int>999999</int></value>"))
    }

    @Test
    fun `test that location currentSeason is preserved and only root currentSeason is updated`() {
        val saveWithLocationSeason = """
            <SaveGame>
                <player>
                    <name>FarmerTuki</name>
                    <money>500</money>
                </player>
                <locations>
                    <GameLocation xsi:type="Farm">
                        <currentSeason>summer</currentSeason>
                    </GameLocation>
                </locations>
                <currentSeason>spring</currentSeason>
                <dayOfMonth>1</dayOfMonth>
                <year>1</year>
            </SaveGame>
        """.trimIndent()

        val edits = EditableSaveData(
            money = 999999,
            season = "Winter",
            dayOfMonth = 10,
            year = 2
        )
        val modified = editor.applyEditsToXml(saveWithLocationSeason, edits)

        val locBlock = modified.substringAfter("<locations>").substringBefore("</locations>")
        val rootBlock = modified.substringAfter("</locations>")

        assertTrue("Location season must be preserved as summer", locBlock.contains("<currentSeason>summer</currentSeason>"))
        assertTrue("Root season must be updated to winter", rootBlock.contains("<currentSeason>winter</currentSeason>"))
        assertTrue("Root dayOfMonth must be updated to 10", rootBlock.contains("<dayOfMonth>10</dayOfMonth>"))
        assertTrue("Root year must be updated to 2", rootBlock.contains("<year>2</year>"))
    }
}
