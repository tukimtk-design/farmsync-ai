package com.tukimtk.farmsync

import com.tukimtk.farmsync.mods.compareVersions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModLifecycleRemediationTest {
    @Test
    fun testCompareVersionsEqual() {
        assertEquals(0, compareVersions("v2.9.1", "2.9.1"))
        assertEquals(0, compareVersions("1.0", "1.0.0"))
        assertEquals(0, compareVersions("1.1.0", "1.1"))
    }

    @Test
    fun testCompareVersionsGreater() {
        assertTrue(compareVersions("2.9.2", "2.9.1") > 0)
        assertTrue(compareVersions("1.1", "1.0.1") > 0)
        assertTrue(compareVersions("v2.0", "1.9.9") > 0)
    }

    @Test
    fun testCompareVersionsLesser() {
        assertTrue(compareVersions("2.9.1", "2.9.2") < 0)
        assertTrue(compareVersions("1.0.1", "1.1") < 0)
        assertTrue(compareVersions("1.9.9", "v2.0") < 0)
    }
}
