package com.bulubulu.recordlifeitems.ui.projects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bulubulu.recordlifeitems.data.AppDatabase
import com.bulubulu.recordlifeitems.data.entity.Project
import com.bulubulu.recordlifeitems.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProjectEditViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val projectRepository = ProjectRepository(database.projectDao())

    private val projectId: Long = savedStateHandle.get<Long>("projectId") ?: 0L

    val project: StateFlow<Project?> = projectRepository.getById(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _selectedColor = MutableStateFlow(0L)
    val selectedColor: StateFlow<Long> = _selectedColor.asStateFlow()

    private val _startDate = MutableStateFlow("")
    val startDate: StateFlow<String> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow("")
    val endDate: StateFlow<String> = _endDate.asStateFlow()

    private val _selectedWeekdays = MutableStateFlow<Set<Int>>(emptySet())
    val selectedWeekdays: StateFlow<Set<Int>> = _selectedWeekdays.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    init {
        // Load the project and populate editable fields
        viewModelScope.launch {
            val proj = project.value ?: project.firstOrNull()
            proj?.let { loadProjectFields(it) }
        }

        // Also react to project flow updates
        viewModelScope.launch {
            project.collect { proj ->
                if (proj != null && _name.value.isEmpty() && _selectedColor.value == 0L) {
                    loadProjectFields(proj)
                }
            }
        }
    }

    private fun loadProjectFields(proj: Project) {
        _name.value = proj.name
        _description.value = proj.description
        _selectedColor.value = proj.color

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Parse startDate from epoch millis
        _startDate.value = proj.startDate?.let { millis ->
            try {
                dateFormat.format(Date(millis))
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        // Parse endDate from epoch millis
        _endDate.value = proj.endDate?.let { millis ->
            try {
                dateFormat.format(Date(millis))
            } catch (e: Exception) {
                ""
            }
        } ?: ""

        // Parse weekDays from JSON array format "[1,3,5]"
        val weekDaysStr = proj.weekDays
        if (!weekDaysStr.isNullOrBlank() && weekDaysStr != "[]") {
            try {
                val jsonArray = org.json.JSONArray(weekDaysStr)
                val days = (0 until jsonArray.length()).map { jsonArray.getInt(it) }
                _selectedWeekdays.value = days.toSet()
            } catch (e: Exception) {
                _selectedWeekdays.value = emptySet()
            }
        } else {
            _selectedWeekdays.value = emptySet()
        }
    }

    fun updateName(name: String) {
        _name.value = name
    }

    fun updateDescription(description: String) {
        _description.value = description
    }

    fun updateColor(color: Long) {
        _selectedColor.value = color
    }

    fun updateStartDate(startDate: String) {
        _startDate.value = startDate
    }

    fun updateEndDate(endDate: String) {
        _endDate.value = endDate
    }

    fun toggleWeekday(day: Int) {
        val current = _selectedWeekdays.value.toMutableSet()
        if (day in current) {
            current.remove(day)
        } else {
            current.add(day)
        }
        _selectedWeekdays.value = current
    }

    fun saveProject(onSuccess: () -> Unit) {
        val currentProject = project.value ?: return
        if (_name.value.isBlank()) return

        viewModelScope.launch {
            val startMillis = parseDateToMillis(_startDate.value)
            val endMillis = parseDateToMillis(_endDate.value)

            // Convert selected weekdays to JSON array format
            val weekDaysJson = if (_selectedWeekdays.value.isNotEmpty()) {
                "[${_selectedWeekdays.value.sorted().joinToString(",")}]"
            } else {
                null
            }

            val updatedProject = currentProject.copy(
                name = _name.value,
                description = _description.value,
                color = _selectedColor.value,
                weekDays = weekDaysJson,
                startDate = startMillis,
                endDate = endMillis
            )

            projectRepository.update(updatedProject)
            _saveSuccess.value = true
            onSuccess()
        }
    }

    private fun parseDateToMillis(dateStr: String): Long? {
        if (dateStr.isBlank()) return null
        return try {
            val parts = dateStr.split("-")
            if (parts.size == 3) {
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val day = parts[2].toInt()
                val cal = Calendar.getInstance()
                cal.set(year, month - 1, day, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
