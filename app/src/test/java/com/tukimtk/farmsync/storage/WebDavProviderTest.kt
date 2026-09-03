package com.tukimtk.farmsync.storage

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class WebDavProviderTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var webDavProvider: WebDavProvider
    private lateinit var config: WebDavConfig

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        config = WebDavConfig(
            serverUrl = mockWebServer.url("/").toString(),
            username = "testuser",
            password = "testpassword"
        )
        webDavProvider = WebDavProvider(config)
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testConnectSuccess() {
        mockWebServer.enqueue(MockResponse().setResponseCode(207))
        assertTrue(webDavProvider.connect())
        
        val request = mockWebServer.takeRequest()
        assertEquals("PROPFIND", request.method)
        assertEquals("/", request.path)
    }

    @Test
    fun testConnectFailure() {
        mockWebServer.enqueue(MockResponse().setResponseCode(401))
        assertFalse(webDavProvider.connect())
    }

    @Test
    fun testDownloadFileSuccess() {
        val fileContent = "test file content"
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(fileContent))

        val localFile = File.createTempFile("test_download", ".txt")
        localFile.deleteOnExit()

        assertTrue(webDavProvider.downloadFile("remote/test.txt", localFile.absolutePath))
        
        val request = mockWebServer.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/remote/test.txt", request.path)
        
        assertEquals(fileContent, localFile.readText())
    }

    @Test
    fun testDownloadFileFailure() {
        mockWebServer.enqueue(MockResponse().setResponseCode(404))

        val localFile = File.createTempFile("test_download", ".txt")
        localFile.deleteOnExit()

        assertFalse(webDavProvider.downloadFile("remote/test.txt", localFile.absolutePath))
    }

    @Test
    fun testDeleteFileSuccess() {
        mockWebServer.enqueue(MockResponse().setResponseCode(204))

        assertTrue(webDavProvider.deleteFile("remote/test.txt"))
        
        val request = mockWebServer.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/remote/test.txt", request.path)
    }

    @Test
    fun testDeleteFileFailure() {
        mockWebServer.enqueue(MockResponse().setResponseCode(403))

        assertFalse(webDavProvider.deleteFile("remote/test.txt"))
    }

    @Test
    fun testBasicAuthentication() {
        mockWebServer.enqueue(MockResponse().setResponseCode(401).setHeader("WWW-Authenticate", "Basic realm=\"Test\""))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        assertTrue(webDavProvider.connect()) // Should retry and succeed

        val req1 = mockWebServer.takeRequest()
        assertNull(req1.getHeader("Authorization"))

        val req2 = mockWebServer.takeRequest()
        assertNotNull(req2.getHeader("Authorization"))
        assertTrue(req2.getHeader("Authorization")?.startsWith("Basic") == true)
    }

    @Test
    fun testDigestAuthentication() {
        val authChallenge = "Digest realm=\"testrealm@host.com\", qop=\"auth\", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\""
        mockWebServer.enqueue(MockResponse().setResponseCode(401).setHeader("WWW-Authenticate", authChallenge))
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        assertTrue(webDavProvider.connect()) // Should retry and succeed

        val req1 = mockWebServer.takeRequest()
        assertNull(req1.getHeader("Authorization"))

        val req2 = mockWebServer.takeRequest()
        assertNotNull(req2.getHeader("Authorization"))
        assertTrue(req2.getHeader("Authorization")?.startsWith("Digest") == true)
    }

    @Test
    fun testListFilesSuccess() {
        val xmlResponse = """
            <?xml version="1.0" encoding="utf-8"?>
            <D:multistatus xmlns:D="DAV:">
                <D:response>
                    <D:href>/remote/dir/</D:href>
                    <D:propstat>
                        <D:prop><D:resourcetype><D:collection/></D:resourcetype></D:prop>
                        <D:status>HTTP/1.1 200 OK</D:status>
                    </D:propstat>
                </D:response>
                <D:response>
                    <D:href>/remote/dir/file1.txt</D:href>
                    <D:propstat>
                        <D:prop><D:resourcetype/></D:prop>
                        <D:status>HTTP/1.1 200 OK</D:status>
                    </D:propstat>
                </D:response>
                <D:response>
                    <D:href>/remote/dir/file2.xml</D:href>
                    <D:propstat>
                        <D:prop><D:resourcetype/></D:prop>
                        <D:status>HTTP/1.1 200 OK</D:status>
                    </D:propstat>
                </D:response>
            </D:multistatus>
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(207).setBody(xmlResponse))

        val files = webDavProvider.listFiles("remote/dir")
        
        val request = mockWebServer.takeRequest()
        assertEquals("PROPFIND", request.method)
        assertEquals("/remote/dir", request.path)
        assertEquals("1", request.getHeader("Depth"))

        assertEquals(3, files.size)
        assertTrue(files.contains("/remote/dir/"))
        assertTrue(files.contains("/remote/dir/file1.txt"))
        assertTrue(files.contains("/remote/dir/file2.xml"))
    }

    @Test
    fun testUploadFileSuccess() {
        // Mock PROPFIND for /remote (exists)
        mockWebServer.enqueue(MockResponse().setResponseCode(207))
        // Mock PROPFIND for /remote/dir (exists)
        mockWebServer.enqueue(MockResponse().setResponseCode(207))
        // Mock PUT for file upload
        mockWebServer.enqueue(MockResponse().setResponseCode(201))

        val localFile = File.createTempFile("test_upload", ".txt")
        localFile.writeText("upload content")
        localFile.deleteOnExit()

        assertTrue(webDavProvider.uploadFile(localFile.absolutePath, "remote/dir/test.txt"))

        val propfindReq1 = mockWebServer.takeRequest()
        assertEquals("PROPFIND", propfindReq1.method)
        assertEquals("/remote", propfindReq1.path)
        
        val propfindReq2 = mockWebServer.takeRequest()
        assertEquals("PROPFIND", propfindReq2.method)
        assertEquals("/remote/dir", propfindReq2.path)

        val putReq = mockWebServer.takeRequest()
        assertEquals("PUT", putReq.method)
        assertEquals("/remote/dir/test.txt", putReq.path)
        assertEquals("upload content", putReq.body.readUtf8())
    }

    @Test
    fun testUploadFileCreatesDirectory() {
        // Mock PROPFIND for /remote (returns 404, does not exist)
        mockWebServer.enqueue(MockResponse().setResponseCode(404))
        // Mock MKCOL for /remote
        mockWebServer.enqueue(MockResponse().setResponseCode(201))
        // Mock PROPFIND for /remote/dir (returns 404, does not exist)
        mockWebServer.enqueue(MockResponse().setResponseCode(404))
        // Mock MKCOL for /remote/dir
        mockWebServer.enqueue(MockResponse().setResponseCode(201))
        // Mock PUT for file upload
        mockWebServer.enqueue(MockResponse().setResponseCode(201))

        val localFile = File.createTempFile("test_upload2", ".txt")
        localFile.writeText("upload content 2")
        localFile.deleteOnExit()

        assertTrue(webDavProvider.uploadFile(localFile.absolutePath, "remote/dir/test2.txt"))

        val propfindReq1 = mockWebServer.takeRequest()
        assertEquals("PROPFIND", propfindReq1.method)
        assertEquals("/remote", propfindReq1.path)

        val mkcolReq1 = mockWebServer.takeRequest()
        assertEquals("MKCOL", mkcolReq1.method)
        assertEquals("/remote", mkcolReq1.path)
        
        val propfindReq2 = mockWebServer.takeRequest()
        assertEquals("PROPFIND", propfindReq2.method)
        assertEquals("/remote/dir", propfindReq2.path)
        
        val mkcolReq2 = mockWebServer.takeRequest()
        assertEquals("MKCOL", mkcolReq2.method)
        assertEquals("/remote/dir", mkcolReq2.path)

        val putReq = mockWebServer.takeRequest()
        assertEquals("PUT", putReq.method)
        assertEquals("/remote/dir/test2.txt", putReq.path)
        assertEquals("upload content 2", putReq.body.readUtf8())
    }
}
