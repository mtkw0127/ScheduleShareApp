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
    val date: LocalDate,
    val user: User,
    val timeType: TimeType,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null
) {

    data class Id(val value: String)

    /**
     * 時間タイプ
     */
    sealed interface TimeType {
        /**
         * 終日
         */
        data object AllDay : TimeType

        /**
         * 時間指定
         */
        data class Timed(
            val start: LocalTime,
            val end: LocalTime
        ) : TimeType
    }

    companion object {
        /**
         * 終日の予定を作成
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
                date = date,
                user = user,
                timeType = TimeType.AllDay
            )
        }

        /**
         * 時間指定の予定を作成
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
                date = date,
                user = user,
                timeType = TimeType.Timed(startTime, endTime),
                startTime = startTime,
                endTime = endTime
            )
        }
    }
}
