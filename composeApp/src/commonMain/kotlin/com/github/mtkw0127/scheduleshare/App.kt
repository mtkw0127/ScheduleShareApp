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
import com.github.mtkw0127.scheduleshare.navigation.Screen
import com.github.mtkw0127.scheduleshare.repository.ScheduleRepository
import com.github.mtkw0127.scheduleshare.theme.ScheduleShareTheme
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    ScheduleShareTheme {
        val navController = rememberNavController()
        val scheduleRepository = remember { ScheduleRepository.createWithSampleData() }

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
                    val calendarState = rememberCalendarState(scheduleRepository)

                    CalendarScreen(
                        months = calendarState.months,
                        focusedMonth = calendarState.focusedMonth,
                        schedules = calendarState.schedules,
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