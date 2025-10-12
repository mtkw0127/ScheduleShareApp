package com.github.mtkw0127.scheduleshare.repository

import com.github.mtkw0127.scheduleshare.model.schedule.Schedule
import com.github.mtkw0127.scheduleshare.model.user.User
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number

/**
 * スケジュールのリポジトリ
 */
class ScheduleRepository(
    private val userRepository: UserRepository
) {
    // TODO: 実際のデータソースに置き換える
    private val schedules = mutableListOf<Schedule>()

    /**
     * その月の予定を取得（共有ユーザーの予定も含む）
     * @param year 年
     * @param month 月
     * @return その月の予定リスト
     */
    fun getSchedulesByMonth(year: Int, month: Int): List<Schedule> {
        val sharedUserIds = userRepository.getSharedUserIds()
        return schedules.filter { schedule ->
            val isUserVisible =
                schedule.createUser.id == User.createTest().id || sharedUserIds.contains(schedule.createUser.id)
            if (!isUserVisible) return@filter false

            // 開始日または終了日がその月に含まれるか、その月をまたいでいる場合
            val scheduleStartInMonth =
                schedule.startDateTime.date.year == year && schedule.startDateTime.date.month.number == month
            val scheduleEndInMonth =
                schedule.endDateTime.date.year == year && schedule.endDateTime.date.month.number == month
            val scheduleSpansMonth =
                schedule.startDateTime.date.year <= year && schedule.startDateTime.date.month.number <= month &&
                        schedule.endDateTime.date.year >= year && schedule.endDateTime.date.month.number >= month
            scheduleStartInMonth || scheduleEndInMonth || scheduleSpansMonth
        }
    }

    /**
     * その日の予定を取得（共有ユーザーの予定も含む）
     * @param date 日付
     * @return その日の予定リスト（時刻順にソート）
     */
    fun getSchedulesByDate(date: LocalDate): List<Schedule> {
        val sharedUserIds = userRepository.getSharedUserIds()
        return schedules.filter { schedule ->
            val isUserVisible =
                schedule.createUser.id == User.createTest().id || sharedUserIds.contains(schedule.createUser.id)
            if (!isUserVisible) return@filter false

            // 指定日が開始日と終了日の間にあるかチェック
            date >= schedule.startDateTime.date && date <= schedule.endDateTime.date
        }.sortedWith(compareBy {
            it.startDateTime
        })
    }

    /**
     * 予定を追加
     * @param schedule 追加する予定
     */
    fun addSchedule(schedule: Schedule) {
        schedules.add(schedule)
    }

    /**
     * IDで予定を取得
     * @param scheduleId 取得する予定のID
     * @return 予定、見つからない場合はnull
     */
    fun getScheduleById(scheduleId: Schedule.Id): Schedule? {
        return schedules.firstOrNull { it.id == scheduleId }
    }

    /**
     * 予定を削除
     * @param scheduleId 削除する予定のID
     */
    fun deleteSchedule(scheduleId: Schedule.Id) {
        schedules.removeAll { it.id == scheduleId }
    }

    /**
     * 予定を更新
     * @param schedule 更新する予定
     */
    fun updateSchedule(schedule: Schedule) {
        val index = schedules.indexOfFirst { it.id == schedule.id }
        if (index != -1) {
            schedules[index] = schedule
        }
    }

    companion object {
        /**
         * サンプルデータを持つリポジトリを作成
         */
        fun createWithSampleData(): ScheduleRepository {
            val userRepository = UserRepository.createWithSampleData()
            val repository = ScheduleRepository(userRepository)
            val testUser = User.createTest()

            // 共有ユーザーのサンプルデータ
            val sharedUser1 = User(User.Id("user_001"), "山田太郎")
            val sharedUser2 = User(User.Id("user_002"), "佐藤花子")
            val sharedUser3 = User(User.Id("user_003"), "鈴木一郎")

            // 自分の予定
            repository.addSchedule(
                Schedule.createAllDay(
                    id = Schedule.Id("1"),
                    title = "終日イベント",
                    description = "終日のサンプル予定",
                    date = LocalDate(2025, 10, 8),
                    createUser = testUser,
                    assignedUsers = emptyList(),
                )
            )

            repository.addSchedule(
                Schedule.createTimed(
                    id = Schedule.Id("2"),
                    title = "ミーティング",
                    description = "プロジェクト定例会議",
                    date = LocalDate(2025, 10, 8),
                    createUser = testUser,
                    assignedUsers = emptyList(),
                    startTime = LocalTime(10, 0),
                    endTime = LocalTime(11, 0),
                )
            )

            repository.addSchedule(
                Schedule.createTimed(
                    id = Schedule.Id("3"),
                    title = "ランチ",
                    description = "チームランチ",
                    date = LocalDate(2025, 10, 8),
                    createUser = testUser,
                    assignedUsers = emptyList(),
                    startTime = LocalTime(12, 0),
                    endTime = LocalTime(13, 0)
                )
            )

            // 共有ユーザーの予定
            repository.addSchedule(
                Schedule.createTimed(
                    id = Schedule.Id("4"),
                    title = "山田さんの会議",
                    description = "営業部ミーティング",
                    date = LocalDate(2025, 10, 8),
                    createUser = sharedUser1,
                    assignedUsers = emptyList(),
                    startTime = LocalTime(14, 0),
                    endTime = LocalTime(15, 0)
                )
            )

            repository.addSchedule(
                Schedule.createTimed(
                    id = Schedule.Id("5"),
                    title = "佐藤さんの打ち合わせ",
                    description = "クライアントとの打ち合わせ",
                    date = LocalDate(2025, 10, 8),
                    createUser = sharedUser2,
                    assignedUsers = emptyList(),
                    startTime = LocalTime(15, 30),
                    endTime = LocalTime(17, 0)
                )
            )

            repository.addSchedule(
                Schedule.createAllDay(
                    id = Schedule.Id("6"),
                    title = "鈴木さんの出張",
                    description = "大阪出張",
                    date = LocalDate(2025, 10, 8),
                    createUser = sharedUser3,
                    assignedUsers = emptyList(),
                )
            )

            return repository
        }
    }
}
