package dev.wondertech.notedup.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import kotlin.time.ExperimentalTime

/**
 * iOS implementation of currentTimeMillis using NSDate.
 * Converts Foundation timeIntervalSince1970 (seconds) to milliseconds.
 *
 * @return Current timestamp in milliseconds since Unix epoch
 */
actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

/**
 * iOS implementation of todayDate.
 * Converts current time to LocalDate in the system's timezone.
 *
 * @return Today's date as LocalDate
 */
@OptIn(ExperimentalTime::class)
actual fun todayDate(): LocalDate {
    val currentMillis = currentTimeMillis()
    val instant = kotlin.time.Instant.fromEpochMilliseconds(currentMillis)
    return instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
}
