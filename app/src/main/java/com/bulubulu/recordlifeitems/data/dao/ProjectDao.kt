package com.bulubulu.recordlifeitems.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bulubulu.recordlifeitems.data.entity.Project
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Query("SELECT * FROM projects ORDER BY sortOrder ASC, createdAt DESC")
    fun getAll(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getById(id: Long): Flow<Project?>

    @Query("SELECT * FROM projects WHERE isActive = 1 ORDER BY sortOrder ASC, createdAt DESC")
    fun getActiveProjects(): Flow<List<Project>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: Project): Long

    @Update
    suspend fun update(project: Project)

    @Delete
    suspend fun delete(project: Project)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: Long)
}
