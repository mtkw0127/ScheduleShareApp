package com.github.mtkw0127.scheduleshare.repository

import com.github.mtkw0127.scheduleshare.model.user.User

/**
 * ユーザーのリポジトリ
 */
class UserRepository {
    // TODO: 実際のデータソースに置き換える
    private val users = mutableMapOf<User.Id, User>()

    // 共有中のユーザーID一覧
    private val sharedUserIds = mutableSetOf<User.Id>()

    // ユーザーの表示/非表示状態
    private val userVisibility = mutableMapOf<User.Id, Boolean>()

    /**
     * 共有中のユーザー一覧を取得
     * @return 共有中のユーザーリスト
     */
    fun getSharedUsers(): List<User> {
        return sharedUserIds.mapNotNull { userId ->
            users[userId]
        }
    }

    /**
     * 共有ユーザーを追加
     * @param userId 共有するユーザーのID
     */
    fun addSharedUser(userId: User.Id) {
        sharedUserIds.add(userId)
        // デフォルトで表示ON
        if (!userVisibility.containsKey(userId)) {
            userVisibility[userId] = true
        }
    }

    /**
     * 共有ユーザーIDの一覧を取得（表示ONのユーザーのみ）
     * @return 共有ユーザーのIDリスト
     */
    fun getSharedUserIds(): Set<User.Id> {
        return sharedUserIds.filter { userId ->
            userVisibility[userId] ?: true
        }.toSet()
    }

    /**
     * ユーザーの表示/非表示を設定
     * @param userId ユーザーのID
     * @param visible 表示するかどうか
     */
    fun setUserVisibility(userId: User.Id, visible: Boolean) {
        if (sharedUserIds.contains(userId)) {
            userVisibility[userId] = visible
        }
    }

    /**
     * ユーザーの表示/非表示状態を取得
     * @param userId ユーザーのID
     * @return 表示するかどうか（デフォルトtrue）
     */
    fun getUserVisibility(userId: User.Id): Boolean {
        return userVisibility[userId] ?: true
    }

    /**
     * ユーザーを追加
     * @param user 追加するユーザー
     */
    fun addUser(user: User) {
        users[user.id] = user
    }

    companion object {
        /**
         * サンプルデータを持つリポジトリを作成
         */
        fun createWithSampleData(): UserRepository {
            val repository = UserRepository()

            // サンプルユーザーを追加
            val sharedUser1 = User(User.Id("user_001"), "山田太郎")
            val sharedUser2 = User(User.Id("user_002"), "佐藤花子")
            val sharedUser3 = User(User.Id("user_003"), "鈴木一郎")

            repository.addUser(sharedUser1)
            repository.addUser(sharedUser2)
            repository.addUser(sharedUser3)

            // 共有ユーザーとして追加
            repository.addSharedUser(sharedUser1.id)
            repository.addSharedUser(sharedUser2.id)
            repository.addSharedUser(sharedUser3.id)

            return repository
        }
    }
}
