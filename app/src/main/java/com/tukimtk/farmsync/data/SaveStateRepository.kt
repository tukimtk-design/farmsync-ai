package com.tukimtk.farmsync.data

import android.content.Context
import android.content.SharedPreferences
import com.tukimtk.farmsync.game.stardew.EditableSaveData

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
}
