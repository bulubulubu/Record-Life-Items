package com.bulubulu.recordlifeitems.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bulubulu.recordlifeitems.data.dao.CheckInDao
import com.bulubulu.recordlifeitems.data.dao.ProjectDao
import com.bulubulu.recordlifeitems.data.entity.CheckIn
import com.bulubulu.recordlifeitems.data.entity.CheckInDetail
import com.bulubulu.recordlifeitems.data.entity.Project
import com.bulubulu.recordlifeitems.data.entity.ScheduleConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Project::class, CheckIn::class, CheckInDetail::class, ScheduleConfig::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun checkInDao(): CheckInDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "record_life_items_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Pre-populate the database with default projects on first creation.
     */
    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDefaultProjects(database.projectDao())
                }
            }
        }

        private suspend fun populateDefaultProjects(projectDao: ProjectDao) {
            val defaultProjects = listOf(
                Project(
                    name = "英语学习",
                    color = 0xFF4CAF50,  // Green
                    description = "每日英语学习打卡",
                    icon = "ic_english",
                    sortOrder = 0
                ),
                Project(
                    name = "锻炼",
                    color = 0xFF2196F3,  // Blue
                    description = "每日锻炼健身打卡",
                    icon = "ic_exercise",
                    sortOrder = 1
                ),
                Project(
                    name = "上班",
                    color = 0xFFFF9800,  // Orange
                    description = "每日工作打卡",
                    icon = "ic_work",
                    sortOrder = 2
                )
            )
            defaultProjects.forEach { project ->
                projectDao.insert(project)
            }
        }
    }
}
