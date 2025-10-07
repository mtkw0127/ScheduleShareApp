package com.github.mtkw0127.scheduleshare.extension

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

fun LocalDate.toYm(): String {
    return "${year}/${month.number.toString().padStart(2, '0')}"
}

fun LocalDate.firstDayOfMonth(): LocalDate {
    return LocalDate(year, month.number, 1)
}

fun LocalDate.toYmd(): String {
    return "${month.number.toString().padStart(2, '0')}/${dayOfMonth.toString().padStart(2, '0')}"
}

fun DayOfWeek.toJapanese(): String {
    return when (this) {
        DayOfWeek.MONDAY -> "月"
        DayOfWeek.TUESDAY -> "火"
        DayOfWeek.WEDNESDAY -> "水"
        DayOfWeek.THURSDAY -> "木"
        DayOfWeek.FRIDAY -> "金"
        DayOfWeek.SATURDAY -> "土"
        DayOfWeek.SUNDAY -> "日"
    }
}
