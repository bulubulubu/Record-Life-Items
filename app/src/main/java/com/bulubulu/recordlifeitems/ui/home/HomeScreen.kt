package com.bulubulu.recordlifeitems.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bulubulu.recordlifeitems.data.entity.Project
import com.bulubulu.recordlifeitems.ui.components.ProjectColorCircle
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCheckInDetail: (Long, String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val calendarDays by viewModel.calendarDays.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val quickCheckInItems by viewModel.quickCheckInItems.collectAsState()

    var showDayPopup by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "记录生活",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                // Month navigation header
                MonthHeader(
                    year = currentMonth.year,
                    month = currentMonth.monthValue,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() }
                )
            }

            item {
                // Calendar view
                CalendarView(
                    days = calendarDays,
                    onDayClick = { date ->
                        viewModel.selectDate(date)
                        showDayPopup = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Quick check-in section
            item {
                Text(
                    text = "今日打卡",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (quickCheckInItems.isEmpty()) {
                item {
                    Text(
                        text = "暂无活跃项目",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(quickCheckInItems) { item ->
                    QuickCheckInRow(
                        project = item.project,
                        isCheckedIn = item.isCheckedInToday,
                        onToggle = { viewModel.toggleQuickCheckIn(item.project) },
                        onClick = {
                            onNavigateToCheckInDetail(
                                item.project.id,
                                LocalDate.now().toString()
                            )
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Day check-in popup - use local variable to avoid !! on nullable delegated property
        val currentDate = selectedDate
        if (showDayPopup && currentDate != null) {
            val dayCheckIns = calendarDays
                .find { it.date == currentDate }
                ?.checkIns
                ?: emptyList()

            DayCheckInPopup(
                date = currentDate,
                checkIns = dayCheckIns,
                onCheckInClick = { checkInWithProject ->
                    showDayPopup = false
                    onNavigateToCheckInDetail(
                        checkInWithProject.project.id,
                        checkInWithProject.checkIn.date
                    )
                },
                onAddCheckIn = {
                    showDayPopup = false
                    // Navigate to add check-in for the selected date
                    // Use first available project
                    val dateToSend = selectedDate
                    if (allProjects.isNotEmpty() && dateToSend != null) {
                        onNavigateToCheckInDetail(
                            allProjects.first().id,
                            dateToSend.toString()
                        )
                    }
                },
                onDismiss = {
                    showDayPopup = false
                    viewModel.clearSelectedDate()
                }
            )
        }
    }
}

@Composable
private fun QuickCheckInRow(
    project: Project,
    isCheckedIn: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProjectColorCircle(
            color = Color(project.color.toULong()),
            size = 24.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = project.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Checkbox(
            checked = isCheckedIn,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
private fun MonthHeader(
    year: Int,
    month: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "上个月"
                )
            }

            Text(
                text = "${year}年${month}月",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "下个月"
                )
            }
        }
    }
}
