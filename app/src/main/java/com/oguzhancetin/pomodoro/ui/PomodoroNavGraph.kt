package com.oguzhancetin.pomodoro.ui

import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.oguzhancetin.pomodoro.screen.main.MainScreen
import com.oguzhancetin.pomodoro.screen.task.TaskScreen


@Composable
fun PomodoroNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = PomodoroDestinations.MAIN_ROUTE
    ) {

        NavHost(navController = navController, startDestination = startDestination) {
            composable(PomodoroDestinations.MAIN_ROUTE) {
                MainScreen(
                    modifier = modifier,
                    onAddTaskButtonClicked = {navController.navigate(PomodoroDestinations.TASK_ROUTE)})
            }
            composable(PomodoroDestinations.SETTING_ROUTE) {
                SettingScreen(modifier = modifier, onBack = { navController.navigate(PomodoroDestinations.MAIN_ROUTE)})
            }
            composable(PomodoroDestinations.TASK_ROUTE) {
                TaskScreen(modifier = modifier)
            }
            composable(PomodoroDestinations.STATUS_ROUTE) {
                StatusScreen(modifier = modifier, onBack = { navController.navigate(PomodoroDestinations.MAIN_ROUTE)})
            }
        }

}
