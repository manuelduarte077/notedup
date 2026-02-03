package dev.wondertech.notedup.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import kotlin.time.ExperimentalTime

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

@OptIn(ExperimentalTime::class)
actual fun todayDate(): LocalDate {
    val currentMillis = currentTimeMillis()
    val instant = kotlin.time.Instant.fromEpochMilliseconds(currentMillis)
    return instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
}
