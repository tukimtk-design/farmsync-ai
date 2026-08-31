package com.tukimtk.farmsync.shizuku

import org.junit.Assert.assertEquals
import org.junit.Test

class ShizukuStateEvaluatorTest {
    @Test
    fun testNotInstalled() {
        val state = ShizukuStateEvaluator.evaluate(false, false, false, false)
        assertEquals(ShizukuState.NotInstalled, state)
    }

    @Test
    fun testNotRunning() {
        val state = ShizukuStateEvaluator.evaluate(true, false, false, false)
        assertEquals(ShizukuState.NotRunning, state)
    }

    @Test
    fun testVersionTooOld() {
        val state = ShizukuStateEvaluator.evaluate(true, true, false, false)
        assertEquals(ShizukuState.VersionTooOld, state)
    }

    @Test
    fun testPermissionRequired() {
        val state = ShizukuStateEvaluator.evaluate(true, true, true, false)
        assertEquals(ShizukuState.PermissionRequired, state)
    }

    @Test
    fun testReady() {
        val state = ShizukuStateEvaluator.evaluate(true, true, true, true)
        assertEquals(ShizukuState.Ready, state)
    }
}
