package com.bulubulu.recordlifeitems.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bulubulu.recordlifeitems.data.entity.CheckIn
import com.bulubulu.recordlifeitems.data.entity.CheckInDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {

    // --- CheckIn queries ---

    @Query("SELECT * FROM checkins WHERE date = :date ORDER BY createdAt DESC")
    fun getByDate(date: String): Flow<List<CheckIn>>

    @Query("SELECT * FROM checkins WHERE projectId = :projectId AND date = :date LIMIT 1")
    fun getByProjectAndDate(projectId: Long, date: String): Flow<CheckIn?>

    @Query("SELECT * FROM checkins WHERE projectId = :projectId AND date = :date LIMIT 1")
    suspend fun getByProjectAndDateOnce(projectId: Long, date: String): CheckIn?

    @Query("SELECT * FROM checkins WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, createdAt DESC")
    fun getByDateRange(startDate: String, endDate: String): Flow<List<CheckIn>>

    @Query("SELECT * FROM checkins WHERE projectId = :projectId ORDER BY date DESC")
    fun getByProjectId(projectId: Long): Flow<List<CheckIn>>

    @Query("SELECT * FROM checkins WHERE projectId = :projectId ORDER BY date DESC")
    suspend fun getByProjectIdOnce(projectId: Long): List<CheckIn>

    // --- Schedule-related check-in queries ---

    @Query("SELECT * FROM checkins WHERE projectId = :projectId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getByProjectAndDateRange(projectId: Long, startDate: String, endDate: String): Flow<List<CheckIn>>

    @Query("SELECT COUNT(*) FROM checkins WHERE projectId = :projectId")
    fun getTotalCheckInCount(projectId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM checkins WHERE projectId = :projectId AND date BETWEEN :startDate AND :endDate")
    fun getCheckInCountBetweenDates(projectId: Long, startDate: String, endDate: String): Flow<Int>

    @Query("SELECT * FROM checkins WHERE date = :date AND projectId IN (:projectIds) ORDER BY createdAt DESC")
    fun getByDateForProjects(date: String, projectIds: List<Long>): Flow<List<CheckIn>>

    @Query("SELECT DISTINCT date FROM checkins WHERE projectId = :projectId ORDER BY date DESC")
    fun getCheckedInDates(projectId: Long): Flow<List<String>>

    @Query("SELECT DISTINCT date FROM checkins WHERE projectId = :projectId ORDER BY date DESC")
    suspend fun getCheckedInDatesOnce(projectId: Long): List<String>

    @Query("SELECT * FROM checkins ORDER BY date DESC LIMIT :limit")
    fun getRecentCheckIns(limit: Int): Flow<List<CheckIn>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkIn: CheckIn): Long

    @Update
    suspend fun update(checkIn: CheckIn)

    @Delete
    suspend fun delete(checkIn: CheckIn)

    @Query("DELETE FROM checkins WHERE id = :id")
    suspend fun deleteById(id: Long)

    // --- CheckInDetail queries ---

    @Query("SELECT * FROM checkin_details WHERE checkInId = :checkInId ORDER BY sortOrder ASC")
    fun getDetailsByCheckInId(checkInId: Long): Flow<List<CheckInDetail>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetail(detail: CheckInDetail): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetails(details: List<CheckInDetail>)

    @Update
    suspend fun updateDetail(detail: CheckInDetail)

    @Delete
    suspend fun deleteDetail(detail: CheckInDetail)

    @Query("DELETE FROM checkin_details WHERE checkInId = :checkInId")
    suspend fun deleteDetailsByCheckInId(checkInId: Long)

    // --- Transaction: insert check-in with details ---

    @Transaction
    suspend fun insertCheckInWithDetails(checkIn: CheckIn, details: List<CheckInDetail>): Long {
        val checkInId = insert(checkIn)
        val detailsWithCheckInId = details.map { it.copy(checkInId = checkInId) }
        insertDetails(detailsWithCheckInId)
        return checkInId
    }

    @Transaction
    suspend fun deleteCheckInWithDetails(checkIn: CheckIn) {
        delete(checkIn)
        // Details are auto-deleted via CASCADE foreign key
    }
}
