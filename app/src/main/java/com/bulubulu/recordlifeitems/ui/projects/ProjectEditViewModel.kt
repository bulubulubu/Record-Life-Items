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

data class ProjectField(
    val name: String = "",
    val type: String = "text"
)

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
    private val _selectedIcon = MutableStateFlow<String?>(null)
    val selectedIcon: StateFlow<String?> = _selectedIcon.asStateFlow()
    val selectedColor: StateFlow<Long> = _selectedColor.asStateFlow()

    private val _startDate = MutableStateFlow("")
    val startDate: StateFlow<String> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow("")
    val endDate: StateFlow<String> = _endDate.asStateFlow()

    private val _selectedWeekdays = MutableStateFlow<Set<Int>>(emptySet())
    val selectedWeekdays: StateFlow<Set<Int>> = _selectedWeekdays.asStateFlow()

    private val _fields = MutableStateFlow<List<ProjectField>>(emptyList())
    val fields: StateFlow<List<ProjectField>> = _fields.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _isNewProject = MutableStateFlow(projectId == 0L)
    val isNewProject: StateFlow<Boolean> = _isNewProject.asStateFlow()

    init {
        if (projectId == 0L) {
            // New project - set defaults
            _name.value = ""
            _description.value = ""
            _selectedColor.value = 0xFF4CAF50L
            _fields.value = emptyList()
        } else {
            // Load existing project
            viewModelScope.launch {
                val proj = project.value ?: project.firstOrNull()
                proj?.let { loadProjectFields(it) }
            }
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
        _selectedIcon.value = proj.icon

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

        // Parse fields from JSON
        val fieldsStr = proj.fields
        if (!fieldsStr.isNullOrBlank() && fieldsStr != "[]") {
            try {
                val jsonArray = org.json.JSONArray(fieldsStr)
                val fieldList = (0 until jsonArray.length()).map { i ->
                    val obj = jsonArray.getJSONObject(i)
                    ProjectField(
                        name = obj.getString("name"),
                        type = obj.optString("type", "text")
                    )
                }
                _fields.value = fieldList
            } catch (e: Exception) {
                _fields.value = emptyList()
            }
        } else {
            _fields.value = emptyList()
        }
    }

    fun updateName(name: String) {
        _name.value = name
    }

    fun updateDescription(description: String) {
        _description.value = description
    }

    fun updateIcon(icon: String?) {
        _selectedIcon.value = icon
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

    fun addField() {
        _fields.value = _fields.value + ProjectField()
    }

    fun updateFieldName(index: Int, name: String) {
        val current = _fields.value.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(name = name)
            _fields.value = current
        }
    }

    fun updateFieldType(index: Int, type: String) {
        val current = _fields.value.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(type = type)
            _fields.value = current
        }
    }

    fun removeField(index: Int) {
        val current = _fields.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _fields.value = current
        }
    }

    fun saveProject(onSuccess: () -> Unit) {
        if (_name.value.isBlank()) return
        if (_isNewProject.value) {
            createNewProject(onSuccess)
            return
        }
        val currentProject = project.value ?: return

        viewModelScope.launch {
            val startMillis = parseDateToMillis(_startDate.value)
            val endMillis = parseDateToMillis(_endDate.value)

            // Convert selected weekdays to JSON array format
            val weekDaysJson = if (_selectedWeekdays.value.isNotEmpty()) {
                "[${_selectedWeekdays.value.sorted().joinToString(",")}]"
            } else {
                null
            }

            // Convert fields to JSON
            val fieldsJson = if (_fields.value.isNotEmpty()) {
                val jsonArray = org.json.JSONArray()
                _fields.value.forEach { field ->
                    val obj = org.json.JSONObject()
                    obj.put("name", field.name)
                    obj.put("type", field.type)
                    jsonArray.put(obj)
                }
                jsonArray.toString()
            } else {
                null
            }

            val updatedProject = currentProject.copy(
                name = _name.value,
                description = _description.value,
                color = _selectedColor.value,
                icon = _selectedIcon.value,
                weekDays = weekDaysJson,
                startDate = startMillis,
                endDate = endMillis,
                fields = fieldsJson
            )

            projectRepository.update(updatedProject)
            _saveSuccess.value = true
            onSuccess()
        }
    }

    private fun createNewProject(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val startMillis = parseDateToMillis(_startDate.value)
            val endMillis = parseDateToMillis(_endDate.value)
            val weekDaysJson = if (_selectedWeekdays.value.isNotEmpty()) {
                "[${_selectedWeekdays.value.sorted().joinToString(",")}]"
            } else null
            val fieldsJson = if (_fields.value.isNotEmpty()) {
                val jsonArray = org.json.JSONArray()
                _fields.value.forEach { field ->
                    val obj = org.json.JSONObject()
                    obj.put("name", field.name)
                    obj.put("type", field.type)
                    jsonArray.put(obj)
                }
                jsonArray.toString()
            } else null

            val project = Project(
                name = _name.value,
                description = _description.value,
                color = _selectedColor.value,
                icon = _selectedIcon.value,
                weekDays = weekDaysJson,
                startDate = startMillis,
                endDate = endMillis,
                fields = fieldsJson
            )
            projectRepository.insert(project)
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
