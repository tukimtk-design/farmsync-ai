package com.tukimtk.farmsync.mods

import org.json.JSONObject

sealed class CompatibilityVerdict {
    object Compatible : CompatibilityVerdict()
    data class NeedsDependency(val missingList: List<String>) : CompatibilityVerdict()
    data class Incompatible(val reason: String) : CompatibilityVerdict()
    object Unknown : CompatibilityVerdict()
}

class ModCompatibilityChecker {
    private val knownIncompatibleMods = setOf(
        "com.example.pcharmony",
        "raw.pc.harmony.mod",
        "some.incompatible.mod"
    )

    fun checkCompatibility(manifestJson: String, installedMods: List<String>, smapiApiVersion: String? = null): CompatibilityVerdict {
        if (manifestJson.isBlank()) return CompatibilityVerdict.Unknown

        val jsonObject: JSONObject
        try {
            jsonObject = JSONObject(manifestJson)
        } catch (e: Exception) {
            return CompatibilityVerdict.Unknown
        }

        // Extract UniqueID
        val uniqueId = jsonObject.optString("UniqueID", "")
        if (uniqueId.isNotEmpty() && isKnownIncompatible(uniqueId)) {
            return CompatibilityVerdict.Incompatible("Known incompatible C# mod requiring raw PC Harmony patches without SMAPI Android support.")
        }
        
        // MinimumApiVersion
        val minApiVersion = jsonObject.optString("MinimumApiVersion", "")
        if (minApiVersion.isNotEmpty() && smapiApiVersion != null) {
            if (!isVersionCompatible(smapiApiVersion, minApiVersion)) {
                return CompatibilityVerdict.Incompatible("Requires SMAPI $minApiVersion but installed is $smapiApiVersion.")
            }
        }

        val missingDependencies = mutableListOf<String>()

        // Parse ContentPackFor
        val contentPackFor = jsonObject.optJSONObject("ContentPackFor")
        if (contentPackFor != null) {
            val contentPackForId = contentPackFor.optString("UniqueID", "")
            if (contentPackForId.isNotEmpty() && !installedMods.contains(contentPackForId)) {
                missingDependencies.add(contentPackForId)
            }
        }

        // Parse Dependencies
        val dependencies = jsonObject.optJSONArray("Dependencies")
        if (dependencies != null) {
            for (i in 0 until dependencies.length()) {
                val depObj = dependencies.optJSONObject(i) ?: continue
                val depId = depObj.optString("UniqueID", "")
                
                // Defaults to true if not present or incorrectly formatted
                val isRequired = if (depObj.has("IsRequired")) depObj.optBoolean("IsRequired", true) else true

                if (depId.isNotEmpty() && isRequired && !installedMods.contains(depId)) {
                    missingDependencies.add(depId)
                }
            }
        }

        if (missingDependencies.isNotEmpty()) {
            return CompatibilityVerdict.NeedsDependency(missingDependencies.distinct())
        }

        return CompatibilityVerdict.Compatible
    }

    private fun isKnownIncompatible(uniqueId: String): Boolean {
        return knownIncompatibleMods.contains(uniqueId)
    }
    
    private fun isVersionCompatible(installedVersion: String, minVersion: String): Boolean {
        val installedParts = installedVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val minParts = minVersion.split(".").map { it.toIntOrNull() ?: 0 }
        
        val maxLen = maxOf(installedParts.size, minParts.size)
        for (i in 0 until maxLen) {
            val instPart = installedParts.getOrElse(i) { 0 }
            val minPart = minParts.getOrElse(i) { 0 }
            
            if (instPart > minPart) return true
            if (instPart < minPart) return false
        }
        
        return true
    }
}
