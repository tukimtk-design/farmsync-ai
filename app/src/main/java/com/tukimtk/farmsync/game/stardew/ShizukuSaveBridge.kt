package com.tukimtk.farmsync.game.stardew

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import rikka.shizuku.Shizuku

data class RealSaveSlot(
    val folderName: String,
    val folderPath: String,
    val farmerName: String,
    val farmName: String,
    val money: Int,
    val season: String,
    val day: Int,
    val year: Int
)

class ShizukuSaveBridge(private val context: Context) {

    private val stardewSavePath = "/storage/emulated/0/Android/data/com.zane.stardewvalley/files/saves"
    private val legacySavePath = "/storage/emulated/0/StardewValley/Saves"

    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Executes shell command via Shizuku or standard runtime fallback
     */
    fun execCommand(cmd: String): String {
        return try {
            val process = if (isShizukuAvailable()) {
                val newProcessMethod = Shizuku::class.java.getMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
                newProcessMethod.invoke(null, arrayOf("sh", "-c", cmd), null, null) as Process
            } else {
                Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
            }
            process.inputStream.bufferedReader().readText().trim()
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Scans real save folders on device via Shizuku or direct files
     */
    fun scanRealSaves(): List<RealSaveSlot> {
        val list = mutableListOf<RealSaveSlot>()

        // 1. Try Shizuku / ADB shell
        if (isShizukuAvailable()) {
            val output = execCommand("ls -1 $stardewSavePath")
            if (output.isNotBlank()) {
                val folders = output.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                for (folder in folders) {
                    val infoContent = execCommand("cat $stardewSavePath/$folder/SaveGameInfo")
                    if (infoContent.isNotBlank()) {
                        val parsed = parseInfoContent(folder, "$stardewSavePath/$folder", infoContent)
                        list.add(parsed)
                    }
                }
            }
        }

        // 2. Try legacy SDCard / non-scoped path
        val legacyDir = File(legacySavePath)
        if (legacyDir.exists() && legacyDir.isDirectory) {
            legacyDir.listFiles()?.forEach { folder ->
                if (folder.isDirectory) {
                    val infoFile = File(folder, "SaveGameInfo")
                    if (infoFile.exists()) {
                        list.add(parseInfoContent(folder.name, folder.absolutePath, infoFile.readText()))
                    }
                }
            }
        }

        return list
    }

    /**
     * Writes updated XML to save folder via Shizuku or direct file IO
     */
    fun writeSaveWithProtection(slotPath: String, edits: EditableSaveData, editor: StardewSaveEditor): Boolean {
        return try {
            if (isShizukuAvailable() && slotPath.startsWith("/storage")) {
                val folderName = slotPath.substringAfterLast("/")
                
                // Read original XML files
                val originalMain = execCommand("cat $slotPath/$folderName")
                val originalInfo = execCommand("cat $slotPath/SaveGameInfo")

                if (originalMain.isNotBlank()) {
                    val updatedMain = editor.applyEditsToXml(originalMain, edits)
                    val updatedInfo = editor.applyEditsToXml(originalInfo.ifBlank { originalMain }, edits)

                    // Write temp files to app private cache then copy via Shizuku
                    val tempDir = File(context.cacheDir, "shizuku_temp").apply { mkdirs() }
                    val tempMain = File(tempDir, folderName).apply { writeText(updatedMain) }
                    val tempInfo = File(tempDir, "SaveGameInfo").apply { writeText(updatedInfo) }

                    execCommand("cp ${tempMain.absolutePath} $slotPath/$folderName")
                    execCommand("cp ${tempInfo.absolutePath} $slotPath/SaveGameInfo")
                    execCommand("chmod 666 $slotPath/$folderName $slotPath/SaveGameInfo")

                    return true
                }
            }

            // Direct File write fallback
            val targetFolder = File(slotPath)
            if (targetFolder.exists() && targetFolder.isDirectory) {
                return editor.saveToDirectory(targetFolder, edits)
            }

            false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Modifies save files directly inside a SAF DocumentFile folder
     */
    fun writeToDocumentTree(treeUri: Uri, edits: EditableSaveData, editor: StardewSaveEditor): Boolean {
        return try {
            val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return false
            val saveGameInfoDoc = rootDoc.findFile("SaveGameInfo")
            val mainSaveDoc = rootDoc.listFiles().find { !it.name.isNullOrBlank() && !it.name!!.startsWith("SaveGameInfo") && !it.name!!.endsWith(".bak") }

            mainSaveDoc?.let { doc ->
                val input: InputStream? = context.contentResolver.openInputStream(doc.uri)
                val originalXml = input?.bufferedReader()?.readText() ?: ""
                input?.close()

                if (originalXml.isNotBlank()) {
                    val updatedXml = editor.applyEditsToXml(originalXml, edits)
                    val output: OutputStream? = context.contentResolver.openOutputStream(doc.uri, "wt")
                    output?.write(updatedXml.toByteArray())
                    output?.close()
                }
            }

            saveGameInfoDoc?.let { doc ->
                val input: InputStream? = context.contentResolver.openInputStream(doc.uri)
                val originalXml = input?.bufferedReader()?.readText() ?: ""
                input?.close()

                if (originalXml.isNotBlank()) {
                    val updatedXml = editor.applyEditsToXml(originalXml, edits)
                    val output: OutputStream? = context.contentResolver.openOutputStream(doc.uri, "wt")
                    output?.write(updatedXml.toByteArray())
                    output?.close()
                }
            }

            true
        } catch (_: Exception) {
            false
        }
    }

    private fun parseInfoContent(folderName: String, folderPath: String, xml: String): RealSaveSlot {
        val nameRegex = Regex("<name>(.*?)</name>", RegexOption.IGNORE_CASE)
        val farmRegex = Regex("<farmName>(.*?)</farmName>", RegexOption.IGNORE_CASE)
        val moneyRegex = Regex("<money>(.*?)</money>", RegexOption.IGNORE_CASE)
        val seasonRegex = Regex("<currentSeason>(.*?)</currentSeason>", RegexOption.IGNORE_CASE)
        val dayRegex = Regex("<dayOfMonth>(.*?)</dayOfMonth>", RegexOption.IGNORE_CASE)
        val yearRegex = Regex("<year>(.*?)</year>", RegexOption.IGNORE_CASE)

        val name = nameRegex.find(xml)?.groupValues?.get(1) ?: folderName.substringBefore("_")
        val farm = farmRegex.find(xml)?.groupValues?.get(1) ?: "Farm"
        val money = moneyRegex.find(xml)?.groupValues?.get(1)?.toIntOrNull() ?: 500
        val season = seasonRegex.find(xml)?.groupValues?.get(1)?.replaceFirstChar { it.uppercase() } ?: "Spring"
        val day = dayRegex.find(xml)?.groupValues?.get(1)?.toIntOrNull() ?: 1
        val year = yearRegex.find(xml)?.groupValues?.get(1)?.toIntOrNull() ?: 1

        return RealSaveSlot(
            folderName = folderName,
            folderPath = folderPath,
            farmerName = name,
            farmName = farm,
            money = money,
            season = season,
            day = day,
            year = year
        )
    }
}
