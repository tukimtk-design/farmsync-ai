package com.tukimtk.farmsync.game

interface GameProfile {
    val gameName: String
    val expectedSaveDirectory: String
}

class StardewProfile : GameProfile {
    override val gameName = "Stardew Valley"
    override val expectedSaveDirectory = "StardewValley"
}

class TerrariaProfile : GameProfile {
    override val gameName = "Terraria"
    override val expectedSaveDirectory = "Terraria"
}
