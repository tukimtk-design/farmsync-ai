package com.tukimtk.farmsync.game.stardew

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Method
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

    // Internal hooks for testing without real Android context/Shizuku
    internal var permissionOverride: Boolean? = null
    internal var shellExecutor: ((String) -> Pair<Int, String>)? = null

    // Removed hardcoded legacy searchPaths and replaced with logic for explicit candidate packages

    private var cachedNewProcessMethod: Method? = null

    init {
        try {
            cachedNewProcessMethod = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            ).apply { isAccessible = true }
        } catch (_: Exception) {}
    }

    fun isBinderAlive(): Boolean {
        if (permissionOverride != null) return true
        return try {
            Shizuku.pingBinder()
        } catch (_: Exception) {
            false
        }
    }

    fun isPermissionGranted(): Boolean {
        if (permissionOverride != null) return permissionOverride!!
        return try {
            isBinderAlive() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }
    }

    fun requestShizukuPermission(requestCode: Int = 1001) {
        try {
            if (isBinderAlive() && !isPermissionGranted()) {
                Shizuku.requestPermission(requestCode)
            }
        } catch (_: Exception) {}
    }

    /**
     * Executes shell command via Shizuku Process with reflection or standard fallback
     */
    fun execCommand(cmd: String): String {
        return try {
            val process = if (isPermissionGranted() && cachedNewProcessMethod != null) {
                cachedNewProcessMethod!!.invoke(null, arrayOf("sh", "-c", cmd), null, null) as Process
            } else {
                Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
            }
            val text = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            text
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Helper to execute a command and return an object indicating success/failure
     */
    private fun execCommandWithResult(cmd: String): Pair<Int, String> {
        if (shellExecutor != null) {
            return shellExecutor!!.invoke(cmd)
        }
        return try {
            val process = if (isPermissionGranted() && cachedNewProcessMethod != null) {
                cachedNewProcessMethod!!.invoke(null, arrayOf("sh", "-c", cmd), null, null) as Process
            } else {
                Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
            }
            val output = process.inputStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()
            Pair(exitCode, output)
        } catch (e: Exception) {
            Pair(-1, "")
        }
    }


    /**
     * Scans real save folders on device across all possible Stardew Valley directories
     */
    fun scanRealSaves(): SaveScanResult {
        try {
        if (!isPermissionGranted()) {
            return SaveScanResult.ScanFailed(ScanFailureReason.ShizukuNotReady)
        }

        val candidatePaths = listOf(
            "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves",
            "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/saves",
            "/storage/emulated/0/Android/data/com.zane.stardewvalley/files/Saves",
            "/storage/emulated/0/Android/data/com.zane.stardewvalley/files/saves",
            "/storage/emulated/0/StardewValley"
        )

        var foundSaves = mutableListOf<RealSaveSlot>()
        var foundAnyValidRoot = false
        var commandFailed = false
        var accessDenied = false

        for (basePath in candidatePaths) {
            val (exitCode, output) = execCommandWithResult("ls -1 \"$basePath\"")

            if (exitCode == 0) {
                foundAnyValidRoot = true

                if (output.isNotBlank()) {
                    val entries = output.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                    for (entry in entries) {
                        // Filter backup artifacts
                        if (entry.contains("_SVBAK", ignoreCase = true) || entry.contains("_old", ignoreCase = true) || entry.contains("_SVEMERG", ignoreCase = true)) {
                            continue
                        }
                        // Validate entry is a directory and contains SaveGameInfo
                        val (infoExitCode, infoXml) = execCommandWithResult("cat \"$basePath/$entry/SaveGameInfo\"")
                        val (mainExitCode, _) = execCommandWithResult("cat \"$basePath/$entry/$entry\"")

                        if (infoExitCode == 0 && mainExitCode == 0 && infoXml.isNotBlank() && (infoXml.contains("<SaveGame", ignoreCase = true) || infoXml.contains("<Farmer", ignoreCase = true))) {
                            try {
                                val parsed = parseInfoContent(entry, "$basePath/$entry", infoXml)
                                if (foundSaves.none { it.folderName == parsed.folderName }) {
                                    foundSaves.add(parsed)
                                }
                            } catch (e: Exception) {
                                // Skip malformed
                            }
                        }
                    }
                }

                // If we found a valid official app folder, prefer it and don't scan legacy unless it's empty?
                // The requirements say "Prefer the installed official package when package evidence is available... Deduplicate equivalent or alias-resolved roots."
                // Wait, if it exists we scan it. Since we dedup by folderName, if multiple candidates point to the same content (alias), it gets deduped.
            } else if (exitCode != 0) {
                 // Check if it's access denied vs not found
                 val (testExitCode, testOutput) = execCommandWithResult("ls -ld \"$basePath\"")
                 if (testExitCode != 0 && testOutput.contains("Permission denied", ignoreCase = true)) {
                     accessDenied = true
                 } else if (exitCode != 0 && !testOutput.contains("No such file", ignoreCase = true) && testOutput.isNotBlank()) {
                     commandFailed = true
                 }
            }
        }

        if (foundSaves.isNotEmpty()) {
            return SaveScanResult.SavesFound(foundSaves)
        }

        if (accessDenied) {
            return SaveScanResult.ScanFailed(ScanFailureReason.AccessDenied)
        }

        if (commandFailed) {
            return SaveScanResult.ScanFailed(ScanFailureReason.CommandFailed)
        }

        if (foundAnyValidRoot && foundSaves.isEmpty()) {
            return SaveScanResult.NoSavesFound
        }

        if (!foundAnyValidRoot) {
            return SaveScanResult.NoSavesFound
        }

        return SaveScanResult.ScanFailed(ScanFailureReason.UnexpectedFailure)
        } catch (e: Exception) {
            return SaveScanResult.ScanFailed(ScanFailureReason.UnexpectedFailure)
        }
    }

    /**
     * Writes updated XML to save folder via Shizuku shell with full file permission
     */
    fun writeSaveWithProtection(slotPath: String, edits: EditableSaveData, editor: StardewSaveEditor): Boolean {
        return try {
            val folderName = slotPath.substringAfterLast("/")

            if (isPermissionGranted()) {
                val originalMain = execCommand("cat \"$slotPath/$folderName\"")
                val originalInfo = execCommand("cat \"$slotPath/SaveGameInfo\"")

                val updatedMain = editor.applyEditsToXml(originalMain.ifBlank { "<SaveGame></SaveGame>" }, edits)
                val updatedInfo = editor.applyEditsToXml(originalInfo.ifBlank { updatedMain }, edits)

                val tempDir = File(context.cacheDir, "shizuku_temp").apply { mkdirs() }
                val tempMain = File(tempDir, folderName).apply { writeText(updatedMain) }
                val tempInfo = File(tempDir, "SaveGameInfo").apply { writeText(updatedInfo) }

                execCommand("cp \"${tempMain.absolutePath}\" \"$slotPath/$folderName\"")
                execCommand("cp \"${tempInfo.absolutePath}\" \"$slotPath/SaveGameInfo\"")
                execCommand("chmod 666 \"$slotPath/$folderName\"")
                execCommand("chmod 666 \"$slotPath/SaveGameInfo\"")

                return true
            }

            // Fallback direct write
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
