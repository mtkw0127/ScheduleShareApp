package com.github.mtkw0127.scheduleshare

import android.app.Application
import com.github.mtkw0127.scheduleshare.shared.preferences.initializeDataStore

class ScheduleShareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // DataStoreを初期化
        initializeDataStore(this)
    }
}
