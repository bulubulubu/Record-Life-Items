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
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()
    val allProjects: StateFlow<List<Project>> = projectRepository.allProjects
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val _todayCheckInMap = MutableStateFlow<Map<Long, CheckIn?>>(emptyMap())
    private val _checkInsByDate = MutableStateFlow<Map<String, List<CheckIn>>>(emptyMap())

    val currentMonthDays: StateFlow<List<CalendarDay>> = combine(
        _currentMonth, _checkInsByDate, allProjects
    ) { month, checkInMap, projects ->
        buildCalendarDays(month, checkInMap, projects)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun isScheduledForToday(project: Project): Boolean {
        val weekDays = project.weekDays
        if (weekDays.isNullOrBlank()) return true
        val todayMillis = ScheduleUtils.todayStart()
        return ScheduleUtils.isScheduledForDate(weekDays, project.startDate, project.endDate, todayMillis)
    }

    val scheduledForToday: StateFlow<List<QuickCheckInItem>> = combine(
        projectRepository.activeProjects, _todayCheckInMap
    ) { projects, todayMap ->
        projects.filter { isScheduledForToday(it) }.map { p ->
            QuickCheckInItem(project = p, isCheckedInToday = todayMap.containsKey(p.id))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notScheduledToday: StateFlow<List<QuickCheckInItem>> = combine(
        projectRepository.activeProjects, _todayCheckInMap
    ) { projects, todayMap ->
        projects.filter { !isScheduledForToday(it) }.map { p ->
            QuickCheckInItem(project = p, isCheckedInToday = todayMap.containsKey(p.id))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadTodayCheckIns()
        loadWideRangeCheckIns()
    }

    private fun loadWideRangeCheckIns() {
        viewModelScope.launch {
            val now = YearMonth.now()
            val start = now.minusMonths(13).atDay(1).toString()
            val end = now.plusMonths(13).atEndOfMonth().toString()
            _checkInsByDate.value = checkInRepository.getByDateRangeSync(start, end).groupBy { it.date }
        }
    }

    private fun buildCalendarDays(month: YearMonth, checkInMap: Map<String, List<CheckIn>>, projects: List<Project>): List<CalendarDay> {
        val projectMap = projects.associateBy { it.id }
        val today = LocalDate.now()
        val first = month.atDay(1)
        val pad = (first.dayOfWeek.value - 1) % 7
        val start = first.minusDays(pad.toLong())
        val days = mutableListOf<CalendarDay>()
        var cur = start
        for (i in 0 until 42) {
            val ds = cur.toString()
            val ci = (checkInMap[ds] ?: emptyList()).map { c ->
                CheckInWithProject(c, projectMap[c.projectId] ?: Project(name = "Unknown", color = 0xFF999999, description = ""))
            }
            days.add(CalendarDay(cur, cur.month == month.month && cur.year == month.year, cur == today, ci))
            cur = cur.plusDays(1)
        }
        return days
    }

    fun getCalendarDaysForMonth(month: YearMonth): List<CalendarDay> {
        return buildCalendarDays(month, _checkInsByDate.value, allProjects.value)
    }

    private fun loadTodayCheckIns() {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val map = mutableMapOf<Long, CheckIn?>()
            for (p in projectRepository.activeProjects.first()) {
                val ci = checkInRepository.getByProjectAndDate(p.id, today).first()
                if (ci != null) map[p.id] = ci
            }
            _todayCheckInMap.value = map
        }
    }

    fun toggleQuickCheckIn(project: Project) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val existing = _todayCheckInMap.value[project.id]
            if (existing != null) {
                checkInRepository.delete(existing)
            } else {
                checkInRepository.insert(CheckIn(projectId = project.id, date = today, summary = "打卡", details = "[]"))
            }
            loadTodayCheckIns()
            loadWideRangeCheckIns()
        }
    }

    fun refreshCheckInData() {
        loadWideRangeCheckIns()
    }

    fun goToMonth(month: YearMonth) { _currentMonth.value = month }
    fun selectDate(date: LocalDate) { _selectedDate.value = date }
    fun clearSelectedDate() { _selectedDate.value = null }
}
