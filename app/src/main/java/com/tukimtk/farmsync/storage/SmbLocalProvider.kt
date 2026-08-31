package com.tukimtk.farmsync.storage

class SmbLocalProvider : StorageProvider {
    override fun connect(): Boolean {
        println("Connecting to Local SMB/CIFS Network Share...")
        return true
    }

    override fun listFiles(path: String): List<String> = listOf("local_save.xml")
    override fun uploadFile(localPath: String, remotePath: String): Boolean = true
    override fun downloadFile(remotePath: String, localPath: String): Boolean = true
}
