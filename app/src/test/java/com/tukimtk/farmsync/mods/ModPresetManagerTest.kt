package com.tukimtk.farmsync.mods

import android.content.Context
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.io.File

class ModPresetManagerTest {

    private lateinit var context: Context
    private lateinit var presetDir: File
    private lateinit var manager: ModPresetManager

    @BeforeEach
    fun setup() {
        context = mock(Context::class.java)
        
        // Create a temporary directory for tests
        val tempDir = File(System.getProperty("java.io.tmpdir"), "mod_presets_test_${System.currentTimeMillis()}")
        tempDir.mkdirs()
        `when`(context.filesDir).thenReturn(tempDir)
        
        presetDir = File(tempDir, "mod_presets")
        
        manager = ModPresetManager(context)
    }

    @AfterEach
    fun tearDown() {
        // Clean up
        if (::context.isInitialized) {
            val tempDir = context.filesDir
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `test preset saving and loading`() {
        val modStates = mapOf(
            "mod1" to true,
            "mod2" to false
        )
        val preset = ModPreset("TestPreset", modStates)
        
        // Save preset
        manager.savePreset(preset)
        
        // Verify file exists
        val expectedFile = File(presetDir, "TestPreset.json")
        assertTrue(expectedFile.exists())
        
        // Load preset
        val loadedPreset = manager.loadPreset("TestPreset")
        assertNotNull(loadedPreset)
        assertEquals("TestPreset", loadedPreset?.name)
        assertEquals(2, loadedPreset?.modStates?.size)
        assertEquals(true, loadedPreset?.modStates?.get("mod1"))
        assertEquals(false, loadedPreset?.modStates?.get("mod2"))
    }

    @Test
    fun `test getSavedPresets and deletePreset`() {
        manager.savePreset(ModPreset("Preset1", emptyMap()))
        manager.savePreset(ModPreset("Preset2", emptyMap()))
        
        val presets = manager.getSavedPresets()
        assertEquals(2, presets.size)
        assertTrue(presets.contains("Preset1"))
        assertTrue(presets.contains("Preset2"))
        
        val deleted = manager.deletePreset("Preset1")
        assertTrue(deleted)
        
        val presetsAfterDelete = manager.getSavedPresets()
        assertEquals(1, presetsAfterDelete.size)
        assertTrue(presetsAfterDelete.contains("Preset2"))
    }

    @Test
    fun `test json serialization and deserialization`() {
        val originalPreset = ModPreset("ExportTest", mapOf("modA" to true, "modB" to false))
        
        val jsonString = manager.exportPresetToJson(originalPreset)
        assertTrue(jsonString.contains("ExportTest"))
        assertTrue(jsonString.contains("modA"))
        assertTrue(jsonString.contains("modB"))
        
        val importedPreset = manager.importPresetFromJson(jsonString)
        assertEquals("ExportTest", importedPreset.name)
        assertEquals(true, importedPreset.modStates["modA"])
        assertEquals(false, importedPreset.modStates["modB"])
    }

    @Test
    fun `test applyPreset calls installer correctly`() {
        val installer = mock(ModInstaller::class.java)
        val modStates = mapOf(
            "mod1" to true,
            "mod2" to false
        )
        manager.savePreset(ModPreset("ApplyTest", modStates))
        
        val installedMods = listOf("mod1", "mod2", "mod3")
        
        manager.applyPreset("ApplyTest", installedMods, installer)
        
        // Verify that installer.toggleModState was called with expected arguments
        verify(installer, times(1)).toggleModState("mod1", true)
        verify(installer, times(1)).toggleModState("mod2", false)
        
        // Should not be called for mod3
        verify(installer, never()).toggleModState("mod3_folder", true)
        verify(installer, never()).toggleModState("mod3_folder", false)
    }
}
