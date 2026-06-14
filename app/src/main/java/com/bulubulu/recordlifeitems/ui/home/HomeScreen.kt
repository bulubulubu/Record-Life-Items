package com.bulubulu.recordlifeitems.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Month navigation header
            MonthHeader(
                year = currentMonth.year,
                month = currentMonth.monthValue,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() }
            )

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

        // Day check-in popup
        if (showDayPopup && selectedDate != null) {
            val dayCheckIns = calendarDays
                .find { it.date == selectedDate }
                ?.checkIns
                ?: emptyList()

            DayCheckInPopup(
                date = selectedDate!!,
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
                    if (allProjects.isNotEmpty()) {
                        onNavigateToCheckInDetail(
                            allProjects.first().id,
                            selectedDate.toString()
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
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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
