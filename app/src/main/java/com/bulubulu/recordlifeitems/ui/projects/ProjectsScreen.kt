package com.bulubulu.recordlifeitems.ui.projects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bulubulu.recordlifeitems.ui.components.ProjectColorCircle
import com.bulubulu.recordlifeitems.data.entity.Project
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    onNavigateToProjectDetail: (Long) -> Unit,
    onNavigateToNewProject: () -> Unit,
    viewModel: ProjectsViewModel = viewModel()
) {
    val projects by viewModel.allProjects.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "项目",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToNewProject() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加项目",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = paddingValues.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(projects) { project ->
                ProjectListItem(
                    project = project,
                    onClick = { onNavigateToProjectDetail(project.id) }
                )
            }
        }
    }


}

@Composable
private fun ProjectListItem(
    project: Project,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProjectColorCircle(
                color = Color(project.color.toInt()),
                size = 36.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val scheduleText = getScheduleText(project)
                if (scheduleText.isNotBlank()) {
                    Text(
                        text = scheduleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (project.description.isNotBlank()) {
                    Text(
                        text = project.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun getScheduleText(project: Project): String {
    val parts = mutableListOf<String>()

    // Weekday info - project.weekDays is a JSON array like "[1,3,5]"
    val weekDaysStr = project.weekDays
    if (!weekDaysStr.isNullOrBlank() && weekDaysStr != "[]") {
        try {
            val jsonArray = JSONArray(weekDaysStr)
            val days = (0 until jsonArray.length()).map { jsonArray.getInt(it) }
            if (days.isNotEmpty()) {
                val dayNames = days.map { day ->
                    when (day) {
                        1 -> "一"
                        2 -> "二"
                        3 -> "三"
                        4 -> "四"
                        5 -> "五"
                        6 -> "六"
                        7 -> "日"
                        else -> ""
                    }
                }.filter { it.isNotBlank() }

                if (dayNames.size == 7) {
                    parts.add("每日")
                } else if (dayNames.isNotEmpty()) {
                    parts.add("周${dayNames.joinToString("")}")
                }
            }
        } catch (e: Exception) {
            // JSON parsing failed, skip
        }
    }

    // Date range info - project.startDate/endDate are epoch millis
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val startMs = project.startDate
    val endMs = project.endDate

    if (startMs != null && endMs != null) {
        parts.add("${dateFormat.format(Date(startMs))} ~ ${dateFormat.format(Date(endMs))}")
    } else if (startMs != null) {
        parts.add("${dateFormat.format(Date(startMs))} 起")
    } else if (endMs != null) {
        parts.add("至 ${dateFormat.format(Date(endMs))}")
    }

    return parts.joinToString(" · ")
}
