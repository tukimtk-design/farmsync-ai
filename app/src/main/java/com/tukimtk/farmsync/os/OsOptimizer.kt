package com.tukimtk.farmsync.os

interface OsOptimizer {
    fun requestBatteryIgnoreOptimizations(): Boolean
    fun setupBackgroundKeepAlive(): Boolean
}
