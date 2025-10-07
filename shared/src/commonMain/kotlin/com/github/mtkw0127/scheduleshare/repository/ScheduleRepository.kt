package com.github.mtkw0127.scheduleshare.repository

import com.github.mtkw0127.scheduleshare.model.schedule.Schedule
import com.github.mtkw0127.scheduleshare.model.user.User
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * スケジュールのリポジトリ
 */
class ScheduleRepository {
    // TODO: 実際のデータソースに置き換える
    private val schedules = mutableListOf<Schedule>()

    /**
     * その月の予定を取得
     * @param year 年
     * @param month 月
     * @return その月の予定リスト
     */
    fun getSchedulesByMonth(year: Int, month: Int): List<Schedule> {
        return schedules.filter {
            it.date.year == year && it.date.monthNumber == month
        }
    }

    /**
     * その日の予定を取得
     * @param date 日付
     * @return その日の予定リスト（時刻順にソート）
     */
    fun getSchedulesByDate(date: LocalDate): List<Schedule> {
        return schedules.filter {
            it.date == date
        }.sortedWith(compareBy {
            when (val timeType = it.timeType) {
                is Schedule.TimeType.AllDay -> LocalTime(0, 0)
                is Schedule.TimeType.Timed -> timeType.start
            }
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
            val repository = ScheduleRepository()
            val testUser = User.createTest()

            // サンプルデータを追加
            repository.addSchedule(
                Schedule.createAllDay(
                    id = Schedule.Id("1"),
                    title = "終日イベント",
                    description = "終日のサンプル予定",
                    date = LocalDate(2025, 10, 8),
                    user = testUser
                )
            )

            repository.addSchedule(
                Schedule.createTimed(
                    id = Schedule.Id("2"),
                    title = "ミーティング",
                    description = "プロジェクト定例会議",
                    date = LocalDate(2025, 10, 8),
                    user = testUser,
                    startTime = LocalTime(10, 0),
                    endTime = LocalTime(11, 0)
                )
            )

            repository.addSchedule(
                Schedule.createTimed(
                    id = Schedule.Id("3"),
                    title = "ランチ",
                    description = "チームランチ",
                    date = LocalDate(2025, 10, 8),
                    user = testUser,
                    startTime = LocalTime(12, 0),
                    endTime = LocalTime(13, 0)
                )
            )

            return repository
        }
    }
}
