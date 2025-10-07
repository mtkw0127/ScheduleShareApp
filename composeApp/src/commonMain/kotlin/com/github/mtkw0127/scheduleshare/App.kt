package com.github.mtkw0127.scheduleshare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.github.mtkw0127.scheduleshare.navigation.Screen
import com.github.mtkw0127.scheduleshare.theme.ScheduleShareTheme
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import scheduleshare.composeapp.generated.resources.Res
import scheduleshare.composeapp.generated.resources.calendar
import scheduleshare.composeapp.generated.resources.settings

@Composable
@Preview
fun App() {
    ScheduleShareTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val showBottomBar = currentDestination?.hierarchy?.any { destination ->
            destination.route == Screen.Calendar::class.qualifiedName ||
                    destination.route == Screen.Settings::class.qualifiedName
        } ?: false

        Scaffold(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.calendar),
                                    contentDescription = "カレンダー"
                                )
                            },
                            label = { Text("カレンダー") },
                            selected = currentDestination.hierarchy.any {
                                it.route == Screen.Calendar::class.qualifiedName
                            },
                            onClick = {
                                navController.navigate(Screen.Calendar) {
                                    popUpTo(Screen.Calendar) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.settings),
                                    contentDescription = "設定"
                                )
                            },
                            label = { Text("設定") },
                            selected = currentDestination.hierarchy.any {
                                it.route == Screen.Settings::class.qualifiedName
                            },
                            onClick = {
                                navController.navigate(Screen.Settings) {
                                    popUpTo(Screen.Calendar)
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
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
                            }
                        )
                    }

                    composable<Screen.Settings> {
                        SettingsScreen(
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
                            }
                        )
                    }
                }
            }
        }
    }
}