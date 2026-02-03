package dev.wondertech.notedup.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.wondertech.notedup.MainActivity
import dev.wondertech.notedup.modal.TaskData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation of NotificationScheduler.
 * Uses AlarmManager to schedule exact alarms for meeting reminders.
 * Notifications are delivered 15 minutes before the task's due time.
 */
class AndroidNotificationScheduler(
    private val context: Context
) : NotificationScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "meeting_notifications"
        const val CHANNEL_NAME = "Meeting Reminders"
        private const val MINUTES_BEFORE = 15
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming meetings"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override suspend fun scheduleMeetingNotification(task: TaskData) =
        withContext(Dispatchers.IO) {
            val notificationTime = task.timestampMillis - (MINUTES_BEFORE * 60 * 1000)
            val currentTime = System.currentTimeMillis()

            if (notificationTime <= currentTime) {
                return@withContext
            }

            val intent = Intent(context, MeetingNotificationReceiver::class.java).apply {
                putExtra("taskTimestamp", task.timestampMillis)
                putExtra("taskTitle", task.title)
                putExtra("taskSubtitle", task.subtitle)
                putExtra("task_meeting_link", task.meetingLink)
                putExtra("is_meeting", true) // Mark as meeting
            }

            val requestCode = task.timestampMillis.toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            notificationTime,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            notificationTime,
                            pendingIntent
                        )
                    }
                } else {
                    // Android 11 and below
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime,
                        pendingIntent
                    )
                }
            } catch (e: Exception) {
                println("AndroidNotificationScheduler: Error scheduling alarm - ${e.message}")
            }
        }

    override suspend fun scheduleTaskNotification(task: TaskData, notificationsEnabled: Boolean) =
        withContext(Dispatchers.IO) {
            if (task.isMeeting) {
                scheduleMeetingNotification(task)
                return@withContext
            }

            if (!notificationsEnabled) {
                return@withContext
            }

            val notificationTime = task.timestampMillis - (MINUTES_BEFORE * 60 * 1000)
            val currentTime = System.currentTimeMillis()

            if (notificationTime <= currentTime) {
                return@withContext
            }

            val intent = Intent(context, MeetingNotificationReceiver::class.java).apply {
                putExtra("taskTimestamp", task.timestampMillis)
                putExtra("taskTitle", task.title)
                putExtra("taskSubtitle", task.subtitle)
                putExtra("task_meeting_link", task.meetingLink)
                putExtra("is_meeting", false)
            }

            val requestCode = task.timestampMillis.toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            notificationTime,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            notificationTime,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime,
                        pendingIntent
                    )
                }
            } catch (e: Exception) {
                println("AndroidNotificationScheduler: Error scheduling alarm - ${e.message}")
            }
        }

    override suspend fun cancelNotification(taskTimestamp: Long) = withContext(Dispatchers.IO) {
        val intent = Intent(context, MeetingNotificationReceiver::class.java)
        val requestCode = taskTimestamp.toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        } catch (e: Exception) {
            println("AndroidNotificationScheduler: Error cancelling alarm - ${e.message}")
        }
    }

    override suspend fun checkPermissionStatus(): Boolean = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            hasPermission
        } else {
            println("AndroidNotificationScheduler: Running on Android < 13, permission not required")
            true
        }
    }

    override suspend fun requestPermission(): Boolean = withContext(Dispatchers.IO) {
        val hasPermission = checkPermissionStatus()

        if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            println("AndroidNotificationScheduler: POST_NOTIFICATIONS permission NOT granted")
            println("AndroidNotificationScheduler: Permission must be requested from Activity/Composable")
        }

        hasPermission
    }

    override suspend fun cancelAllNotifications() {
        println("AndroidNotificationScheduler: cancelAllNotifications() called")
    }
}

class MeetingNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskTimestamp = intent.getLongExtra("taskTimestamp", 0L)
        val taskTitle = intent.getStringExtra("taskTitle") ?: "Meeting"
        val taskSubtitle = intent.getStringExtra("taskSubtitle") ?: ""
        val taskMeetingLink = intent.getStringExtra("task_meeting_link") ?: ""
        val isMeeting = intent.getBooleanExtra("is_meeting", true) // Default true for backward compatibility

        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("openTaskTimestamp", taskTimestamp)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskTimestamp.toInt(),
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationText = if (taskMeetingLink.isNotEmpty()) {
            "$taskSubtitle\n\nMeeting Link: $taskMeetingLink"
        } else {
            taskSubtitle
        }

        val notificationTitle = if (isMeeting) {
            "Meeting: $taskTitle starts in 15 minutes"
        } else {
            "Task: $taskTitle due in 15 minutes"
        }

        val notification = NotificationCompat.Builder(context, AndroidNotificationScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            ) {
                NotificationManagerCompat.from(context).notify(taskTimestamp.toInt(), notification)

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val cancelIntent = Intent(context, MeetingNotificationReceiver::class.java)
                val cancelPendingIntent = PendingIntent.getBroadcast(
                    context,
                    taskTimestamp.toInt(),
                    cancelIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(cancelPendingIntent)
            } else {
                println("MeetingNotificationReceiver: POST_NOTIFICATIONS permission not granted")
            }
        } catch (e: Exception) {
            println("MeetingNotificationReceiver: Error displaying notification - ${e.message}")
        }
    }
}

@Composable
actual fun rememberNotificationScheduler(): NotificationScheduler {
    val context = LocalContext.current
    return remember { AndroidNotificationScheduler(context) }
}
