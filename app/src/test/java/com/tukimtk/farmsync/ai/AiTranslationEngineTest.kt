package com.tukimtk.farmsync.ai

import android.content.Context
import android.content.SharedPreferences
import com.tukimtk.farmsync.game.stardew.ShizukuSaveBridge
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.io.File

class AiTranslationEngineTest {

    private lateinit var context: Context
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var sharedPrefsEditor: SharedPreferences.Editor
    private lateinit var bridge: ShizukuSaveBridge
    private lateinit var tempCacheDir: File

    @BeforeEach
    fun setUp() {
        context = mock(Context::class.java)
        sharedPrefs = mock(SharedPreferences::class.java)
        sharedPrefsEditor = mock(SharedPreferences.Editor::class.java)

        `when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs)
        `when`(sharedPrefs.edit()).thenReturn(sharedPrefsEditor)
        `when`(sharedPrefsEditor.putString(anyString(), anyString())).thenReturn(sharedPrefsEditor)

        tempCacheDir = File(System.getProperty("java.io.tmpdir"), "farmsync_test_cache_" + System.currentTimeMillis()).apply { mkdirs() }
        `when`(context.cacheDir).thenReturn(tempCacheDir)

        bridge = mock(ShizukuSaveBridge::class.java)
        `when`(bridge.isPermissionGranted()).thenReturn(false)
    }

    @Test
    fun `generateAndDeployMod produces valid Content Patcher manifest and content JSON`() = runBlocking {
        val engine = AiTranslationEngine(context)

        val result = engine.generateAndDeployMod(
            scopes = setOf(TranslationScope.MENUS, TranslationScope.DIALOGUES, TranslationScope.QUESTS, TranslationScope.ITEMS),
            persona = TranslationPersona.CASUAL,
            provider = AiProviderType.DEEPSEEK,
            apiKey = "",
            bridge = bridge
        )

        assertTrue(result.isSuccess)
        val manifestFile = File(tempCacheDir, "FarmSync_ThaiTranslation/manifest.json")
        val contentFile = File(tempCacheDir, "FarmSync_ThaiTranslation/content.json")

        assertTrue(manifestFile.exists(), "manifest.json should exist")
        assertTrue(contentFile.exists(), "content.json should exist")

        val manifestJson = JSONObject(manifestFile.readText(Charsets.UTF_8))
        assertEquals("Pathoschild.ContentPatcher", manifestJson.getJSONObject("ContentPackFor").getString("UniqueID"))
        assertEquals("com.tukimtk.farmsync.thaitranslation", manifestJson.getString("UniqueID"))

        val contentJson = JSONObject(contentFile.readText(Charsets.UTF_8))
        assertEquals("1.28.0", contentJson.getString("Format"))
        val changes = contentJson.getJSONArray("Changes")
        assertTrue(changes.length() >= 4, "Should have changes for menus, dialogues, quests, and items")

        // Verify Menus
        var foundMenu = false
        for (i in 0 until changes.length()) {
            val change = changes.getJSONObject(i)
            if (change.optString("Target") == "Strings/UI") {
                foundMenu = true
                val entries = change.getJSONObject("Entries")
                assertEquals("เริ่มเกมใหม่", entries.getString("NewGame"))
                assertEquals("โหลดเซฟเกม", entries.getString("LoadGame"))
                assertEquals("กระเป๋าเก็บของ", entries.getString("Inventory"))
            }
        }
        assertTrue(foundMenu, "Strings/UI target should be present")
    }

    @Test
    fun `generateAndDeployMod reflects persona differences in dialogues`() = runBlocking {
        val engine = AiTranslationEngine(context)

        // Casual
        engine.generateAndDeployMod(
            scopes = setOf(TranslationScope.DIALOGUES),
            persona = TranslationPersona.CASUAL,
            provider = AiProviderType.DEEPSEEK,
            apiKey = "",
            bridge = bridge
        )
        val contentCasual = JSONObject(File(tempCacheDir, "FarmSync_ThaiTranslation/content.json").readText())
        var abigailCasualIntro = ""
        val changesCasual = contentCasual.getJSONArray("Changes")
        for (i in 0 until changesCasual.length()) {
            val c = changesCasual.getJSONObject(i)
            if (c.optString("Target") == "Characters/Dialogue/Abigail") {
                abigailCasualIntro = c.getJSONObject("Entries").getString("Introduction")
            }
        }
        assertTrue(abigailCasualIntro.contains("แอบิเกลนะ"), "Casual dialogue should contain friendly anime tone")

        // Polite
        engine.generateAndDeployMod(
            scopes = setOf(TranslationScope.DIALOGUES),
            persona = TranslationPersona.POLITE,
            provider = AiProviderType.GEMINI,
            apiKey = "",
            bridge = bridge
        )
        val contentPolite = JSONObject(File(tempCacheDir, "FarmSync_ThaiTranslation/content.json").readText())
        var abigailPoliteIntro = ""
        val changesPolite = contentPolite.getJSONArray("Changes")
        for (i in 0 until changesPolite.length()) {
            val c = changesPolite.getJSONObject(i)
            if (c.optString("Target") == "Characters/Dialogue/Abigail") {
                abigailPoliteIntro = c.getJSONObject("Entries").getString("Introduction")
            }
        }
        assertTrue(abigailPoliteIntro.contains("สวัสดีค่ะ"), "Polite dialogue should contain polite particles")
    }

    @Test
    fun `translateWithAi falls back gracefully to offline dictionary when key is empty`() = runBlocking {
        val engine = AiTranslationEngine(context)
        val res = engine.translateWithAi("Inventory", AiProviderType.DEEPSEEK, "", TranslationPersona.CASUAL)
        assertEquals("กระเป๋าเก็บของ", res)

        val res2 = engine.translateWithAi("New Game", AiProviderType.GEMINI, "", TranslationPersona.POLITE)
        assertEquals("เริ่มเกมใหม่", res2)
    }
}
