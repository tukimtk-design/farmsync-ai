package com.tukimtk.farmsync.sync

import com.tukimtk.farmsync.model.SaveMetadata
import com.tukimtk.farmsync.model.Season
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SaveDecisionEngineTest {

    private val engine = SaveDecisionEngine()

    private fun createBaseSave(
        farmId: String = "Farm1",
        version: String = "1.6.9",
        year: Int = 1,
        season: Season = Season.Spring,
        day: Int = 1,
        money: Int = 500,
        playTime: Long = 1000L,
        hash: String = "hash1",
        timestamp: Long = 100L,
        isValid: Boolean = true
    ): SaveMetadata {
        return SaveMetadata(farmId, version, year, season, day, money, playTime, hash, timestamp, isValid)
    }

    @Test
    fun test_identical_hashes_returns_identical() {
        val pc = createBaseSave(timestamp = 100L)
        val mobile = createBaseSave(timestamp = 999L) // Newer timestamp, but same hash
        val result = engine.evaluate(pc, mobile)
        assertEquals(SyncAction.IDENTICAL, result.action)
    }

    @Test
    fun test_pc_newer_ingame_time_older_timestamp() {
        val pc = createBaseSave(year = 3, hash = "hash_pc", timestamp = 100L) // Older timestamp
        val mobile = createBaseSave(year = 1, hash = "hash_mobile", timestamp = 999L) // Newer timestamp
        val result = engine.evaluate(pc, mobile)
        assertEquals(SyncAction.PUSH_PC_TO_MOBILE, result.action)
    }

    @Test
    fun test_version_incompatibility_blocks_sync() {
        val pc = createBaseSave(version = "1.6.0", hash = "hash_pc")
        val mobile = createBaseSave(version = "1.5.0", hash = "hash_mobile")

        val result1 = engine.evaluate(pc, mobile)
        assertEquals(SyncAction.INCOMPATIBLE_VERSION, result1.action)

        val result2 = engine.evaluate(mobile, pc)
        assertEquals(SyncAction.INCOMPATIBLE_VERSION, result2.action)
    }

    @Test
    fun test_diverging_progress_returns_conflict() {
        // Same day, pc has more money, mobile has more playtime
        val pc = createBaseSave(money = 5000, playTime = 1000L, hash = "hash_pc")
        val mobile = createBaseSave(money = 4000, playTime = 2000L, hash = "hash_mobile")
        val result = engine.evaluate(pc, mobile)
        assertEquals(SyncAction.CONFLICT, result.action)
    }

    @Test
    fun test_symmetry() {
        val pc = createBaseSave(year = 3, hash = "hash_pc")
        val mobile = createBaseSave(year = 1, hash = "hash_mobile")

        val resultPcToMobile = engine.evaluate(pc, mobile)
        val resultMobileToPc = engine.evaluate(mobile, pc)

        assertEquals(SyncAction.PUSH_PC_TO_MOBILE, resultPcToMobile.action)
        assertEquals(SyncAction.PUSH_MOBILE_TO_PC, resultMobileToPc.action)
    }

    @Test
    fun test_malformed_input_fails_closed() {
        val pc = createBaseSave(isValid = false)
        val mobile = createBaseSave(isValid = true)
        val result = engine.evaluate(pc, mobile)
        assertEquals(SyncAction.ERROR_MALFORMED, result.action)
    }
}
