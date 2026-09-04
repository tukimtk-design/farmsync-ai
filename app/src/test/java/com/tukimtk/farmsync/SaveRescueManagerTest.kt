package com.tukimtk.farmsync

import com.tukimtk.farmsync.game.stardew.SaveRescueManager
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class SaveRescueManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `test extractFolderNameFromZip correctly parses folder name with underscore timestamps`() {
        val dummyContext = android.content.ContextWrapper(null)
        val rescueManager = SaveRescueManager(dummyContext)

        // 1. Create a zip with SaveGameInfo and FarmTuki_447782319 entry
        val zipFile = File(tempFolder.root, "Backup_FarmTuki_447782319_20260904_183015_PreEdit.zip")
        val zos = ZipOutputStream(FileOutputStream(zipFile))
        zos.putNextEntry(ZipEntry("SaveGameInfo"))
        zos.write("<SaveGame></SaveGame>".toByteArray())
        zos.closeEntry()
        zos.putNextEntry(ZipEntry("FarmTuki_447782319"))
        zos.write("<SaveGame></SaveGame>".toByteArray())
        zos.closeEntry()
        zos.close()

        val extractedName = rescueManager.extractFolderNameFromZip(zipFile)
        assertEquals("FarmTuki_447782319", extractedName)
    }

    @Test
    fun `test extractFolderNameFromZip fallback regex when zip has no entries`() {
        val dummyContext = android.content.ContextWrapper(null)
        val rescueManager = SaveRescueManager(dummyContext)

        // Empty zip
        val zipFile = File(tempFolder.root, "Backup_MyFarm_987654321_20260904_120000_Manual.zip")
        val zos = ZipOutputStream(FileOutputStream(zipFile))
        zos.close()

        val extractedName = rescueManager.extractFolderNameFromZip(zipFile)
        assertEquals("MyFarm_987654321", extractedName)
    }
}
