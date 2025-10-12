package com.github.mtkw0127.scheduleshare.repository

import com.github.mtkw0127.scheduleshare.model.user.User
import com.github.mtkw0127.scheduleshare.model.user.UserColor

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

    // ユーザーの色設定
    private val userColors = mutableMapOf<User.Id, UserColor>()

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
        // デフォルトの色を設定
        if (!userColors.containsKey(userId)) {
            userColors[userId] = UserColor.default()
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
     * ユーザーの色を設定
     * @param userId ユーザーのID
     * @param color 設定する色
     */
    fun setUserColor(userId: User.Id, color: UserColor) {
        if (sharedUserIds.contains(userId)) {
            userColors[userId] = color
        }
    }

    /**
     * ユーザーの色を取得
     * @param userId ユーザーのID
     * @return ユーザーの色（デフォルトBLUE）
     */
    fun getUserColor(userId: User.Id): UserColor {
        return userColors[userId] ?: UserColor.default()
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

            // 各ユーザーに異なる色を設定
            repository.setUserColor(partner.id, UserColor.PURPLE)
            repository.setUserColor(child1.id, UserColor.GREEN)
            repository.setUserColor(child2.id, UserColor.ORANGE)

            return repository
        }
    }
}
