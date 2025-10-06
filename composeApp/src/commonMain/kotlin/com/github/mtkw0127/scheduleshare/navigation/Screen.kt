package com.github.mtkw0127.scheduleshare.navigation

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Calendar : Screen

    @Serializable
    data class DaySchedule(
        val year: Int,
        val month: Int,
        val day: Int
    ) : Screen {
        companion object {
            fun from(date: LocalDate): DaySchedule {
                return DaySchedule(
                    year = date.year,
                    month = date.monthNumber,
                    day = date.dayOfMonth
                )
            }
        }

        fun toLocalDate(): LocalDate {
            return LocalDate(year, month, day)
        }
    }
}
