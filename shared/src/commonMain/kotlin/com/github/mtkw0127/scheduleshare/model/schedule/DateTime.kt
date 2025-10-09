package com.github.mtkw0127.scheduleshare.model.schedule

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * 日付と時刻を表すクラス
 * LocalTimeがnullの場合は終日、指定されている場合は時間指定
 */
data class DateTime(
    val date: LocalDate,
    val time: LocalTime? = null
) : Comparable<DateTime> {

    /**
     * 終日かどうか
     */
    val isAllDay: Boolean
        get() = time == null

    /**
     * 時間指定かどうか
     */
    val isTimed: Boolean
        get() = time != null

    override fun compareTo(other: DateTime): Int {
        val dateComparison = date.compareTo(other.date)
        if (dateComparison != 0) return dateComparison

        // 日付が同じ場合は時刻で比較
        return when {
            time == null && other.time == null -> 0
            time == null -> -1  // 終日は時間指定より前
            other.time == null -> 1
            else -> time.compareTo(other.time)
        }
    }

    companion object {
        /**
         * 終日の日時を作成
         */
        fun allDay(date: LocalDate): DateTime {
            return DateTime(date = date, time = null)
        }

        /**
         * 時間指定の日時を作成
         */
        fun timed(date: LocalDate, time: LocalTime): DateTime {
            return DateTime(date = date, time = time)
        }
    }
}
