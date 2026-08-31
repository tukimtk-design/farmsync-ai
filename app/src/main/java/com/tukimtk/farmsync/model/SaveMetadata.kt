package com.tukimtk.farmsync.model

data class SaveMetadata(
    val farmId: String,
    val gameVersion: String,
    val inGameYear: Int,
    val inGameSeason: Season,
    val inGameDay: Int,
    val money: Int,
    val playTime: Long,
    val fileHash: String,
    val lastModifiedTimestamp: Long,
    val isValidParse: Boolean
)

enum class Season(val index: Int) {
    Spring(0),
    Summer(1),
    Fall(2),
    Winter(3)
}
