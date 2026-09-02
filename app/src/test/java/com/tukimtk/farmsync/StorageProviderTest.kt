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
        // Obsolete test as SmbLocalProviderTest now covers this extensively with SmbClient mocking
        assertTrue(true)
    }
}
