package com.tukimtk.farmsync.desktop

data class SaveMetadata(
    val folderName: String,
    val farmerName: String,
    val farmName: String,
    val money: Int,
    val season: String,
    val year: Int,
    val lastModified: Long
)
