package com.bulubulu.recordlifeitems.ui.checkin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bulubulu.recordlifeitems.data.AppDatabase
import com.bulubulu.recordlifeitems.data.entity.CheckIn
import com.bulubulu.recordlifeitems.data.entity.CheckInDetail
import com.bulubulu.recordlifeitems.data.entity.Project
import com.bulubulu.recordlifeitems.data.repository.CheckInRepository
import com.bulubulu.recordlifeitems.data.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailField(
    val id: Long = 0,
    val key: String = "",
    val value: String = ""
)

class CheckInViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val checkInRepository = CheckInRepository(database.checkInDao())
    private val projectRepository = ProjectRepository(database.projectDao())

    private val projectId: Long = savedStateHandle["projectId"] ?: 0L
    private val date: String = savedStateHandle["date"] ?: ""

    val project: StateFlow<Project?> = projectRepository.getById(projectId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedDate = MutableStateFlow(date)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _summary = MutableStateFlow("")
    val summary: StateFlow<String> = _summary.asStateFlow()

    private val _detailFields = MutableStateFlow<List<DetailField>>(listOf(DetailField()))
    val detailFields: StateFlow<List<DetailField>> = _detailFields.asStateFlow()

    private val _existingCheckIn = MutableStateFlow<CheckIn?>(null)

    init {
        // Load existing check-in if available
        viewModelScope.launch {
            val existing = checkInRepository.getByProjectAndDate(projectId, date).firstOrNull()
            if (existing != null) {
                _existingCheckIn.value = existing
                _selectedDate.value = existing.date
                _summary.value = existing.summary

                val details = checkInRepository.getDetailsByCheckInId(existing.id).firstOrNull()
                if (!details.isNullOrEmpty()) {
                    _detailFields.value = details.map {
                        DetailField(id = it.id, key = it.key, value = it.value)
                    }
                }
            }
        }
    }

    fun updateSummary(summary: String) {
        _summary.value = summary
    }

    fun updateDate(date: String) {
        _selectedDate.value = date
    }

    fun updateDetailField(index: Int, key: String, value: String) {
        val currentFields = _detailFields.value.toMutableList()
        if (index < currentFields.size) {
            currentFields[index] = currentFields[index].copy(key = key, value = value)
            _detailFields.value = currentFields
        }
    }

    fun addDetailField() {
        val currentFields = _detailFields.value.toMutableList()
        currentFields.add(DetailField())
        _detailFields.value = currentFields
    }

    fun removeDetailField(index: Int) {
        val currentFields = _detailFields.value.toMutableList()
        if (currentFields.size > 1 && index < currentFields.size) {
            currentFields.removeAt(index)
            _detailFields.value = currentFields
        }
    }

    fun saveCheckIn(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentSummary = _summary.value
            val currentDate = _selectedDate.value
            val currentFields = _detailFields.value

            if (currentSummary.isBlank() || currentDate.isBlank()) return@launch

            val checkIn = CheckIn(
                id = _existingCheckIn.value?.id ?: 0,
                projectId = projectId,
                date = currentDate,
                summary = currentSummary,
                createdAt = _existingCheckIn.value?.createdAt ?: System.currentTimeMillis()
            )

            val details = currentFields
                .filter { it.key.isNotBlank() || it.value.isNotBlank() }
                .mapIndexed { index, field ->
                    CheckInDetail(
                        id = field.id,
                        checkInId = 0, // Will be set by transaction
                        key = field.key,
                        value = field.value,
                        sortOrder = index
                    )
                }

            if (_existingCheckIn.value != null) {
                // Update existing
                checkInRepository.update(checkIn)
                checkInRepository.deleteDetailsByCheckInId(checkIn.id)
                if (details.isNotEmpty()) {
                    val detailsWithId = details.map { it.copy(checkInId = checkIn.id) }
                    detailsWithId.forEach { checkInRepository.insertDetail(it) }
                }
            } else {
                // Insert new
                checkInRepository.insertCheckInWithDetails(checkIn, details)
            }

            onSuccess()
        }
    }
}
