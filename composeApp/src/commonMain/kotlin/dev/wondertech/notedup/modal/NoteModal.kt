package dev.wondertech.notedup.modal

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

/**
 * Represents a note with title and content.
 *
 * @property timestampMillis The note's creation time in milliseconds since epoch (used as primary key)
 * @property title The title of the note
 * @property content The content/body of the note
 */
@OptIn(ExperimentalTime::class)
data class NoteData(
    val timestampMillis: Long,
    val title: String,
    val content: String
) {
    /**
     * Computed formatted date string for display.
     * Converts timestampMillis to a human-readable format like "Jan 28, 2026".
     */
    val formattedDate: String
        get() {
            val instant = kotlin.time.Instant.fromEpochMilliseconds(timestampMillis)
            val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val monthNames = listOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )
            val monthName = monthNames.getOrNull(dateTime.month.number - 1) ?: dateTime.month.number.toString()
            return "$monthName ${dateTime.day}, ${dateTime.year}"
        }
}
