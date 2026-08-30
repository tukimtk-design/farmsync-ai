package com.tukimtk.farmsync

import com.tukimtk.farmsync.sync.SaveBranchManager
import com.tukimtk.farmsync.sync.SaveNode
import org.junit.Test
import org.junit.Assert.assertEquals

class SaveBranchManagerTest {
    @Test
    fun testBranchCreation() {
        val manager = SaveBranchManager()
        val rootNode = SaveNode("root_1", 1000L, null)
        val branch = manager.createBranch(rootNode, "branch_a")

        assertEquals("branch_a", branch.id)
        assertEquals("root_1", branch.parentId)
    }
}
