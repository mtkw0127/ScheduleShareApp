package com.github.mtkw0127.scheduleshare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.github.mtkw0127.scheduleshare.navigation.Screen
import com.github.mtkw0127.scheduleshare.theme.ScheduleShareTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    ScheduleShareTheme {
        val navController = rememberNavController()

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
                    val calendarState = rememberCalendarState()

                    CalendarScreen(
                        months = calendarState.months,
                        focusedMonth = calendarState.focusedMonth,
                        moveToNext = calendarState::moveToNextMonth,
                        moveToPrev = calendarState::moveToPrevMonth,
                        onClickDate = { day ->
                            navController.navigate(Screen.DaySchedule.from(day.value))
                        },
                        onUserIconClick = {
                            navController.navigate(Screen.Settings)
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

                composable<Screen.DaySchedule> { backStackEntry ->
                    val daySchedule: Screen.DaySchedule = backStackEntry.toRoute()
                    DayScheduleScreen(
                        date = daySchedule.toLocalDate(),
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onDateChange = { newDate ->
                            navController.navigate(Screen.DaySchedule.from(newDate)) {
                                popUpTo(Screen.DaySchedule::class) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}