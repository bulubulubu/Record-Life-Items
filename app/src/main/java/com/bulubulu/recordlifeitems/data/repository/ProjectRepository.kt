package com.bulubulu.recordlifeitems.data.repository

import com.bulubulu.recordlifeitems.data.dao.ProjectDao
import com.bulubulu.recordlifeitems.data.entity.Project
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {

    val allProjects: Flow<List<Project>> = projectDao.getAll()
    val activeProjects: Flow<List<Project>> = projectDao.getActiveProjects()

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
}
