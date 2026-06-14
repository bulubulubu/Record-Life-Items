package com.bulubulu.recordlifeitems.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "checkin_details",
    foreignKeys = [
        ForeignKey(
            entity = CheckIn::class,
            parentColumns = ["id"],
            childColumns = ["checkInId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["checkInId"])
    ]
)
data class CheckInDetail(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val checkInId: Long,
    val key: String,        // e.g. "学习内容"
    val value: String,      // e.g. "50个新单词"
    val sortOrder: Int = 0
)
