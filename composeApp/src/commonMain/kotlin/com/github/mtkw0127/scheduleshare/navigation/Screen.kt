package com.github.mtkw0127.scheduleshare.navigation

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Login : Screen

    @Serializable
    data object Calendar : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object QRShare : Screen

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
                    month = date.month.number,
                    day = date.day
                )
            }
        }

        fun toLocalDate(): LocalDate {
            return LocalDate(year, month, day)
        }
    }

    @Serializable
    data class WeekSchedule(
        val year: Int,
        val month: Int,
        val day: Int
    ) : Screen {
        companion object {
            fun from(date: LocalDate): WeekSchedule {
                return WeekSchedule(
                    year = date.year,
                    month = date.month.number,
                    day = date.day
                )
            }
        }

        fun toLocalDate(): LocalDate {
            return LocalDate(year, month, day)
        }
    }

    @Serializable
    data class ScheduleDetail(
        val scheduleId: String
    ) : Screen {
        companion object {
            fun from(scheduleId: String): ScheduleDetail {
                return ScheduleDetail(scheduleId = scheduleId)
            }
        }
    }

    @Serializable
    data class ScheduleAdd(
        val year: Int,
        val month: Int,
        val day: Int,
        val scheduleId: String? = null,
        val startHour: Int? = null,
        val startMinute: Int? = null,
        val endHour: Int? = null,
        val endMinute: Int? = null
    ) : Screen {
        companion object {
            fun from(
                date: LocalDate,
                scheduleId: String? = null,
                startTime: kotlinx.datetime.LocalTime? = null,
                endTime: kotlinx.datetime.LocalTime? = null
            ): ScheduleAdd {
                return ScheduleAdd(
                    year = date.year,
                    month = date.month.number,
                    day = date.day,
                    scheduleId = scheduleId,
                    startHour = startTime?.hour,
                    startMinute = startTime?.minute,
                    endHour = endTime?.hour,
                    endMinute = endTime?.minute
                )
            }
        }

        fun toLocalDate(): LocalDate {
            return LocalDate(year, month, day)
        }
    }
}
