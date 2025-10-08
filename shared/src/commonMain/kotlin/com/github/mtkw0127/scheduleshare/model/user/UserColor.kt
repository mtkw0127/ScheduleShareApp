package com.github.mtkw0127.scheduleshare.model.user

/**
 * ユーザーの予定表示色
 */
enum class UserColor(val value: Long) {
    BLUE(0xFF2196F3),
    GREEN(0xFF4CAF50),
    ORANGE(0xFFFF9800),
    PURPLE(0xFF9C27B0),
    RED(0xFFF44336);

    companion object {
        /**
         * デフォルトの色を取得
         */
        fun default(): UserColor = BLUE

        /**
         * 値から色を取得
         */
        fun fromValue(value: Long): UserColor {
            return entries.firstOrNull { it.value == value } ?: default()
        }
    }
}
