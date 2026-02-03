package dev.wondertech.notedup.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.wondertech.notedup.common.DeleteConfirmationDialog
import dev.wondertech.notedup.common.NotedUpTopAppBar
import dev.wondertech.notedup.common.TaskCard
import dev.wondertech.notedup.common.TaskSummaryCards
import dev.wondertech.notedup.database.LocalDatabase
import dev.wondertech.notedup.modal.TaskData
import dev.wondertech.notedup.notifications.rememberNotificationPermissionRequester
import dev.wondertech.notedup.notifications.rememberNotificationScheduler
import dev.wondertech.notedup.utils.DateTimeUtils.isTaskOverdue
import dev.wondertech.notedup.utils.currentTimeMillis
import kotlinx.coroutines.launch
import notedup.composeapp.generated.resources.Res
import notedup.composeapp.generated.resources.no_task
import notedup.composeapp.generated.resources.settings_icon
import org.jetbrains.compose.resources.painterResource


class MainScreen : Screen {

    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow
        val databaseHelper = LocalDatabase.current
        val coroutineScope = rememberCoroutineScope()
        val notificationScheduler = rememberNotificationScheduler()

        val requestNotificationPermission = rememberNotificationPermissionRequester { isGranted ->
            if (isGranted) {
                println("MainScreen: Notification permission granted")
            } else {
                println("MainScreen: Notification permission denied")
            }
        }

        LaunchedEffect(Unit) {
            val hasPermission = notificationScheduler.checkPermissionStatus()
            if (!hasPermission) {
                requestNotificationPermission()
            } else {
                println("MainScreen: Notification permission already granted")
            }
        }

        var allTasks by remember { mutableStateOf<List<TaskData>>(emptyList()) }
        var selectedFilter by remember { mutableStateOf("Active") }
        var filteredTasks by remember { mutableStateOf<List<TaskData>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        var showDeleteDialog by remember { mutableStateOf(false) }
        var taskToDelete by remember { mutableStateOf<TaskData?>(null) }

        LaunchedEffect(Unit) {
            try {
                isLoading = true
                allTasks = databaseHelper.getAllTasks()
            } catch (e: Exception) {
                e.printStackTrace()
                allTasks = emptyList()
            } finally {
                isLoading = false
            }
        }

        LaunchedEffect(selectedFilter, allTasks) {
            val currentTime = currentTimeMillis()

            filteredTasks = when (selectedFilter) {
                "All" -> allTasks.sortedBy { it.timestampMillis }
                "Completed" -> allTasks.filter { it.isDone }.sortedBy { it.timestampMillis }
                "Active" -> allTasks
                    .filter { !it.isDone && it.timestampMillis >= currentTime }
                    .sortedBy { it.timestampMillis }

                "Overdue" -> allTasks
                    .filter { !it.isDone && isTaskOverdue(it.timestampMillis) }
                    .sortedBy { it.timestampMillis }

                else -> allTasks.sortedBy { it.timestampMillis }
            }
        }

        Scaffold { _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp)
            ) {

                NotedUpTopAppBar(
                    title = "NotedUp",
                    canShowNavigationIcon = false,
                    trailingIcon = Res.drawable.settings_icon,
                    onOtherIconClick = {
                        navigator.push(NotesScreen())
                    },
                    onTrailingIconClick = {
                        navigator.push(SettingsScreen())
                    }
                )

                Spacer(Modifier.height(16.dp))

                if (allTasks.isNotEmpty()) {
                    val totalTasks = allTasks.size
                    val completedTasks = allTasks.count { it.isDone }
                    val activeTasks = allTasks.count { !it.isDone }
                    val overdueTasks = allTasks.count { !it.isDone && isTaskOverdue(it.timestampMillis) }

                    TaskSummaryCards(
                        totalTasks = totalTasks,
                        completedTasks = completedTasks,
                        activeTasks = activeTasks,
                        overdueTasks = overdueTasks,
                        selectedFilter = selectedFilter,
                        onFilterSelected = { filter ->
                            selectedFilter = filter
                        }
                    )

                    Spacer(Modifier.height(24.dp))
                }

                if (allTasks.isNotEmpty() && filteredTasks.isNotEmpty()) {
                    Text(
                        text = when (selectedFilter) {
                            "All" -> "All Tasks"
                            "Completed" -> "Completed Tasks"
                            "Active" -> "Active Tasks"
                            "Overdue" -> "Overdue Tasks"
                            else -> "Recent Tasks"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Loading tasks...",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                } else if (filteredTasks.isEmpty() && allTasks.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.no_task),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No ${selectedFilter.lowercase()} tasks",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (allTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.no_task),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No tasks yet\nadd one to get started",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    )
                    {
                        items(filteredTasks, key = { it.timestampMillis }) { task ->
                            TaskCard(
                                taskData = task,
                                onTaskDoneToggle = { isDone ->
                                    coroutineScope.launch {
                                        try {
                                            databaseHelper.updateTaskDoneStatus(task.timestampMillis, isDone)

                                            if (isDone && task.isMeeting) {
                                                notificationScheduler.cancelNotification(task.timestampMillis)
                                            }

                                            allTasks = allTasks.map { currentTask ->
                                                if (currentTask.timestampMillis == task.timestampMillis) {
                                                    currentTask.copy(isDone = isDone)
                                                } else {
                                                    currentTask
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                                onTaskItemToggle = { taskItemId, isChecked ->
                                    coroutineScope.launch {
                                        try {
                                            databaseHelper.toggleTaskItemCompletion(taskItemId, isChecked)

                                            allTasks = allTasks.map { currentTask ->
                                                if (currentTask.timestampMillis == task.timestampMillis) {
                                                    val updatedTaskList = currentTask.taskList.map { item ->
                                                        if (item.id == taskItemId) {
                                                            item.copy(isCompleted = isChecked)
                                                        } else {
                                                            item
                                                        }
                                                    }
                                                    val completedCount = updatedTaskList.count { it.isCompleted }

                                                    databaseHelper.updateCompletedCount(
                                                        currentTask.timestampMillis,
                                                        completedCount
                                                    )

                                                    currentTask.copy(
                                                        taskList = updatedTaskList,
                                                        completedTasks = completedCount
                                                    )
                                                } else {
                                                    currentTask
                                                }
                                            }

                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                                onClick = {
                                    navigator.push(PreviewTaskScreen(taskTimestampToEdit = task.timestampMillis))
                                },
                                onLongClick = {
                                    taskToDelete = task
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }

            if (showDeleteDialog && taskToDelete != null) {
                DeleteConfirmationDialog(
                    showDialog = showDeleteDialog,
                    taskTitle = taskToDelete?.title ?: "",
                    onDismiss = {
                        showDeleteDialog = false
                        taskToDelete = null
                    },
                    onConfirm = {
                        coroutineScope.launch {
                            try {
                                taskToDelete?.let { task ->
                                    databaseHelper.deleteTask(task.timestampMillis)
                                    allTasks = allTasks.filter { it.timestampMillis != task.timestampMillis }
                                    showDeleteDialog = false
                                    taskToDelete = null
                                }
                            } catch (e: Exception) {
                                showDeleteDialog = false
                                taskToDelete = null
                            }
                        }
                    }
                )
            }
        }
    }
}