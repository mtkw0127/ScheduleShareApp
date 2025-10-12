package com.github.mtkw0127.scheduleshare.shared.preferences

/**
 * 共有ユーザーの設定を表すデータクラス
 */
data class SharedUserPreferences(
    /**
     * ユーザーID -> カラー値のマップ
     */
    val userColors: Map<String, Long> = emptyMap(),

    /**
     * ユーザーID -> 表示/非表示のマップ
     */
    val userVisibility: Map<String, Boolean> = emptyMap()
)

/**
 * 個別のユーザー設定
 */
data class UserColorAndVisibility(
    val color: Long,
    val isVisible: Boolean
)
