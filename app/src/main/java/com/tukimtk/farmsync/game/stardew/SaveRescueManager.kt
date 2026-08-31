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
                farmName = saveFolder.name.substringBefore("_"),
                sizeBytes = targetZip.length(),
                zipFile = targetZip
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Lists all available historical backups sorted by newest first
     */
    fun listSnapshots(): List<BackupSnapshot> {
        val list = mutableListOf<BackupSnapshot>()
        val displayFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        backupVaultDir.listFiles()?.filter { it.extension.equals("zip", ignoreCase = true) }?.forEach { zip ->
            list.add(
                BackupSnapshot(
                    id = zip.nameWithoutExtension,
                    fileName = zip.name,
                    timestamp = zip.lastModified(),
                    formattedDate = displayFormat.format(Date(zip.lastModified())),
                    farmName = zip.name.substringAfter("Backup_").substringBefore("_"),
                    sizeBytes = zip.length(),
                    zipFile = zip
                )
            )
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

            val farmFolderName = snapshot.fileName.substringAfter("Backup_").substringBeforeLast("_")
            val destFolder = File(targetSaveParentDir, farmFolderName).apply { mkdirs() }

            val buffer = ByteArray(8192)
            while (entry != null) {
                val outFile = File(destFolder, entry.name)
                val fos = FileOutputStream(outFile)
                var len: Int
                while (zis.read(buffer).also { len = it } > 0) {
                    fos.write(buffer, 0, len)
                }
                fos.close()
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
