package com.tukimtk.farmsync

import com.tukimtk.farmsync.ai.StardewTagShield
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StardewTagShieldTest {

    @Test
    fun testTagShieldAndUnshield() {
        val shield = StardewTagShield()
        val originalText = "Hey %firstname, want to visit %farm today? \$e#\$b#See you soon! [123]"

        val shielded = shield.shield(originalText)

        // Ensure tags are replaced by placeholders
        assertTrue(!shielded.maskedText.contains("%firstname"))
        assertTrue(!shielded.maskedText.contains("%farm"))
        assertTrue(shielded.tokenMap.isNotEmpty())

        // Simulate translation of the non-placeholder words
        val simulatedTranslated = shielded.maskedText
            .replace("Hey", "สวัสดี")
            .replace("want to visit", "อยากมาเที่ยวที่")
            .replace("today", "วันนี้ไหม")
            .replace("See you soon!", "แล้วเจอกันเร็วๆ นี้นะ!")

        // Unshield
        val finalResult = shield.unshield(simulatedTranslated, shielded.tokenMap)

        // Ensure original tags are 100% restored
        assertTrue(finalResult.contains("%firstname"))
        assertTrue(finalResult.contains("%farm"))
        assertTrue(finalResult.contains("\$e"))
        assertTrue(finalResult.contains("[123]"))
    }
}
