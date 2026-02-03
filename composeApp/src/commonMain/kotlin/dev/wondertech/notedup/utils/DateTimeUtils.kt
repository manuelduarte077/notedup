package dev.wondertech.notedup.utils

import kotlinx.datetime.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
object DateTimeUtils {
    /**
     * Converts LocalDate and time components to milliseconds timestamp
     */
    fun dateTimeToMillis(
        date: LocalDate,
        hour: Int,
        minute: Int
    ): Long {
        val localDateTime = LocalDateTime(
            date.year, date.month.number, date.day,
            hour, minute, 0, 0
        )
        return localDateTime.toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
    }

    /**
     * Gets start of day in milliseconds for a given LocalDate (00:00:00)
     */
    fun getStartOfDayMillis(date: LocalDate): Long {
        return dateTimeToMillis(date, 0, 0)
    }

    /**
     * Gets end of day in milliseconds for a given LocalDate (23:59:59.999)
     */
    fun getEndOfDayMillis(date: LocalDate): Long {
        return dateTimeToMillis(date, 23, 59) + 59_999  // Add 59 seconds + 999 millis
    }

    /**
     * Converts milliseconds timestamp to LocalDateTime
     */
    fun millisToLocalDateTime(millis: Long): LocalDateTime {
        val instant = Instant.fromEpochMilliseconds(millis)
        return instant.toLocalDateTime(TimeZone.currentSystemDefault())
    }

    /**
     * Extracts hour from timestamp (0-23)
     */
    fun getHourFromMillis(millis: Long): Int {
        return millisToLocalDateTime(millis).hour
    }

    /**
     * Gets the hour slot index (0-23) for displaying in hour list
     */
    fun getHourSlotIndex(millis: Long): Int {
        return getHourFromMillis(millis)
    }


    /**
     * Checks if a task is overdue based on its deadline timestamp.
     * A task is considered overdue if its timestamp is in the past.
     *
     * @param taskTimestampMillis The task's deadline in milliseconds
     * @return true if the task deadline has passed, false otherwise
     */
    fun isTaskOverdue(taskTimestampMillis: Long): Boolean {
        val currentTimeMillis = currentTimeMillis()
        return taskTimestampMillis < currentTimeMillis
    }
}
