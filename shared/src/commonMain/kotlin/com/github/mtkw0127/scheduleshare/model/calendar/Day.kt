package com.github.mtkw0127.scheduleshare.model.calendar

import kotlinx.datetime.LocalDate

sealed interface Day {
    val value: LocalDate
}
