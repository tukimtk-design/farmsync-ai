package com.tukimtk.farmsync.mods

import android.content.Context
import java.io.File
import org.json.JSONObject

data class ModPreset(
    val name: String,
    val modStates: Map<String, Boolean>
)

class ModPresetManager(private val context: Context) {

    private val presetDir: File by lazy {
        File(context.filesDir, "mod_presets").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
    }

    fun savePreset(preset: ModPreset) {
        val safeName = sanitizeFileName(preset.name)
        val file = File(presetDir, "$safeName.json")
        val jsonString = exportPresetToJson(preset)
        file.writeText(jsonString)
    }

    fun loadPreset(name: String): ModPreset? {
        val safeName = sanitizeFileName(name)
        val file = File(presetDir, "$safeName.json")
        if (!file.exists()) return null
        return try {
            val jsonString = file.readText()
            importPresetFromJson(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    fun getSavedPresets(): List<String> {
        val files = presetDir.listFiles { _, name -> name.endsWith(".json") }
        return files?.map { it.nameWithoutExtension } ?: emptyList()
    }

    fun deletePreset(name: String): Boolean {
        val safeName = sanitizeFileName(name)
        val file = File(presetDir, "$safeName.json")
        return file.delete()
    }

    fun exportPresetToJson(preset: ModPreset): String {
        val json = JSONObject()
        json.put("name", preset.name)
        val statesJson = JSONObject()
        preset.modStates.forEach { (modId, isEnabled) ->
            statesJson.put(modId, isEnabled)
        }
        json.put("modStates", statesJson)
        return json.toString(2)
    }

    fun importPresetFromJson(jsonString: String): ModPreset {
        val json = JSONObject(jsonString)
        val name = json.getString("name")
        val statesJson = json.getJSONObject("modStates")
        val modStates = mutableMapOf<String, Boolean>()
        
        val keys = statesJson.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            modStates[key] = statesJson.getBoolean(key)
        }
        
        return ModPreset(name, modStates)
    }

    fun applyPreset(presetName: String, installedMods: List<String>, installer: ModInstaller) {
        val preset = loadPreset(presetName) ?: return
        
        // Iterate through all mods managed by the preset
        for ((modId, shouldBeEnabled) in preset.modStates) {
            // Find the actual folder name among installed mods if it exists
            val folderName = installedMods.find { 
                it == modId
            } ?: modId // fallback to modId if not specifically matched
            
            installer.toggleModState(folderName, shouldBeEnabled)
        }
    }
}
