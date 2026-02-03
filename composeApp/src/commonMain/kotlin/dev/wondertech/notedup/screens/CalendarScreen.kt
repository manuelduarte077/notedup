package dev.wondertech.notedup.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.wondertech.notedup.common.DeleteConfirmationDialog
import dev.wondertech.notedup.common.HorizontalCalendar
import dev.wondertech.notedup.common.NotedUpTopAppBar
import dev.wondertech.notedup.database.LocalDatabase
import dev.wondertech.notedup.modal.TaskData
import dev.wondertech.notedup.screens.components.HourColumnItem
import dev.wondertech.notedup.utils.DateTimeUtils
import dev.wondertech.notedup.utils.todayDate
import dev.wondertech.notedup.utils.Utils.hoursList
import kotlinx.coroutines.launch
import notedup.composeapp.generated.resources.Res
import notedup.composeapp.generated.resources.add_icon

class CalendarScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {

        val navigator = LocalNavigator.currentOrThrow
        val databaseHelper = LocalDatabase.current
        val coroutineScope = rememberCoroutineScope()

        var selectedDate by remember {
            mutableStateOf(todayDate())
        }

        var tasksForSelectedDate by remember { mutableStateOf<List<TaskData>>(emptyList()) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var taskToDelete by remember { mutableStateOf<TaskData?>(null) }

        LaunchedEffect(selectedDate) {
            val startMillis = DateTimeUtils.getStartOfDayMillis(selectedDate)
            val endMillis = DateTimeUtils.getEndOfDayMillis(selectedDate)
            tasksForSelectedDate = databaseHelper.getTasksForDate(startMillis, endMillis)
        }

        fun groupTasksByHour(tasks: List<TaskData>): Map<Int, List<TaskData>> {
            return tasks
                .groupBy { DateTimeUtils.getHourSlotIndex(it.timestampMillis) }
                .mapValues { (_, tasksInHour) ->
                    tasksInHour.sortedBy { it.timestampMillis }
                }
        }

        Scaffold { _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(21.dp)
            ) {
                NotedUpTopAppBar(
                    title = "Schedule",
                    canShowNavigationIcon = false,
                    otherIcon = Res.drawable.add_icon,
                    onOtherIconClick = {
                        navigator.push(CreateTaskScreen())
                    }
                )

                HorizontalCalendar { newSelectedDate ->
                    selectedDate = newSelectedDate
                    println("Selected date: $selectedDate")
                }

                val tasksByHour = remember(tasksForSelectedDate) {
                    groupTasksByHour(tasksForSelectedDate)
                }

                Column(modifier = Modifier.fillMaxWidth().wrapContentHeight().verticalScroll(rememberScrollState())) {
                    hoursList.forEachIndexed { index, hourString ->
                        val tasksForThisHour = tasksByHour[index] ?: emptyList()
                        HourColumnItem(
                            hour = hourString,
                            items = tasksForThisHour,
                            onTaskItemToggle = { taskItemId, isChecked ->
                                coroutineScope.launch {
                                    try {
                                        databaseHelper.toggleTaskItemCompletion(taskItemId, isChecked)

                                        val startMillis = DateTimeUtils.getStartOfDayMillis(selectedDate)
                                        val endMillis = DateTimeUtils.getEndOfDayMillis(selectedDate)
                                        tasksForSelectedDate = databaseHelper.getTasksForDate(startMillis, endMillis)
                                    } catch (e: Exception) {
                                        println("CalendarScreen: Error toggling task item - ${e.message}")
                                    }
                                }
                            },
                            onTaskClick = { taskData ->
                                navigator.push(PreviewTaskScreen(taskTimestampToEdit = taskData.timestampMillis))
                            },
                            onTaskLongClick = { taskData ->
                                taskToDelete = taskData
                                showDeleteDialog = true
                            }
                        )
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
                                    val startMillis = DateTimeUtils.getStartOfDayMillis(selectedDate)
                                    val endMillis = DateTimeUtils.getEndOfDayMillis(selectedDate)
                                    tasksForSelectedDate = databaseHelper.getTasksForDate(startMillis, endMillis)

                                    showDeleteDialog = false
                                    taskToDelete = null
                                }
                            } catch (e: Exception) {
                                println("CalendarScreen: Error deleting task - ${e.message}")
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


