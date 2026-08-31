package com.tukimtk.farmsync.storage

class GoogleDriveProvider : StorageProvider {
    override fun connect(): Boolean {
        println("Connecting to Google Drive API v3...")
        return true
    }

    override fun listFiles(path: String): List<String> = listOf("save1.xml", "save2.xml")
    override fun uploadFile(localPath: String, remotePath: String): Boolean = true
    override fun downloadFile(remotePath: String, localPath: String): Boolean = true
}
