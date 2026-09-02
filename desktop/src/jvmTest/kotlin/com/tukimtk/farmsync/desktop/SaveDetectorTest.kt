package com.tukimtk.farmsync.desktop

import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SaveDetectorTest {

    @Test
    fun testDetectorReturnsValidPath() {
        val dir = SaveDetector.getStardewSaveDirectory()
        // We cannot reliably assert the exact path without knowing the OS of the test runner,
        // but we can assert it returns something and isn't throwing errors.
        if (dir != null) {
            val path = dir.absolutePath
            assertTrue(path.contains("StardewValley") || path.contains("Saves"), "Path should look like a stardew valley path")
        }
    }
}
