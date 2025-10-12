package com.github.mtkw0127.scheduleshare.shared.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * ユーザー設定を管理するリポジトリ
 */
class UserPreferenceRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val IS_COLUMN_LAYOUT_ENABLED = booleanPreferencesKey("is_column_layout_enabled")
    }

    /**
     * ユーザー設定をFlowとして取得
     */
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            // エラーが発生した場合は空のPreferencesを返す
            emit(emptyPreferences())
        }
        .map { preferences ->
            UserPreferences(
                isColumnLayoutEnabled = preferences[IS_COLUMN_LAYOUT_ENABLED] ?: true
            )
        }

    /**
     * 予定の詳細画面の表示モードを保存
     * @param enabled true: ユーザーごとに縦割りで表示、false: すべてのユーザーの予定を一緒に表示
     */
    suspend fun setColumnLayoutEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_COLUMN_LAYOUT_ENABLED] = enabled
        }
    }

    /**
     * 予定の詳細画面の表示モードを取得
     * @return true: ユーザーごとに縦割りで表示、false: すべてのユーザーの予定を一緒に表示
     */
    suspend fun isColumnLayoutEnabled(): Boolean {
        return dataStore.data
            .catch { exception ->
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[IS_COLUMN_LAYOUT_ENABLED] ?: true
            }
            .first()
    }
}
