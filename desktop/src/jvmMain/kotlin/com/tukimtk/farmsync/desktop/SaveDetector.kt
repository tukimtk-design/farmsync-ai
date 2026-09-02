package com.tukimtk.farmsync.desktop

import java.io.File
import java.util.Locale

object SaveDetector {
    
    fun getStardewSaveDirectory(): File? {
        val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
        val userHome = System.getProperty("user.home")

        val path = when {
            osName.contains("win") -> {
                val appData = System.getenv("APPDATA") ?: "$userHome\\AppData\\Roaming"
                "$appData\\StardewValley\\Saves"
            }
            osName.contains("mac") -> {
                "$userHome/.config/StardewValley/Saves"
            }
            osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> {
                "$userHome/.config/StardewValley/Saves"
            }
            else -> return null
        }
        
        return File(path)
    }
}
