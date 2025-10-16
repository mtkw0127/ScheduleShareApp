package com.github.mtkw0127.scheduleshare

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.github.mtkw0127.scheduleshare.model.schedule.Schedule
import com.github.mtkw0127.scheduleshare.model.user.User
import com.github.mtkw0127.scheduleshare.model.user.UserColor
import com.github.mtkw0127.scheduleshare.navigation.Screen
import com.github.mtkw0127.scheduleshare.repository.HolidayRepository
import com.github.mtkw0127.scheduleshare.repository.ScheduleRepository
import com.github.mtkw0127.scheduleshare.repository.UserRepository
import com.github.mtkw0127.scheduleshare.shared.preferences.SharedUserPreferenceRepository
import com.github.mtkw0127.scheduleshare.shared.preferences.UserPreferenceRepository
import com.github.mtkw0127.scheduleshare.shared.preferences.createDataStore
import com.github.mtkw0127.scheduleshare.theme.ScheduleShareTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
@Preview
fun App() {
    ScheduleShareTheme {
        val navController = rememberNavController()
        val userRepository = remember { UserRepository.createWithSampleData() }
        val holidayRepository = remember { HolidayRepository() }
        val userPreferenceRepository = remember { UserPreferenceRepository(createDataStore()) }
        val sharedUserPreferenceRepository =
            remember { SharedUserPreferenceRepository(createDataStore()) }

        // 初回起動時にデフォルトの色を設定
        LaunchedEffect(Unit) {
            val partner = User.Id("user_001")
            val child1 = User.Id("user_002")
            val child2 = User.Id("user_003")

            // 既に色が設定されていない場合のみデフォルト色を設定
            if (sharedUserPreferenceRepository.getUserColor(partner.value) == null) {
                sharedUserPreferenceRepository.setUserColor(partner.value, UserColor.PURPLE.value)
            }
            if (sharedUserPreferenceRepository.getUserColor(child1.value) == null) {
                sharedUserPreferenceRepository.setUserColor(child1.value, UserColor.GREEN.value)
            }
            if (sharedUserPreferenceRepository.getUserColor(child2.value) == null) {
                sharedUserPreferenceRepository.setUserColor(child2.value, UserColor.ORANGE.value)
            }
        }

        val scheduleRepository = remember {
            ScheduleRepository(userRepository).apply {
                val testUser = User.createTest()

                // 共有ユーザーのサンプルデータ
                val partner = User(User.Id("user_001"), "パートナー")
                val child1 = User(User.Id("user_002"), "長男")
                val child2 = User(User.Id("user_003"), "長女")

                // あなたの予定
                addSchedule(
                    Schedule.createTimed(
                        id = Schedule.Id("1"),
                        title = "仕事",
                        description = "リモートワーク",
                        date = LocalDate(2025, 10, 8),
                        createUser = testUser,
                        assignedUsers = listOf(testUser),
                        startTime = LocalTime(9, 0),
                        endTime = LocalTime(18, 0),
                        location = "自宅"
                    )
                )

                addSchedule(
                    Schedule.createTimed(
                        id = Schedule.Id("2"),
                        title = "家族で夕食",
                        description = "お気に入りのレストラン",
                        date = LocalDate(2025, 10, 8),
                        createUser = testUser,
                        assignedUsers = listOf(testUser, partner, child1, child2),
                        startTime = LocalTime(19, 0),
                        endTime = LocalTime(21, 0),
                        location = "イタリアンレストラン ラ・ベッラ"
                    )
                )

                addSchedule(
                    Schedule.createMultiDayAllDay(
                        id = Schedule.Id("3"),
                        title = "家族旅行",
                        description = "沖縄旅行",
                        startDate = LocalDate(2025, 10, 10),
                        endDate = LocalDate(2025, 10, 12),
                        createUser = testUser,
                        assignedUsers = listOf(testUser, partner, child1, child2),
                        location = "沖縄県那覇市"
                    )
                )

                // パートナーの予定
                addSchedule(
                    Schedule.createTimed(
                        id = Schedule.Id("4"),
                        title = "美容院",
                        description = "カット＆カラー",
                        createUser = partner,
                        assignedUsers = listOf(partner),
                        date = LocalDate(2025, 10, 8),
                        startTime = LocalTime(10, 0),
                        endTime = LocalTime(12, 0),
                        location = "ヘアサロン シャイン"
                    )
                )

                addSchedule(
                    Schedule.createTimed(
                        id = Schedule.Id("5"),
                        title = "友達とランチ",
                        description = "久しぶりの再会",
                        createUser = partner,
                        assignedUsers = listOf(partner),
                        date = LocalDate(2025, 10, 8),
                        startTime = LocalTime(12, 30),
                        endTime = LocalTime(14, 0),
                        location = "カフェ ロータス"
                    )
                )

                addSchedule(
                    Schedule.createMultiDayAllDay(
                        id = Schedule.Id("6"),
                        title = "実家帰省",
                        description = "両親に会いに行く",
                        createUser = partner,
                        assignedUsers = listOf(partner),
                        startDate = LocalDate(2025, 10, 13),
                        endDate = LocalDate(2025, 10, 14),
                    )
                )

                // 長男の予定
                addSchedule(
                    Schedule.createTimed(
                        id = Schedule.Id("7"),
                        title = "サッカー教室",
                        description = "週末の練習",
                        createUser = child1,
                        assignedUsers = listOf(child1),
                        date = LocalDate(2025, 10, 8),
                        startTime = LocalTime(10, 0),
                        endTime = LocalTime(12, 0),
                        location = "市民スポーツセンター"
                    )
                )

                addSchedule(
                    Schedule.createAllDay(
                        id = Schedule.Id("8"),
                        title = "遠足",
                        description = "動物園へ",
                        createUser = child1,
                        assignedUsers = listOf(child1),
                        date = LocalDate(2025, 10, 9),
                        location = "上野動物園"
                    )
                )

                // 長女の予定
                addSchedule(
                    Schedule.createTimed(
                        id = Schedule.Id("9"),
                        title = "ピアノレッスン",
                        description = "発表会の練習",
                        createUser = child2,
                        assignedUsers = listOf(child2),
                        date = LocalDate(2025, 10, 8),
                        startTime = LocalTime(14, 0),
                        endTime = LocalTime(15, 0),
                        location = "ヤマハ音楽教室"
                    )
                )

                addSchedule(
                    Schedule.createTimed(
                        id = Schedule.Id("10"),
                        title = "ピアノ発表会",
                        description = "市民ホール",
                        date = LocalDate(2025, 10, 15),
                        createUser = child2,
                        assignedUsers = listOf(child2),
                        startTime = LocalTime(13, 0),
                        endTime = LocalTime(16, 0),
                        location = "市民文化ホール"
                    )
                )

                // 家族共通の予定
                addSchedule(
                    Schedule.createMultiDayTimed(
                        id = Schedule.Id("11"),
                        title = "デートナイト",
                        description = "2人の時間",
                        createUser = testUser,
                        assignedUsers = listOf(testUser, partner),
                        startDate = LocalDate(2025, 10, 6),
                        endDate = LocalDate(2025, 10, 7),
                        startTime = LocalTime(18, 0),
                        endTime = LocalTime(22, 0),
                    )
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Login
            ) {
                composable<Screen.Login> {
                    LoginScreen(
                        onLoginClick = {
                            navController.navigate(Screen.Calendar) {
                                popUpTo(Screen.Login) { inclusive = true }
                            }
                        }
                    )
                }

                composable<Screen.Calendar> {
                    val calendarState =
                        rememberCalendarState(
                            scheduleRepository,
                            userRepository,
                            sharedUserPreferenceRepository,
                            holidayRepository
                        )
                    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

                    CalendarScreen(
                        months = calendarState.months,
                        initialMonthIndex = calendarState.initialMonthIndex,
                        focusedMonth = calendarState.focusedMonth,
                        schedules = calendarState.schedules,
                        holidays = calendarState.holidays,
                        sharedUsers = calendarState.sharedUsers,
                        userVisibilityMap = calendarState.userVisibilityMap,
                        userColorMap = calendarState.userColorMap,
                        onPageChanged = { pageIndex ->
                            val newMonth = calendarState.getMonthForPage(pageIndex)
                            calendarState.updateFocusedMonth(newMonth)
                        },
                        onClickDate = { day ->
                            navController.navigate(Screen.DaySchedule.from(day.value))
                        },
                        onUserIconClick = {
                            navController.navigate(Screen.Settings)
                        },
                        onQRShareClick = {
                            navController.navigate(Screen.QRShare)
                        },
                        onWeekScheduleClick = {
                            navController.navigate(Screen.WeekSchedule.from(today))
                        },
                        onDayScheduleClick = {
                            navController.navigate(Screen.DaySchedule.from(today))
                        },
                        onUserVisibilityChange = { userId, visible ->
                            calendarState.updateUserVisibility(userId, visible)
                        },
                        onUserColorChange = { userId, color ->
                            calendarState.updateUserColor(userId, color)
                        }
                    )
                }

                composable<Screen.Settings> {
                    SettingsScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onLogoutClick = {
                            navController.navigate(Screen.Login) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                composable<Screen.QRShare> {
                    QRShareScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<Screen.DaySchedule> { backStackEntry ->
                    val daySchedule: Screen.DaySchedule = backStackEntry.toRoute()
                    DayScheduleScreen(
                        date = daySchedule.toLocalDate(),
                        scheduleRepository = scheduleRepository,
                        userRepository = userRepository,
                        userPreferenceRepository = userPreferenceRepository,
                        sharedUserPreferenceRepository = sharedUserPreferenceRepository,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onDateChange = {
                            // 内部状態で日付管理するため、何もしない
                        },
                        onAddScheduleClick = {
                            navController.navigate(Screen.ScheduleAdd.from(daySchedule.toLocalDate()))
                        },
                        onAddScheduleAtTime = { time ->
                            val endTime = LocalTime(
                                hour = if (time.hour == 23) 23 else time.hour + 1,
                                minute = if (time.hour == 23) 59 else time.minute
                            )
                            navController.navigate(
                                Screen.ScheduleAdd.from(
                                    date = daySchedule.toLocalDate(),
                                    startTime = time,
                                    endTime = endTime
                                )
                            )
                        },
                        onScheduleClick = { schedule ->
                            navController.navigate(
                                Screen.ScheduleDetail.from(schedule.id.value)
                            )
                        }
                    )
                }

                composable<Screen.ScheduleDetail> { backStackEntry ->
                    val scheduleDetail: Screen.ScheduleDetail = backStackEntry.toRoute()
                    ScheduleDetailScreen(
                        scheduleId = scheduleDetail.scheduleId,
                        scheduleRepository = scheduleRepository,
                        userRepository = userRepository,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onEditClick = {
                            // 予定の詳細から編集画面へ遷移
                            val schedule = scheduleRepository.getScheduleById(Schedule.Id(scheduleDetail.scheduleId))
                            if (schedule != null) {
                                navController.navigate(
                                    Screen.ScheduleAdd.from(
                                        date = schedule.startDateTime.date,
                                        scheduleId = scheduleDetail.scheduleId
                                    )
                                )
                            }
                        }
                    )
                }

                composable<Screen.ScheduleAdd> { backStackEntry ->
                    val scheduleAdd: Screen.ScheduleAdd = backStackEntry.toRoute()
                    ScheduleAddScreen(
                        date = scheduleAdd.toLocalDate(),
                        scheduleRepository = scheduleRepository,
                        userRepository = userRepository,
                        scheduleId = scheduleAdd.scheduleId,
                        initialStartHour = scheduleAdd.startHour,
                        initialStartMinute = scheduleAdd.startMinute,
                        initialEndHour = scheduleAdd.endHour,
                        initialEndMinute = scheduleAdd.endMinute,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onSaveClick = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<Screen.WeekSchedule> { backStackEntry ->
                    val weekSchedule: Screen.WeekSchedule = backStackEntry.toRoute()
                    WeekScheduleScreen(
                        date = weekSchedule.toLocalDate(),
                        scheduleRepository = scheduleRepository,
                        userRepository = userRepository,
                        sharedUserPreferenceRepository = sharedUserPreferenceRepository,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onScheduleClick = { schedule ->
                            navController.navigate(
                                Screen.ScheduleDetail.from(schedule.id.value)
                            )
                        }
                    )
                }
            }
        }
    }
}