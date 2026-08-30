package com.tukimtk.farmsync.ai

import java.util.regex.Pattern

data class ShieldedContent(
    val maskedText: String,
    val tokenMap: Map<String, String>
)

class StardewTagShield {

    // Regex for Stardew Valley dialogue tags:
    // 1. Keyword tags: %firstname, %farm, %spouse, %pet, %kid1, %kid2, %year, %season
    // 2. Control chars: ^, $e, $h, $s, $k, $u, $a, $c, $b, $q, $r, $d, $l, $p, $t
    // 3. Bracket tags: [123], [gender male|female], [split], [mood ...]
    private val tagRegex = Regex(
        "(%[a-zA-Z0-9_]+|\\$[a-zA-Z0-9_]+|\\^|\\[[^\\]]+\\])"
    )

    fun shield(inputText: String): ShieldedContent {
        val tokenMap = mutableMapOf<String, String>()
        var counter = 0

        val masked = tagRegex.replace(inputText) { matchResult ->
            val placeholder = "__TAG_${counter++}__"
            tokenMap[placeholder] = matchResult.value
            placeholder
        }

        return ShieldedContent(
            maskedText = masked,
            tokenMap = tokenMap
        )
    }

    fun unshield(translatedText: String, tokenMap: Map<String, String>): String {
        var restored = translatedText
        for ((placeholder, originalTag) in tokenMap) {
            restored = restored.replace(placeholder, originalTag)
        }
        return restored
    }
}
