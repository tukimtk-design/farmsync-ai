package com.tukimtk.farmsync.ai

class BatchModTranslator(private val engine: AiTranslationEngine) {
    fun translateBatch(modText: String): String {
        // In a real scenario, this would use StardewGlossary terms as context for the AI prompt
        val context = StardewGlossary.terms.keys.joinToString(", ")
        return engine.translate(modText, "Thai Gamer (Context: $context)")
    }
}
