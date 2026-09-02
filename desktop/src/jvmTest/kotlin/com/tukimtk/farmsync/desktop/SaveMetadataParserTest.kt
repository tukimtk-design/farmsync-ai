package com.tukimtk.farmsync.desktop

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SaveMetadataParserTest {

    @Test
    fun testParseSaveGameInfo() {
        val tempDirPath = kotlin.io.path.createTempDirectory("Saves_Test")
        val tempDir = tempDirPath.toFile()
        val saveFolder = File(tempDir, "TestFarmer_123456789")
        saveFolder.mkdir()
        
        val saveGameInfoFile = File(saveFolder, "SaveGameInfo")
        val mockXml = """
            <?xml version="1.0" encoding="utf-8"?>
            <Farmer xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
              <name>TestFarmer</name>
              <farmName>Awesome</farmName>
              <money>50000</money>
              <currentSeason>summer</currentSeason>
              <year>2</year>
            </Farmer>
        """.trimIndent()
        
        saveGameInfoFile.writeText(mockXml)
        
        val metadata = SaveMetadataParser.parse(saveFolder)
        
        assertNotNull(metadata)
        assertEquals("TestFarmer_123456789", metadata.folderName)
        assertEquals("TestFarmer", metadata.farmerName)
        assertEquals("Awesome", metadata.farmName)
        assertEquals(50000, metadata.money)
        assertEquals("summer", metadata.season)
        assertEquals(2, metadata.year)
        
        tempDir.deleteRecursively()
    }
}
