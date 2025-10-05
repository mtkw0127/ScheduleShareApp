package com.github.mtkw0127.scheduleshare.model.calendar

import kotlinx.datetime.LocalDate

data class Weekday(
    override val value: LocalDate,
) : Day
