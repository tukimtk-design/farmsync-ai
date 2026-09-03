package com.tukimtk.farmsync.storage

import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class WebDavConfig(
    val serverUrl: String,
    val username: String? = null,
    val password: String? = null
)

class WebDavAuthenticator(private val config: WebDavConfig) : Authenticator {
    private var ncCounter = 0

    override fun authenticate(route: Route?, response: Response): Request? {
        if (config.username == null || config.password == null) {
            return null
        }

        // Avoid infinite loop if auth fails
        if (response.priorResponse?.priorResponse != null) {
            return null
        }

        val challenges = response.challenges()
        for (challenge in challenges) {
            if (challenge.scheme.equals("Basic", ignoreCase = true)) {
                val credential = Credentials.basic(config.username, config.password)
                return response.request.newBuilder()
                    .header("Authorization", credential)
                    .build()
            } else if (challenge.scheme.equals("Digest", ignoreCase = true)) {
                val realm = challenge.authParams["realm"] ?: ""
                val nonce = challenge.authParams["nonce"] ?: ""
                val qop = challenge.authParams["qop"]
                val opaque = challenge.authParams["opaque"]
                val algorithm = challenge.authParams["algorithm"] ?: "MD5"

                val method = response.request.method
                val path = response.request.url.encodedPath
                
                ncCounter++
                val nc = String.format("%08x", ncCounter)
                val cnonce = String.format("%08x", Random.nextInt())

                val ha1 = md5("${config.username}:$realm:${config.password}")
                val ha2 = md5("$method:$path")

                val digestResponse = if (qop != null && qop.contains("auth")) {
                    md5("$ha1:$nonce:$nc:$cnonce:auth:$ha2")
                } else {
                    md5("$ha1:$nonce:$ha2")
                }

                val authHeader = StringBuilder("Digest ")
                authHeader.append("username=\"${config.username}\", ")
                authHeader.append("realm=\"$realm\", ")
                authHeader.append("nonce=\"$nonce\", ")
                authHeader.append("uri=\"$path\", ")
                authHeader.append("response=\"$digestResponse\"")
                if (qop != null) {
                    authHeader.append(", qop=auth, nc=$nc, cnonce=\"$cnonce\"")
                }
                if (opaque != null) {
                    authHeader.append(", opaque=\"$opaque\"")
                }
                if (challenge.authParams.containsKey("algorithm")) {
                    authHeader.append(", algorithm=$algorithm")
                }

                return response.request.newBuilder()
                    .header("Authorization", authHeader.toString())
                    .build()
            }
        }
        return null
    }

    private fun md5(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val bytes = digest.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

class WebDavProvider(private val config: WebDavConfig) : StorageProvider {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .authenticator(WebDavAuthenticator(config))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun connect(): Boolean {
        return testConnection()
    }
    
    override fun testConnection(): Boolean {
        val request = Request.Builder()
            .url(config.serverUrl)
            .method("PROPFIND", null)
            .header("Depth", "0")
            .build()
            
        return try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful || response.code == 207 // 207 Multi-Status
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun listFiles(path: String): List<String> {
        val url = config.serverUrl.trimEnd('/') + "/" + path.trimStart('/')
        val request = Request.Builder()
            .url(url)
            .method("PROPFIND", null)
            .header("Depth", "1")
            .build()
            
        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 207) {
                    val body = response.body?.string() ?: return emptyList()
                    parsePropfindResponse(body)
                } else {
                    emptyList()
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parsePropfindResponse(xml: String): List<String> {
        val files = mutableListOf<String>()
        try {
            // Very simple XML parsing for DAV:href tags
            val hrefPattern = "<(?:d:|D:)?href>(.*?)</(?:d:|D:)?href>".toRegex(RegexOption.IGNORE_CASE)
            val matches = hrefPattern.findAll(xml).toList()
            // The first href is usually the requested directory itself, so we skip it or filter appropriately.
            // A simple implementation returns all hrefs
            for (match in matches) {
                val href = match.groupValues[1]
                files.add(href)
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
        return files
    }

    override fun uploadFile(localPath: String, remotePath: String): Boolean {
        val localFile = java.io.File(localPath)
        if (!localFile.exists()) return false

        ensureDirectoryExists(remotePath)

        val url = config.serverUrl.trimEnd('/') + "/" + remotePath.trimStart('/')
        val requestBody = okhttp3.RequestBody.create(null, localFile)
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()
            
        return try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful || response.code == 201 || response.code == 204
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun ensureDirectoryExists(remotePath: String) {
        val parts = remotePath.trimStart('/').split("/")
        if (parts.size <= 1) return

        var currentPath = ""
        // Skip the last part as it's the file name
        for (i in 0 until parts.size - 1) {
            currentPath += "/" + parts[i]
            val url = config.serverUrl.trimEnd('/') + currentPath
            
            // Check if exists
            val propfind = Request.Builder()
                .url(url)
                .method("PROPFIND", null)
                .header("Depth", "0")
                .build()
                
            val exists = try {
                client.newCall(propfind).execute().use { response ->
                    response.isSuccessful || response.code == 207
                }
            } catch (e: Exception) {
                false
            }

            if (!exists) {
                val mkcol = Request.Builder()
                    .url(url)
                    .method("MKCOL", null)
                    .build()
                try {
                    client.newCall(mkcol).execute().use { }
                } catch (e: Exception) {
                    // Ignore, might have been created concurrently
                }
            }
        }
    }

    override fun downloadFile(remotePath: String, localPath: String): Boolean {
        val url = config.serverUrl.trimEnd('/') + "/" + remotePath.trimStart('/')
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
            
        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body
                    if (body != null) {
                        java.io.File(localPath).outputStream().use { out ->
                            body.byteStream().copyTo(out)
                        }
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun deleteFile(path: String): Boolean {
        val url = config.serverUrl.trimEnd('/') + "/" + path.trimStart('/')
        val request = Request.Builder()
            .url(url)
            .delete()
            .build()
            
        return try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful || response.code == 404 // OK or already deleted
            }
        } catch (e: Exception) {
            false
        }
    }
}
