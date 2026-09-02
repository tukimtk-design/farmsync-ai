package com.tukimtk.farmsync.desktop

import java.io.File
import java.io.IOException

object SaveMetadataParser {
    
    // We can use a simple Regex parser for basic XML extraction to be fast and not rely on heavy DOM parsing.
    
    private fun extractTagContent(xml: String, tag: String): String? {
        val regex = Regex("<$tag>(.*?)</$tag>", RegexOption.IGNORE_CASE)
        val matchResult = regex.find(xml)
        return matchResult?.groupValues?.get(1)
    }

    fun parse(saveFolder: File): SaveMetadata? {
        if (!saveFolder.isDirectory) return null
        
        val folderName = saveFolder.name
        val saveGameInfoFile = File(saveFolder, "SaveGameInfo")
        
        if (!saveGameInfoFile.exists() || !saveGameInfoFile.isFile) return null
        
        return try {
            val xmlContent = saveGameInfoFile.readText()
            
            val farmerName = extractTagContent(xmlContent, "name") ?: "Unknown"
            val farmName = extractTagContent(xmlContent, "farmName") ?: "Unknown"
            val money = extractTagContent(xmlContent, "money")?.toIntOrNull() ?: 0
            val season = extractTagContent(xmlContent, "currentSeason") ?: "spring"
            val year = extractTagContent(xmlContent, "year")?.toIntOrNull() ?: 1
            
            SaveMetadata(
                folderName = folderName,
                farmerName = farmerName,
                farmName = farmName,
                money = money,
                season = season,
                year = year,
                lastModified = saveGameInfoFile.lastModified()
            )
        } catch (e: Exception) {
            null
        }
    }
    
    fun findAllSaves(savesDir: File?): List<SaveMetadata> {
        if (savesDir == null || !savesDir.exists() || !savesDir.isDirectory) {
            return emptyList()
        }
        
        val metadataList = mutableListOf<SaveMetadata>()
        
        savesDir.listFiles()?.forEach { folder ->
            if (folder.isDirectory) {
                parse(folder)?.let { metadata ->
                    metadataList.add(metadata)
                }
            }
        }
        
        // Sort by last modified descending
        return metadataList.sortedByDescending { it.lastModified }
    }
}
