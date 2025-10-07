package com.github.mtkw0127.scheduleshare.navigation

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Login : Screen

    @Serializable
    data object Calendar : Screen

    @Serializable
    data object Settings : Screen

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

    @Serializable
    data class ScheduleAdd(
        val year: Int,
        val month: Int,
        val day: Int,
        val scheduleId: String? = null
    ) : Screen {
        companion object {
            fun from(date: LocalDate, scheduleId: String? = null): ScheduleAdd {
                return ScheduleAdd(
                    year = date.year,
                    month = date.monthNumber,
                    day = date.dayOfMonth,
                    scheduleId = scheduleId
                )
            }
        }

        fun toLocalDate(): LocalDate {
            return LocalDate(year, month, day)
        }
    }
}
