package com.github.mtkw0127.scheduleshare

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.mtkw0127.scheduleshare.generator.CalendarGenerator
import com.github.mtkw0127.scheduleshare.model.calendar.Month
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Stable
class CalendarState @OptIn(ExperimentalTime::class) constructor(
    initialFocusedMonth: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
) {
    var months by mutableStateOf<List<Month>>(emptyList())
        private set

    var focusedMonth by mutableStateOf(initialFocusedMonth)
        private set

    init {
        loadMonths(initialFocusedMonth)
    }

    private fun loadMonths(centerMonth: LocalDate) {
        val prevMonth = centerMonth.minus(1, DateTimeUnit.MONTH)
        val nextMonth = centerMonth.plus(1, DateTimeUnit.MONTH)

        months = listOf(
            CalendarGenerator.createMonth(prevMonth),
            CalendarGenerator.createMonth(centerMonth),
            CalendarGenerator.createMonth(nextMonth)
        )
    }

    fun moveToNextMonth() {
        val newFocusedMonth = focusedMonth.plus(1, DateTimeUnit.MONTH)

        // 新しい月を追加（focusedMonthの次の月）
        val newMonth = CalendarGenerator.createMonth(newFocusedMonth.plus(1, DateTimeUnit.MONTH))
        months = months.drop(1) + newMonth
        focusedMonth = newFocusedMonth
    }

    fun moveToPrevMonth() {
        val newFocusedMonth = focusedMonth.minus(1, DateTimeUnit.MONTH)

        // 新しい月を追加（focusedMonthの前の月）
        val newMonth = CalendarGenerator.createMonth(newFocusedMonth.minus(1, DateTimeUnit.MONTH))
        months = listOf(newMonth) + months.dropLast(1)
        focusedMonth = newFocusedMonth
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun rememberCalendarState(
    initialFocusedMonth: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
): CalendarState {
    return remember(initialFocusedMonth) {
        CalendarState(initialFocusedMonth)
    }
}
