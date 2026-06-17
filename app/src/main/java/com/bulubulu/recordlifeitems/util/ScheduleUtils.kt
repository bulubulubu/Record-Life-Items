package com.bulubulu.recordlifeitems.util

import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utility helpers for schedule configuration and date calculations.
 */
object ScheduleUtils {

    /**
     * Day-of-week constants (Calendar.DAY_OF_WEEK values).
     * 1 = Sunday, 2 = Monday, ..., 7 = Saturday (Java standard).
     * We store [2,4,6] for Mon/Wed/Fri internally.
     */
    const val SUNDAY = 1
    const val MONDAY = 2
    const val TUESDAY = 3
    const val WEDNESDAY = 4
    const val THURSDAY = 5
    const val FRIDAY = 6
    const val SATURDAY = 7

    /**
     * Friendly display names for weekday numbers.
     */
    private val DAY_NAMES = mapOf(
        MONDAY to "周一",
        TUESDAY to "周二",
        WEDNESDAY to "周三",
        THURSDAY to "周四",
        FRIDAY to "周五",
        SATURDAY to "周六",
        SUNDAY to "周日"
    )

    private val ENGLISH_DAY_NAMES = mapOf(
        MONDAY to "Mon",
        TUESDAY to "Tue",
        WEDNESDAY to "Wed",
        THURSDAY to "Thu",
        FRIDAY to "Fri",
        SATURDAY to "Sat",
        SUNDAY to "Sun"
    )

    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    /**
     * Parse a JSON array string of weekday numbers into a Set.
     * Input format: "[2,4,6]" → Set(2, 4, 6)
     */
    fun parseWeekDays(json: String?): Set<Int> {
        if (json.isNullOrBlank()) return emptySet()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getInt(it) }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    /**
     * Convert a Set of weekday numbers to a JSON array string.
     * Input: setOf(2, 4, 6) → "[2,4,6]"
     */
    fun weekDaysToJson(days: Set<Int>): String {
        val arr = JSONArray()
        days.sorted().forEach { arr.put(it) }
        return arr.toString()
    }

    /**
     * Get the Calendar.DAY_OF_WEEK value for a given epoch millis timestamp.
     */
    fun getDayOfWeek(epochMillis: Long): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMillis
        return cal.get(Calendar.DAY_OF_WEEK)
    }

    /**
     * Check if a project is scheduled for a given epoch millis date,
     * based on the simple weekDays field on the Project entity.
     */
    /**
     * Convert UI weekday (1=Mon..7=Sun) to Java Calendar.DAY_OF_WEEK (1=Sun,2=Mon..7=Sat).
     */
    private fun uiDayToJavaDay(uiDay: Int): Int {
        return when (uiDay) {
            7 -> 1  // Sun -> Calendar.SUNDAY
            else -> uiDay + 1  // Mon(1)->2, Tue(2)->3, ..., Sat(6)->7
        }
    }

    fun isScheduledForDate(
        weekDays: String?,
        startDate: Long?,
        endDate: Long?,
        targetDate: Long
    ): Boolean {
        val selectedDays = parseWeekDays(weekDays).map { uiDayToJavaDay(it) }.toSet()
        if (selectedDays.isEmpty()) return false

        // Check date range
        if (startDate != null && targetDate < startDate) return false
        if (endDate != null && targetDate > endDate) return false

        // Check day of week
        val dayOfWeek = getDayOfWeek(targetDate)
        return dayOfWeek in selectedDays
    }

    /**
     * Get the number of scheduled days in a date range.
     */
    fun countScheduledDays(
        weekDays: String?,
        startDate: Long?,
        endDate: Long?,
        rangeStart: Long,
        rangeEnd: Long
    ): Int {
        val selectedDays = parseWeekDays(weekDays)
        if (selectedDays.isEmpty()) return 0

        val effectiveStart = maxOf(rangeStart, startDate ?: rangeStart)
        val effectiveEnd = minOf(rangeEnd, endDate ?: rangeEnd)

        if (effectiveStart > effectiveEnd) return 0

        var count = 0
        val cal = Calendar.getInstance()
        cal.timeInMillis = effectiveStart
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        while (cal.timeInMillis <= effectiveEnd) {
            if (cal.get(Calendar.DAY_OF_WEEK) in selectedDays) {
                count++
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return count
    }

    /**
     * Format epoch millis to yyyy-MM-dd string.
     */
    fun formatDate(epochMillis: Long): String {
        return DATE_FORMAT.format(Date(epochMillis))
    }

    /**
     * Parse yyyy-MM-dd string to epoch millis (start of day in local timezone).
     */
    fun parseDate(dateStr: String): Long? {
        return try {
            val date = DATE_FORMAT.parse(dateStr)
            date?.time
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get display name for a weekday number (Chinese).
     */
    fun getDayDisplayName(day: Int): String {
        return DAY_NAMES[day] ?: "未知"
    }

    /**
     * Get display name for a weekday number (English).
     */
    fun getDayDisplayNameEn(day: Int): String {
        return ENGLISH_DAY_NAMES[day] ?: "Unknown"
    }

    /**
     * Get all weekday numbers in display order (Monday first).
     */
    fun getAllWeekDays(): List<Int> {
        return listOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
    }

    /**
     * Get weekday display names in Chinese, Monday first.
     */
    fun getAllDayDisplayNames(): List<Pair<Int, String>> {
        return getAllWeekDays().map { it to getDayDisplayName(it) }
    }

    /**
     * Get today's start-of-day timestamp.
     */
    fun todayStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * Get the start of a given date.
     */
    fun startOfDay(epochMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * Get the end of a given date.
     */
    fun endOfDay(epochMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }
}
