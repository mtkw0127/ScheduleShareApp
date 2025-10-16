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
import com.github.mtkw0127.scheduleshare.repository.HolidayRepository
import com.github.mtkw0127.scheduleshare.repository.ScheduleRepository
import com.github.mtkw0127.scheduleshare.repository.UserRepository
import com.github.mtkw0127.scheduleshare.shared.preferences.SharedUserPreferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private val sharedUserPreferenceRepository: SharedUserPreferenceRepository,
    private val holidayRepository: HolidayRepository = HolidayRepository(),
    initialFocusedMonth: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
) {
    private val scope = CoroutineScope(Dispatchers.Main)
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

    var holidays by mutableStateOf<Map<LocalDate, HolidayRepository.Holiday>>(emptyMap())
        private set

    // 今月のインデックス（前後100年分の中での位置）
    val initialMonthIndex: Int

    init {
        loadMonths(initialFocusedMonth)
        loadSchedules()
        loadSharedUsers()
        loadHolidays()

        // 初期月のインデックスを計算（前100年 = 1200ヶ月）
        initialMonthIndex = 100 * 12
    }

    private fun loadSharedUsers() {
        sharedUsers = userRepository.getSharedUsers()

        // DataStoreから保存済みの設定を読み込み
        scope.launch {
            val visibilityMap = mutableMapOf<User.Id, Boolean>()
            val colorMap = mutableMapOf<User.Id, UserColor>()

            sharedUsers.forEach { user ->
                // DataStoreから取得、なければデフォルト値を使用
                val visibility = sharedUserPreferenceRepository.getUserVisibility(user.id.value)
                visibilityMap[user.id] = visibility

                val savedColor = sharedUserPreferenceRepository.getUserColor(user.id.value)
                val color = if (savedColor != null) {
                    // 保存されたColorをUserColorに変換
                    UserColor.fromValue(savedColor)
                } else {
                    // デフォルト値を使用
                    UserColor.default()
                }
                colorMap[user.id] = color
            }

            userVisibilityMap = visibilityMap
            userColorMap = colorMap
        }
    }

    fun updateUserVisibility(userId: User.Id, visible: Boolean) {
        // DataStoreに保存
        scope.launch {
            sharedUserPreferenceRepository.setUserVisibility(userId.value, visible)
        }
        // 表示状態のマップを更新
        userVisibilityMap = userVisibilityMap.toMutableMap().apply {
            this[userId] = visible
        }
        loadSchedules()
    }

    fun updateUserColor(userId: User.Id, color: UserColor) {
        // DataStoreに保存
        scope.launch {
            sharedUserPreferenceRepository.setUserColor(userId.value, color.value.toLong())
        }
        // 色のマップを更新
        userColorMap = userColorMap.toMutableMap().apply {
            this[userId] = color
        }
    }

    private fun loadSchedules() {
        // 表示中の3ヶ月分（前月・当月・翌月）の予定を取得
        val prevMonth = focusedMonth.minus(1, DateTimeUnit.MONTH)
        val nextMonth = focusedMonth.plus(1, DateTimeUnit.MONTH)

        val allSchedules = mutableMapOf<LocalDate, List<Schedule>>()

        listOf(prevMonth, focusedMonth, nextMonth).forEach { month ->
            val monthSchedules = scheduleRepository.getSchedulesByMonth(
                year = month.year,
                month = month.month.number
            )

            // visibilityがtrueのユーザーのスケジュールのみをフィルタリング
            val visibleSchedules = monthSchedules.filter { schedule ->
                userVisibilityMap[schedule.createUser.id] ?: true
            }

            allSchedules.putAll(visibleSchedules.groupBy {
                it.time.startDate
            })
        }

        schedules = allSchedules
    }

    private fun loadHolidays() {
        // 表示中の3ヶ月分（前月・当月・翌月）の祝日を取得
        val prevMonth = focusedMonth.minus(1, DateTimeUnit.MONTH)
        val nextMonth = focusedMonth.plus(1, DateTimeUnit.MONTH)

        val allHolidays = mutableMapOf<LocalDate, HolidayRepository.Holiday>()

        listOf(prevMonth.year, focusedMonth.year, nextMonth.year).distinct().forEach { year ->
            val yearHolidays = holidayRepository.getJapaneseHolidays(year)
            yearHolidays.forEach { holiday ->
                allHolidays[holiday.date] = holiday
            }
        }

        holidays = allHolidays
    }

    private fun loadMonths(centerMonth: LocalDate) {
        // 前後100年分の月を生成（計200年 = 2400ヶ月）
        val monthsList = mutableListOf<Month>()

        // 開始日（centerMonthから100年前）
        var currentMonth = centerMonth.minus(100 * 12, DateTimeUnit.MONTH)

        // 200年分（2400ヶ月）生成
        repeat(200 * 12) {
            monthsList.add(CalendarGenerator.createMonth(currentMonth))
            currentMonth = currentMonth.plus(1, DateTimeUnit.MONTH)
        }

        months = monthsList
    }

    fun updateFocusedMonth(newFocusedMonth: LocalDate) {
        focusedMonth = newFocusedMonth
        loadSchedules()
        loadHolidays()
    }

    // ページインデックスから対応する月を取得
    fun getMonthForPage(pageIndex: Int): LocalDate {
        if (months.isEmpty() || pageIndex !in months.indices) {
            return focusedMonth
        }
        return months[pageIndex].firstDay
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun rememberCalendarState(
    scheduleRepository: ScheduleRepository,
    userRepository: UserRepository,
    sharedUserPreferenceRepository: SharedUserPreferenceRepository,
    holidayRepository: HolidayRepository,
    initialFocusedMonth: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
): CalendarState {
    return remember(
        scheduleRepository,
        userRepository,
        sharedUserPreferenceRepository,
        initialFocusedMonth
    ) {
        CalendarState(
            scheduleRepository,
            userRepository,
            sharedUserPreferenceRepository,
            holidayRepository,
            initialFocusedMonth
        )
    }
}
