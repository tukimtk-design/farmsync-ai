package com.tukimtk.farmsync.os

class AndroidPathResolver {
    fun resolveAndroidDataPath(packageName: String, brand: String): String {
        // Stub: In reality, this might adjust for weird manufacturer mount points
        return "/storage/emulated/0/Android/data/$packageName"
    }
}
