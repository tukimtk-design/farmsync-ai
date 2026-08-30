package com.tukimtk.farmsync

import com.tukimtk.farmsync.ai.StardewGlossary
import com.tukimtk.farmsync.ai.BatchModTranslator
import com.tukimtk.farmsync.ai.AiTranslationEngine
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class StardewGlossaryTest {
    @Test
    fun testGlossaryLookup() {
        assertEquals("พาร์สนิป", StardewGlossary.terms["Parsnip"])
    }

    @Test
    fun testBatchTranslationContext() {
        val engine = AiTranslationEngine()
        val translator = BatchModTranslator(engine)
        val result = translator.translateBatch("Hello")

        assertTrue(result.contains("Thai Gamer"))
        assertTrue(result.contains("Parsnip"))
    }
}
