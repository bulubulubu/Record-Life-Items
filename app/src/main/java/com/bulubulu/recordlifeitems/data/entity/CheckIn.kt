package com.bulubulu.recordlifeitems.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "checkins",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["projectId"]),
        Index(value = ["date"]),
        Index(value = ["projectId", "date"], unique = true)
    ]
)
data class CheckIn(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val date: String,           // format: yyyy-MM-dd
    val summary: String,        // e.g. "学习了60min"
    val details: String = "[]", // JSON format for flexible content
    val createdAt: Long = System.currentTimeMillis()
)
