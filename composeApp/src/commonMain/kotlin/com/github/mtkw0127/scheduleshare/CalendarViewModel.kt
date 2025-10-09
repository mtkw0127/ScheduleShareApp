package com.github.mtkw0127.scheduleshare

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.mtkw0127.scheduleshare.generator.CalendarGenerator
import com.github.mtkw0127.scheduleshare.model.calendar.Month
import com.github.mtkw0127.scheduleshare.model.schedule.Schedule
import com.github.mtkw0127.scheduleshare.model.user.User
import com.github.mtkw0127.scheduleshare.model.user.UserColor
import com.github.mtkw0127.scheduleshare.repository.ScheduleRepository
import com.github.mtkw0127.scheduleshare.repository.UserRepository
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Stable
class CalendarState @OptIn(ExperimentalTime::class) constructor(
    private val scheduleRepository: ScheduleRepository,
    private val userRepository: UserRepository,
    initialFocusedMonth: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
) {
    var months by mutableStateOf<List<Month>>(emptyList())
        private set

    var focusedMonth by mutableStateOf(initialFocusedMonth)
        private set

    var schedules by mutableStateOf<Map<LocalDate, List<Schedule>>>(emptyMap())
        private set

    var sharedUsers by mutableStateOf<List<User>>(emptyList())
        private set

    var userVisibilityMap by mutableStateOf<Map<User.Id, Boolean>>(emptyMap())
        private set

    var userColorMap by mutableStateOf<Map<User.Id, UserColor>>(emptyMap())
        private set

    init {
        loadMonths(initialFocusedMonth)
        loadSchedules()
        loadSharedUsers()
    }

    private fun loadSharedUsers() {
        sharedUsers = userRepository.getSharedUsers()
        // 表示状態のマップを更新
        userVisibilityMap = sharedUsers.associate { user ->
            user.id to userRepository.getUserVisibility(user.id)
        }
        // 色のマップを更新
        userColorMap = sharedUsers.associate { user ->
            user.id to userRepository.getUserColor(user.id)
        }
    }

    fun updateUserVisibility(userId: User.Id, visible: Boolean) {
        userRepository.setUserVisibility(userId, visible)
        // 表示状態のマップを更新
        userVisibilityMap = userVisibilityMap.toMutableMap().apply {
            this[userId] = visible
        }
        loadSchedules()
    }

    fun updateUserColor(userId: User.Id, color: UserColor) {
        userRepository.setUserColor(userId, color)
        // 色のマップを更新
        userColorMap = userColorMap.toMutableMap().apply {
            this[userId] = color
        }
    }

    private fun loadSchedules() {
        // 表示中の月の予定を取得
        val schedulesForMonth = scheduleRepository.getSchedulesByMonth(
            year = focusedMonth.year,
            month = focusedMonth.month.number
        )
        // TODO: データの整形
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
        loadSchedules()
    }

    fun moveToPrevMonth() {
        val newFocusedMonth = focusedMonth.minus(1, DateTimeUnit.MONTH)

        // 新しい月を追加（focusedMonthの前の月）
        val newMonth = CalendarGenerator.createMonth(newFocusedMonth.minus(1, DateTimeUnit.MONTH))
        months = listOf(newMonth) + months.dropLast(1)
        focusedMonth = newFocusedMonth
        loadSchedules()
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun rememberCalendarState(
    scheduleRepository: ScheduleRepository,
    userRepository: UserRepository,
    initialFocusedMonth: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
): CalendarState {
    return remember(scheduleRepository, userRepository, initialFocusedMonth) {
        CalendarState(scheduleRepository, userRepository, initialFocusedMonth)
    }
}
