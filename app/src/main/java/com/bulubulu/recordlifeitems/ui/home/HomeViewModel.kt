package com.bulubulu.recordlifeitems.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bulubulu.recordlifeitems.data.AppDatabase
import com.bulubulu.recordlifeitems.data.entity.CheckIn
import com.bulubulu.recordlifeitems.data.entity.Project
import com.bulubulu.recordlifeitems.data.repository.CheckInRepository
import com.bulubulu.recordlifeitems.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import com.bulubulu.recordlifeitems.util.ScheduleUtils

data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val checkIns: List<CheckInWithProject> = emptyList()
)

data class CheckInWithProject(
    val checkIn: CheckIn,
    val project: Project
)

data class QuickCheckInItem(
    val project: Project,
    val isCheckedInToday: Boolean
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val checkInRepository = CheckInRepository(database.checkInDao())
    private val projectRepository = ProjectRepository(database.projectDao())

    private val _currentMonth = MutableStateFlow(YearMonth.now())

    private val _currentMonthDays = MutableStateFlow<List<CalendarDay>>(emptyList())
    val currentMonthDays: StateFlow<List<CalendarDay>> = _currentMonthDays.asStateFlow()
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    // All projects for mapping colors
    val allProjects: StateFlow<List<Project>> = projectRepository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's check-ins as a simple map (projectId -> checkIn)
    private val _todayCheckInMap = MutableStateFlow<Map<Long, CheckIn?>>(emptyMap())
    val todayCheckInMap: StateFlow<Map<Long, CheckIn?>> = _todayCheckInMap.asStateFlow()

    // Check if a project is scheduled for today
    private fun isScheduledForToday(project: Project): Boolean {
        val weekDays = project.weekDays
        // No schedule set = available every day
        if (weekDays.isNullOrBlank()) return true
        val todayMillis = ScheduleUtils.todayStart()
        return ScheduleUtils.isScheduledForDate(weekDays, project.startDate, project.endDate, todayMillis)
    }

    // Projects scheduled for today (main check-in section)
    val scheduledForToday: StateFlow<List<QuickCheckInItem>> = combine(
        projectRepository.activeProjects,
        _todayCheckInMap
    ) { projects, todayMap ->
        projects.filter { isScheduledForToday(it) }.map { project ->
            QuickCheckInItem(
                project = project,
                isCheckedInToday = todayMap.containsKey(project.id)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Projects NOT scheduled for today (separate section)
    val notScheduledToday: StateFlow<List<QuickCheckInItem>> = combine(
        projectRepository.activeProjects,
        _todayCheckInMap
    ) { projects, todayMap ->
        projects.filter { !isScheduledForToday(it) }.map { project ->
            QuickCheckInItem(
                project = project,
                isCheckedInToday = todayMap.containsKey(project.id)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calendar days with check-in indicators
    val calendarDays: StateFlow<List<CalendarDay>> = combine(
        _currentMonth,
        checkInRepository.getByDateRange(
            _currentMonth.value.atDay(1).toString(),
            _currentMonth.value.atEndOfMonth().toString()
        ),
        allProjects
    ) { month, checkIns, projects ->
        val projectMap = projects.associateBy { it.id }
        val checkInsByDate = checkIns.groupBy { it.date }
        val today = LocalDate.now()

        val firstDayOfMonth = month.atDay(1)
        val lastDayOfMonth = month.atEndOfMonth()

        // Padding days from previous month
        val startDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1=Monday
        val paddingDays = (startDayOfWeek - 1) % 7
        val startDate = firstDayOfMonth.minusDays(paddingDays.toLong())

        val days = mutableListOf<CalendarDay>()
        var currentDate = startDate

        // Generate 6 weeks of days (42 days max)
        for (i in 0 until 42) {
            val dayCheckIns = checkInsByDate[currentDate.toString()]?.map { checkIn ->
                CheckInWithProject(
                    checkIn = checkIn,
                    project = projectMap[checkIn.projectId] ?: Project(
                        name = "Unknown",
                        color = 0xFF999999,
                        description = ""
                    )
                )
            } ?: emptyList()

            days.add(
                CalendarDay(
                    date = currentDate,
                    isCurrentMonth = currentDate.month == month.month && currentDate.year == month.year,
                    isToday = currentDate == today,
                    checkIns = dayCheckIns
                )
            )
            currentDate = currentDate.plusDays(1)
        }

        days
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadTodayCheckIns()
        viewModelScope.launch {
            _currentMonthDays.value = getCalendarDaysForMonth(_currentMonth.value)
        }
    }

    private fun loadTodayCheckIns() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val projects = projectRepository.activeProjects.first()

            val map = mutableMapOf<Long, CheckIn?>()
            for (project in projects) {
                val checkIn = checkInRepository.getByProjectAndDate(project.id, today).first()
                if (checkIn != null) {
                    map[project.id] = checkIn
                }
            }
            _todayCheckInMap.value = map
        }
    }

    fun toggleQuickCheckIn(project: Project) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val existingCheckIn = _todayCheckInMap.value[project.id]

            if (existingCheckIn != null) {
                // Remove check-in
                checkInRepository.delete(existingCheckIn)
            } else {
                // Add quick check-in with auto-generated summary
                val checkIn = CheckIn(
                    projectId = project.id,
                    date = today,
                    summary = "已打卡",
                    details = "[]"
                )
                checkInRepository.insert(checkIn)
            }
            // Refresh today's check-ins
            loadTodayCheckIns()
        }
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun goToMonth(month: YearMonth) {
        _currentMonth.value = month
        viewModelScope.launch {
            _currentMonthDays.value = getCalendarDaysForMonth(month)
        }
    }

    suspend fun getCalendarDaysForMonth(month: YearMonth): List<CalendarDay> {
        val today = LocalDate.now()
        val firstDayOfMonth = month.atDay(1)
        val lastDayOfMonth = month.atEndOfMonth()

        // Get check-ins for this month
        val checkIns = checkInRepository.getByDateRangeSync(
            firstDayOfMonth.toString(),
            lastDayOfMonth.toString()
        )
        val projectMap = allProjects.value.associateBy { it.id }
        val checkInsByDate = checkIns.groupBy { it.date }

        val startDayOfWeek = firstDayOfMonth.dayOfWeek.value
        val paddingDays = (startDayOfWeek - 1) % 7
        val startDate = firstDayOfMonth.minusDays(paddingDays.toLong())

        val days = mutableListOf<CalendarDay>()
        var currentDate = startDate

        for (i in 0 until 42) {
            val dayCheckIns = checkInsByDate[currentDate.toString()]?.map { checkIn ->
                CheckInWithProject(
                    checkIn = checkIn,
                    project = projectMap[checkIn.projectId] ?: Project(
                        name = "Unknown",
                        color = 0xFF999999,
                        description = ""
                    )
                )
            } ?: emptyList()

            days.add(
                CalendarDay(
                    date = currentDate,
                    isCurrentMonth = currentDate.month == month.month && currentDate.year == month.year,
                    isToday = currentDate == today,
                    checkIns = dayCheckIns
                )
            )
            currentDate = currentDate.plusDays(1)
        }

        return days
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun clearSelectedDate() {
        _selectedDate.value = null
    }
}
