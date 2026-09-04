package com.tukimtk.farmsync.data

import android.content.Context
import android.content.SharedPreferences
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class SaveStateRepositoryAiTest {

    private lateinit var context: Context
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var sharedPrefsEditor: SharedPreferences.Editor
    private val inMemoryPrefs = mutableMapOf<String, Any>()

    @BeforeEach
    fun setUp() {
        context = mock(Context::class.java)
        sharedPrefs = mock(SharedPreferences::class.java)
        sharedPrefsEditor = mock(SharedPreferences.Editor::class.java)

        `when`(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPrefs)
        `when`(sharedPrefs.edit()).thenReturn(sharedPrefsEditor)

        `when`(sharedPrefsEditor.putString(anyString(), anyString())).thenAnswer { invocation ->
            val key = invocation.arguments[0] as String
            val value = invocation.arguments[1] as String
            inMemoryPrefs[key] = value
            sharedPrefsEditor
        }
        `when`(sharedPrefsEditor.putStringSet(anyString(), any())).thenAnswer { invocation ->
            val key = invocation.arguments[0] as String
            val value = invocation.arguments[1] as Set<String>
            inMemoryPrefs[key] = value
            sharedPrefsEditor
        }

        `when`(sharedPrefs.getString(anyString(), any())).thenAnswer { invocation ->
            val key = invocation.arguments[0] as String
            val def = invocation.arguments[1] as? String
            (inMemoryPrefs[key] as? String) ?: def
        }

        `when`(sharedPrefs.getStringSet(anyString(), any())).thenAnswer { invocation ->
            val key = invocation.arguments[0] as String
            val def = invocation.arguments[1] as? Set<String>
            @Suppress("UNCHECKED_CAST")
            (inMemoryPrefs[key] as? Set<String>) ?: def
        }
    }

    @Test
    fun `test AI provider and API keys get and set`() {
        val repo = SaveStateRepository(context)

        // Defaults
        assertEquals("DEEPSEEK", repo.getAiProvider())
        assertEquals("", repo.getDeepSeekApiKey())
        assertEquals("", repo.getActiveApiKey())

        // Set DeepSeek key
        repo.setDeepSeekApiKey("sk-deepseek-12345")
        assertEquals("sk-deepseek-12345", repo.getDeepSeekApiKey())
        assertEquals("sk-deepseek-12345", repo.getActiveApiKey())

        // Switch to Gemini
        repo.setAiProvider("GEMINI")
        repo.setGeminiApiKey("AIzaSy-gemini-test")
        assertEquals("GEMINI", repo.getAiProvider())
        assertEquals("AIzaSy-gemini-test", repo.getGeminiApiKey())
        assertEquals("AIzaSy-gemini-test", repo.getActiveApiKey())

        // Switch to OpenAI
        repo.setAiProvider("OPENAI")
        repo.setOpenAiApiKey("sk-openai-test")
        repo.setCustomEndpoint("https://my-custom-proxy.com/v1/chat/completions")
        assertEquals("OPENAI", repo.getAiProvider())
        assertEquals("sk-openai-test", repo.getOpenAiApiKey())
        assertEquals("sk-openai-test", repo.getActiveApiKey())
        assertEquals("https://my-custom-proxy.com/v1/chat/completions", repo.getCustomEndpoint())
    }

    @Test
    fun `test persona and scopes get and set`() {
        val repo = SaveStateRepository(context)

        // Persona
        assertEquals("CASUAL", repo.getTranslationPersona())
        repo.setTranslationPersona("POLITE")
        assertEquals("POLITE", repo.getTranslationPersona())

        // Scopes
        val newScopes = setOf("MENUS", "DIALOGUES")
        repo.setTranslationScopes(newScopes)
        assertEquals(newScopes, repo.getTranslationScopes())
    }
}
