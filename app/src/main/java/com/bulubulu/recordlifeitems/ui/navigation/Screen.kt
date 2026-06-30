package com.bulubulu.recordlifeitems.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String = "",
    val icon: ImageVector? = null
) {
    data object Home : Screen(
        route = "home",
        title = "日历",
        icon = Icons.Default.CalendarMonth
    )

    data object Projects : Screen(
        route = "projects",
        title = "项目",
        icon = Icons.Default.Folder
    )

    data object Profile : Screen(
        route = "profile",
        title = "个人",
        icon = Icons.Default.Person
    )

    data object DeletedProjects : Screen(
        route = "deleted_projects",
        title = "已删除的活动"
    )

    data object CheckInDetail : Screen(
        route = "checkin_detail/{projectId}/{date}",
        title = "打卡详情"
    ) {
        fun createRoute(projectId: Long, date: String) = "checkin_detail/$projectId/$date"
    }

    data object ProjectDetail : Screen(
        route = "project_detail/{projectId}",
        title = "项目详情"
    ) {
        fun createRoute(projectId: Long) = "project_detail/$projectId"
    }

    data object ProjectHistory : Screen(
        route = "project_history/{projectId}",
        title = "打卡记录"
    ) {
        fun createRoute(projectId: Long) = "project_history/$projectId"
    }

    companion object {
        val bottomNavItems = listOf(Home, Projects, Profile)
    }
}
