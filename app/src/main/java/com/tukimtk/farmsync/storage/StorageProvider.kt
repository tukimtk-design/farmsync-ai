package com.tukimtk.farmsync.storage

import java.io.InputStream
import java.io.OutputStream

interface StorageProvider {
    fun connect(): Boolean
    fun listFiles(path: String): List<String>
    fun uploadFile(localPath: String, remotePath: String): Boolean
    fun downloadFile(remotePath: String, localPath: String): Boolean
    fun deleteFile(path: String): Boolean
    fun testConnection(): Boolean
}

data class SmbConfig(
    val hostIp: String,
    val port: Int = 445,
    val shareName: String,
    val username: String? = null,
    val password: String? = null
)

sealed class SmbError : Exception() {
    object AuthFailed : SmbError()
    object HostUnreachable : SmbError()
    object ShareNotFound : SmbError()
    class ReadWriteError(message: String) : SmbError()
}

interface SmbClient {
    fun connect(config: SmbConfig): Boolean
    fun listFiles(remotePath: String): List<String>
    fun getInputStream(remotePath: String): InputStream
    fun getOutputStream(remotePath: String): OutputStream
    fun rename(oldPath: String, newPath: String): Boolean
}
