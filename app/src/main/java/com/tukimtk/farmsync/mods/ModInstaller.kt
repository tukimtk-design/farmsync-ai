package com.tukimtk.farmsync.mods

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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

            // 1. Get real file display name from ContentResolver
            var realFileName: String? = null
            try {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        realFileName = cursor.getString(nameIndex)
                    }
                }
            } catch (_: Exception) {}

            val cleanFileName = (realFileName ?: uri.lastPathSegment ?: "Custom_Mod")
                .substringAfterLast("/")
                .removeSuffix(".zip")
                .replace(Regex("-\\d+.*"), "") // Remove Nexus IDs like "-7286-2-5-6"
                .replace("_", " ")
                .trim()

            val modsDir = File(context.getExternalFilesDir(null), "Mods").apply { mkdirs() }

            val zipIn = ZipInputStream(inputStream)
            var entry = zipIn.nextEntry
            var fileCount = 0
            val manifestCandidates = mutableListOf<String>()

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
                        manifestCandidates.add(outFile.readText())
                    }
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
            zipIn.close()

            // 2. Parse manifest with BOM cleaning & Regex Fallback
            var modName = cleanFileName.ifBlank { "Custom Mod" }
            var author = "Unknown Author"
            var version = "1.0.0"
            var uniqueId = "mod_${cleanFileName.lowercase().replace(" ", "_")}"

            // Look through all found manifests (prioritizing main CP or largest manifest)
            for (rawManifest in manifestCandidates) {
                val parsed = parseManifest(rawManifest)
                if (parsed != null) {
                    // If modName was raw numeric ID or generic, replace with parsed
                    if (modName == cleanFileName || modName.all { it.isDigit() }) {
                        modName = parsed.name
                        author = parsed.author
                        version = parsed.version
                        uniqueId = parsed.uniqueId
                    }
                    // If we found a main Content Patcher or Expansion manifest, keep it as primary
                    if (parsed.name.contains("Ridgeside", ignoreCase = true) ||
                        parsed.name.contains("Expanded", ignoreCase = true) ||
                        !parsed.name.startsWith("[")) {
                        modName = parsed.name
                        author = parsed.author
                        version = parsed.version
                        uniqueId = parsed.uniqueId
                        break
                    }
                }
            }

            // Cleanup mod name if it has brackets like [CP] Ridgeside Village -> Ridgeside Village
            val displayModName = if (modName.startsWith("[") && modName.contains("]")) {
                modName.substringAfter("]").trim()
            } else {
                modName
            }

            ModInstallResult(
                isSuccess = true,
                uniqueId = uniqueId,
                modName = displayModName.ifBlank { cleanFileName },
                author = author,
                version = version,
                extractedFilesCount = fileCount,
                message = "Mod '$displayModName' ($version) installed successfully! ($fileCount files extracted)"
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

    private data class ParsedManifest(val name: String, val author: String, val version: String, val uniqueId: String)

    private fun parseManifest(rawJson: String): ParsedManifest? {
        val clean = rawJson.replace("\uFEFF", "")
            .replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("//.*"), "")
            .trim()

        try {
            val json = JSONObject(clean)
            val name = json.optString("Name", "")
            val author = json.optString("Author", "Unknown Author")
            val version = json.optString("Version", "1.0.0")
            val uniqueId = json.optString("UniqueID", "mod_${name.lowercase().replace(" ", "_")}")
            if (name.isNotBlank()) {
                return ParsedManifest(name, author, version, uniqueId)
            }
        } catch (_: Exception) {}

        // Fallback Regex
        val nameRegex = Regex("\"Name\"\\s*:\\s*\"([^\"]+)\"", RegexOption.IGNORE_CASE)
        val authorRegex = Regex("\"Author\"\\s*:\\s*\"([^\"]+)\"", RegexOption.IGNORE_CASE)
        val versionRegex = Regex("\"Version\"\\s*:\\s*\"([^\"]+)\"", RegexOption.IGNORE_CASE)
        val idRegex = Regex("\"UniqueID\"\\s*:\\s*\"([^\"]+)\"", RegexOption.IGNORE_CASE)

        val name = nameRegex.find(rawJson)?.groupValues?.get(1)
        if (name != null) {
            val author = authorRegex.find(rawJson)?.groupValues?.get(1) ?: "Unknown Author"
            val version = versionRegex.find(rawJson)?.groupValues?.get(1) ?: "1.0.0"
            val uniqueId = idRegex.find(rawJson)?.groupValues?.get(1) ?: "mod_${name.lowercase().replace(" ", "_")}"
            return ParsedManifest(name, author, version, uniqueId)
        }

        return null
    }
}
