package com.github.mtkw0127.scheduleshare.shared.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

private lateinit var appContext: Context

/**
 * Android Contextを初期化する関数
 * Application.onCreate()で呼び出す必要がある
 */
fun initializeDataStore(context: Context) {
    appContext = context.applicationContext
}

/**
 * Android用のDataStoreインスタンスを作成
 */
actual fun createDataStore(): DataStore<Preferences> {
    return appContext.dataStore
}
