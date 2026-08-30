package com.tukimtk.farmsync.sync

data class SaveNode(val id: String, val timestamp: Long, val parentId: String?)

class SaveBranchManager {
    fun createBranch(node: SaveNode, newName: String): SaveNode {
        return SaveNode(newName, System.currentTimeMillis(), node.id)
    }
}
