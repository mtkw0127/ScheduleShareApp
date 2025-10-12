package com.github.mtkw0127.scheduleshare.model.schedule

import com.github.mtkw0127.scheduleshare.model.user.User
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

// 予定の日時パターンを表す sealed interface
sealed interface ScheduleTime {

    sealed interface MultiDateSchedule {
        fun isThisWeek(date: LocalDate): Boolean
        fun duration(): Int
    }

    sealed interface SingleDateSchedule

    val startDate: LocalDate

    // 開始と終了が別日（時刻ありで日を跨ぐ）
    data class DateTimeRange(
        override val startDate: LocalDate,
        val start: LocalDateTime,
        val end: LocalDateTime
    ) : ScheduleTime, MultiDateSchedule {
        override fun isThisWeek(date: LocalDate): Boolean {
            val weekDates = date..date.plus(6, DateTimeUnit.DAY)
            return start.date in weekDates || end.date in weekDates
        }

        override fun duration(): Int {
            return start.date.daysUntil(end.date)
        }
    }

    // 開始と終了が別日（終日で日を跨ぐ）
    data class AllDayRange(
        override val startDate: LocalDate,
        val endDate: LocalDate
    ) : ScheduleTime, MultiDateSchedule {
        override fun isThisWeek(date: LocalDate): Boolean {
            val weekDates = date..date.plus(6, DateTimeUnit.DAY)
            return startDate in weekDates || endDate in weekDates
        }

        override fun duration(): Int {
            return startDate.daysUntil(endDate)
        }
    }

    // 同一日（一日終日の予定）
    data class SingleAllDay(
        override val startDate: LocalDate
    ) : ScheduleTime, SingleDateSchedule

    // 同一日（一日の中で何時から何時の予定）
    data class SingleTimeDay(
        override val startDate: LocalDate,
        val startTime: LocalTime,
        val endTime: LocalTime
    ) : ScheduleTime, SingleDateSchedule
}

/**
 * 予定のUIモデル
 */
data class Schedule(
    val id: Id,
    val title: String,
    val description: String,
    val time: ScheduleTime,
    val user: User
) {

    data class Id(val value: String)

    /**
     * 終日かどうか
     */
    val isAllDay: Boolean
        get() = time is ScheduleTime.SingleAllDay || time is ScheduleTime.AllDayRange

    /**
     * 時間指定かどうか
     */
    val isTimed: Boolean
        get() = time is ScheduleTime.SingleTimeDay || time is ScheduleTime.DateTimeRange

    /**
     * 一日終日の予定かどうか
     */
    val isSingleAllDay: Boolean
        get() = time is ScheduleTime.SingleAllDay

    /**
     * 連日予定かどうか
     */
    val isMultiDay: Boolean
        get() = time is ScheduleTime.AllDayRange || time is ScheduleTime.DateTimeRange

    /**
     * 開始日時を取得
     */
    val startDateTime: DateTime
        get() = when (val t = time) {
            is ScheduleTime.SingleAllDay -> DateTime.allDay(t.startDate)
            is ScheduleTime.SingleTimeDay -> DateTime.timed(t.startDate, t.startTime)
            is ScheduleTime.AllDayRange -> DateTime.allDay(t.startDate)
            is ScheduleTime.DateTimeRange -> DateTime.timed(t.start.date, t.start.time)
        }

    /**
     * 終了日時を取得
     */
    val endDateTime: DateTime
        get() = when (val t = time) {
            is ScheduleTime.SingleAllDay -> DateTime.allDay(t.startDate)
            is ScheduleTime.SingleTimeDay -> DateTime.timed(t.startDate, t.endTime)
            is ScheduleTime.AllDayRange -> DateTime.allDay(t.endDate)
            is ScheduleTime.DateTimeRange -> DateTime.timed(t.end.date, t.end.time)
        }

    companion object {
        /**
         * 終日の予定を作成（単日）
         */
        fun createAllDay(
            id: Id,
            title: String,
            description: String,
            date: LocalDate,
            user: User
        ): Schedule {
            return Schedule(
                id = id,
                title = title,
                description = description,
                time = ScheduleTime.SingleAllDay(date),
                user = user
            )
        }

        /**
         * 終日の予定を作成（連日）
         */
        fun createMultiDayAllDay(
            id: Id,
            title: String,
            description: String,
            startDate: LocalDate,
            endDate: LocalDate,
            user: User
        ): Schedule {
            return Schedule(
                id = id,
                title = title,
                description = description,
                time = ScheduleTime.AllDayRange(startDate, endDate),
                user = user
            )
        }

        /**
         * 時間指定の予定を作成（単日）
         */
        fun createTimed(
            id: Id,
            title: String,
            description: String,
            date: LocalDate,
            user: User,
            startTime: LocalTime,
            endTime: LocalTime
        ): Schedule {
            return Schedule(
                id = id,
                title = title,
                description = description,
                time = ScheduleTime.SingleTimeDay(date, startTime, endTime),
                user = user
            )
        }

        /**
         * 時間指定の予定を作成（連日）
         */
        fun createMultiDayTimed(
            id: Id,
            title: String,
            description: String,
            startDate: LocalDate,
            endDate: LocalDate,
            user: User,
            startTime: LocalTime,
            endTime: LocalTime
        ): Schedule {
            return Schedule(
                id = id,
                title = title,
                description = description,
                time = ScheduleTime.DateTimeRange(
                    startDate = startDate,
                    start = LocalDateTime(startDate, startTime),
                    end = LocalDateTime(endDate, endTime)
                ),
                user = user
            )
        }
    }
}
