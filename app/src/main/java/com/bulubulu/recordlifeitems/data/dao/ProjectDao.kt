package com.bulubulu.recordlifeitems.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bulubulu.recordlifeitems.data.entity.Project
import com.bulubulu.recordlifeitems.data.entity.ScheduleConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Query("SELECT * FROM projects ORDER BY sortOrder ASC, createdAt DESC")
    fun getAll(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getById(id: Long): Flow<Project?>

    @Query("SELECT * FROM projects WHERE isActive = 1 ORDER BY sortOrder ASC, createdAt DESC")
    fun getActiveProjects(): Flow<List<Project>>

    // --- Schedule-related queries ---

    @Query("SELECT * FROM projects WHERE weekDays IS NOT NULL AND weekDays != '[]' AND isActive = 1 ORDER BY sortOrder ASC")
    fun getScheduledProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE weekDays IS NOT NULL AND weekDays != '[]' AND isActive = 1")
    suspend fun getScheduledProjectsList(): List<Project>

    @Query("SELECT * FROM projects WHERE startDate IS NOT NULL")
    fun getProjectsWithDateRange(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE startDate <= :date AND (endDate IS NULL OR endDate >= :date) AND isActive = 1")
    fun getActiveProjectsAtDate(date: Long): Flow<List<Project>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: Project): Long

    @Update
    suspend fun update(project: Project)

    @Delete
    suspend fun delete(project: Project)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: Long)

    // --- ScheduleConfig queries ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleConfig(scheduleConfig: ScheduleConfig): Long

    @Query("SELECT * FROM schedule_configs WHERE projectId = :projectId")
    fun getScheduleConfigByProjectId(projectId: Long): Flow<ScheduleConfig?>

    @Query("SELECT * FROM schedule_configs WHERE projectId = :projectId")
    suspend fun getScheduleConfigByProjectIdOnce(projectId: Long): ScheduleConfig?

    @Query("SELECT * FROM schedule_configs WHERE isActive = 1")
    fun getActiveScheduleConfigs(): Flow<List<ScheduleConfig>>

    @Query("SELECT * FROM schedule_configs WHERE isActive = 1")
    suspend fun getActiveScheduleConfigsList(): List<ScheduleConfig>

    @Update
    suspend fun updateScheduleConfig(scheduleConfig: ScheduleConfig)

    @Query("DELETE FROM schedule_configs WHERE projectId = :projectId")
    suspend fun deleteScheduleConfigByProjectId(projectId: Long)

    @Query("UPDATE projects SET weekDays = :weekDays WHERE id = :projectId")
    suspend fun updateProjectWeekDays(projectId: Long, weekDays: String?)

    @Query("UPDATE projects SET startDate = :startDate, endDate = :endDate WHERE id = :projectId")
    suspend fun updateProjectDateRange(projectId: Long, startDate: Long?, endDate: Long?)
}
