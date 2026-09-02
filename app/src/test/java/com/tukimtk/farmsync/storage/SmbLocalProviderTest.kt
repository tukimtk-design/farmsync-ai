package com.tukimtk.farmsync.storage

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class SmbLocalProviderTest {

    private lateinit var mockClient: MockSmbClient
    private lateinit var provider: SmbLocalProvider
    private val config = SmbConfig("192.168.1.100", 445, "saves", "user", "pass")

    @Before
    fun setup() {
        mockClient = MockSmbClient()
        provider = SmbLocalProvider(mockClient, config)
    }

    @Test
    fun testConnection_success() {
        mockClient.shouldConnect = true
        assertTrue(provider.connect())
        assertTrue(provider.testConnection(config))
    }

    @Test(expected = SmbError.AuthFailed::class)
    fun testConnection_authFailed() {
        mockClient.shouldConnect = false
        mockClient.errorToThrow = SmbError.AuthFailed
        provider.testConnection(config)
    }

    @Test(expected = SmbError.HostUnreachable::class)
    fun testConnection_hostUnreachable() {
        mockClient.shouldConnect = false
        mockClient.errorToThrow = SmbError.HostUnreachable
        provider.testConnection(config)
    }
    
    @Test(expected = SmbError.HostUnreachable::class)
    fun testConnection_unknownError() {
        mockClient.shouldConnect = false
        mockClient.errorToThrow = RuntimeException("Unknown network error")
        provider.testConnection(config)
    }

    @Test
    fun listFiles_success() {
        mockClient.shouldConnect = true
        mockClient.filesToReturn = listOf("save1.xml", "save2.xml")
        val files = provider.listFiles("/remote/path")
        assertEquals(2, files.size)
        assertEquals("save1.xml", files[0])
    }

    @Test(expected = SmbError.ShareNotFound::class)
    fun listFiles_shareNotFound() {
        mockClient.shouldConnect = true
        mockClient.errorToThrowOnList = SmbError.ShareNotFound
        provider.listFiles("/remote/path")
    }

    @Test
    fun downloadFile_success() {
        mockClient.shouldConnect = true
        val remoteData = "dummy content".toByteArray()
        mockClient.inputStreamToReturn = ByteArrayInputStream(remoteData)
        
        val tempFile = File.createTempFile("test_download", ".tmp")
        tempFile.deleteOnExit()
        
        val success = provider.downloadFile("/remote/file.xml", tempFile.absolutePath)
        assertTrue(success)
        assertArrayEquals(remoteData, tempFile.readBytes())
    }

    @Test
    fun uploadFile_success_atomicReplace() {
        mockClient.shouldConnect = true
        val localFile = File.createTempFile("test_upload", ".tmp")
        localFile.writeText("upload content")
        localFile.deleteOnExit()
        
        val outStream = ByteArrayOutputStream()
        mockClient.outputStreamToReturn = outStream
        mockClient.shouldRename = true

        val success = provider.uploadFile(localFile.absolutePath, "/remote/file.xml")
        assertTrue(success)
        
        assertEquals("upload content", String(outStream.toByteArray()))
        
        // Verify atomic rename was called correctly
        assertEquals("/remote/file.xml.tmp", mockClient.renameOldPath)
        assertEquals("/remote/file.xml", mockClient.renameNewPath)
    }

    @Test
    fun uploadFile_renameFailed() {
        mockClient.shouldConnect = true
        val localFile = File.createTempFile("test_upload", ".tmp")
        localFile.writeText("upload content")
        localFile.deleteOnExit()
        
        val outStream = ByteArrayOutputStream()
        mockClient.outputStreamToReturn = outStream
        mockClient.shouldRename = false // Rename fails

        val success = provider.uploadFile(localFile.absolutePath, "/remote/file.xml")
        assertFalse(success)
    }
}

class MockSmbClient : SmbClient {
    var shouldConnect = true
    var errorToThrow: Exception? = null
    
    var filesToReturn = emptyList<String>()
    var errorToThrowOnList: Exception? = null

    var inputStreamToReturn: InputStream? = null
    var outputStreamToReturn: OutputStream? = null

    var shouldRename = true
    var renameOldPath: String? = null
    var renameNewPath: String? = null

    override fun connect(config: SmbConfig): Boolean {
        if (errorToThrow != null) throw errorToThrow!!
        return shouldConnect
    }

    override fun listFiles(remotePath: String): List<String> {
        if (errorToThrowOnList != null) throw errorToThrowOnList!!
        return filesToReturn
    }

    override fun getInputStream(remotePath: String): InputStream {
        return inputStreamToReturn ?: ByteArrayInputStream(ByteArray(0))
    }

    override fun getOutputStream(remotePath: String): OutputStream {
        return outputStreamToReturn ?: ByteArrayOutputStream()
    }

    override fun rename(oldPath: String, newPath: String): Boolean {
        renameOldPath = oldPath
        renameNewPath = newPath
        return shouldRename
    }
}
