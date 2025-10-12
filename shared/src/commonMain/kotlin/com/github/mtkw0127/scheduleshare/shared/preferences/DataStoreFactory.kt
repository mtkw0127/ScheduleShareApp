package com.github.mtkw0127.scheduleshare.shared.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

/**
 * DataStoreインスタンスを作成するための関数
 * プラットフォーム固有の実装で提供される
 */
expect fun createDataStore(): DataStore<Preferences>
