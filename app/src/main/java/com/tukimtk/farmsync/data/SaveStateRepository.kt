package com.tukimtk.farmsync.data

import android.content.Context
import android.content.SharedPreferences
import com.tukimtk.farmsync.game.stardew.EditableSaveData
import org.json.JSONArray
import org.json.JSONObject

data class PersistedMod(
    val id: String,
    val uniqueId: String,
    val name: String,
    val author: String,
    val version: String,
    val isEnabled: Boolean,
    val folderName: String = ""
)

class SaveStateRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("farmsync_save_data", Context.MODE_PRIVATE)

    fun loadSaveData(): EditableSaveData {
        return EditableSaveData(
            characterName = prefs.getString("farmer_name", "Tuki") ?: "Tuki",
            farmName = prefs.getString("farm_name", "Sunrise Peak") ?: "Sunrise Peak",
            money = prefs.getInt("money", 184500),
            season = prefs.getString("season", "Summer") ?: "Summer",
            dayOfMonth = prefs.getInt("day", 14),
            year = prefs.getInt("year", 2),
            maxHealth = prefs.getInt("max_health", 100),
            maxStamina = prefs.getInt("max_stamina", 270),
            maxBackpackSlots = prefs.getInt("backpack_slots", 36)
        )
    }

    fun persistSaveData(data: EditableSaveData) {
        prefs.edit().apply {
            putString("farmer_name", data.characterName)
            putString("farm_name", data.farmName)
            putInt("money", data.money)
            putString("season", data.season)
            putInt("day", data.dayOfMonth)
            putInt("year", data.year)
            putInt("max_health", data.maxHealth)
            putInt("max_stamina", data.maxStamina)
            putInt("backpack_slots", data.maxBackpackSlots)
            apply()
        }
    }

    fun getSelectedStorage(): String {
        return prefs.getString("selected_storage", "Local Wi-Fi SMB (1 Gbps Direct)") ?: "Local Wi-Fi SMB (1 Gbps Direct)"
    }

    fun setSelectedStorage(storage: String) {
        prefs.edit().putString("selected_storage", storage).apply()
    }

    fun loadInstalledMods(): List<PersistedMod> {
        val jsonStr = prefs.getString("installed_mods_json", null)
        if (jsonStr.isNullOrBlank()) {
            return listOf(
                PersistedMod("sve", "FlashShifter.StardewValleyExpandedCP", "Stardew Valley Expanded", "FlashShifter", "v1.14.24", true, "StardewValleyExpanded"),
                PersistedMod("ui_info", "Annosz.UIInfoSuite2", "UI Info Suite 2", "Annosz", "v2.3.3", true, "UIInfoSuite2"),
                PersistedMod("thai_ai", "FarmSync.ThaiAI", "ม็อดแปลบทสนทนาภาษาไทย (AI BYOK)", "FarmSync AI", "v1.0.0", true, "FarmSyncThaiAI")
            )
        }

        val list = mutableListOf<PersistedMod>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    PersistedMod(
                        id = obj.getString("id"),
                        uniqueId = obj.optString("uniqueId", ""),
                        name = obj.getString("name"),
                        author = obj.getString("author"),
                        version = obj.getString("version"),
                        isEnabled = obj.optBoolean("isEnabled", true),
                        folderName = obj.optString("folderName", "")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun saveInstalledMods(mods: List<PersistedMod>) {
        val arr = JSONArray()
        mods.forEach { mod ->
            val obj = JSONObject().apply {
                put("id", mod.id)
                put("uniqueId", mod.uniqueId)
                put("name", mod.name)
                put("author", mod.author)
                put("version", mod.version)
                put("isEnabled", mod.isEnabled)
                put("folderName", mod.folderName)
            }
            arr.put(obj)
        }
        prefs.edit().putString("installed_mods_json", arr.toString()).apply()
    }
}
