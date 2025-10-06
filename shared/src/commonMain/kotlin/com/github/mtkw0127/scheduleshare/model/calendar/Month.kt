package com.github.mtkw0127.scheduleshare.model.calendar

import kotlinx.datetime.LocalDate

data class Month(
    val firstDay: LocalDate,
    val firstWeek: Week,
    val secondWeek: Week,
    val thirdWeek: Week,
    val fourthWeek: Week,
    val fifthWeek: Week,
    val sixthWeek: Week?,
)
