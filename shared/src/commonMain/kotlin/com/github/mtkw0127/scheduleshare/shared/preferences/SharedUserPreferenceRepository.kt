package com.github.mtkw0127.scheduleshare.shared.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 共有ユーザーの設定（色とVisibility）を管理するリポジトリ
 */
class SharedUserPreferenceRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private const val USER_COLOR_PREFIX = "user_color_"
        private const val USER_VISIBILITY_PREFIX = "user_visibility_"
    }

    /**
     * 共有ユーザー設定をFlowとして取得
     */
    val sharedUserPreferencesFlow: Flow<SharedUserPreferences> = dataStore.data
        .catch { exception ->
            emit(emptyPreferences())
        }
        .map { preferences ->
            val userColors = mutableMapOf<String, Long>()
            val userVisibility = mutableMapOf<String, Boolean>()

            preferences.asMap().forEach { (key, value) ->
                when {
                    key.name.startsWith(USER_COLOR_PREFIX) -> {
                        val userId = key.name.removePrefix(USER_COLOR_PREFIX)
                        userColors[userId] = value as Long
                    }
                    key.name.startsWith(USER_VISIBILITY_PREFIX) -> {
                        val userId = key.name.removePrefix(USER_VISIBILITY_PREFIX)
                        userVisibility[userId] = value as Boolean
                    }
                }
            }

            SharedUserPreferences(
                userColors = userColors,
                userVisibility = userVisibility
            )
        }

    /**
     * ユーザーの色を保存
     * @param userId ユーザーID
     * @param color カラー値（Long）
     */
    suspend fun setUserColor(userId: String, color: Long) {
        dataStore.edit { preferences ->
            preferences[longPreferencesKey("$USER_COLOR_PREFIX$userId")] = color
        }
    }

    /**
     * ユーザーの表示/非表示を保存
     * @param userId ユーザーID
     * @param isVisible 表示する場合はtrue
     */
    suspend fun setUserVisibility(userId: String, isVisible: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("$USER_VISIBILITY_PREFIX$userId")] = isVisible
        }
    }

    /**
     * ユーザーの色を取得
     * @param userId ユーザーID
     * @return カラー値（保存されていない場合はnull）
     */
    suspend fun getUserColor(userId: String): Long? {
        return dataStore.data
            .catch { exception ->
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[longPreferencesKey("$USER_COLOR_PREFIX$userId")]
            }
            .first()
    }

    /**
     * ユーザーの表示/非表示を取得
     * @param userId ユーザーID
     * @return 表示する場合はtrue（保存されていない場合はtrue）
     */
    suspend fun getUserVisibility(userId: String): Boolean {
        return dataStore.data
            .catch { exception ->
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[booleanPreferencesKey("$USER_VISIBILITY_PREFIX$userId")] ?: true
            }
            .first()
    }

    /**
     * 特定のユーザーの色とVisibilityを取得
     * @param userId ユーザーID
     * @return UserColorAndVisibility（保存されていない場合はnull）
     */
    suspend fun getUserColorAndVisibility(userId: String): UserColorAndVisibility? {
        val color = getUserColor(userId)
        val visibility = getUserVisibility(userId)
        return if (color != null) {
            UserColorAndVisibility(color, visibility)
        } else {
            null
        }
    }

    /**
     * 特定のユーザーの設定をすべて削除
     * @param userId ユーザーID
     */
    suspend fun deleteUserPreferences(userId: String) {
        dataStore.edit { preferences ->
            preferences.remove(longPreferencesKey("$USER_COLOR_PREFIX$userId"))
            preferences.remove(booleanPreferencesKey("$USER_VISIBILITY_PREFIX$userId"))
        }
    }

    /**
     * すべてのユーザー設定を削除
     */
    suspend fun clearAllPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
