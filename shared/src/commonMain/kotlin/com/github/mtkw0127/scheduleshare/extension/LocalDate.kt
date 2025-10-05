package com.github.mtkw0127.scheduleshare.extension

import kotlinx.datetime.LocalDate

fun LocalDate.toYm(): String {
    return "${year}/${monthNumber.toString().padStart(2, '0')}"
}

fun LocalDate.firstDayOfMonth(): LocalDate {
    return LocalDate(year, monthNumber, 1)
}
