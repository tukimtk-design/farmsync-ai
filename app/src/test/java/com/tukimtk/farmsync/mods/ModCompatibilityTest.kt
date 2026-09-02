package com.tukimtk.farmsync.mods

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ModCompatibilityTest {
    
    private val checker = ModCompatibilityChecker()

    @Test
    fun testCompatibleMod() {
        val manifestJson = """
            {
                "Name": "Simple Mod",
                "Author": "User",
                "Version": "1.0.0",
                "Description": "Test",
                "UniqueID": "com.user.simplemod"
            }
        """.trimIndent()
        
        val verdict = checker.checkCompatibility(manifestJson, emptyList())
        assertEquals(CompatibilityVerdict.Compatible, verdict)
    }

    @Test
    fun testMissingDependency() {
        val manifestJson = """
            {
                "Name": "Dep Mod",
                "UniqueID": "com.user.depmod",
                "Dependencies": [
                    {
                        "UniqueID": "Pathoschild.ContentPatcher",
                        "IsRequired": true
                    }
                ]
            }
        """.trimIndent()

        val verdict = checker.checkCompatibility(manifestJson, listOf("some.other.mod"))
        assertTrue(verdict is CompatibilityVerdict.NeedsDependency)
        assertEquals(listOf("Pathoschild.ContentPatcher"), (verdict as CompatibilityVerdict.NeedsDependency).missingList)
    }

    @Test
    fun testOptionalDependency() {
        val manifestJson = """
            {
                "Name": "Dep Mod",
                "UniqueID": "com.user.depmod",
                "Dependencies": [
                    {
                        "UniqueID": "Pathoschild.ContentPatcher",
                        "IsRequired": false
                    }
                ]
            }
        """.trimIndent()

        val verdict = checker.checkCompatibility(manifestJson, emptyList())
        assertEquals(CompatibilityVerdict.Compatible, verdict)
    }

    @Test
    fun testSatisfiedDependency() {
        val manifestJson = """
            {
                "Name": "Dep Mod",
                "UniqueID": "com.user.depmod",
                "Dependencies": [
                    {
                        "UniqueID": "Pathoschild.ContentPatcher",
                        "IsRequired": true
                    }
                ]
            }
        """.trimIndent()

        val verdict = checker.checkCompatibility(manifestJson, listOf("Pathoschild.ContentPatcher"))
        assertEquals(CompatibilityVerdict.Compatible, verdict)
    }

    @Test
    fun testMissingContentPackFor() {
        val manifestJson = """
            {
                "Name": "CP Mod",
                "UniqueID": "com.user.cpmod",
                "ContentPackFor": {
                    "UniqueID": "Pathoschild.ContentPatcher"
                }
            }
        """.trimIndent()

        val verdict = checker.checkCompatibility(manifestJson, emptyList())
        assertTrue(verdict is CompatibilityVerdict.NeedsDependency)
        assertEquals(listOf("Pathoschild.ContentPatcher"), (verdict as CompatibilityVerdict.NeedsDependency).missingList)
    }

    @Test
    fun testSatisfiedContentPackFor() {
        val manifestJson = """
            {
                "Name": "CP Mod",
                "UniqueID": "com.user.cpmod",
                "ContentPackFor": {
                    "UniqueID": "Pathoschild.ContentPatcher"
                }
            }
        """.trimIndent()

        val verdict = checker.checkCompatibility(manifestJson, listOf("Pathoschild.ContentPatcher"))
        assertEquals(CompatibilityVerdict.Compatible, verdict)
    }

    @Test
    fun testIncompatibleMod() {
        val manifestJson = """
            {
                "Name": "Bad Mod",
                "UniqueID": "com.example.pcharmony"
            }
        """.trimIndent()
        
        val verdict = checker.checkCompatibility(manifestJson, emptyList())
        assertTrue(verdict is CompatibilityVerdict.Incompatible)
    }
    
    @Test
    fun testImplicitRequiredDependency() {
        val manifestJson = """
            {
                "Name": "Dep Mod",
                "UniqueID": "com.user.depmod",
                "Dependencies": [
                    {
                        "UniqueID": "spacechase0.SpaceCore"
                    }
                ]
            }
        """.trimIndent()

        val verdict = checker.checkCompatibility(manifestJson, emptyList())
        assertTrue(verdict is CompatibilityVerdict.NeedsDependency)
        assertEquals(listOf("spacechase0.SpaceCore"), (verdict as CompatibilityVerdict.NeedsDependency).missingList)
    }

    @Test
    fun testMinimumApiVersionCompatible() {
        val manifestJson = """
            {
                "Name": "Api Mod",
                "UniqueID": "com.user.apimod",
                "MinimumApiVersion": "3.18.2"
            }
        """.trimIndent()

        val verdict = checker.checkCompatibility(manifestJson, emptyList(), "3.18.3")
        assertEquals(CompatibilityVerdict.Compatible, verdict)
    }

    @Test
    fun testMinimumApiVersionExact() {
        val manifestJson = """
            {
                "Name": "Api Mod",
                "UniqueID": "com.user.apimod",
                "MinimumApiVersion": "4.0.0"
            }
        """.trimIndent()

        val verdict = checker.checkCompatibility(manifestJson, emptyList(), "4.0.0")
        assertEquals(CompatibilityVerdict.Compatible, verdict)
    }

    @Test
    fun testMinimumApiVersionIncompatible() {
        val manifestJson = """
            {
                "Name": "Api Mod",
                "UniqueID": "com.user.apimod",
                "MinimumApiVersion": "4.0.0"
            }
        """.trimIndent()

        val verdict = checker.checkCompatibility(manifestJson, emptyList(), "3.18.3")
        assertTrue(verdict is CompatibilityVerdict.Incompatible)
        assertTrue((verdict as CompatibilityVerdict.Incompatible).reason.contains("Requires SMAPI 4.0.0"))
    }
}
