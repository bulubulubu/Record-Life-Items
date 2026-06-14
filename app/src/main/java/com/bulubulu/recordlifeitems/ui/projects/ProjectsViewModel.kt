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

    fun addProject(name: String, color: Long, description: String) {
        viewModelScope.launch {
            val project = Project(
                name = name,
                color = color,
                description = description,
                sortOrder = allProjects.value.size
            )
            projectRepository.insert(project)
        }
    }

    fun updateProject(project: Project) {
        viewModelScope.launch {
            projectRepository.update(project)
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            projectRepository.delete(project)
        }
    }
}
