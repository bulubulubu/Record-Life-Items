package com.bulubulu.recordlifeitems.ui.projects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bulubulu.recordlifeitems.data.AppDatabase
import com.bulubulu.recordlifeitems.data.entity.Project
import com.bulubulu.recordlifeitems.data.repository.ProjectRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProjectsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val projectRepository = ProjectRepository(database.projectDao())

    val allProjects: StateFlow<List<Project>> = projectRepository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedProjects: StateFlow<List<Project>> = projectRepository.deletedProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addProject(
        name: String,
        color: Long,
        description: String,
        startDate: String = "",
        endDate: String = "",
        weekdays: String = ""
    ) {
        viewModelScope.launch {
            val project = Project(
                name = name,
                color = color,
                description = description,
                sortOrder = allProjects.value.size
            )
            val projectId = projectRepository.insert(project)

            // If schedule info provided, enable schedule
            if (weekdays.isNotBlank() || startDate.isNotBlank() || endDate.isNotBlank()) {
                val startMillis = parseDateToMillis(startDate)
                val endMillis = parseDateToMillis(endDate)
                // Convert comma-separated weekdays to JSON array format
                val weekDaysJson = if (weekdays.isNotBlank()) {
                    val days = weekdays.split(",").filter { it.isNotBlank() }
                    "[${days.joinToString(",")}]"
                } else {
                    "[]"
                }
                projectRepository.enableSchedule(
                    projectId = projectId,
                    weekDays = weekDaysJson,
                    startDate = startMillis,
                    endDate = endMillis
                )
            }
        }
    }

    fun saveReorder(items: List<Project>) {
        viewModelScope.launch {
            items.forEachIndexed { index, project ->
                projectRepository.update(project.copy(sortOrder = index))
            }
        }
    }

    fun reorderProjects(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            val currentList = allProjects.value.toMutableList()
            if (fromIndex in currentList.indices && toIndex in currentList.indices) {
                val item = currentList.removeAt(fromIndex)
                currentList.add(toIndex, item)
                // Update sortOrder for all items
                currentList.forEachIndexed { index, project ->
                    projectRepository.update(project.copy(sortOrder = index))
                }
            }
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            projectRepository.update(project)
        }
    }

    fun softDelete(project: Project) {
        viewModelScope.launch {
            projectRepository.update(project.copy(isActive = false))
        }
    }

    fun restoreProject(project: Project) {
        viewModelScope.launch {
            projectRepository.update(project.copy(isActive = true))
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            projectRepository.delete(project)
        }
    }

    private fun parseDateToMillis(dateStr: String): Long? {
        if (dateStr.isBlank()) return null
        return try {
            val parts = dateStr.split("-")
            if (parts.size == 3) {
                val year = parts[0].toLong()
                val month = parts[1].toLong()
                val day = parts[2].toLong()
                // Simple epoch millis calculation
                val cal = java.util.Calendar.getInstance()
                cal.set(year.toInt(), month.toInt() - 1, day.toInt(), 0, 0, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                cal.timeInMillis
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
