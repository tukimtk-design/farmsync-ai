package com.tukimtk.farmsync.game.stardew

import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

class ScanOutcomeTest {

    private lateinit var bridge: ShizukuSaveBridge

    @Before
    fun setup() {
        val handler = InvocationHandler { _, _, _ -> null }
        val context = Proxy.newProxyInstance(
            Context::class.java.classLoader,
            arrayOf(Context::class.java),
            handler
        ) as Context
        bridge = ShizukuSaveBridge(context)
        bridge.permissionOverride = true // Simulate Shizuku ready
    }

    private val validSaveGameInfo = "<SaveGame><Farmer><name>TestFarmer</name><farmName>TestFarm</farmName><money>500</money><currentSeason>spring</currentSeason><dayOfMonth>1</dayOfMonth><year>1</year></Farmer></SaveGame>"
    private val validMainSave = "<SaveGame><player><name>TestFarmer</name></player></SaveGame>"

    @Test
    fun testOfficialUppercaseSavesRootSelected() {
        bridge.shellExecutor = { cmd ->
            when (cmd) {
                "ls -1 \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves\"" -> Pair(0, "TestSave_123456")
                "cat \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/TestSave_123456/SaveGameInfo\"" -> Pair(0, validSaveGameInfo)
                "cat \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/TestSave_123456/TestSave_123456\"" -> Pair(0, validMainSave)
                else -> Pair(-1, "No such file or directory")
            }
        }
        val result = bridge.scanRealSaves()
        assertTrue(result is SaveScanResult.SavesFound)
        assertEquals(1, (result as SaveScanResult.SavesFound).saves.size)
        assertEquals("TestSave_123456", result.saves[0].folderName)
    }

    @Test
    fun testOfficialLowercaseSavesRootSelected() {
        bridge.shellExecutor = { cmd ->
            when (cmd) {
                "ls -1 \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/saves\"" -> Pair(0, "TestSave_123456")
                "cat \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/saves/TestSave_123456/SaveGameInfo\"" -> Pair(0, validSaveGameInfo)
                "cat \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/saves/TestSave_123456/TestSave_123456\"" -> Pair(0, validMainSave)
                else -> Pair(-1, "No such file or directory")
            }
        }
        val result = bridge.scanRealSaves()
        assertTrue(result is SaveScanResult.SavesFound)
        assertEquals(1, (result as SaveScanResult.SavesFound).saves.size)
    }

    @Test
    fun testEquivalentCaseVariantsDoNotDuplicateOutput() {
        bridge.shellExecutor = { cmd ->
            when (cmd) {
                "ls -1 \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves\"" -> Pair(0, "TestSave_123456")
                "ls -1 \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/saves\"" -> Pair(0, "TestSave_123456")
                "cat \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/TestSave_123456/SaveGameInfo\"" -> Pair(0, validSaveGameInfo)
                "cat \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/TestSave_123456/TestSave_123456\"" -> Pair(0, validMainSave)
                "cat \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/saves/TestSave_123456/SaveGameInfo\"" -> Pair(0, validSaveGameInfo)
                "cat \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/saves/TestSave_123456/TestSave_123456\"" -> Pair(0, validMainSave)
                else -> Pair(-1, "No such file or directory")
            }
        }
        val result = bridge.scanRealSaves()
        assertTrue(result is SaveScanResult.SavesFound)
        assertEquals(1, (result as SaveScanResult.SavesFound).saves.size)
    }

    @Test
    fun testOfficialComChucklefishRootIsPreferredOverComZane() {
        bridge.shellExecutor = { cmd ->
            when (cmd) {
                "ls -1 \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves\"" -> Pair(0, "TestSave_123456")
                "ls -1 \"/storage/emulated/0/Android/data/com.zane.stardewvalley/files/Saves\"" -> Pair(0, "TestSave_123456")
                "cat \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/TestSave_123456/SaveGameInfo\"" -> Pair(0, validSaveGameInfo)
                "cat \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/TestSave_123456/TestSave_123456\"" -> Pair(0, validMainSave)
                "cat \"/storage/emulated/0/Android/data/com.zane.stardewvalley/files/Saves/TestSave_123456/SaveGameInfo\"" -> Pair(0, validSaveGameInfo)
                "cat \"/storage/emulated/0/Android/data/com.zane.stardewvalley/files/Saves/TestSave_123456/TestSave_123456\"" -> Pair(0, validMainSave)
                else -> Pair(-1, "No such file or directory")
            }
        }
        val result = bridge.scanRealSaves()
        assertTrue(result is SaveScanResult.SavesFound)
        assertEquals(1, (result as SaveScanResult.SavesFound).saves.size)
        // Verify path is from chucklefish
        assertTrue(result.saves[0].folderPath.contains("com.chucklefish.stardewvalley"))
    }

    @Test
    fun testLegacyComZaneRootRemainsSupported() {
        bridge.shellExecutor = { cmd ->
            when (cmd) {
                "ls -1 \"/storage/emulated/0/Android/data/com.zane.stardewvalley/files/Saves\"" -> Pair(0, "Zane_456")
                "cat \"/storage/emulated/0/Android/data/com.zane.stardewvalley/files/Saves/Zane_456/SaveGameInfo\"" -> Pair(0, validSaveGameInfo)
                "cat \"/storage/emulated/0/Android/data/com.zane.stardewvalley/files/Saves/Zane_456/Zane_456\"" -> Pair(0, validMainSave)
                else -> Pair(-1, "No such file or directory")
            }
        }
        val result = bridge.scanRealSaves()
        assertTrue(result is SaveScanResult.SavesFound)
        assertEquals(1, (result as SaveScanResult.SavesFound).saves.size)
        assertTrue(result.saves[0].folderPath.contains("com.zane.stardewvalley"))
    }

    @Test
    fun testLegacySharedStorageRootRemainsSupported() {
        bridge.shellExecutor = { cmd ->
            when (cmd) {
                "ls -1 \"/storage/emulated/0/StardewValley\"" -> Pair(0, "Shared_789")
                "cat \"/storage/emulated/0/StardewValley/Shared_789/SaveGameInfo\"" -> Pair(0, validSaveGameInfo)
                "cat \"/storage/emulated/0/StardewValley/Shared_789/Shared_789\"" -> Pair(0, validMainSave)
                else -> Pair(-1, "No such file or directory")
            }
        }
        val result = bridge.scanRealSaves()
        assertTrue(result is SaveScanResult.SavesFound)
        assertEquals(1, (result as SaveScanResult.SavesFound).saves.size)
        assertTrue(result.saves[0].folderPath.contains("StardewValley"))
    }

    @Test
    fun testNoCandidateRootExistsGivesNoSavesFound() {
        bridge.shellExecutor = { _ -> Pair(-1, "No such file or directory") }
        val result = bridge.scanRealSaves()
        assertTrue(result is SaveScanResult.NoSavesFound)
    }

    @Test
    fun testCandidateRootExistsButContainsNoValidSaveGivesNoSavesFound() {
        bridge.shellExecutor = { cmd ->
            when (cmd) {
                "ls -1 \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves\"" -> Pair(0, "")
                else -> Pair(-1, "No such file or directory")
            }
        }
        val result = bridge.scanRealSaves()
        assertTrue(result is SaveScanResult.NoSavesFound)
    }

    @Test
    fun testCandidateMissingTheMainSaveFileRejected() {
        bridge.shellExecutor = { cmd ->
            when (cmd) {
                "ls -1 \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves\"" -> Pair(0, "TestSave_123456")
                "cat \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/TestSave_123456/SaveGameInfo\"" -> Pair(0, validSaveGameInfo)
                "cat \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/TestSave_123456/TestSave_123456\"" -> Pair(-1, "No such file")
                else -> Pair(-1, "No such file or directory")
            }
        }
        val result = bridge.scanRealSaves()
        assertTrue(result is SaveScanResult.NoSavesFound)
    }

    @Test
    fun testCandidateMissingSaveGameInfoRejected() {
        bridge.shellExecutor = { cmd ->
            when (cmd) {
                "ls -1 \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves\"" -> Pair(0, "TestSave_123456")
                "cat \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/TestSave_123456/SaveGameInfo\"" -> Pair(-1, "No such file")
                "cat \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/TestSave_123456/TestSave_123456\"" -> Pair(0, validMainSave)
                else -> Pair(-1, "No such file or directory")
            }
        }
        val result = bridge.scanRealSaves()
        assertTrue(result is SaveScanResult.NoSavesFound)
    }

    @Test
    fun testPermissionDeniedGivesScanFailedWithAccessDenied() {
        bridge.shellExecutor = { cmd ->
            if (cmd.startsWith("ls -ld")) {
                Pair(-1, "Permission denied")
            } else {
                Pair(-1, "ls: cannot open directory: Permission denied")
            }
        }
        val result = bridge.scanRealSaves()
        assertTrue(result is SaveScanResult.ScanFailed)
        assertEquals(ScanFailureReason.AccessDenied, (result as SaveScanResult.ScanFailed).reason)
    }

    @Test
    fun testNonZeroCommandGivesScanFailedWithCommandFailed() {
        bridge.shellExecutor = { cmd ->
            if (cmd.startsWith("ls -ld")) {
                Pair(-1, "Some weird error")
            } else {
                Pair(-1, "Some weird command failure")
            }
        }
        val result = bridge.scanRealSaves()
        assertTrue(result is SaveScanResult.ScanFailed)
        assertEquals(ScanFailureReason.CommandFailed, (result as SaveScanResult.ScanFailed).reason)
    }

    @Test
    fun testUnexpectedExceptionGivesBoundedScanFailed() {
        bridge.shellExecutor = { _ -> throw RuntimeException("Simulated crash") }
        val result = bridge.scanRealSaves()
        assertTrue(result is SaveScanResult.ScanFailed)
        assertEquals(ScanFailureReason.UnexpectedFailure, (result as SaveScanResult.ScanFailed).reason)
    }

    @Test
    fun testValidMainSaveAndSaveGameInfoPairGivesSavesFound() {
        bridge.shellExecutor = { cmd ->
            when (cmd) {
                "ls -1 \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves\"" -> Pair(0, "TestSave_123456")
                "cat \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/TestSave_123456/SaveGameInfo\"" -> Pair(0, validSaveGameInfo)
                "cat \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/TestSave_123456/TestSave_123456\"" -> Pair(0, validMainSave)
                else -> Pair(-1, "No such file or directory")
            }
        }
        val result = bridge.scanRealSaves()
        assertTrue(result is SaveScanResult.SavesFound)
        assertEquals(1, (result as SaveScanResult.SavesFound).saves.size)
    }

    @Test
    fun testBackupArtifactsAreRejected() {
        bridge.shellExecutor = { cmd ->
            when (cmd) {
                "ls -1 \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves\"" -> Pair(0, "TestSave_123456_SVBAK")
                "cat \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/TestSave_123456_SVBAK/SaveGameInfo\"" -> Pair(0, validSaveGameInfo)
                "cat \"/storage/emulated/0/Android/data/com.chucklefish.stardewvalley/files/Saves/TestSave_123456_SVBAK/TestSave_123456_SVBAK\"" -> Pair(0, validMainSave)
                else -> Pair(-1, "No such file or directory")
            }
        }
        val result = bridge.scanRealSaves()
        assertTrue(result is SaveScanResult.NoSavesFound)
    }
}
