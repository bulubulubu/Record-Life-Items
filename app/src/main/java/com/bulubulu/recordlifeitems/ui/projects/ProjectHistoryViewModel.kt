package com.bulubulu.recordlifeitems.ui.projects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bulubulu.recordlifeitems.data.AppDatabase
import com.bulubulu.recordlifeitems.data.entity.CheckIn
import com.bulubulu.recordlifeitems.data.entity.Project
import com.bulubulu.recordlifeitems.data.repository.CheckInRepository
import com.bulubulu.recordlifeitems.data.repository.ProjectRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ProjectHistoryViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val checkInRepository = CheckInRepository(database.checkInDao())
    private val projectRepository = ProjectRepository(database.projectDao())

    private val projectId: Long = savedStateHandle.get<Long>("projectId") ?: 0L

    val project: StateFlow<Project?> = projectRepository.getById(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val checkIns: StateFlow<List<CheckIn>> = checkInRepository.getByProjectId(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
