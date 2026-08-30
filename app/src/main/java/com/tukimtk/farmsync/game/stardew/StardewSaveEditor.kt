package com.tukimtk.farmsync.game.stardew

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

    fun applyEditsToXml(originalXml: String, edits: EditableSaveData): String {
        var modified = originalXml

        // Replace player tags
        modified = replaceXmlTag(modified, "name", edits.characterName)
        modified = replaceXmlTag(modified, "farmName", edits.farmName)
        modified = replaceXmlTag(modified, "money", edits.money.toString())
        modified = replaceXmlTag(modified, "currentSeason", edits.season.lowercase())
        modified = replaceXmlTag(modified, "dayOfMonth", edits.dayOfMonth.toString())
        modified = replaceXmlTag(modified, "year", edits.year.toString())
        modified = replaceXmlTag(modified, "maxHealth", edits.maxHealth.toString())
        modified = replaceXmlTag(modified, "maxStamina", edits.maxStamina.toString())

        return modified
    }

    private fun replaceXmlTag(xml: String, tag: String, newValue: String): String {
        val regex = Regex("<$tag>(.*?)</$tag>", RegexOption.IGNORE_CASE)
        return if (regex.containsMatchIn(xml)) {
            regex.replace(xml, "<$tag>$newValue</$tag>")
        } else {
            xml
        }
    }
}
