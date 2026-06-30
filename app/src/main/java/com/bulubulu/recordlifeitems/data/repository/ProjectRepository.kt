package com.bulubulu.recordlifeitems.data.repository

import com.bulubulu.recordlifeitems.data.dao.ProjectDao
import com.bulubulu.recordlifeitems.data.entity.Project
import com.bulubulu.recordlifeitems.data.entity.ScheduleConfig
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {

    // --- Project operations ---

    val allProjects: Flow<List<Project>> = projectDao.getActiveProjects()
    val activeProjects: Flow<List<Project>> = projectDao.getActiveProjects()
    val deletedProjects: Flow<List<Project>> = projectDao.getDeletedProjects()

    fun getById(id: Long): Flow<Project?> {
        return projectDao.getById(id)
    }

    suspend fun insert(project: Project): Long {
        return projectDao.insert(project)
    }

    suspend fun update(project: Project) {
        projectDao.update(project)
    }

    suspend fun delete(project: Project) {
        projectDao.delete(project)
    }

    suspend fun deleteById(id: Long) {
        projectDao.deleteById(id)
    }

    // --- Schedule project queries ---

    fun getScheduledProjects(): Flow<List<Project>> {
        return projectDao.getScheduledProjects()
    }

    suspend fun getScheduledProjectsList(): List<Project> {
        return projectDao.getScheduledProjectsList()
    }

    fun getProjectsWithDateRange(): Flow<List<Project>> {
        return projectDao.getProjectsWithDateRange()
    }

    fun getActiveProjectsAtDate(date: Long): Flow<List<Project>> {
        return projectDao.getActiveProjectsAtDate(date)
    }

    // --- ScheduleConfig operations ---

    suspend fun insertScheduleConfig(scheduleConfig: ScheduleConfig): Long {
        return projectDao.insertScheduleConfig(scheduleConfig)
    }

    fun getScheduleConfigByProjectId(projectId: Long): Flow<ScheduleConfig?> {
        return projectDao.getScheduleConfigByProjectId(projectId)
    }

    suspend fun getScheduleConfigByProjectIdOnce(projectId: Long): ScheduleConfig? {
        return projectDao.getScheduleConfigByProjectIdOnce(projectId)
    }

    fun getActiveScheduleConfigs(): Flow<List<ScheduleConfig>> {
        return projectDao.getActiveScheduleConfigs()
    }

    suspend fun getActiveScheduleConfigsList(): List<ScheduleConfig> {
        return projectDao.getActiveScheduleConfigsList()
    }

    suspend fun updateScheduleConfig(scheduleConfig: ScheduleConfig) {
        projectDao.updateScheduleConfig(scheduleConfig)
    }

    suspend fun deleteScheduleConfigByProjectId(projectId: Long) {
        projectDao.deleteScheduleConfigByProjectId(projectId)
    }

    // --- Combined schedule update ---

    /**
     * Update both the Project's schedule fields and the ScheduleConfig in one call.
     */
    suspend fun updateProjectSchedule(
        projectId: Long,
        weekDays: String?,
        startDate: Long?,
        endDate: Long?
    ) {
        projectDao.updateProjectWeekDays(projectId, weekDays)
        projectDao.updateProjectDateRange(projectId, startDate, endDate)
    }

    /**
     * Enable schedule for a project: set weekDays on Project and create/update ScheduleConfig.
     */
    suspend fun enableSchedule(
        projectId: Long,
        weekDays: String,
        startDate: Long?,
        endDate: Long?,
        recurrence: String = "WEEKLY",
        intervalDays: Int = 1
    ) {
        // Update the Project entity
        projectDao.updateProjectWeekDays(projectId, weekDays)
        projectDao.updateProjectDateRange(projectId, startDate, endDate)

        // Create or update ScheduleConfig
        val existing = projectDao.getScheduleConfigByProjectIdOnce(projectId)
        if (existing != null) {
            projectDao.updateScheduleConfig(
                existing.copy(
                    weekDays = weekDays,
                    startDate = startDate,
                    endDate = endDate,
                    isActive = true,
                    recurrence = recurrence,
                    intervalDays = intervalDays,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            projectDao.insertScheduleConfig(
                ScheduleConfig(
                    projectId = projectId,
                    weekDays = weekDays,
                    startDate = startDate,
                    endDate = endDate,
                    isActive = true,
                    recurrence = recurrence,
                    intervalDays = intervalDays
                )
            )
        }
    }

    /**
     * Disable schedule for a project: clear weekDays on Project and deactivate ScheduleConfig.
     */
    suspend fun disableSchedule(projectId: Long) {
        projectDao.updateProjectWeekDays(projectId, null)
        projectDao.updateProjectDateRange(projectId, null, null)

        val existing = projectDao.getScheduleConfigByProjectIdOnce(projectId)
        if (existing != null) {
            projectDao.updateScheduleConfig(
                existing.copy(
                    isActive = false,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
