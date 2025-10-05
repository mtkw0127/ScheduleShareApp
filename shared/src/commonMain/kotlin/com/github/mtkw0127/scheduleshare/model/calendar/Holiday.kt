package com.github.mtkw0127.scheduleshare.model.calendar

import kotlinx.datetime.LocalDate

data class Holiday(
    override val value: LocalDate,
    val name: String // 休日の名前
) : Day
