package com.tukimtk.farmsync.mods

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream
import org.json.JSONObject

data class ModInstallResult(
    val isSuccess: Boolean,
    val uniqueId: String,
    val modName: String,
    val author: String,
    val version: String,
    val extractedFilesCount: Int,
    val message: String
)

class ModInstaller(private val context: Context) {

    fun installModFromUri(uri: Uri): ModInstallResult {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                return ModInstallResult(false, "", "Unknown", "Unknown", "Unknown", 0, "Cannot open file stream")
            }

            val modsDir = File(context.getExternalFilesDir(null), "Mods").apply { mkdirs() }

            val zipIn = ZipInputStream(inputStream)
            var entry = zipIn.nextEntry
            var fileCount = 0
            var manifestContent: String? = null

            val buffer = ByteArray(8192)

            while (entry != null) {
                val entryName = entry.name
                
                // Security check for Zip Slip vulnerability
                if (entryName.contains("..")) {
                    entry = zipIn.nextEntry
                    continue
                }

                val outFile = File(modsDir, entryName)

                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    val fos = FileOutputStream(outFile)
                    var len: Int
                    while (zipIn.read(buffer).also { len = it } > 0) {
                        fos.write(buffer, 0, len)
                    }
                    fos.close()
                    fileCount++

                    if (entryName.endsWith("manifest.json", ignoreCase = true)) {
                        manifestContent = outFile.readText()
                    }
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
            zipIn.close()

            // Parse manifest if found
            var cleanFileName = (uri.lastPathSegment ?: "Mod_${System.currentTimeMillis()}")
                .substringAfterLast("/")
                .removeSuffix(".zip")
            
            var modName = cleanFileName
            var author = "Unknown Author"
            var version = "1.0.0"
            var uniqueId = "custom_${cleanFileName.lowercase().replace(" ", "_")}"

            manifestContent?.let { jsonStr ->
                try {
                    val json = JSONObject(jsonStr)
                    modName = json.optString("Name", modName)
                    author = json.optString("Author", author)
                    version = json.optString("Version", version)
                    uniqueId = json.optString("UniqueID", uniqueId)
                } catch (_: Exception) {}
            }

            ModInstallResult(
                isSuccess = true,
                uniqueId = uniqueId,
                modName = modName,
                author = author,
                version = version,
                extractedFilesCount = fileCount,
                message = "Mod '$modName' ($version) installed successfully!"
            )
        } catch (e: Exception) {
            ModInstallResult(
                isSuccess = false,
                uniqueId = "",
                modName = "Error",
                author = "-",
                version = "-",
                extractedFilesCount = 0,
                message = "Failed to install mod: ${e.localizedMessage ?: e.message}"
            )
        }
    }
}
