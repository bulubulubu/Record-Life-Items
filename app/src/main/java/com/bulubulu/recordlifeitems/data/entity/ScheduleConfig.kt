package com.bulubulu.recordlifeitems.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Detailed schedule configuration for a project.
 * Supports recurrence patterns and active/inactive toggling.
 */
@Entity(
    tableName = "schedule_configs",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["projectId"], unique = true)
    ]
)
data class ScheduleConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val startDate: Long?,                // Schedule start date (epoch millis)
    val endDate: Long?,                  // Schedule end date (epoch millis), null = open-ended
    val weekDays: String?,               // JSON array: [1,3,5] for Mon/Wed/Fri
    val isActive: Boolean = true,        // Whether schedule is currently active
    val recurrence: String = "WEEKLY",   // Recurrence pattern: WEEKLY, DAILY, CUSTOM
    val intervalDays: Int = 1,           // For DAILY/CUSTOM: repeat every N days
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
