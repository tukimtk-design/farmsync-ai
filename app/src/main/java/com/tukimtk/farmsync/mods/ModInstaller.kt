package com.tukimtk.farmsync.mods

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream
import org.json.JSONObject
import com.tukimtk.farmsync.game.stardew.ShizukuSaveBridge

data class ModInstallResult(
    val isSuccess: Boolean,
    val uniqueId: String,
    val modName: String,
    val author: String,
    val version: String,
    val extractedFilesCount: Int,
    val deployedFolderName: String,
    val message: String
)

fun compareVersions(v1: String, v2: String): Int {
    val regex = Regex("\\d+")
    val v1Parts = regex.findAll(v1).map { it.value.toInt() }.toList()
    val v2Parts = regex.findAll(v2).map { it.value.toInt() }.toList()
    val maxLen = maxOf(v1Parts.size, v2Parts.size)
    for (i in 0 until maxLen) {
        val p1 = v1Parts.getOrElse(i) { 0 }
        val p2 = v2Parts.getOrElse(i) { 0 }
        if (p1 != p2) return p1.compareTo(p2)
    }
    return 0
}

class ModInstaller(
    private val context: Context,
    private val bridge: ShizukuSaveBridge = ShizukuSaveBridge(context)
) {

    fun installModFromUri(uri: Uri): ModInstallResult {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                return ModInstallResult(false, "", "Unknown", "Unknown", "Unknown", 0, "", "Cannot open file stream")
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

            val stagingParent = context.getExternalFilesDir("mod_staging") ?: context.cacheDir
            val stagingDir = File(stagingParent, "mod_staging_${System.currentTimeMillis()}").apply { 
                mkdirs()
                setReadable(true, false)
                setExecutable(true, false)
            }

            val zipIn = ZipInputStream(inputStream)
            var entry = zipIn.nextEntry
            var fileCount = 0
            val manifestCandidates = mutableListOf<String>()
            val extractedManifestPaths = mutableListOf<String>()

            val buffer = ByteArray(8192)

            while (entry != null) {
                val entryName = entry.name

                // Security check for Zip Slip vulnerability
                if (entryName.contains("..")) {
                    entry = zipIn.nextEntry
                    continue
                }

                val outFile = File(stagingDir, entryName)

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
                        extractedManifestPaths.add(outFile.absolutePath)
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

            // Identify primary mod folder
            var sourceModFolder = stagingDir
            if (extractedManifestPaths.isNotEmpty()) {
                // Heuristic: shortest path to manifest is likely the main one
                val mainManifestPath = extractedManifestPaths.minByOrNull { it.length }
                if (mainManifestPath != null) {
                    sourceModFolder = File(mainManifestPath).parentFile ?: stagingDir
                }
            }

            // Fallback: If the root has no manifest, but there's exactly one folder inside, use it
            if (extractedManifestPaths.isEmpty()) {
                val subdirs = stagingDir.listFiles { f -> f.isDirectory }
                if (subdirs != null && subdirs.size == 1) {
                    sourceModFolder = subdirs[0]
                }
            }

            // Cleanup mod name if it has brackets like [CP] Ridgeside Village -> Ridgeside Village
            val displayModName = if (modName.startsWith("[") && modName.contains("]")) {
                modName.substringAfter("]").trim()
            } else {
                modName
            }
            val finalModName = displayModName.ifBlank { cleanFileName }
            
            // Generate a safe folder name
            val safeFolderName = uniqueId.replace(Regex("[^a-zA-Z0-9_\\-\\.]"), "_").ifBlank { finalModName.replace(" ", "_") }

            // Deploy via Shizuku
            if (!bridge.isPermissionGranted()) {
                stagingDir.deleteRecursively()
                return ModInstallResult(false, uniqueId, finalModName, author, version, fileCount, "", "Shizuku permission not granted. Cannot deploy mod.")
            }

            // Ensure staging files are readable by Shizuku shell
            bridge.execCommand("chmod -R 777 ${escapeShellArg(stagingDir.absolutePath)}")

            val stardewModDirs = listOf(
                "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Mods",
                "/storage/emulated/0/Android/data/com.zane.stardewvalley/files/Mods",
                "/storage/emulated/0/StardewValley/Mods"
            )

            var deployedFolder = ""
            var deployedSuccessfully = false
            var lastDeployError = ""

            for (modDir in stardewModDirs) {
                // Ensure mod dir exists
                bridge.execCommand("mkdir -p ${escapeShellArg(modDir)}")
                
                val targetPath = "$modDir/$safeFolderName"
                // Remove existing if any
                bridge.execCommand("rm -rf ${escapeShellArg(targetPath)}")
                // Copy new files
                val cpRes = bridge.execCommand("cp -r ${escapeShellArg(sourceModFolder.absolutePath)} ${escapeShellArg(targetPath)} 2>&1")
                if (cpRes.isNotBlank() && !cpRes.contains("OK")) {
                    lastDeployError = cpRes.trim()
                }

                // Grant full permissions to deployed mod
                bridge.execCommand("chmod -R 777 ${escapeShellArg(targetPath)}")
                
                // Verify copy
                val checkRes = bridge.execCommand("[ -d ${escapeShellArg(targetPath)} ] && echo OK")
                if (checkRes.contains("OK")) {
                    deployedFolder = safeFolderName
                    deployedSuccessfully = true
                }
            }
            
            // Cleanup staging
            stagingDir.deleteRecursively()

            if (deployedSuccessfully) {
                ModInstallResult(
                    isSuccess = true,
                    uniqueId = uniqueId,
                    modName = finalModName,
                    author = author,
                    version = version,
                    extractedFilesCount = fileCount,
                    deployedFolderName = deployedFolder,
                    message = "Mod '$finalModName' ($version) installed successfully! ($fileCount files extracted)"
                )
            } else {
                 ModInstallResult(
                    isSuccess = false,
                    uniqueId = uniqueId,
                    modName = finalModName,
                    author = author,
                    version = version,
                    extractedFilesCount = fileCount,
                    deployedFolderName = "",
                    message = "Failed to deploy mod via Shizuku. ${if (lastDeployError.isNotBlank()) "($lastDeployError)" else "Storage access might be restricted."}"
                )
            }
        } catch (e: Exception) {
            ModInstallResult(
                isSuccess = false,
                uniqueId = "",
                modName = "Error",
                author = "-",
                version = "-",
                extractedFilesCount = 0,
                deployedFolderName = "",
                message = "Failed to install mod: ${e.localizedMessage ?: e.message}"
            )
        }
    }

    private fun escapeShellArg(arg: String): String {
        return "'" + arg.replace("'", "'\\''") + "'"
    }

    fun toggleModState(folderName: String, isEnabled: Boolean): Boolean {
        if (folderName.isBlank()) return false
        if (!bridge.isPermissionGranted()) return false

        val stardewModDirs = listOf(
            "/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Mods",
            "/storage/emulated/0/Android/data/com.zane.stardewvalley/files/Mods"
        )
        
        var toggledAny = false
        for (modDir in stardewModDirs) {
            val enabledPath = "$modDir/$folderName"
            val disabledPath = "$modDir/$folderName.disabled"
            
            if (isEnabled) {
                val checkRes = bridge.execCommand("[ -d ${escapeShellArg(disabledPath)} ] && echo OK")
                if (checkRes.contains("OK")) {
                     bridge.execCommand("mv ${escapeShellArg(disabledPath)} ${escapeShellArg(enabledPath)}")
                     toggledAny = true
                } else {
                     val checkEn = bridge.execCommand("[ -d ${escapeShellArg(enabledPath)} ] && echo OK")
                     if (checkEn.contains("OK")) toggledAny = true // already enabled
                }
            } else {
                val checkRes = bridge.execCommand("[ -d ${escapeShellArg(enabledPath)} ] && echo OK")
                if (checkRes.contains("OK")) {
                     bridge.execCommand("mv ${escapeShellArg(enabledPath)} ${escapeShellArg(disabledPath)}")
                     toggledAny = true
                } else {
                     val checkDis = bridge.execCommand("[ -d ${escapeShellArg(disabledPath)} ] && echo OK")
                     if (checkDis.contains("OK")) toggledAny = true // already disabled
                }
            }
        }
        return toggledAny
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
