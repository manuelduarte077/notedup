package dev.wondertech.notedup.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import dev.wondertech.notedup.common.DeleteConfirmationDialog
import dev.wondertech.notedup.common.NotedUpTopAppBar
import dev.wondertech.notedup.database.LocalDatabase
import dev.wondertech.notedup.modal.TaskData
import dev.wondertech.notedup.modal.TaskItem
import dev.wondertech.notedup.navigation.BottomNavTab
import dev.wondertech.notedup.notifications.rememberNotificationScheduler
import dev.wondertech.notedup.preferences.AppSettings
import dev.wondertech.notedup.preferences.getPreferencesManager
import dev.wondertech.notedup.primaryColorVariant
import dev.wondertech.notedup.utils.NativeDatePicker
import dev.wondertech.notedup.utils.NativeTimePicker
import dev.wondertech.notedup.utils.currentTimeMillis
import dev.wondertech.notedup.utils.todayDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.number
import notedup.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


class CreateTaskScreen(
    private val taskTimestampToEdit: Long? = null
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val tabNavigator = LocalTabNavigator.current
        val databaseHelper = LocalDatabase.current

        val isEditMode = taskTimestampToEdit != null
        var existingTask by remember { mutableStateOf<TaskData?>(null) }
        val canNavigateBack = navigator.size > 1

        LaunchedEffect(taskTimestampToEdit) {
            if (taskTimestampToEdit != null) {
                try {
                    existingTask = databaseHelper.getTaskByTimestamp(taskTimestampToEdit)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        var taskTitle by remember { mutableStateOf("") }
        var taskDescription by remember { mutableStateOf("") }
        var selectedDate by remember {
            val today = todayDate()
            mutableStateOf(
                "${today.year}-${today.month.number.toString().padStart(2, '0')}-${
                    today.day.toString().padStart(2, '0')
                }"
            )
        }
        var selectedHour by remember {
            val now = Instant.fromEpochMilliseconds(currentTimeMillis())
            val currentDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
            val oneHourLater = currentDateTime.hour + 1
            val hour24 = if (oneHourLater >= 24) oneHourLater - 24 else oneHourLater
            val (hour12, _) = when {
                hour24 == 0 -> Pair(12, "AM")
                hour24 < 12 -> Pair(hour24, "AM")
                hour24 == 12 -> Pair(12, "PM")
                else -> Pair(hour24 - 12, "PM")
            }
            mutableStateOf(hour12)
        }
        var selectedMinute by remember {
            val now = Instant.fromEpochMilliseconds(currentTimeMillis())
            val currentDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
            mutableStateOf(currentDateTime.minute)
        }
        var selectedAmPm by remember {
            val now = Instant.fromEpochMilliseconds(currentTimeMillis())
            val currentDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
            val oneHourLater = currentDateTime.hour + 1
            val hour24 = if (oneHourLater >= 24) oneHourLater - 24 else oneHourLater
            val (_, amPm) = when {
                hour24 == 0 -> Pair(12, "AM")
                hour24 < 12 -> Pair(hour24, "AM")
                hour24 == 12 -> Pair(12, "PM")
                else -> Pair(hour24 - 12, "PM")
            }
            mutableStateOf(amPm)
        }
        var isMeetingTask by remember { mutableStateOf(false) }
        var meetingLink by remember { mutableStateOf("") }

        var showDatePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }

        var taskDetailItems by remember { mutableStateOf(listOf("")) }
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        val notificationScheduler = rememberNotificationScheduler()

        val preferencesManager = remember { getPreferencesManager() }
        val settings by preferencesManager.settingsFlow.collectAsState(AppSettings())

        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isSaving by remember { mutableStateOf(false) }

        var showDeleteDialog by remember { mutableStateOf(false) }

        fun convertTo24Hour(hour12: Int, amPm: String): Int {
            return when {
                amPm == "AM" && hour12 == 12 -> 0
                amPm == "AM" -> hour12
                amPm == "PM" && hour12 == 12 -> 12
                else -> hour12 + 12
            }
        }

        fun convertTo12Hour(hour24: Int): Pair<Int, String> {
            return when {
                hour24 == 0 -> Pair(12, "AM")
                hour24 < 12 -> Pair(hour24, "AM")
                hour24 == 12 -> Pair(12, "PM")
                else -> Pair(hour24 - 12, "PM")
            }
        }

        fun formatDateDisplay(dateString: String): String {
            val parts = dateString.split("-")
            if (parts.size != 3) return dateString
            val year = parts[0]
            val month = parts[1].toIntOrNull() ?: return dateString
            val day = parts[2].toIntOrNull() ?: return dateString

            val monthNames = listOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )
            return "${monthNames.getOrNull(month - 1) ?: month} $day, $year"
        }

        fun formatTimeDisplay(hour: Int, minute: Int, amPm: String): String {
            return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $amPm"
        }

        LaunchedEffect(existingTask) {
            existingTask?.let { task ->
                taskTitle = task.title
                taskDescription = task.subtitle
                isMeetingTask = task.isMeeting
                meetingLink = task.meetingLink

                val instant = Instant.fromEpochMilliseconds(task.timestampMillis)
                val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                selectedDate = "${dateTime.year}-${
                    dateTime.month.number.toString().padStart(2, '0')
                }-${dateTime.day.toString().padStart(2, '0')}"

                val (hour12, amPm) = convertTo12Hour(dateTime.hour)
                selectedHour = hour12
                selectedMinute = dateTime.minute
                selectedAmPm = amPm

                taskDetailItems = if (task.taskList.isEmpty()) {
                    listOf("")
                } else {
                    task.taskList.map { it.text }
                }
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
                    title = if (isEditMode) "Edit your Task" else "Make your Task",
                    canShowNavigationIcon = canNavigateBack,
                    otherIcon = Res.drawable.tick_icon,
                    trailingIcon = if (isEditMode) Res.drawable.delete_icon else null,
                    onBackButtonClick = {
                        if (canNavigateBack) {
                            navigator.pop()
                        }
                    },
                    onTrailingIconClick = {
                        if (isEditMode) {
                            showDeleteDialog = true
                        }
                    },
                    onOtherIconClick = {
                        if (taskTitle.isNotBlank() && selectedDate.isNotBlank()) {
                            if (isSaving) return@NotedUpTopAppBar

                            if (isMeetingTask && meetingLink.trim().isNotEmpty()) {
                                val urlPattern = "^(https?://|www\\.).+".toRegex(RegexOption.IGNORE_CASE)
                                if (!meetingLink.trim().matches(urlPattern)) {
                                    errorMessage =
                                        "Please enter a valid URL for the meeting link (e.g., https://zoom.us/j/...)"
                                    return@NotedUpTopAppBar
                                }
                            }

                            coroutineScope.launch {
                                try {
                                    isSaving = true
                                    errorMessage = null

                                    val dateParts = selectedDate.split("-")
                                    if (dateParts.size == 3) {
                                        val year = dateParts[0].toIntOrNull() ?: run {
                                            errorMessage = "Invalid year in date"
                                            isSaving = false
                                            return@launch
                                        }
                                        val month = dateParts[1].toIntOrNull() ?: run {
                                            errorMessage = "Invalid month in date"
                                            isSaving = false
                                            return@launch
                                        }
                                        val day = dateParts[2].toIntOrNull() ?: run {
                                            errorMessage = "Invalid day in date"
                                            isSaving = false
                                            return@launch
                                        }

                                        if (month !in 1..12) {
                                            errorMessage = "Month must be between 1-12"
                                            isSaving = false
                                            return@launch
                                        }
                                        if (day !in 1..31) {
                                            errorMessage = "Day must be between 1-31"
                                            isSaving = false
                                            return@launch
                                        }

                                        val hour24 = convertTo24Hour(selectedHour, selectedAmPm)
                                        val localDateTime = LocalDateTime(
                                            year, month, day,
                                            hour24, selectedMinute, 0, 0
                                        )

                                        val timestampMillis = if (isEditMode) {
                                            taskTimestampToEdit
                                        } else {
                                            localDateTime
                                                .toInstant(TimeZone.currentSystemDefault())
                                                .toEpochMilliseconds()
                                        }

                                        val taskItems = if (isEditMode) {
                                            val existingItems = existingTask?.taskList ?: emptyList()
                                            taskDetailItems
                                                .filter { it.isNotBlank() }
                                                .mapIndexed { index, text ->
                                                    val existingItem = existingItems.getOrNull(index)
                                                    val itemId = existingItem?.id ?: "${timestampMillis}_item_$index"
                                                    val isCompleted = if (existingItem?.text == text) {
                                                        existingItem.isCompleted
                                                    } else {
                                                        false
                                                    }

                                                    TaskItem(
                                                        id = itemId,
                                                        text = text,
                                                        isCompleted = isCompleted
                                                    )
                                                }
                                        } else {
                                            taskDetailItems
                                                .filter { it.isNotBlank() }
                                                .mapIndexed { index, text ->
                                                    TaskItem(
                                                        id = "${timestampMillis}_item_$index",
                                                        text = text,
                                                        isCompleted = false
                                                    )
                                                }
                                        }

                                        val completedCount = taskItems.count { it.isCompleted }
                                        val taskData = TaskData(
                                            timestampMillis = timestampMillis,
                                            title = taskTitle,
                                            subtitle = taskDescription,
                                            taskList = taskItems,
                                            completedTasks = completedCount,
                                            isMeeting = isMeetingTask,
                                            meetingLink = if (isMeetingTask) meetingLink.trim() else ""
                                        )

                                        if (isEditMode) {
                                            databaseHelper.updateTask(taskData)
                                        } else {
                                            databaseHelper.insertTask(taskData)
                                        }

                                        try {
                                            notificationScheduler.cancelNotification(taskData.timestampMillis)

                                            if (!taskData.isDone) {
                                                val hasPermission = notificationScheduler.checkPermissionStatus()
                                                if (hasPermission) {
                                                    notificationScheduler.scheduleTaskNotification(
                                                        taskData,
                                                        settings.notificationsEnabled
                                                    )
                                                } else {
                                                    println("CreateTask: No notification permission")
                                                }
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }

                                        if (isEditMode) {
                                            navigator.pop()
                                            navigator.pop()
                                        } else {
                                            navigator.pop()
                                            tabNavigator.current = BottomNavTab.HomeTab
                                        }
                                    } else {
                                        errorMessage = "Invalid date format. Use YYYY-MM-DD"
                                        isSaving = false
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    errorMessage = "Failed to save task: ${e.message}"
                                    isSaving = false
                                }
                            }
                        } else {
                            errorMessage = when {
                                taskTitle.isBlank() -> "Please enter a task title"
                                selectedDate.isBlank() -> "Please enter a deadline date"
                                else -> "Please fill in required fields"
                            }
                        }
                    }
                )

                errorMessage?.let { message ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFFFCDD2),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = message,
                            color = Color(0xFFC62828),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Meeting?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Switch(
                            checked = isMeetingTask,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    coroutineScope.launch {
                                        val hasPermission = notificationScheduler.checkPermissionStatus()
                                        if (hasPermission) {
                                            println("CreateTask: Permission granted, enabling meeting toggle")
                                            isMeetingTask = true
                                        } else {
                                            println("CreateTask: Permission not granted, cannot enable meeting")
                                            errorMessage =
                                                "Please enable notification permission in Settings to use meeting reminders"
                                            isMeetingTask = false
                                        }
                                    }
                                } else {
                                    isMeetingTask = false
                                    errorMessage = null
                                }
                            }
                        )

                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Deadline & Time",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = formatDateDisplay(selectedDate),
                            onValueChange = { },
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (!isEditMode) {
                                        showDatePicker = true
                                    }
                                },
                            enabled = false,
                            leadingIcon = {
                                Image(
                                    painter = painterResource(Res.drawable.calendar),
                                    contentDescription = "Calendar",
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = if (isEditMode)
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.onBackground,
                                disabledTextColor = MaterialTheme.colorScheme.onBackground,
                                disabledLeadingIconColor = primaryColorVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = formatTimeDisplay(selectedHour, selectedMinute, selectedAmPm),
                            onValueChange = { },
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (!isEditMode) {
                                        showTimePicker = true
                                    }
                                },
                            enabled = false,
                            leadingIcon = {
                                Image(
                                    painter = painterResource(Res.drawable.clock),
                                    contentDescription = "Clock",
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = if (isEditMode)
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.onBackground,
                                disabledTextColor = MaterialTheme.colorScheme.onBackground,
                                disabledLeadingIconColor = primaryColorVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Task Title",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Enter task title",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            cursorColor = MaterialTheme.colorScheme.onBackground
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (isMeetingTask) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Meeting Link (Optional)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        OutlinedTextField(
                            value = meetingLink,
                            onValueChange = { meetingLink = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "Enter meeting link (e.g., Zoom, Google Meet)",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                cursorColor = MaterialTheme.colorScheme.onBackground
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Description",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OutlinedTextField(
                        value = taskDescription,
                        onValueChange = { taskDescription = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        placeholder = {
                            Text(
                                text = "Enter task description",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            cursorColor = MaterialTheme.colorScheme.onBackground
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        maxLines = 15,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Task Details Checklist
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tasks List",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        TextButton(
                            onClick = {
                                val lastItem = taskDetailItems.lastOrNull()
                                if (!lastItem.isNullOrBlank()) {
                                    taskDetailItems = taskDetailItems + ""
                                    coroutineScope.launch {
                                        delay(100)
                                        scrollState.animateScrollTo(scrollState.maxValue)
                                    }
                                }
                            }
                        ) {
                            Text(
                                text = "+ Add Item",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        taskDetailItems.forEachIndexed { index, item ->
                            if (index < taskDetailItems.size) {
                                var itemText by remember(item) { mutableStateOf(item) }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = itemText,
                                        onValueChange = { newText ->
                                            itemText = newText
                                            val newItems = taskDetailItems.toMutableList()
                                            if (index >= newItems.size) {
                                                repeat(index - newItems.size + 1) {
                                                    newItems.add("")
                                                }
                                            }
                                            newItems[index] = newText
                                            taskDetailItems = newItems
                                        },
                                        modifier = Modifier.weight(1f),
                                        placeholder = {
                                            Text(
                                                text = "Task item ${index + 1}",
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                            cursorColor = MaterialTheme.colorScheme.onBackground
                                        ),
                                        maxLines = 2,
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )

                                    if (taskDetailItems.size > 1) {
                                        Icon(
                                            modifier = Modifier.clickable {
                                                val newItems = taskDetailItems.toMutableList()
                                                if (index < newItems.size) {
                                                    newItems.removeAt(index)
                                                    taskDetailItems = newItems
                                                }
                                            }.size(18.dp),
                                            painter = painterResource(Res.drawable.close_icon),
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            if (isEditMode && existingTask != null) {
                DeleteConfirmationDialog(
                    showDialog = showDeleteDialog,
                    taskTitle = existingTask?.title ?: "",
                    onDismiss = {
                        showDeleteDialog = false
                    },
                    onConfirm = {
                        coroutineScope.launch {
                            try {
                                taskTimestampToEdit?.let { timestamp ->
                                    databaseHelper.deleteTask(timestamp)
                                    showDeleteDialog = false
                                    navigator.pop()
                                }
                            } catch (e: Exception) {
                                println("DeleteTask: Error - ${e.message}")
                                errorMessage = "Failed to delete task: ${e.message}"
                                showDeleteDialog = false
                            }
                        }
                    }
                )
            }

            if (showDatePicker) {
                val dateParts = selectedDate.split("-")
                val initialYear = dateParts.getOrNull(0)?.toIntOrNull() ?: 2024
                val initialMonth = dateParts.getOrNull(1)?.toIntOrNull() ?: 1
                val initialDay = dateParts.getOrNull(2)?.toIntOrNull() ?: 1

                NativeDatePicker(
                    initialYear = initialYear,
                    initialMonth = initialMonth,
                    initialDay = initialDay,
                    onDateSelected = { year, month, day ->
                        selectedDate = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                        showDatePicker = false
                    },
                    onDismiss = {
                        showDatePicker = false
                    }
                )
            }

            if (showTimePicker) {
                NativeTimePicker(
                    initialHour = selectedHour,
                    initialMinute = selectedMinute,
                    initialAmPm = selectedAmPm,
                    onTimeSelected = { hour, minute, amPm ->
                        selectedHour = hour
                        selectedMinute = minute
                        selectedAmPm = amPm
                        showTimePicker = false
                    },
                    onDismiss = {
                        showTimePicker = false
                    }
                )
            }
        }
    }
}