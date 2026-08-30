package com.tukimtk.farmsync.model

data class StardewMod(
    val uniqueId: String,
    val name: String,
    val version: String,
    val description: String,
    val isEnabled: Boolean
)
