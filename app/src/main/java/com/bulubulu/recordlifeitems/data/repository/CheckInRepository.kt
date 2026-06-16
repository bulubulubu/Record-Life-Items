package com.bulubulu.recordlifeitems.data.repository

import com.bulubulu.recordlifeitems.data.dao.CheckInDao
import com.bulubulu.recordlifeitems.data.entity.CheckIn
import com.bulubulu.recordlifeitems.data.entity.CheckInDetail
import kotlinx.coroutines.flow.Flow

class CheckInRepository(private val checkInDao: CheckInDao) {

    // --- CheckIn operations ---

    fun getByDate(date: String): Flow<List<CheckIn>> {
        return checkInDao.getByDate(date)
    }

    fun getByProjectAndDate(projectId: Long, date: String): Flow<CheckIn?> {
        return checkInDao.getByProjectAndDate(projectId, date)
    }

    fun getByDateRange(startDate: String, endDate: String): Flow<List<CheckIn>> {
        return checkInDao.getByDateRange(startDate, endDate)
    }

    suspend fun getByDateRangeSync(startDate: String, endDate: String): List<CheckIn> {
        return checkInDao.getByDateRangeOnce(startDate, endDate)
    }

    fun getByProjectId(projectId: Long): Flow<List<CheckIn>> {
        return checkInDao.getByProjectId(projectId)
    }

    suspend fun insert(checkIn: CheckIn): Long {
        return checkInDao.insert(checkIn)
    }

    suspend fun update(checkIn: CheckIn) {
        checkInDao.update(checkIn)
    }

    suspend fun delete(checkIn: CheckIn) {
        checkInDao.delete(checkIn)
    }

    suspend fun deleteById(id: Long) {
        checkInDao.deleteById(id)
    }

    // --- CheckInDetail operations ---

    fun getDetailsByCheckInId(checkInId: Long): Flow<List<CheckInDetail>> {
        return checkInDao.getDetailsByCheckInId(checkInId)
    }

    suspend fun insertDetail(detail: CheckInDetail): Long {
        return checkInDao.insertDetail(detail)
    }

    suspend fun updateDetail(detail: CheckInDetail) {
        checkInDao.updateDetail(detail)
    }

    suspend fun deleteDetail(detail: CheckInDetail) {
        checkInDao.deleteDetail(detail)
    }

    suspend fun deleteDetailsByCheckInId(checkInId: Long) {
        checkInDao.deleteDetailsByCheckInId(checkInId)
    }

    // --- Transaction: insert check-in with details ---

    suspend fun insertCheckInWithDetails(checkIn: CheckIn, details: List<CheckInDetail>): Long {
        return checkInDao.insertCheckInWithDetails(checkIn, details)
    }

    suspend fun deleteCheckInWithDetails(checkIn: CheckIn) {
        checkInDao.deleteCheckInWithDetails(checkIn)
    }
}
