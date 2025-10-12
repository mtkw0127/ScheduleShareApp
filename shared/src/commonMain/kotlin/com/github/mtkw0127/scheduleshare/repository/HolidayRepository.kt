package com.github.mtkw0127.scheduleshare.repository

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlin.math.floor

class HolidayRepository {
    data class Holiday(val date: LocalDate, val name: String)

    fun getJapaneseHolidays(year: Int): List<Holiday> {
        val holidays = mutableListOf<Holiday>()

        // 1️⃣ 固定日
        holidays += listOf(
            Holiday(LocalDate(year, 1, 1), "元日"),
            Holiday(LocalDate(year, 2, 11), "建国記念の日"),
            Holiday(LocalDate(year, 4, 29), "昭和の日"),
            Holiday(LocalDate(year, 5, 3), "憲法記念日"),
            Holiday(LocalDate(year, 5, 4), "みどりの日"),
            Holiday(LocalDate(year, 5, 5), "こどもの日"),
            Holiday(LocalDate(year, 8, 11), "山の日"),
            Holiday(LocalDate(year, 11, 3), "文化の日"),
            Holiday(LocalDate(year, 11, 23), "勤労感謝の日"),
            Holiday(LocalDate(year, 2, 23), "天皇誕生日")
        )

        // 2️⃣ ハッピーマンデー
        fun nthWeekdayOfMonth(year: Int, month: Int, weekday: DayOfWeek, n: Int): LocalDate {
            var date = LocalDate(year, month, 1)
            val firstWeekday = date.dayOfWeek
            val delta = (weekday.ordinal - firstWeekday.ordinal + 7) % 7
            date = date.plus(delta.toLong(), DateTimeUnit.DAY)
            return date.plus(7L * (n - 1), DateTimeUnit.DAY)
        }

        holidays += listOf(
            Holiday(nthWeekdayOfMonth(year, 1, DayOfWeek.MONDAY, 2), "成人の日"),
            Holiday(nthWeekdayOfMonth(year, 7, DayOfWeek.MONDAY, 3), "海の日"),
            Holiday(nthWeekdayOfMonth(year, 9, DayOfWeek.MONDAY, 3), "敬老の日"),
            Holiday(nthWeekdayOfMonth(year, 10, DayOfWeek.MONDAY, 2), "スポーツの日")
        )

        // 3️⃣ 春分日・秋分日（概算式）
        fun calcShunbun(year: Int): LocalDate {
            val day = floor(20.8431 + 0.242194 * (year - 1980) - floor((year - 1980) / 4.0)).toInt()
            return LocalDate(year, 3, day)
        }

        fun calcShubun(year: Int): LocalDate {
            val day = floor(23.2488 + 0.242194 * (year - 1980) - floor((year - 1980) / 4.0)).toInt()
            return LocalDate(year, 9, day)
        }

        holidays += listOf(
            Holiday(calcShunbun(year), "春分の日"),
            Holiday(calcShubun(year), "秋分の日")
        )

        // 4️⃣ 振替休日（日曜と重なったら翌平日）
        val holidayMap = holidays.associateBy { it.date }.toMutableMap()
        holidays.forEach { h ->
            if (h.date.dayOfWeek == DayOfWeek.SUNDAY) {
                var nextDay = h.date.plus(1, DateTimeUnit.DAY)
                while (holidayMap.containsKey(nextDay)) {
                    nextDay = nextDay.plus(1, DateTimeUnit.DAY)
                }
                holidayMap[nextDay] = Holiday(nextDay, "振替休日")
            }
        }

        // 5️⃣ 国民の休日（祝日に挟まれた平日）
        val dates = holidayMap.keys.sorted()
        for (i in 0 until dates.size - 1) {
            val diff = dates[i + 1].daysUntil(dates[i])
            if (diff == -2) { // 祝日で挟まれた平日
                val middle = dates[i].plus(1, DateTimeUnit.DAY)
                if (!holidayMap.containsKey(middle)) {
                    holidayMap[middle] = Holiday(middle, "国民の休日")
                }
            }
        }

        return holidayMap.values.sortedBy { it.date }
    }
}