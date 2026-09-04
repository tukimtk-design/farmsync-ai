package com.tukimtk.farmsync.game.stardew

import java.io.File

data class EditableSaveData(
    var characterName: String = "Tuki",
    var farmName: String = "Sunrise Peak",
    var money: Int = 184500,
    var season: String = "Summer",
    var dayOfMonth: Int = 14,
    var year: Int = 2,
    var maxHealth: Int = 100,
    var maxStamina: Int = 270,
    var maxBackpackSlots: Int = 36
)

class StardewSaveEditor {

    /**
     * Safely applies edits to Stardew Valley XML without corrupting NPC, Animal, or Item names.
     * Only modifies tags inside <player> block and root timeline tags.
     */
    fun applyEditsToXml(originalXml: String, edits: EditableSaveData): String {
        var result = originalXml

        // 1. Process <player> block specifically
        val playerStartTag = "<player>"
        val playerEndTag = "</player>"
        val playerStartIndex = result.indexOf(playerStartTag, ignoreCase = true)
        val playerEndIndex = result.indexOf(playerEndTag, ignoreCase = true)

        if (playerStartIndex != -1 && playerEndIndex != -1 && playerEndIndex > playerStartIndex) {
            val beforePlayer = result.substring(0, playerStartIndex + playerStartTag.length)
            val playerContent = result.substring(playerStartIndex + playerStartTag.length, playerEndIndex)
            val afterPlayer = result.substring(playerEndIndex)

            var modifiedPlayer = playerContent

            // Modify player tags strictly inside <player>
            modifiedPlayer = replaceFirstTag(modifiedPlayer, "name", edits.characterName)
            modifiedPlayer = replaceFirstTag(modifiedPlayer, "farmName", edits.farmName)
            modifiedPlayer = replaceFirstTag(modifiedPlayer, "money", edits.money.toString())
            modifiedPlayer = replaceFirstTag(modifiedPlayer, "maxHealth", edits.maxHealth.toString())
            modifiedPlayer = replaceFirstTag(modifiedPlayer, "maxStamina", edits.maxStamina.toString())

            // Ensure totalMoneyEarned is at least equal to current money
            val totalMoneyRegex = Regex("<totalMoneyEarned>(\\d+)</totalMoneyEarned>", RegexOption.IGNORE_CASE)
            val currentTotal = totalMoneyRegex.find(modifiedPlayer)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            if (edits.money > currentTotal) {
                modifiedPlayer = replaceFirstTag(modifiedPlayer, "totalMoneyEarned", (edits.money + 50000).toString())
            }

            result = beforePlayer + modifiedPlayer + afterPlayer

            // 1.1 Process <farmerTeam> block specifically (Stardew Valley 1.5/1.6 Shared Wallet)
            val teamStartTag = "<farmerTeam"
            val teamEndTag = "</farmerTeam>"
            val teamStartIndex = result.indexOf(teamStartTag, ignoreCase = true)
            val teamEndIndex = result.indexOf(teamEndTag, ignoreCase = true)

            if (teamStartIndex != -1 && teamEndIndex != -1 && teamEndIndex > teamStartIndex) {
                // Handle both <farmerTeam> and <farmerTeam xsi:type="...">
                val tagCloseIndex = result.indexOf(">", teamStartIndex)
                if (tagCloseIndex != -1 && tagCloseIndex < teamEndIndex) {
                    val beforeTeam = result.substring(0, tagCloseIndex + 1)
                    val teamContent = result.substring(tagCloseIndex + 1, teamEndIndex)
                    val afterTeam = result.substring(teamEndIndex)

                    var modifiedTeam = teamContent
                    modifiedTeam = replaceFirstTag(modifiedTeam, "money", edits.money.toString())

                    val teamTotalMoneyRegex = Regex("<totalMoneyEarned>(\\d+)</totalMoneyEarned>", RegexOption.IGNORE_CASE)
                    val teamCurrentTotal = teamTotalMoneyRegex.find(modifiedTeam)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                    if (edits.money > teamCurrentTotal) {
                        modifiedTeam = replaceFirstTag(modifiedTeam, "totalMoneyEarned", (edits.money + 50000).toString())
                    }

                    // Update individualMoney in case separate wallets are active
                    val indMoneyRegex = Regex("<individualMoney>(.*?)</individualMoney>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                    val indMoneyMatch = indMoneyRegex.find(modifiedTeam)
                    if (indMoneyMatch != null) {
                        val indContent = indMoneyMatch.groupValues[1]
                        val updatedIndContent = indContent.replace(Regex("<int>\\d+</int>", RegexOption.IGNORE_CASE), "<int>${edits.money}</int>")
                        modifiedTeam = modifiedTeam.replaceRange(indMoneyMatch.range, "<individualMoney>$updatedIndContent</individualMoney>")
                    }

                    result = beforeTeam + modifiedTeam + afterTeam
                }
            }
        } else {
            // For SaveGameInfo (which has root <Farmer> instead of <player>)
            result = replaceFirstTag(result, "name", edits.characterName)
            result = replaceFirstTag(result, "farmName", edits.farmName)
            result = replaceFirstTag(result, "money", edits.money.toString())
            result = replaceFirstTag(result, "maxHealth", edits.maxHealth.toString())
            result = replaceFirstTag(result, "maxStamina", edits.maxStamina.toString())

            val totalMoneyRegex = Regex("<totalMoneyEarned>(\\d+)</totalMoneyEarned>", RegexOption.IGNORE_CASE)
            val currentTotal = totalMoneyRegex.find(result)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            if (edits.money > currentTotal) {
                result = replaceFirstTag(result, "totalMoneyEarned", (edits.money + 50000).toString())
            }
        }

        // 2. Modify root timeline tags (Case-insensitive season formatting)
        result = replaceFirstTag(result, "currentSeason", edits.season.lowercase())
        result = replaceFirstTag(result, "dayOfMonth", edits.dayOfMonth.toString())
        result = replaceFirstTag(result, "year", edits.year.toString())

        return result
    }

    /**
     * Replaces ONLY the FIRST occurrence of a specific tag, preventing global corruption
     */
    private fun replaceFirstTag(content: String, tag: String, newValue: String): String {
        val regex = Regex("<$tag>(.*?)</$tag>", RegexOption.IGNORE_CASE)
        val match = regex.find(content) ?: return content
        return content.replaceRange(match.range, "<$tag>$newValue</$tag>")
    }

    /**
     * Applies edits to both the Main Save File and SaveGameInfo in the specified save folder.
     */
    fun saveToDirectory(saveFolder: File, edits: EditableSaveData): Boolean {
        return try {
            if (!saveFolder.exists() || !saveFolder.isDirectory) return false

            val files = saveFolder.listFiles() ?: return false
            val saveGameInfoFile = files.find { it.name.equals("SaveGameInfo", ignoreCase = true) }
            val mainSaveFile = files.find { !it.name.startsWith("SaveGameInfo") && !it.name.endsWith(".bak") && !it.name.endsWith("_old") }

            mainSaveFile?.let { file ->
                val xml = file.readText()
                val updatedXml = applyEditsToXml(xml, edits)
                file.writeText(updatedXml)
            }

            saveGameInfoFile?.let { file ->
                val xml = file.readText()
                val updatedXml = applyEditsToXml(xml, edits)
                file.writeText(updatedXml)
            }

            true
        } catch (_: Exception) {
            false
        }
    }
}
