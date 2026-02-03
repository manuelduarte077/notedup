package dev.wondertech.notedup.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

@OptIn(ExperimentalTime::class)
actual fun todayDate(): LocalDate {
    val currentMillis = currentTimeMillis()
    val instant = Instant.fromEpochMilliseconds(currentMillis)

    return instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
}
