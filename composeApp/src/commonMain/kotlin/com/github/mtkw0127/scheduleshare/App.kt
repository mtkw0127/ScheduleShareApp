package com.github.mtkw0127.scheduleshare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.github.mtkw0127.scheduleshare.model.schedule.Schedule
import com.github.mtkw0127.scheduleshare.model.user.User
import com.github.mtkw0127.scheduleshare.navigation.Screen
import com.github.mtkw0127.scheduleshare.repository.ScheduleRepository
import com.github.mtkw0127.scheduleshare.repository.UserRepository
import com.github.mtkw0127.scheduleshare.theme.ScheduleShareTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    ScheduleShareTheme {
        val navController = rememberNavController()
        val userRepository = remember { UserRepository.createWithSampleData() }
        val scheduleRepository = remember {
            ScheduleRepository(userRepository).apply {
                val testUser = User.createTest()

                // 共有ユーザーのサンプルデータ
                val sharedUser1 = User(User.Id("user_001"), "山田太郎")
                val sharedUser2 = User(User.Id("user_002"), "佐藤花子")
                val sharedUser3 = User(User.Id("user_003"), "鈴木一郎")

                // 自分の予定
                addSchedule(
                    Schedule.createAllDay(
                        id = Schedule.Id("1"),
                        title = "終日イベント",
                        description = "終日のサンプル予定",
                        date = LocalDate(2025, 10, 8),
                        user = testUser
                    )
                )

                addSchedule(
                    Schedule.createTimed(
                        id = Schedule.Id("2"),
                        title = "ミーティング",
                        description = "プロジェクト定例会議",
                        date = kotlinx.datetime.LocalDate(2025, 10, 8),
                        user = testUser,
                        startTime = LocalTime(10, 0),
                        endTime = LocalTime(11, 0)
                    )
                )

                addSchedule(
                    Schedule.createTimed(
                        id = Schedule.Id("3"),
                        title = "ランチ",
                        description = "チームランチ",
                        date = LocalDate(2025, 10, 8),
                        user = testUser,
                        startTime = LocalTime(12, 0),
                        endTime = LocalTime(13, 0)
                    )
                )

                // 共有ユーザーの予定
                addSchedule(
                    Schedule.createTimed(
                        id = Schedule.Id("4"),
                        title = "山田さんの会議",
                        description = "営業部ミーティング",
                        date = LocalDate(2025, 10, 8),
                        user = sharedUser1,
                        startTime = LocalTime(14, 0),
                        endTime = LocalTime(15, 0)
                    )
                )

                addSchedule(
                    Schedule.createTimed(
                        id = Schedule.Id("5"),
                        title = "佐藤さんの打ち合わせ",
                        description = "クライアントとの打ち合わせ",
                        date = LocalDate(2025, 10, 8),
                        user = sharedUser2,
                        startTime = LocalTime(15, 30),
                        endTime = LocalTime(17, 0)
                    )
                )

                addSchedule(
                    Schedule.createAllDay(
                        id = Schedule.Id("6"),
                        title = "鈴木さんの出張",
                        description = "大阪出張",
                        date = LocalDate(2025, 10, 8),
                        user = sharedUser3
                    )
                )

                // 連日予定のサンプル
                addSchedule(
                    Schedule.createMultiDayAllDay(
                        id = Schedule.Id("7"),
                        title = "夏季休暇",
                        description = "家族旅行",
                        startDate = LocalDate(2025, 10, 10),
                        endDate = LocalDate(2025, 10, 12),
                        user = testUser
                    )
                )
            }
        }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .fillMaxSize(),
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
                    val calendarState = rememberCalendarState(scheduleRepository, userRepository)

                    CalendarScreen(
                        months = calendarState.months,
                        focusedMonth = calendarState.focusedMonth,
                        schedules = calendarState.schedules,
                        sharedUsers = calendarState.sharedUsers,
                        userVisibilityMap = calendarState.userVisibilityMap,
                        userColorMap = calendarState.userColorMap,
                        moveToNext = calendarState::moveToNextMonth,
                        moveToPrev = calendarState::moveToPrevMonth,
                        onClickDate = { day ->
                            navController.navigate(Screen.DaySchedule.from(day.value))
                        },
                        onUserIconClick = {
                            navController.navigate(Screen.Settings)
                        },
                        onQRShareClick = {
                            navController.navigate(Screen.QRShare)
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
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onDateChange = { newDate ->
                            navController.navigate(Screen.DaySchedule.from(newDate)) {
                                popUpTo(Screen.DaySchedule::class) { inclusive = true }
                            }
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
                                Screen.ScheduleAdd.from(
                                    daySchedule.toLocalDate(),
                                    schedule.id.value
                                )
                            )
                        }
                    )
                }

                composable<Screen.ScheduleAdd> { backStackEntry ->
                    val scheduleAdd: Screen.ScheduleAdd = backStackEntry.toRoute()
                    ScheduleAddScreen(
                        date = scheduleAdd.toLocalDate(),
                        scheduleRepository = scheduleRepository,
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
            }
        }
    }
}