package com.tukimtk.farmsync

import com.tukimtk.farmsync.os.AndroidPathResolver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AndroidPathResolverTest {

    @Test
    fun testResolveAndroidDataPath() {
        val resolver = AndroidPathResolver()
        val packageName = "com.zane.stardewvalley"
        val expectedPath = "/storage/emulated/0/Android/data/com.zane.stardewvalley"

        val result = resolver.resolveAndroidDataPath(packageName, "Xiaomi")

        assertEquals(expectedPath, result)
    }
}
