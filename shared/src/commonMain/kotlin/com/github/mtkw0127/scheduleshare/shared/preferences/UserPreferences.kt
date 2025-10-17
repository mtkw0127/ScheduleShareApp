package com.github.mtkw0127.scheduleshare.shared.preferences

/**
 * ユーザーの個人設定を表すデータクラス
 */
data class UserPreferences(
    /**
     * 予定の詳細画面の表示モード
     * true: ユーザーごとに縦割りで表示
     * false: すべてのユーザーの予定を一緒に表示
     */
    val isColumnLayoutEnabled: Boolean = true,

    /**
     * カレンダー画面の表示モード
     * "Calendar": カレンダー表示
     * "List": リスト表示
     */
    val calendarViewMode: String = "Calendar"
)
