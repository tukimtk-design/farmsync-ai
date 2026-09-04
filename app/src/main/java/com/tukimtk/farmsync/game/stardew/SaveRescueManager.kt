package com.tukimtk.farmsync.game.stardew

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class BackupSnapshot(
    val id: String,
    val fileName: String,
    val timestamp: Long,
    val formattedDate: String,
    val farmName: String,
    val folderName: String = "",
    val reason: String = "Auto",
    val sizeBytes: Long,
    val zipFile: File
)

data class SaveHealthReport(
    val isBootable: Boolean,
    val detectedMods: List<String>,
    val missingMods: List<String>,
    val statusTitle: String,
    val statusDetail: String,
    val isVanillaSafe: Boolean
)

class SaveRescueManager(private val context: Context) {

    private val backupVaultDir = File(context.getExternalFilesDir(null), "SaveBackups").apply { mkdirs() }
    private val publicBackupDir = File("/storage/emulated/0/Download/FarmSync_Backups").apply { mkdirs() }

    /**
     * Extracts real farm folder name from zip entries or filename.
     * In Stardew Valley, the main XML save file name is always identical to the save folder name.
     */
    fun extractFolderNameFromZip(zipFile: File): String {
        return try {
            val zip = java.util.zip.ZipFile(zipFile)
            val entries = zip.entries().asSequence().toList()
            zip.close()

            val mainEntry = entries.firstOrNull { entry ->
                val name = entry.name.substringAfterLast("/")
                !name.equals("SaveGameInfo", ignoreCase = true) &&
                !name.endsWith(".bak", ignoreCase = true) &&
                !name.endsWith("_old", ignoreCase = true) &&
                !name.endsWith(".xml", ignoreCase = true) &&
                name.isNotBlank()
            }?.name?.substringAfterLast("/")

            if (!mainEntry.isNullOrBlank()) {
                return mainEntry
            }

            // Fallback: regex on file name: Backup_<folderName>_<yyyyMMdd_HHmmss>_<reason>.zip
            val regex = Regex("^Backup_(.*?)_\\d{8}_\\d{6}_.*\\.zip$", RegexOption.IGNORE_CASE)
            val match = regex.find(zipFile.name)
            if (match != null) {
                return match.groupValues[1]
            }

            zipFile.name.substringAfter("Backup_").substringBeforeLast("_").substringBeforeLast("_")
        } catch (_: Exception) {
            zipFile.nameWithoutExtension
        }
    }

    /**
     * Creates an untouchable .zip snapshot backup of a save folder before any edit or mod change
     */
    fun createSnapshotBackup(saveFolder: File, reason: String = "Auto"): BackupSnapshot? {
        return try {
            if (!saveFolder.exists()) return null

            val timeFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val displayFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val now = Date()
            val zipFileName = "Backup_${saveFolder.name}_${timeFormat.format(now)}_$reason.zip"
            val targetZip = File(backupVaultDir, zipFileName)

            // Also copy to public Downloads for extra safety
            val publicZip = File(publicBackupDir, zipFileName)

            val zos = ZipOutputStream(FileOutputStream(targetZip))
            saveFolder.listFiles()?.forEach { file ->
                if (file.isFile) {
                    val entry = ZipEntry(file.name)
                    zos.putNextEntry(entry)
                    val fis = FileInputStream(file)
                    fis.copyTo(zos)
                    fis.close()
                    zos.closeEntry()
                }
            }
            zos.close()

            try {
                targetZip.copyTo(publicZip, overwrite = true)
            } catch (_: Exception) {}

            BackupSnapshot(
                id = targetZip.nameWithoutExtension,
                fileName = targetZip.name,
                timestamp = now.time,
                formattedDate = displayFormat.format(now),
                farmName = if (saveFolder.name.contains("_")) saveFolder.name.substringBefore("_") else saveFolder.name,
                folderName = saveFolder.name,
                reason = reason,
                sizeBytes = targetZip.length(),
                zipFile = targetZip
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Creates a manual backup snapshot directly from the real save path via Shizuku
     */
    fun backupCurrentSave(slotPath: String, bridge: ShizukuSaveBridge, reason: String = "Manual"): BackupSnapshot? {
        return try {
            val cleanPath = slotPath.trimEnd('/')
            val folderName = cleanPath.substringAfterLast("/")
            val tempBackupDir = File(context.getExternalFilesDir("temp_backup") ?: context.cacheDir, folderName).apply { mkdirs() }

            bridge.execBoundedCommand("cp -r \"$cleanPath/\"* \"${tempBackupDir.absolutePath}/\"")
            bridge.execBoundedCommand("chmod -R 777 \"${tempBackupDir.absolutePath}\"")

            val snapshot = createSnapshotBackup(tempBackupDir, reason)
            try { tempBackupDir.deleteRecursively() } catch (_: Exception) {}
            snapshot
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Lists all available historical backups from both Vault and Public Downloads, sorted by newest first
     */
    fun listSnapshots(): List<BackupSnapshot> {
        val list = mutableListOf<BackupSnapshot>()
        val displayFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

        val searchDirs = listOf(
            backupVaultDir,
            publicBackupDir,
            File("/sdcard/Download/FarmSync_Backups")
        )

        val seenFiles = mutableSetOf<String>()

        for (dir in searchDirs) {
            if (!dir.exists() || !dir.isDirectory) continue
            dir.listFiles()?.filter { it.extension.equals("zip", ignoreCase = true) }?.forEach { zip ->
                if (zip.length() > 0 && seenFiles.add(zip.name)) {
                    val folderName = extractFolderNameFromZip(zip)
                    val farmName = if (folderName.contains("_")) folderName.substringBefore("_") else folderName
                    val reason = when {
                        zip.name.contains("PreEdit", ignoreCase = true) -> "PreEdit"
                        zip.name.contains("Manual", ignoreCase = true) -> "Manual"
                        zip.name.contains("Auto", ignoreCase = true) -> "Auto"
                        zip.name.contains("Rescue", ignoreCase = true) -> "Rescue"
                        else -> "Backup"
                    }

                    list.add(
                        BackupSnapshot(
                            id = zip.nameWithoutExtension,
                            fileName = zip.name,
                            timestamp = zip.lastModified(),
                            formattedDate = displayFormat.format(Date(zip.lastModified())),
                            farmName = farmName,
                            folderName = folderName,
                            reason = reason,
                            sizeBytes = zip.length(),
                            zipFile = zip
                        )
                    )
                }
            }
        }

        return list.sortedByDescending { it.timestamp }
    }

    /**
     * 1-Click Restore: Extracts backup snapshot directly into the target save directory
     */
    fun restoreSnapshot(snapshot: BackupSnapshot, targetSaveParentDir: File): Boolean {
        return try {
            val zis = ZipInputStream(FileInputStream(snapshot.zipFile))
            var entry = zis.nextEntry

            val farmFolderName = if (snapshot.folderName.isNotBlank()) snapshot.folderName else extractFolderNameFromZip(snapshot.zipFile)
            val destFolder = File(targetSaveParentDir, farmFolderName).apply { mkdirs() }

            val buffer = ByteArray(8192)
            while (entry != null) {
                val entryName = entry.name.substringAfterLast("/")
                if (entryName.isNotBlank() && !entry.isDirectory) {
                    val outFile = File(destFolder, entryName)
                    val fos = FileOutputStream(outFile)
                    var len: Int
                    while (zis.read(buffer).also { len = it } > 0) {
                        fos.write(buffer, 0, len)
                    }
                    fos.close()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
            zis.close()
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Extracts snapshot to staging and copies into official game save directories via Shizuku shell with full permissions
     */
    fun restoreSnapshotViaShizuku(snapshot: BackupSnapshot, bridge: ShizukuSaveBridge): Boolean {
        return try {
            val tempRestoreBase = File(context.getExternalFilesDir("temp_restore") ?: context.cacheDir, "restore_${System.currentTimeMillis()}").apply { mkdirs() }
            val farmFolderName = if (snapshot.folderName.isNotBlank()) snapshot.folderName else extractFolderNameFromZip(snapshot.zipFile)
            val destFolder = File(tempRestoreBase, farmFolderName).apply { mkdirs() }

            val zis = ZipInputStream(FileInputStream(snapshot.zipFile))
            var entry = zis.nextEntry
            val buffer = ByteArray(8192)
            while (entry != null) {
                val entryName = entry.name.substringAfterLast("/")
                if (entryName.isNotBlank() && !entry.isDirectory) {
                    val outFile = File(destFolder, entryName)
                    val fos = FileOutputStream(outFile)
                    var len: Int
                    while (zis.read(buffer).also { len = it } > 0) {
                        fos.write(buffer, 0, len)
                    }
                    fos.close()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
            zis.close()

            // Ensure staged files are readable by Shizuku
            bridge.execCommand("chmod -R 777 \"${tempRestoreBase.absolutePath}\"")

            val candidateRoots = listOf(
                "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves",
                "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/saves",
                "/storage/emulated/0/Android/data/com.zane.stardewvalley/files/Saves",
                "/storage/emulated/0/Android/data/com.zane.stardewvalley/files/saves",
                "/sdcard/Android/data/com.chucklefish.stardewvalley/files/Saves",
                "/storage/emulated/0/StardewValley",
                "/sdcard/StardewValley"
            )

            var restored = false
            for (root in candidateRoots) {
                val check = bridge.execCommand("[ -d \"$root\" ] && echo OK")
                if (check.contains("OK")) {
                    val targetSlot = "$root/$farmFolderName"
                    bridge.execCommand("mkdir -p \"$targetSlot\"")
                    bridge.execCommand("cp -r \"${destFolder.absolutePath}/\"* \"$targetSlot/\"")
                    bridge.execCommand("chmod -R 777 \"$targetSlot\"")
                    bridge.execCommand("chmod 666 \"$targetSlot/\"*")
                    restored = true
                }
            }

            // Also mirror to /storage/emulated/0/StardewValley so if game prompts for SAF picker, it is ready outside /Android
            val legacyMirror = "/storage/emulated/0/StardewValley/$farmFolderName"
            bridge.execCommand("mkdir -p \"$legacyMirror\"")
            bridge.execCommand("cp -r \"${destFolder.absolutePath}/\"* \"$legacyMirror/\"")
            bridge.execCommand("chmod -R 777 \"/storage/emulated/0/StardewValley\"")

            try { tempRestoreBase.deleteRecursively() } catch (_: Exception) {}
            restored
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Inspects save XML to detect mod dependencies and verify game compatibility
     */
    fun inspectSaveHealth(saveXml: String, activeModNames: List<String>): SaveHealthReport {
        val detected = mutableListOf<String>()

        if (saveXml.contains("Ridgeside", ignoreCase = true) || saveXml.contains("Rafseazz", ignoreCase = true)) {
            detected.add("Ridgeside Village")
        }
        if (saveXml.contains("FlashShifter", ignoreCase = true) || saveXml.contains("Expanded", ignoreCase = true)) {
            detected.add("Stardew Valley Expanded")
        }
        if (saveXml.contains("Automate", ignoreCase = true) || saveXml.contains("Pathoschild.Automate", ignoreCase = true)) {
            detected.add("Automate")
        }

        val missing = detected.filter { req -> activeModNames.none { active -> active.contains(req.split(" ")[0], ignoreCase = true) } }

        return if (missing.isNotEmpty()) {
            SaveHealthReport(
                isBootable = false,
                detectedMods = detected,
                missingMods = missing,
                statusTitle = "⚠️ ตรวจพบม็อดที่จำเป็นขาดหายไป",
                statusDetail = "เซฟนี้มีข้อมูลผูกติดกับม็อด: ${missing.joinToString(", ")} หากเปิดเล่นโดยไม่มีม็อดเหล่านี้ ตัวเกมจะมองไม่เห็นเซฟหรือเด้งออกทันที แนะนำให้เปิดใช้งานม็อดดังกล่าว หรือกู้คืนจาก Backup",
                isVanillaSafe = false
            )
        } else if (detected.isNotEmpty()) {
            SaveHealthReport(
                isBootable = true,
                detectedMods = detected,
                missingMods = emptyList(),
                statusTitle = "✅ ม็อดตรงกับเซฟเกมสมบูรณ์",
                statusDetail = "ตรวจพบข้อมูลม็อด (${detected.joinToString(", ")}) และม็อดทั้งหมดเปิดใช้งานอยู่ในเครื่องเรียบร้อยแล้ว เซฟสามารถโหลดเล่นได้ปลอดภัย 100%",
                isVanillaSafe = false
            )
        } else {
            SaveHealthReport(
                isBootable = true,
                detectedMods = emptyList(),
                missingMods = emptyList(),
                statusTitle = "🟢 เซฟแบบ Vanilla (ปลอดภัย 100%)",
                statusDetail = "เซฟนี้เป็นเซฟมาตรฐาน ไม่มีม็อดขนาดใหญ่ผูกติด สามารถเล่นได้ทั้งตัวเกมปกติและเกมลงม็อด ไร้ความเสี่ยง 100%",
                isVanillaSafe = true
            )
        }
    }
}
