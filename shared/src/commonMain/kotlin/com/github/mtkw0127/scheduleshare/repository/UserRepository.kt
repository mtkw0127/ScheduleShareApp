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
    }

    /**
     * 共有ユーザーIDの一覧を取得
     * @return 共有ユーザーのIDリスト
     */
    fun getSharedUserIds(): Set<User.Id> {
        return sharedUserIds.toSet()
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

            // サンプルユーザーを追加（カップル・家族）
            val partner = User(User.Id("user_001"), "パートナー")
            val child1 = User(User.Id("user_002"), "長男")
            val child2 = User(User.Id("user_003"), "長女")

            repository.addUser(partner)
            repository.addUser(child1)
            repository.addUser(child2)

            // 共有ユーザーとして追加
            repository.addSharedUser(partner.id)
            repository.addSharedUser(child1.id)
            repository.addSharedUser(child2.id)

            return repository
        }
    }
}
