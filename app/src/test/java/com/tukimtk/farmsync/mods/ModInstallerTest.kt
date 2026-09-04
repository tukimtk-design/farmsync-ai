package com.tukimtk.farmsync.mods

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.tukimtk.farmsync.game.stardew.ShizukuSaveBridge
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ModInstallerTest {

    @Test
    fun `compareVersions handles major minor patch correctly`() {
        assertTrue(compareVersions("1.0.0", "1.0.1") < 0)
        assertTrue(compareVersions("1.1.0", "1.0.1") > 0)
        assertTrue(compareVersions("2.0.0", "1.9.9") > 0)
        assertTrue(compareVersions("1.0.0", "1.0.0") == 0)
        assertTrue(compareVersions("v1.14.24", "1.14.23") > 0)
    }

    @Test
    fun `installModFromUri detects manifest correctly from nested zip`() {
        // Setup mock context
        val context = mock(Context::class.java)
        val resolver = mock(ContentResolver::class.java)
        `when`(context.contentResolver).thenReturn(resolver)
        val cacheDir = File(System.getProperty("java.io.tmpdir"), "test_cache")
        cacheDir.mkdirs()
        `when`(context.cacheDir).thenReturn(cacheDir)

        // Create a fake zip in memory
        val baos = ByteArrayOutputStream()
        val zos = ZipOutputStream(baos)
        
        // Add a manifest entry
        val manifestContent = """{
            "Name": "Test Mod",
            "Author": "Test Author",
            "Version": "1.2.3",
            "UniqueID": "test.mod.id"
        }"""
        
        zos.putNextEntry(ZipEntry("NestedFolder/manifest.json"))
        zos.write(manifestContent.toByteArray())
        zos.closeEntry()
        
        // Add another file
        zos.putNextEntry(ZipEntry("NestedFolder/content.json"))
        zos.write("{}".toByteArray())
        zos.closeEntry()
        zos.close()

        val bais = ByteArrayInputStream(baos.toByteArray())
        
        val uri = mock(Uri::class.java)
        `when`(uri.lastPathSegment).thenReturn("test_mod.zip")
        `when`(resolver.openInputStream(uri)).thenReturn(bais)

        val bridge = ShizukuSaveBridge(context).apply { permissionOverride = false }
        val installer = ModInstaller(context, bridge)
        val result = installer.installModFromUri(uri)

        // Cannot fully test Shizuku deployment logic in unit tests without extensive mocking
        // But we can check if manifest parsing worked before deployment failure
        assertEquals("Test Mod", result.modName)
        assertEquals("Test Author", result.author)
        assertEquals("1.2.3", result.version)
        assertEquals("test.mod.id", result.uniqueId)
        
        // Shizuku permission is not granted in test env, so it should fail gracefully
        assertFalse(result.isSuccess)
        assertTrue(result.message.contains("Shizuku permission not granted"))
        
        cacheDir.deleteRecursively()
    }
}
