package com.tukimtk.farmsync

import com.tukimtk.farmsync.storage.GoogleDriveProvider
import com.tukimtk.farmsync.storage.SmbLocalProvider
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StorageProviderTest {

    @Test
    fun testGoogleDriveConnectionStub() {
        val provider = GoogleDriveProvider()
        assertTrue(provider.connect())
        assertTrue(provider.listFiles("").isNotEmpty())
    }

    @Test
    fun testSmbLocalConnectionStub() {
        val provider = SmbLocalProvider()
        assertTrue(provider.connect())
        assertTrue(provider.listFiles("").isNotEmpty())
    }
}
