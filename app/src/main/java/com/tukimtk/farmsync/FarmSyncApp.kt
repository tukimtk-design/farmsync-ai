package com.tukimtk.farmsync

import android.app.Application
import com.tukimtk.farmsync.shizuku.ShizukuStateManager

class FarmSyncApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ShizukuStateManager.init(this)
    }
}
