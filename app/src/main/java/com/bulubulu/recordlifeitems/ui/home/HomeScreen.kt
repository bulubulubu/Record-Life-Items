package com.bulubulu.recordlifeitems.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.Today
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bulubulu.recordlifeitems.data.entity.Project
import com.bulubulu.recordlifeitems.ui.components.ProjectColorCircle
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.material3.HorizontalDivider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onNavigateToCheckInDetail: (Long, String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val allProjects by viewModel.allProjects.collectAsState()
    val scheduledForToday by viewModel.scheduledForToday.collectAsState()
    val notScheduledToday by viewModel.notScheduledToday.collectAsState()

    var showDayPopup by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Calculate initial page: 0 = 12 months ago, 24 = today, 48 = 12 months ahead
    val initialPage = 24
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 49 } // 49 months: -24 to +24 from current
    )

    // Sync pager position with ViewModel
    LaunchedEffect(pagerState.currentPage) {
        val targetMonth = YearMonth.now().plusMonths((pagerState.currentPage - initialPage).toLong())
        if (targetMonth != currentMonth) {
            viewModel.goToMonth(targetMonth)
        }
    }

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
                // Month navigation header with Today button
                MonthHeader(
                    year = currentMonth.year,
                    month = currentMonth.monthValue,
                    onPreviousMonth = {
                        coroutineScope.launch {
                            val target = pagerState.currentPage - 1
                            pagerState.animateScrollToPage(target)
                        }
                    },
                    onNextMonth = {
                        coroutineScope.launch {
                            val target = pagerState.currentPage + 1
                            pagerState.animateScrollToPage(target)
                        }
                    },
                    onToday = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(initialPage)
                        }
                    }
                )
            }

            item {
                // Calendar HorizontalPager with smooth animation
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalAlignment = Alignment.Top
                ) { page ->
                    val month = YearMonth.now().plusMonths((page - initialPage).toLong())
                    CalendarView(
                        days = viewModel.getCalendarDaysForMonth(month),
                        onDayClick = { date ->
                            viewModel.selectDate(date)
                            showDayPopup = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Scheduled for today section
            item {
                Text(
                    text = "今日打卡",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (scheduledForToday.isEmpty()) {
                item {
                    Text(
                        text = "今日暂无需打卡的项目",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            } else {
                items(scheduledForToday) { item ->
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

            // Not scheduled today section
            if (notScheduledToday.isNotEmpty()) {
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                item {
                    Text(
                        text = "今日无需打卡",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                items(notScheduledToday) { item ->
                    QuickCheckInRow(
                        project = item.project,
                        isCheckedIn = item.isCheckedInToday,
                        onToggle = { viewModel.toggleQuickCheckIn(item.project) },
                        onClick = {
                            onNavigateToCheckInDetail(
                                item.project.id,
                                LocalDate.now().toString()
                            )
                        },
                        dimmed = true
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Day check-in popup
        val currentDate = selectedDate
        if (showDayPopup && currentDate != null) {
            val dayCheckIns = viewModel.getCalendarDaysForMonth(
                YearMonth.from(currentDate)
            ).find { it.date == currentDate }?.checkIns ?: emptyList()

            DayCheckInPopup(
                date = currentDate,
                checkIns = dayCheckIns,
                projects = allProjects,
                onCheckInClick = { checkInWithProject ->
                    showDayPopup = false
                    onNavigateToCheckInDetail(
                        checkInWithProject.project.id,
                        checkInWithProject.checkIn.date
                    )
                },
                onAddCheckIn = { projectId ->
                    showDayPopup = false
                    onNavigateToCheckInDetail(projectId, currentDate.toString())
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
    onClick: () -> Unit,
    dimmed: Boolean = false
) {
    val contentAlpha = if (dimmed) 0.6f else 1f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.alpha(contentAlpha)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProjectColorCircle(
                    color = Color(project.color.toInt()),
                    size = 24.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = project.name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
            }
        }

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
    onNextMonth: () -> Unit,
    onToday: () -> Unit
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

            Row {
                // Today button
                IconButton(onClick = onToday) {
                    Icon(
                        imageVector = Icons.Default.Today,
                        contentDescription = "回到今天",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "下个月"
                    )
                }
            }
        }
    }
}
