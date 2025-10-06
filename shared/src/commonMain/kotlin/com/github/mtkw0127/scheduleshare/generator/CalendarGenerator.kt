package com.github.mtkw0127.scheduleshare.generator

import com.github.mtkw0127.scheduleshare.extension.firstDayOfMonth
import com.github.mtkw0127.scheduleshare.model.calendar.Day
import com.github.mtkw0127.scheduleshare.model.calendar.Holiday
import com.github.mtkw0127.scheduleshare.model.calendar.Month
import com.github.mtkw0127.scheduleshare.model.calendar.Week
import com.github.mtkw0127.scheduleshare.model.calendar.Weekday
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

object CalendarGenerator {
    fun createMonth(today: LocalDate): Month {
        // その月の初日を取得
        val firstDayOfMonth = today.firstDayOfMonth()
        val firstWeek = createFirstWeek(firstDayOfMonth)

        // 第二週作成
        val secondWeek =
            createWeekNotFirstAndLast(firstWeek.saturday.value.plus(1, DateTimeUnit.DAY))
        // 第三週作成
        val thirdWeek =
            createWeekNotFirstAndLast(secondWeek.saturday.value.plus(1, DateTimeUnit.DAY))
        // 第四週作成
        val fourthWeek =
            createWeekNotFirstAndLast(thirdWeek.saturday.value.plus(1, DateTimeUnit.DAY))
        // 第五週作成
        val fifthWeek =
            createWeekNotFirstAndLast(fourthWeek.saturday.value.plus(1, DateTimeUnit.DAY))

        // 第六週が必要かチェック（第六週の日曜日が当月内なら第六週も作成）
        val sixthWeekSunday = fifthWeek.saturday.value.plus(1, DateTimeUnit.DAY)
        val sixthWeek = if (sixthWeekSunday.month == firstDayOfMonth.month) {
            createWeekNotFirstAndLast(sixthWeekSunday)
        } else {
            null
        }

        return Month(
            firstDay = firstDayOfMonth,
            firstWeek = firstWeek,
            secondWeek = secondWeek,
            thirdWeek = thirdWeek,
            fourthWeek = fourthWeek,
            fifthWeek = fifthWeek,
            sixthWeek = sixthWeek,
        )
    }

    private fun createWeekNotFirstAndLast(firstDay: LocalDate): Week {
        return Week(
            sunday = firstDay.toDay(),
            monday = firstDay.plus(1, DateTimeUnit.DAY).toDay(),
            tuesday = firstDay.plus(2, DateTimeUnit.DAY).toDay(),
            wednesday = firstDay.plus(3, DateTimeUnit.DAY).toDay(),
            thursday = firstDay.plus(4, DateTimeUnit.DAY).toDay(),
            friday = firstDay.plus(5, DateTimeUnit.DAY).toDay(),
            saturday = firstDay.plus(6, DateTimeUnit.DAY).toDay(),
        )
    }

    private fun createFirstWeek(firstDay: LocalDate): Week {
        // １日より前に何日分のデータを作成する必要があるか
        val firstWeek = when (checkNotNull(firstDay.dayOfWeek)) {
            DayOfWeek.SUNDAY -> listOf(
                firstDay,
                firstDay.plus(1, DateTimeUnit.DAY),
                firstDay.plus(2, DateTimeUnit.DAY),
                firstDay.plus(3, DateTimeUnit.DAY),
                firstDay.plus(4, DateTimeUnit.DAY),
                firstDay.plus(5, DateTimeUnit.DAY),
                firstDay.plus(6, DateTimeUnit.DAY),
            )

            DayOfWeek.MONDAY -> listOf(
                firstDay.minus(1, DateTimeUnit.DAY),
                firstDay,
                firstDay.plus(1, DateTimeUnit.DAY),
                firstDay.plus(2, DateTimeUnit.DAY),
                firstDay.plus(3, DateTimeUnit.DAY),
                firstDay.plus(4, DateTimeUnit.DAY),
                firstDay.plus(5, DateTimeUnit.DAY),
            )

            DayOfWeek.TUESDAY -> listOf(
                firstDay.minus(2, DateTimeUnit.DAY),
                firstDay.minus(1, DateTimeUnit.DAY),
                firstDay,
                firstDay.plus(1, DateTimeUnit.DAY),
                firstDay.plus(2, DateTimeUnit.DAY),
                firstDay.plus(3, DateTimeUnit.DAY),
                firstDay.plus(4, DateTimeUnit.DAY),
            )

            DayOfWeek.WEDNESDAY -> listOf(
                firstDay.minus(3, DateTimeUnit.DAY),
                firstDay.minus(2, DateTimeUnit.DAY),
                firstDay.minus(1, DateTimeUnit.DAY),
                firstDay,
                firstDay.plus(1, DateTimeUnit.DAY),
                firstDay.plus(2, DateTimeUnit.DAY),
                firstDay.plus(3, DateTimeUnit.DAY),
            )

            DayOfWeek.THURSDAY -> listOf(
                firstDay.minus(4, DateTimeUnit.DAY),
                firstDay.minus(3, DateTimeUnit.DAY),
                firstDay.minus(2, DateTimeUnit.DAY),
                firstDay.minus(1, DateTimeUnit.DAY),
                firstDay,
                firstDay.plus(1, DateTimeUnit.DAY),
                firstDay.plus(2, DateTimeUnit.DAY),
            )

            DayOfWeek.FRIDAY -> listOf(
                firstDay.minus(5, DateTimeUnit.DAY),
                firstDay.minus(4, DateTimeUnit.DAY),
                firstDay.minus(3, DateTimeUnit.DAY),
                firstDay.minus(2, DateTimeUnit.DAY),
                firstDay.minus(1, DateTimeUnit.DAY),
                firstDay,
                firstDay.plus(1, DateTimeUnit.DAY),
            )

            DayOfWeek.SATURDAY -> listOf(
                firstDay.minus(6, DateTimeUnit.DAY),
                firstDay.minus(5, DateTimeUnit.DAY),
                firstDay.minus(4, DateTimeUnit.DAY),
                firstDay.minus(3, DateTimeUnit.DAY),
                firstDay.minus(2, DateTimeUnit.DAY),
                firstDay.minus(1, DateTimeUnit.DAY),
                firstDay,
            )

            else -> throw IllegalStateException()
        }
        return Week(
            sunday = firstWeek[0].toDay(),
            monday = firstWeek[1].toDay(),
            tuesday = firstWeek[2].toDay(),
            wednesday = firstWeek[3].toDay(),
            thursday = firstWeek[4].toDay(),
            friday = firstWeek[5].toDay(),
            saturday = firstWeek[6].toDay(),
        )
    }

    private fun LocalDate.toDay(): Day {
        return when (dayOfWeek) {
            DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> Holiday(
                value = this,
                name = "",
            )

            else -> Weekday(
                value = this,
            )
        }
    }
}