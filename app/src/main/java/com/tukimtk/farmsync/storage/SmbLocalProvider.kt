package com.tukimtk.farmsync.storage

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SmbLocalProvider(
    private val client: SmbClient,
    private val config: SmbConfig
) : StorageProvider {

    override fun connect(): Boolean {
        println("Connecting to Local SMB/CIFS Network Share...")
        return testConnection(config)
    }

    fun testConnection(config: SmbConfig): Boolean {
        try {
            return client.connect(config)
        } catch (e: Exception) {
            when (e) {
                is SmbError -> throw e
                else -> throw SmbError.HostUnreachable
            }
        }
    }

    override fun listFiles(path: String): List<String> {
        try {
            if (!client.connect(config)) return emptyList()
            return client.listFiles(path)
        } catch (e: Exception) {
            when (e) {
                is SmbError -> throw e
                else -> throw SmbError.ReadWriteError(e.message ?: "Unknown error")
            }
        }
    }

    override fun uploadFile(localPath: String, remotePath: String): Boolean {
        try {
            if (!client.connect(config)) return false
            val localFile = File(localPath)
            if (!localFile.exists()) return false

            val remoteTempPath = "$remotePath.tmp"
            
            client.getOutputStream(remoteTempPath).use { outStream ->
                FileInputStream(localFile).use { inStream ->
                    inStream.copyTo(outStream)
                }
            }

            return client.rename(remoteTempPath, remotePath)
        } catch (e: Exception) {
            when (e) {
                is SmbError -> throw e
                else -> throw SmbError.ReadWriteError(e.message ?: "Unknown error")
            }
        }
    }

    override fun downloadFile(remotePath: String, localPath: String): Boolean {
        try {
            if (!client.connect(config)) return false
            val localFile = File(localPath)
            
            client.getInputStream(remotePath).use { inStream ->
                FileOutputStream(localFile).use { outStream ->
                    inStream.copyTo(outStream)
                }
            }
            return true
        } catch (e: Exception) {
            when (e) {
                is SmbError -> throw e
                else -> throw SmbError.ReadWriteError(e.message ?: "Unknown error")
            }
        }
    }

    override fun deleteFile(path: String): Boolean {
        return false // Stub implementation
    }

    override fun testConnection(): Boolean {
        return testConnection(config)
    }
}
