package dev.wondertech.notedup.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.wondertech.notedup.common.*
import dev.wondertech.notedup.database.LocalDatabase
import dev.wondertech.notedup.modal.TaskData
import dev.wondertech.notedup.notifications.rememberNotificationScheduler
import dev.wondertech.notedup.utils.DateTimeUtils.isTaskOverdue
import dev.wondertech.notedup.utils.formatDateDisplay
import dev.wondertech.notedup.utils.formatTimeDisplay
import kotlinx.coroutines.launch
import notedup.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import kotlin.time.ExperimentalTime


class PreviewTaskScreen(
    private val taskTimestampToEdit: Long
) : Screen {

    @OptIn(ExperimentalTime::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val databaseHelper = LocalDatabase.current
        val coroutineScope = rememberCoroutineScope()
        val scrollState = rememberScrollState()
        val notificationScheduler = rememberNotificationScheduler()

        var taskData by remember { mutableStateOf<TaskData?>(null) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(taskTimestampToEdit) {
            try {
                isLoading = true
                taskData = databaseHelper.getTaskByTimestamp(taskTimestampToEdit)
                isLoading = false
            } catch (e: Exception) {
                println("PreviewTaskScreen: Error loading task - ${e.message}")
                isLoading = false
                navigator.pop()
            }
        }

        Scaffold { _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NotedUpTopAppBar(
                    title = "Task Details",
                    canShowNavigationIcon = true,
                    otherIcon = Res.drawable.edit_icon,
                    trailingIcon = Res.drawable.delete_icon,
                    onBackButtonClick = {
                        navigator.pop()
                    },
                    onOtherIconClick = {
                        navigator.push(CreateTaskScreen(taskTimestampToEdit = taskTimestampToEdit))
                    },
                    onTrailingIconClick = {
                        showDeleteDialog = true
                    }
                )

                taskData?.let { task ->
                    NotedUpStatusBadge(
                        modifier = Modifier.fillMaxWidth(),
                        status = task.isDone.toTaskStatus(),
                        isOverdue = isTaskOverdue(task.timestampMillis),
                        onStatusChange = { newStatus ->
                            val isDone = newStatus.toBoolean()
                            coroutineScope.launch {
                                try {
                                    databaseHelper.updateTaskDoneStatus(task.timestampMillis, isDone)

                                    if (isDone && task.isMeeting) {
                                        notificationScheduler.cancelNotification(task.timestampMillis)
                                    }
                                    taskData = task.copy(isDone = isDone)
                                } catch (e: Exception) {
                                    println("Error updating task status: ${e.message}")
                                }
                            }
                        },
                        fullWidth = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 16.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.calendar),
                                contentDescription = "Calendar",
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = task.timestampMillis.formatDateDisplay(),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.clock),
                                contentDescription = "Clock",
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = task.timestampMillis.formatTimeDisplay(),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (task.isMeeting && task.meetingLink.isNotEmpty()) {
                        val uriHandler = LocalUriHandler.current
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0066CC).copy(alpha = 0.1f))
                                .clickable {
                                    try {
                                        uriHandler.openUri(task.meetingLink)
                                    } catch (e: Exception) {
                                        // Handle invalid URL gracefully
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Meeting link",
                                tint = Color(0xFF0066CC),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = task.meetingLink,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF0066CC),
                                modifier = Modifier.weight(1f),
                                textDecoration = TextDecoration.Underline
                            )
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open link",
                                tint = Color(0xFF0066CC),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = task.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (task.subtitle.isNotBlank()) {
                        Text(
                            text = task.subtitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (task.taskList.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Sub-tasks list",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground.copy(0.8f)
                            )

                            task.taskList.forEach { taskItem ->
                                TaskItemRow(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    taskItem = taskItem,
                                    maxLines = 20,
                                    onToggle = { isCompleted ->
                                        coroutineScope.launch {
                                            databaseHelper.toggleTaskItemCompletion(taskItem.id, isCompleted)
                                            taskData = databaseHelper.getTaskByTimestamp(taskTimestampToEdit)
                                        }
                                    }
                                )
                            }

                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Loading task...",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        DeleteConfirmationDialog(
            showDialog = showDeleteDialog,
            taskTitle = taskData?.title ?: "Task",
            onConfirm = {
                coroutineScope.launch {
                    try {
                        databaseHelper.deleteTask(taskTimestampToEdit)
                        showDeleteDialog = false
                        navigator.pop()
                    } catch (e: Exception) {
                        println("PreviewTaskScreen: Error deleting task - ${e.message}")
                        showDeleteDialog = false
                    }
                }
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }
}
