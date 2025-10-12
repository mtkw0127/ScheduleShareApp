package com.github.mtkw0127.scheduleshare.model.user

/**
 * ユーザーのUIモデル
 */
data class User(
    val id: Id,
    val name: String,
    val iconUrl: String? = null
) {
    data class Id(val value: String)

    companion object {
        /**
         * テストユーザーを作成
         */
        fun createTest(): User {
            return User(
                id = Id("test_user_001"),
                name = "あなた",
                iconUrl = null
            )
        }
    }
}
