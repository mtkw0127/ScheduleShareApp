package com.github.mtkw0127.scheduleshare.model.schedule

import com.github.mtkw0127.scheduleshare.model.user.User
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * 予定のUIモデル
 */
data class Schedule(
    val id: Id,
    val title: String,
    val description: String,
    val startDateTime: DateTime,
    val endDateTime: DateTime,
    val user: User
) {

    data class Id(val value: String)

    /**
     * 終日かどうか
     */
    val isAllDay: Boolean
        get() = startDateTime.isAllDay && endDateTime.isAllDay

    /**
     * 時間指定かどうか
     */
    val isTimed: Boolean
        get() = startDateTime.isTimed && endDateTime.isTimed

    /**
     * 単日予定かどうか
     */
    val isSingleDay: Boolean
        get() = startDateTime.date == endDateTime.date

    /**
     * 連日予定かどうか
     */
    val isMultiDay: Boolean
        get() = startDateTime.date != endDateTime.date

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
                startDateTime = DateTime.allDay(date),
                endDateTime = DateTime.allDay(date),
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
                startDateTime = DateTime.allDay(startDate),
                endDateTime = DateTime.allDay(endDate),
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
                startDateTime = DateTime.timed(date, startTime),
                endDateTime = DateTime.timed(date, endTime),
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
                startDateTime = DateTime.timed(startDate, startTime),
                endDateTime = DateTime.timed(endDate, endTime),
                user = user
            )
        }
    }
}
