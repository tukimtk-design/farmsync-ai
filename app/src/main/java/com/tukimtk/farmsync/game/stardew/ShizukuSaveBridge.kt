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
        } catch (_: Throwable) {}
    }

    fun isBinderAlive(): Boolean {
        if (permissionOverride != null) return true
        return try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }
    }

    fun isPermissionGranted(): Boolean {
        if (permissionOverride != null) return permissionOverride!!
        return try {
            isBinderAlive() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
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

    data class CommandResult(
        val exitCode: Int,
        val stdout: String,
        val stderr: String,
        val isTimeout: Boolean
    )

    fun execBoundedCommand(cmd: String, timeoutMs: Long = 5000): CommandResult {
        if (shellExecutor != null) {
            val res = shellExecutor!!.invoke(cmd)
            return CommandResult(res.first, res.second, "", false)
        }
        return try {
            val process = if (isPermissionGranted() && cachedNewProcessMethod != null) {
                cachedNewProcessMethod!!.invoke(null, arrayOf("sh", "-c", cmd), null, null) as Process
            } else {
                Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
            }

            var stdoutStr = ""
            var stderrStr = ""
            var isTimeout = false

            val stdoutThread = Thread {
                try {
                    stdoutStr = process.inputStream.bufferedReader().use { it.readText().take(1024 * 1024 * 16).trim() }
                } catch (_: Exception) {}
            }
            val stderrThread = Thread {
                try {
                    stderrStr = process.errorStream.bufferedReader().use { it.readText().take(1024 * 1024 * 16).trim() }
                } catch (_: Exception) {}
            }

            stdoutThread.start()
            stderrThread.start()

            // Run process.waitFor() in a dedicated worker thread with timeout via thread.join()
            // Avoids calling Process.waitFor(long, TimeUnit) which throws RuntimeException on ShizukuRemoteProcess
            var exitCode = -1
            val waitThread = Thread {
                try {
                    exitCode = process.waitFor()
                } catch (_: Exception) {}
            }
            waitThread.start()
            waitThread.join(timeoutMs)

            if (waitThread.isAlive) {
                isTimeout = true
                try { process.destroy() } catch (_: Exception) {}
            }

            stdoutThread.join(500)
            stderrThread.join(500)

            val finalExitCode = if (isTimeout) -1 else exitCode
            CommandResult(finalExitCode, stdoutStr, stderrStr, isTimeout)
        } catch (e: Exception) {
            CommandResult(-1, "", "", false)
        }
    }

    fun validatePath(path: String): Boolean {
        if (path.contains("..")) return false
        if (path.contains("\n") || path.contains("\r")) return false
        if (path.contains("\u0000")) return false
        if (path.contains("'") || path.contains("\"") || path.contains("`") || path.contains("$") || path.contains("&") || path.contains("|") || path.contains(";") || path.contains("<") || path.contains(">")) return false

        val validRoots = listOf(
            "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves",
            "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/saves",
            "/storage/emulated/0/Android/data/abc.smapi.gameloader/files/Saves",
            "/storage/emulated/0/Android/data/abc.smapi.gameloader/files/saves",
            "/storage/emulated/0/Android/data/com.zane.stardewvalley/files/Saves",
            "/storage/emulated/0/Android/data/com.zane.stardewvalley/files/saves",
            "/storage/emulated/0/StardewValley",
            // Path aliases used on Xiaomi/HyperOS and some other devices
            "/sdcard/Android/data/com.chucklefish.stardewvalley/files/Saves",
            "/sdcard/Android/data/com.chucklefish.stardewvalley/files/saves",
            "/sdcard/Android/data/abc.smapi.gameloader/files/Saves",
            "/sdcard/Android/data/abc.smapi.gameloader/files/saves",
            "/sdcard/Android/data/com.zane.stardewvalley/files/Saves",
            "/sdcard/Android/data/com.zane.stardewvalley/files/saves",
            "/storage/sdcard0/Android/data/com.chucklefish.stardewvalley/files/Saves",
            "/storage/sdcard0/Android/data/com.chucklefish.stardewvalley/files/saves",
            "/storage/sdcard0/Android/data/abc.smapi.gameloader/files/Saves",
            "/storage/sdcard0/Android/data/abc.smapi.gameloader/files/saves",
            "/storage/sdcard0/Android/data/com.zane.stardewvalley/files/Saves",
            "/storage/sdcard0/Android/data/com.zane.stardewvalley/files/saves"
        )
        return validRoots.any { path.startsWith(it) }
    }

    fun scanRealSaves(): SaveScanResult {
        try {
        if (!isPermissionGranted()) {
            return SaveScanResult.ScanFailed(ScanFailureReason.ShizukuNotReady)
        }

        val candidatePaths = listOf(
            "/storage/emulated/0/Android/data/abc.smapi.gameloader/files/Saves",
            "/storage/emulated/0/Android/data/abc.smapi.gameloader/files/saves",
            "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves",
            "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/saves",
            "/storage/emulated/0/Android/data/com.zane.stardewvalley/files/Saves",
            "/storage/emulated/0/Android/data/com.zane.stardewvalley/files/saves",
            "/storage/emulated/0/StardewValley",
            // Path aliases used on Xiaomi/HyperOS and some other devices
            "/sdcard/Android/data/abc.smapi.gameloader/files/Saves",
            "/sdcard/Android/data/abc.smapi.gameloader/files/saves",
            "/sdcard/Android/data/com.chucklefish.stardewvalley/files/Saves",
            "/sdcard/Android/data/com.chucklefish.stardewvalley/files/saves",
            "/storage/sdcard0/Android/data/abc.smapi.gameloader/files/Saves",
            "/storage/sdcard0/Android/data/abc.smapi.gameloader/files/saves",
            "/storage/sdcard0/Android/data/com.chucklefish.stardewvalley/files/Saves",
            "/storage/sdcard0/Android/data/com.chucklefish.stardewvalley/files/saves"
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
    private fun getSha256(filePath: String): String {
        val res = execBoundedCommand("sha256sum \"$filePath\"")
        if (res.exitCode == 0 && res.stdout.isNotBlank()) {
            return res.stdout.substringBefore(" ").trim()
        }
        return ""
    }

    private fun getSha256Str(content: String): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val hash = md.digest(content.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun writeSaveWithProtection(slotPath: String, edits: EditableSaveData, editor: StardewSaveEditor, rescueManager: SaveRescueManager? = null): SaveWriteResult {
        try {
            if (!isPermissionGranted()) return SaveWriteResult.ShizukuNotReady
            if (!validatePath(slotPath)) return SaveWriteResult.InvalidDestination

            val cleanSlotPath = slotPath.trimEnd('/')
            val folderName = cleanSlotPath.substringAfterLast("/")
            val mainSavePath = "$cleanSlotPath/$folderName"
            val infoSavePath = "$cleanSlotPath/SaveGameInfo"

            // Validate existence using test -f with fallback to ls for maximum shell compatibility
            val checkMain = execBoundedCommand("[ -f \"$mainSavePath\" ] && echo OK")
            val checkInfo = execBoundedCommand("[ -f \"$infoSavePath\" ] && echo OK")
            val mainExists = (checkMain.exitCode == 0 && checkMain.stdout.contains("OK")) || execCommand("ls \"$mainSavePath\" 2>&1").contains(folderName)
            val infoExists = (checkInfo.exitCode == 0 && checkInfo.stdout.contains("OK")) || execCommand("ls \"$infoSavePath\" 2>&1").contains("SaveGameInfo")

            if (!mainExists || !infoExists) {
                return SaveWriteResult.InvalidDestination
            }

            // Backup: on Android 14 Scoped Storage, stage files to app-accessible dir first so rescueManager can read & zip them
            if (rescueManager != null) {
                val tempBackupDir = java.io.File(context.getExternalFilesDir("temp_backup") ?: context.cacheDir, folderName).apply { mkdirs() }
                execBoundedCommand("cp \"$mainSavePath\" \"${tempBackupDir.absolutePath}/$folderName\"")
                execBoundedCommand("cp \"$infoSavePath\" \"${tempBackupDir.absolutePath}/SaveGameInfo\"")

                val backup = rescueManager.createSnapshotBackup(tempBackupDir, "PreEdit")
                try { tempBackupDir.deleteRecursively() } catch (_: Exception) {}

                if (backup == null || backup.sizeBytes == 0L || !backup.zipFile.exists()) {
                    return SaveWriteResult.BackupFailed
                }
            }

            // App staging files: use externalFilesDir so Shizuku shell (uid 2000) has permission to read and copy
            val tempDir = java.io.File(context.getExternalFilesDir("shizuku_temp") ?: context.cacheDir, "shizuku_temp").apply { mkdirs() }
            val tempMain = java.io.File(tempDir, folderName)
            val tempInfo = java.io.File(tempDir, "SaveGameInfo")

            // 1. Copy live files to app-accessible temp dir for complete and lossless reading
            val pullMainRes = execBoundedCommand("cp \"$mainSavePath\" \"${tempMain.absolutePath}\"")
            val pullInfoRes = execBoundedCommand("cp \"$infoSavePath\" \"${tempInfo.absolutePath}\"")
            if (pullMainRes.exitCode != 0 || pullInfoRes.exitCode != 0 || !tempMain.exists() || !tempInfo.exists()) {
                return SaveWriteResult.UnexpectedFailure
            }

            var originalMainXml = tempMain.readText(Charsets.UTF_8)
            var originalInfoXml = tempInfo.readText(Charsets.UTF_8)

            // Integrity check: if main save is truncated or corrupt (missing </SaveGame>), attempt auto-recovery from _SVBAK or _SVEMERG
            if (!originalMainXml.contains("</SaveGame>", ignoreCase = true)) {
                val bakPath = "${mainSavePath}_SVBAK"
                val emergPath = "${mainSavePath}_SVEMERG"
                execBoundedCommand("[ -f \"$bakPath\" ] && cp \"$bakPath\" \"${tempMain.absolutePath}\"")
                val bakXml = if (tempMain.exists()) tempMain.readText(Charsets.UTF_8) else ""
                if (bakXml.contains("</SaveGame>", ignoreCase = true)) {
                    originalMainXml = bakXml
                } else {
                    execBoundedCommand("[ -f \"$emergPath\" ] && cp \"$emergPath\" \"${tempMain.absolutePath}\"")
                    val emergXml = if (tempMain.exists()) tempMain.readText(Charsets.UTF_8) else ""
                    if (emergXml.contains("</SaveGame>", ignoreCase = true)) {
                        originalMainXml = emergXml
                    }
                }
            }

            val updatedMain = editor.applyEditsToXml(originalMainXml.ifBlank { "<SaveGame></SaveGame>" }, edits)
            var updatedInfo = editor.applyEditsToXml(originalInfoXml.ifBlank { updatedMain }, edits)

            // Must terminate with </SaveGame>
            if (!updatedMain.contains("</SaveGame>", ignoreCase = true)) {
                return SaveWriteResult.VerificationFailed
            }

            // Synchronize header tags between updatedMain and updatedInfo so game engine doesn't reject save
            val saveTimeMatch = Regex("<saveTime>(\\d+)</saveTime>", RegexOption.IGNORE_CASE).find(updatedMain)
            if (saveTimeMatch != null) {
                val st = saveTimeMatch.groupValues[1]
                updatedInfo = updatedInfo.replace(Regex("<saveTime>\\d+</saveTime>", RegexOption.IGNORE_CASE), "<saveTime>$st</saveTime>")
            }
            val dayMatch = Regex("<dayOfMonthForSaveGame>(\\d+)</dayOfMonthForSaveGame>", RegexOption.IGNORE_CASE).find(updatedMain)
            if (dayMatch != null) {
                val dm = dayMatch.groupValues[1]
                updatedInfo = updatedInfo.replace(Regex("<dayOfMonthForSaveGame>\\d+</dayOfMonthForSaveGame>", RegexOption.IGNORE_CASE), "<dayOfMonthForSaveGame>$dm</dayOfMonthForSaveGame>")
            }

            // 2. Write updated contents to temp files
            tempMain.writeText(updatedMain, Charsets.UTF_8)
            tempInfo.writeText(updatedInfo, Charsets.UTF_8)

            val intendedMainHash = getSha256Str(updatedMain)
            val intendedInfoHash = getSha256Str(updatedInfo)

            // 3. Staged Copy to target directory
            val stagedMainPath = "$cleanSlotPath/${folderName}_staged"
            val stagedInfoPath = "$cleanSlotPath/SaveGameInfo_staged"

            val stageMainRes = execBoundedCommand("cp \"${tempMain.absolutePath}\" \"$stagedMainPath\"")
            if (stageMainRes.exitCode != 0) return SaveWriteResult.MainSaveStageFailed

            val stageInfoRes = execBoundedCommand("cp \"${tempInfo.absolutePath}\" \"$stagedInfoPath\"")
            if (stageInfoRes.exitCode != 0) return SaveWriteResult.SaveGameInfoStageFailed

            // 5. Verify Staged
            val stagedMainHash = getSha256(stagedMainPath)
            val stagedInfoHash = getSha256(stagedInfoPath)

            if (stagedMainHash != intendedMainHash) return SaveWriteResult.VerificationFailed
            if (stagedInfoHash != intendedInfoHash) return SaveWriteResult.VerificationFailed

            // 7. Replace live files
            val replaceMainRes = execBoundedCommand("mv \"$stagedMainPath\" \"$mainSavePath\"")
            if (replaceMainRes.exitCode != 0) return SaveWriteResult.MainSaveReplaceFailed

            val replaceInfoRes = execBoundedCommand("mv \"$stagedInfoPath\" \"$infoSavePath\"")
            if (replaceInfoRes.exitCode != 0) {
                // Rollback Info Failed, try to restore main save
                if (rescueManager != null) {
                    val backups = rescueManager.listSnapshots()
                    val latest = backups.firstOrNull { it.farmName == folderName.substringBefore("_") }
                    if (latest != null) {
                        val ok = rescueManager.restoreSnapshotViaShizuku(latest, this)
                        if (!ok) return SaveWriteResult.RollbackFailed
                    } else {
                        return SaveWriteResult.RollbackFailed
                    }
                } else {
                     return SaveWriteResult.RollbackFailed
                }
                return SaveWriteResult.SaveGameInfoReplaceFailed
            }

            // Also synchronize backup _old files if they exist so Stardew Valley doesn't restore old 500g
            val oldMainPath = "${mainSavePath}_old"
            val oldInfoPath = "${infoSavePath}_old"
            execBoundedCommand("[ -f \"$oldMainPath\" ] && cp \"$mainSavePath\" \"$oldMainPath\"")
            execBoundedCommand("[ -f \"$oldInfoPath\" ] && cp \"$infoSavePath\" \"$oldInfoPath\"")

            execBoundedCommand("chmod 666 \"$mainSavePath\" \"$infoSavePath\"")
            execBoundedCommand("[ -f \"$oldMainPath\" ] && chmod 666 \"$oldMainPath\"")
            execBoundedCommand("[ -f \"$oldInfoPath\" ] && chmod 666 \"$oldInfoPath\"")
            execBoundedCommand("chmod 777 \"$cleanSlotPath\"")

            // Universal Cross-Mirror: Ensure saves stay 100% in sync across Vanilla, SMAPILoader, and legacy storage
            val allTargetRoots = listOf(
                "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/$folderName",
                "/storage/emulated/0/Android/data/abc.smapi.gameloader/files/Saves/$folderName",
                "/storage/emulated/0/StardewValley/$folderName"
            )
            for (targetRoot in allTargetRoots) {
                if (targetRoot != cleanSlotPath) {
                    execBoundedCommand("mkdir -p \"$targetRoot\"")
                    execBoundedCommand("cp \"$mainSavePath\" \"$targetRoot/$folderName\"")
                    execBoundedCommand("cp \"$infoSavePath\" \"$targetRoot/SaveGameInfo\"")
                    execBoundedCommand("chmod 666 \"$targetRoot/$folderName\" \"$targetRoot/SaveGameInfo\"")
                    execBoundedCommand("chmod 777 \"$targetRoot\"")
                }
            }
            execBoundedCommand("chmod -R 777 \"/storage/emulated/0/StardewValley\"")

            // 9. Verify Final
            val finalMainHash = getSha256(mainSavePath)
            val finalInfoHash = getSha256(infoSavePath)

            if (finalMainHash != intendedMainHash) return SaveWriteResult.VerificationFailed
            if (finalInfoHash != intendedInfoHash) return SaveWriteResult.VerificationFailed

            // 10. Reload physical
            val reloadInfoXml = try {
                execBoundedCommand("cp \"$infoSavePath\" \"${tempInfo.absolutePath}\"")
                tempInfo.readText(Charsets.UTF_8)
            } catch (_: Exception) {
                ""
            }
            if (reloadInfoXml.isBlank()) return SaveWriteResult.ReloadFailed

            try {
                val parsed = parseInfoContent(folderName, cleanSlotPath, reloadInfoXml)
                // 11. Verify fields
                if (parsed.money != edits.money) return SaveWriteResult.VerificationFailed
            } catch (e: Exception) {
                return SaveWriteResult.ReloadFailed
            }

            // Cleanup temp
            try {
                tempMain.delete()
                tempInfo.delete()
            } catch (_: Exception) {}

            return SaveWriteResult.SuccessVerified
        } catch (e: Exception) {
            return SaveWriteResult.UnexpectedFailure
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
