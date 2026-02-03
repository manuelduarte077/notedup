package dev.wondertech.notedup.modal

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

data class TaskItem(
    val id: String,
    val text: String,
    val isCompleted: Boolean = false
)

/**
 * Represents a complete task with multiple sub-tasks and metadata.
 * This is the main data model for managing tasks in the application.
 *
 * @property timestampMillis The task's date and time in milliseconds since epoch (used as primary key)
 * @property id Unique identifier for the task, defaults to timestampMillis as a string
 * @property title The main title of the task
 * @property subtitle Additional description or subtitle for the task
 * @property taskList List of individual task items belonging to this task
 * @property completedTasks Number of completed tasks in the taskList
 */
data class TaskData(
    val timestampMillis: Long,
    val id: String = timestampMillis.toString(),
    val title: String,
    val subtitle: String,
    val taskList: List<TaskItem>,
    var completedTasks: Int = 0,
    val isDone: Boolean = false,
    val isMeeting: Boolean = false,
    val meetingLink: String = ""
) {

    @OptIn(ExperimentalTime::class)
    val deadline: String
        get() {
            val instant = kotlin.time.Instant.fromEpochMilliseconds(timestampMillis)
            val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

            return "${dateTime.date} ${dateTime.time.hour}:${dateTime.time.minute.toString().padStart(2, '0')}"
        }
}