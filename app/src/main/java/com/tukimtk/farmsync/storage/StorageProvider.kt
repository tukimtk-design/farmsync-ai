package com.tukimtk.farmsync.storage

interface StorageProvider {
    fun connect(): Boolean
    fun listFiles(path: String): List<String>
    fun uploadFile(localPath: String, remotePath: String): Boolean
    fun downloadFile(remotePath: String, localPath: String): Boolean
}
