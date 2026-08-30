package com.tukimtk.farmsync.model

data class GameSaveMetadata(
    val farmName: String,
    val characterName: String,
    val season: String,
    val date: Int,
    val year: Int,
    val money: Int
)
