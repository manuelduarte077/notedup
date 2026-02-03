package dev.wondertech.notedup.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.wondertech.notedup.modal.TaskData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UserNotifications.*
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@OptIn(ExperimentalForeignApi::class)
class IOSNotificationScheduler : NotificationScheduler {

    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

    companion object {
        private const val MINUTES_BEFORE = 15
    }

    override suspend fun scheduleMeetingNotification(task: TaskData) {
        val notificationTime = task.timestampMillis - (MINUTES_BEFORE * 60 * 1000)
        val currentTime = NSDate().timeIntervalSince1970 * 1000

        if (notificationTime <= currentTime.toLong()) {
            return
        }

        dispatch_async(dispatch_get_main_queue()) {
            try {
                val bodyText = if (task.meetingLink.isNotEmpty()) {
                    "${task.subtitle}\n\nMeeting Link: ${task.meetingLink}"
                } else {
                    task.subtitle
                }

                val content = UNMutableNotificationContent().apply {
                    setTitle("Meeting: ${task.title} starts in 15 minutes")
                    setBody(bodyText)
                    setSound(UNNotificationSound.defaultSound())

                    setUserInfo(
                        mapOf(
                            "taskTimestamp" to task.timestampMillis.toString(),
                            "meetingLink" to task.meetingLink
                        )
                    )
                }

                val seconds1970To2001 = 978307200.0
                val triggerDate =
                    NSDate(timeIntervalSinceReferenceDate = (notificationTime.toDouble() / 1000.0) - seconds1970To2001)
                val calendar = NSCalendar.currentCalendar

                val components = calendar.components(
                    ((1L shl 0) or  // NSCalendarUnitEra
                            (1L shl 1) or  // NSCalendarUnitYear
                            (1L shl 2) or  // NSCalendarUnitMonth
                            (1L shl 3) or  // NSCalendarUnitDay
                            (1L shl 4) or  // NSCalendarUnitHour
                            (1L shl 5) or  // NSCalendarUnitMinute
                            (1L shl 6)).toULong(),    // NSCalendarUnitSecond
                    fromDate = triggerDate
                )

                val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                    components,
                    repeats = false
                )

                val identifier = "meeting_${task.timestampMillis}"
                val request = UNNotificationRequest.requestWithIdentifier(
                    identifier = identifier,
                    content = content,
                    trigger = trigger
                )

                notificationCenter.addNotificationRequest(request) { error ->
                    if (error != null) {
                        println("IOSNotificationScheduler: Error scheduling notification - $error")
                    } else {
                        println("IOSNotificationScheduler: Scheduled notification for '${task.title}' at $notificationTime")
                    }
                }
            } catch (e: Exception) {
                println("IOSNotificationScheduler: Exception scheduling notification - ${e.message}")
            }
        }
    }

    /**
     * Schedule a task notification based on task type and user preferences.
     * - If task.isMeeting = true, always schedules notification (ignores notificationsEnabled)
     * - If task.isMeeting = false and notificationsEnabled = true, schedules regular task notification
     * - If task.isMeeting = false and notificationsEnabled = false, skips scheduling
     */
    override suspend fun scheduleTaskNotification(task: TaskData, notificationsEnabled: Boolean) {
        if (task.isMeeting) {
            scheduleMeetingNotification(task)
            return
        }

        if (!notificationsEnabled) {
            println("IOSNotificationScheduler: Regular task notifications disabled, skipping")
            return
        }

        val notificationTime = task.timestampMillis - (MINUTES_BEFORE * 60 * 1000)
        val currentTime = NSDate().timeIntervalSince1970 * 1000

        if (notificationTime <= currentTime.toLong()) {
            println("IOSNotificationScheduler: Notification time is in the past, skipping")
            return
        }

        dispatch_async(dispatch_get_main_queue()) {
            try {
                val bodyText = if (task.meetingLink.isNotEmpty()) {
                    "${task.subtitle}\n\nMeeting Link: ${task.meetingLink}"
                } else {
                    task.subtitle
                }

                val content = UNMutableNotificationContent().apply {
                    setTitle("Task: ${task.title} due in 15 minutes")
                    setBody(bodyText)
                    setSound(UNNotificationSound.defaultSound())

                    setUserInfo(
                        mapOf(
                            "taskTimestamp" to task.timestampMillis.toString(),
                            "meetingLink" to task.meetingLink,
                            "isMeeting" to "false"
                        )
                    )
                }

                val seconds1970To2001 = 978307200.0
                val triggerDate =
                    NSDate(timeIntervalSinceReferenceDate = (notificationTime.toDouble() / 1000.0) - seconds1970To2001)
                val calendar = NSCalendar.currentCalendar

                val components = calendar.components(
                    ((1L shl 0) or  // NSCalendarUnitEra
                            (1L shl 1) or  // NSCalendarUnitYear
                            (1L shl 2) or  // NSCalendarUnitMonth
                            (1L shl 3) or  // NSCalendarUnitDay
                            (1L shl 4) or  // NSCalendarUnitHour
                            (1L shl 5) or  // NSCalendarUnitMinute
                            (1L shl 6)).toULong(),    // NSCalendarUnitSecond
                    fromDate = triggerDate
                )

                val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                    components,
                    repeats = false
                )

                val identifier = "task_${task.timestampMillis}"
                val request = UNNotificationRequest.requestWithIdentifier(
                    identifier = identifier,
                    content = content,
                    trigger = trigger
                )

                notificationCenter.addNotificationRequest(request) { error ->
                    if (error != null) {
                        println("IOSNotificationScheduler: Error scheduling regular task notification - $error")
                    } else {
                        println("IOSNotificationScheduler: Scheduled regular task notification for '${task.title}' at $notificationTime")
                    }
                }
            } catch (e: Exception) {
                println("IOSNotificationScheduler: Exception scheduling regular task notification - ${e.message}")
            }
        }
    }

    /**
     * Cancel a previously scheduled notification.
     * Uses the task timestamp to identify and cancel the correct notification.
     */
    override suspend fun cancelNotification(taskTimestamp: Long) {
        dispatch_async(dispatch_get_main_queue()) {
            try {
                val meetingIdentifier = "meeting_$taskTimestamp"
                val taskIdentifier = "task_$taskTimestamp"
                notificationCenter.removePendingNotificationRequestsWithIdentifiers(
                    listOf(
                        meetingIdentifier,
                        taskIdentifier
                    )
                )
                println("IOSNotificationScheduler: Cancelled notification for $taskTimestamp")
            } catch (e: Exception) {
                println("IOSNotificationScheduler: Error cancelling notification - ${e.message}")
            }
        }
    }

    /**
     * Check current notification authorization status on iOS without requesting.
     * Returns the current permission state without showing any dialog.
     */
    override suspend fun checkPermissionStatus(): Boolean {
        var granted = false
        var completed = false

        dispatch_async(dispatch_get_main_queue()) {
            try {
                notificationCenter.getNotificationSettingsWithCompletionHandler { settings ->
                    granted = settings?.authorizationStatus == 2L
                    completed = true
                    println("IOSNotificationScheduler: Permission status checked - granted = $granted")
                }
            } catch (e: Exception) {
                println("IOSNotificationScheduler: Exception checking permission - ${e.message}")
                granted = false
                completed = true
            }
        }

        var waitCount = 0
        while (!completed && waitCount < 30) {
            delay(100)
            waitCount++
        }

        if (!completed) {
            println("IOSNotificationScheduler: Permission check timeout, assuming not granted")
            granted = false
        }

        return granted
    }

    /**
     * Request user notification authorization on iOS.
     * Prompts the user to allow notifications with alert, sound, and badge options.
     * User can change this later in Settings > App > Notifications.
     * This is a blocking call that requests permission synchronously.
     * Only shows the dialog if permission has not been previously requested.
     */
    override suspend fun requestPermission(): Boolean {
        val hasPermission = checkPermissionStatus()
        if (hasPermission) {
            println("IOSNotificationScheduler: Permission already granted, skipping request")
            return true
        }

        var granted = false
        var completed = false

        dispatch_async(dispatch_get_main_queue()) {
            try {
                notificationCenter.requestAuthorizationWithOptions(
                    UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
                ) { success, error ->
                    granted = success
                    completed = true
                    if (error != null) {
                        println("IOSNotificationScheduler: Permission request error - $error")
                    } else {
                        println("IOSNotificationScheduler: Notification permission granted = $success")
                    }
                }
            } catch (e: Exception) {
                println("IOSNotificationScheduler: Exception requesting permission - ${e.message}")
                granted = false
                completed = true
            }
        }

        var waitCount = 0
        while (!completed && waitCount < 30) {
            delay(100)
            waitCount++
        }

        if (!completed) {
            println("IOSNotificationScheduler: Permission request timeout")
            granted = false
        }

        return granted
    }

    /**
     * Cancel all pending notifications.
     * Useful for cleanup when user disables notifications or uninstalls app.
     */
    override suspend fun cancelAllNotifications() {
        dispatch_async(dispatch_get_main_queue()) {
            try {
                notificationCenter.removeAllPendingNotificationRequests()
                println("IOSNotificationScheduler: Cancelled all pending notifications")
            } catch (e: Exception) {
                println("IOSNotificationScheduler: Error cancelling all notifications - ${e.message}")
            }
        }
    }
}

/**
 * Composable provider for NotificationScheduler on iOS.
 * Returns an IOSNotificationScheduler instance.
 */
@Composable
actual fun rememberNotificationScheduler(): NotificationScheduler {
    return remember { IOSNotificationScheduler() }
}
