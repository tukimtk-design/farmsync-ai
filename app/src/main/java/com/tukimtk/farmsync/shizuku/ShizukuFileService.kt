package com.tukimtk.farmsync.shizuku

interface ShizukuFileService {
    fun readFile(path: String): String
    fun writeFile(path: String, content: String): Boolean
}
