package com.bulubulu.recordlifeitems.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bulubulu.recordlifeitems.ui.checkin.CheckInScreen
import com.bulubulu.recordlifeitems.ui.home.HomeScreen
import com.bulubulu.recordlifeitems.ui.projects.ProjectsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToCheckInDetail = { projectId, date ->
                        navController.navigate(Screen.CheckInDetail.createRoute(projectId, date))
                    }
                )
            }

            composable(Screen.Projects.route) {
                ProjectsScreen(
                    onNavigateToProjectDetail = { projectId ->
                        navController.navigate(Screen.ProjectDetail.createRoute(projectId))
                    }
                )
            }

            composable(
                route = Screen.CheckInDetail.route,
                arguments = listOf(
                    navArgument("projectId") { type = NavType.LongType },
                    navArgument("date") { type = NavType.StringType }
                )
            ) {
                CheckInScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.ProjectDetail.route,
                arguments = listOf(
                    navArgument("projectId") { type = NavType.LongType }
                )
            ) {
                // Project detail - for now shows project's check-ins
                // This could be expanded to a full screen
                CheckInScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Only show bottom bar on main screens
    val showBottomBar = currentDestination?.route in Screen.bottomNavItems.map { it.route }

    if (showBottomBar) {
        NavigationBar {
            Screen.bottomNavItems.forEach { screen ->
                val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                NavigationBarItem(
                    icon = {
                        screen.icon?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = screen.title
                            )
                        }
                    },
                    label = { Text(screen.title) },
                    selected = selected,
                    onClick = {
                        navController.navigate(screen.route) {
                            // Pop up to start destination to avoid building up large stack
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a tab
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
