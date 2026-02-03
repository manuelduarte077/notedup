package dev.wondertech.notedup.common

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.wondertech.notedup.*
import dev.wondertech.notedup.modal.TaskData
import dev.wondertech.notedup.modal.TaskItem
import dev.wondertech.notedup.utils.DateTimeUtils.isTaskOverdue
import notedup.composeapp.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun NotedUpTopAppBar(
    title: String,
    canShowNavigationIcon: Boolean,
    otherIcon: DrawableResource? = null,
    trailingIcon: DrawableResource? = null,
    onBackButtonClick: () -> Unit = {},
    onTrailingIconClick: () -> Unit = {},
    onOtherIconClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (canShowNavigationIcon) {
            Icon(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        onBackButtonClick()
                    },
                painter = painterResource(Res.drawable.back_button),
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            modifier = Modifier.weight(1f),
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (otherIcon != null) {
            TaskarooRoundedIcon(
                icon = otherIcon, getAddButtonClick = {
                    onOtherIconClick()
                })
        }

        if (trailingIcon != null) {
            TaskarooRoundedIcon(
                icon = trailingIcon, getAddButtonClick = {
                    onTrailingIconClick()
                })
        }
    }
}

/**
 * Alert dialog for confirming item deletion
 *
 * @param showDialog Whether the dialog should be displayed
 * @param taskTitle The title of the item to be deleted
 * @param itemType The type of item being deleted (default: "Task")
 * @param onDismiss Callback invoked when dialog is dismissed or cancelled
 * @param onConfirm Callback invoked when user confirms deletion
 */
@Composable
fun DeleteConfirmationDialog(
    showDialog: Boolean, taskTitle: String, itemType: String = "Task", onDismiss: () -> Unit, onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss, title = {
                Text(
                    text = "Delete $itemType",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }, text = {
                Text(
                    text = "Are you sure you want to delete \"$taskTitle\" permanently?",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }, confirmButton = {
                TextButton(
                    onClick = onConfirm
                ) {
                    Text(
                        text = "Delete", color = Color.Red, fontWeight = FontWeight.Bold
                    )
                }
            }, dismissButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                }
            }, containerColor = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * Circular icon button with transparent background and border
 *
 * @param icon The drawable resource for the icon
 * @param getAddButtonClick Callback invoked when the icon is clicked
 */
@Composable
fun TaskarooRoundedIcon(icon: DrawableResource, getAddButtonClick: () -> Unit) {
    Surface(
        modifier = Modifier.clip(CircleShape).clickable {
            getAddButtonClick()
        },
        color = Color.Transparent,
        shape = CircleShape,
        border = BorderStroke(1.dp, color = primaryLiteColorVariant)
    ) {
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

/**
 * Compact task card displaying task title, items, and deadline
 *
 * @param modifier Modifier to apply to the card
 * @param taskData The task data to display
 * @param onTaskItemToggle Callback invoked when a task item checkbox is toggled
 * @param onClick Callback invoked when the card is clicked
 * @param onLongClick Callback invoked when the card is long-pressed
 */
@Composable
fun TaskCardConcise(
    modifier: Modifier,
    taskData: TaskData,
    onTaskItemToggle: (String, Boolean) -> Unit = { _, _ -> },
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.alpha(if (taskData.isDone) 0.85f else 1f)
            .combinedClickable(onClick = { onClick() }, onLongClick = { onLongClick() }),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.onBackground),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                modifier = Modifier.wrapContentWidth(),
                text = taskData.title,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
                textDecoration = if (taskData.isDone) TextDecoration.LineThrough else TextDecoration.None
            )

            if (taskData.isMeeting && taskData.meetingLink.isNotEmpty()) {
                val uriHandler = LocalUriHandler.current

                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).clickable {
                        try {
                            uriHandler.openUri(taskData.meetingLink)
                        } catch (e: Exception) {
                            // Handle invalid URL gracefully
                        }
                    },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Meeting link",
                        tint = Color(0xFF0066CC),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = taskData.meetingLink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF0066CC),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = TextDecoration.Underline
                    )
                }
            } else if (taskData.subtitle.isNotEmpty()) {
                Text(
                    modifier = Modifier.wrapContentWidth(),
                    text = taskData.subtitle,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textDecoration = if (taskData.isDone) TextDecoration.LineThrough else TextDecoration.None
                )
            }

            if (taskData.taskList.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    taskData.taskList.take(2).forEach { taskItem ->
                        TaskItemRow(
                            taskItem = taskItem, maxLines = 2, isConciseItem = true, onToggle = { isChecked ->
                                onTaskItemToggle(taskItem.id, isChecked)
                            })
                    }
                }
            }

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Due: " + taskData.deadline,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
                textDecoration = if (taskData.isDone) TextDecoration.LineThrough else TextDecoration.None
            )

        }
    }
}


/**
 * Full-featured task card with title, subtitle, priority, deadline, and progress tracking
 *
 * @param taskData The task data to display
 * @param onTaskItemToggle Callback invoked when a task item checkbox is toggled
 * @param onClick Callback invoked when the card is clicked
 * @param onLongClick Callback invoked when the card is long-pressed
 */
@Composable
fun TaskCard(
    taskData: TaskData,
    onTaskDoneToggle: (Boolean) -> Unit = {},
    onTaskItemToggle: (String, Boolean) -> Unit = { _, _ -> },
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {

    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).clip(RoundedCornerShape(16.dp))
            .alpha(if (taskData.isDone) 0.85f else 1f)
            .combinedClickable(onClick = { onClick() }, onLongClick = { onLongClick() }),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.onBackground),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .then(if (taskData.taskList.isNotEmpty()) Modifier.padding(top = 16.dp) else Modifier.padding(vertical = 16.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {

                Text(
                    modifier = Modifier.weight(1f),
                    text = taskData.title,
                    fontSize = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textDecoration = if (taskData.isDone) TextDecoration.LineThrough else TextDecoration.None
                )

                Spacer(Modifier.height(16.dp))

                TaskarooStatusBadge(
                    status = taskData.isDone.toTaskStatus(),
                    isOverdue = isTaskOverdue(taskData.timestampMillis),
                    onStatusChange = { newStatus ->
                        onTaskDoneToggle(newStatus.toBoolean())
                    },
                    fullWidth = false
                )
            }

            Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp)) {

                if (taskData.isMeeting && taskData.meetingLink.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable {
                            try {
                                uriHandler.openUri(taskData.meetingLink)
                            } catch (e: Exception) {
                                // Handle invalid URL gracefully
                            }
                        },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Meeting link",
                            tint = Color(0xFF0F7BE8),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = taskData.meetingLink,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF0F7BE8),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                } else if (taskData.subtitle.isNotEmpty()) {
                    Text(
                        text = taskData.subtitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (taskData.isDone) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }

            Text(
                modifier = Modifier.padding(start = 16.dp, top = 6.dp),
                text = "Due " + taskData.deadline,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textDecoration = if (taskData.isDone) TextDecoration.LineThrough else TextDecoration.None
            )

            if (taskData.taskList.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp).background(
                        color = MaterialTheme.colorScheme.primary.copy(0.15f)
                    ).padding(start = 16.dp, end = 16.dp, top = 12.dp),
                ) {
                    Text(
                        text = "Sub-tasks list",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Column(
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        taskData.taskList.take(2).forEach { taskItem ->
                            TaskItemRow(
                                taskItem = taskItem, maxLines = 2, onToggle = { isChecked ->
                                    onTaskItemToggle(taskItem.id, isChecked)
                                })
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom circular checkbox with animated check mark
 * @param checked Whether the checkbox is currently checked
 * @param onCheckedChange Callback invoked when checkbox state changes
 * @param modifier Modifier to apply to the checkbox
 */
@Composable
fun CircularCheckbox(
    checked: Boolean, onCheckedChange: (Boolean) -> Unit, uncheckColor: Color, modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(24.dp).drawBehind {
            val strokeWidth = Stroke(width = 1.dp.toPx())
            val radius = size.minDimension / 2

            drawCircle(
                color = uncheckColor.copy(alpha = if (checked) 0.5f else 0.8f),
                radius = radius,
                style = if (checked) Stroke(width = strokeWidth.width) else strokeWidth
            )

            if (checked) {
                drawCircle(
                    color = primaryColorVariant, radius = radius - strokeWidth.width / 2
                )
            }
        }.clip(CircleShape).clickable { onCheckedChange(!checked) }, contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = checked, enter = fadeIn(animationSpec = tween(150)) + expandVertically(
                animationSpec = tween(150), expandFrom = Alignment.CenterVertically
            ), exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(
                animationSpec = tween(150), shrinkTowards = Alignment.CenterVertically
            )
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Checked",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

/**
 * Task Status enum representing the completion status of a task
 *
 * @property displayName The user-facing display name for the status
 * @property isCompleted Whether this status represents a completed task
 */
enum class TaskStatus(val displayName: String, val isCompleted: Boolean) {
    /** Task is not yet completed */
    UNDONE("Undone", false),

    /** Task has been completed */
    COMPLETED("Done", true),

    /** Task is undone and past its deadline */
    OVERDUE("Overdue", false)
}

/**
 * Extension function to convert Boolean to TaskStatus
 */
fun Boolean.toTaskStatus() = if (this) TaskStatus.COMPLETED else TaskStatus.UNDONE

/**
 * Extension function to convert TaskStatus to Boolean
 */
fun TaskStatus.toBoolean() = this.isCompleted

@Composable
fun TaskarooStatusBadge(
    status: TaskStatus,
    isOverdue: Boolean,
    onStatusChange: (TaskStatus) -> Unit,
    modifier: Modifier = Modifier,
    fullWidth: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }

    val displayStatus = if (!status.isCompleted && isOverdue) {
        TaskStatus.OVERDUE
    } else {
        status
    }

    val statusColor = when (displayStatus) {
        TaskStatus.COMPLETED -> completedStatusColor
        TaskStatus.OVERDUE -> overdueStatusColor
        TaskStatus.UNDONE -> undoneStatusColor
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(8.dp)
                )
                .clip(RoundedCornerShape(8.dp))
                .border(width = 1.dp, color = MaterialTheme.colorScheme.onBackground, shape = RoundedCornerShape(8.dp))
                .clickable { showDialog = true }
                .padding(horizontal = if (fullWidth) 16.dp else 8.dp, vertical = if (fullWidth) 12.dp else 4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (status.isCompleted) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.RadioButtonUnchecked
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(if (fullWidth) 24.dp else 16.dp)
                )

                Text(
                    text = displayStatus.displayName,
                    fontSize = if (fullWidth) 16.sp else 12.sp,
                    fontWeight = if (fullWidth) FontWeight.Bold else FontWeight.Medium,
                    color = statusColor
                )

                if (fullWidth) {
                    Spacer(Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Change status",
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        TaskStatusDialog(
            showDialog = showDialog,
            currentStatus = status,
            onStatusSelected = { newStatus ->
                onStatusChange(newStatus)
            },
            onDismiss = { showDialog = false }
        )
    }
}


@Composable
fun TaskStatusDialog(
    showDialog: Boolean, currentStatus: TaskStatus, onStatusSelected: (TaskStatus) -> Unit, onDismiss: () -> Unit
) {
    if (showDialog) {
        var selectedStatus by remember { mutableStateOf(currentStatus) }

        AlertDialog(
            onDismissRequest = onDismiss, title = {
                Text(
                    text = "Change Status",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }, text = {
                Column(
                    modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Select task status:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )

                    StatusOptionCard(
                        icon = Icons.Default.RadioButtonUnchecked,
                        statusText = "Undone",
                        statusColor = undoneStatusColor,
                        statusBackground = undoneStatusBackground,
                        isSelected = selectedStatus == TaskStatus.UNDONE,
                        onClick = { selectedStatus = TaskStatus.UNDONE })

                    StatusOptionCard(
                        icon = Icons.Default.CheckCircle,
                        statusText = "Done",
                        statusColor = completedStatusColor,
                        statusBackground = completedStatusBackground,
                        isSelected = selectedStatus == TaskStatus.COMPLETED,
                        onClick = { selectedStatus = TaskStatus.COMPLETED })

                    StatusOptionCard(
                        icon = Icons.Default.RadioButtonUnchecked,
                        statusText = "Overdue",
                        statusColor = overdueStatusColor,
                        statusBackground = overdueStatusBackground,
                        isSelected = selectedStatus == TaskStatus.OVERDUE,
                        onClick = { selectedStatus = TaskStatus.OVERDUE })
                }
            }, confirmButton = {
                TextButton(
                    onClick = {
                        onStatusSelected(selectedStatus)
                        onDismiss()
                    }) {
                    Text("OK", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }, dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                }
            }, containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp)
        )
    }
}


@Composable
private fun StatusOptionCard(
    icon: ImageVector,
    statusText: String,
    statusColor: Color,
    statusBackground: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) statusBackground else MaterialTheme.colorScheme.background
        ),
        border = BorderStroke(
            width = 1.dp, color = statusColor
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon, contentDescription = null, tint = statusColor, modifier = Modifier.size(24.dp)
            )
            Text(
                text = statusText,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = statusColor,
                modifier = Modifier.weight(1f)
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


/**
 * Individual task item row with checkbox and text
 *
 * @param taskItem The task item data to display
 * @param isConciseItem Whether to use smaller sizing for compact layout
 * @param onToggle Callback invoked when the checkbox is toggled
 */
@Composable
fun TaskItemRow(
    modifier: Modifier = Modifier,
    taskItem: TaskItem,
    maxLines: Int,
    isConciseItem: Boolean = false,
    onToggle: (Boolean) -> Unit
) {
    var isChecked by remember { mutableStateOf(taskItem.isCompleted) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CircularCheckbox(
            checked = isChecked, uncheckColor = MaterialTheme.colorScheme.onBackground, onCheckedChange = { checked ->
                isChecked = checked
                onToggle(checked)
            }, modifier = Modifier.size(if (isConciseItem) 15.dp else 18.dp)
        )

        Text(
            text = taskItem.text,
            fontSize = if (isConciseItem) 13.sp else 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (isChecked) 0.5f else 0.8f),
            textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
fun TaskSummaryCards(
    totalTasks: Int,
    completedTasks: Int,
    activeTasks: Int,
    overdueTasks: Int,
    selectedFilter: String = "Active",
    onFilterSelected: (String) -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SummaryCard(
                title = "All",
                count = totalTasks,
                icon = Res.drawable.all_icon,
                backgroundColor = Color(0xFF609CFC),
                isSelected = selectedFilter == "All",
                onClick = { onFilterSelected("All") },
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Completed",
                count = completedTasks,
                icon = Res.drawable.completed_icon,
                backgroundColor = Color(0xFFE8C254),
                isSelected = selectedFilter == "Completed",
                onClick = { onFilterSelected("Completed") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "Active",
                count = activeTasks,
                icon = Res.drawable.inprogress_icon,
                backgroundColor = Color(0xFF41C4AA),
                isSelected = selectedFilter == "Active",
                onClick = { onFilterSelected("Active") },
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Overdue",
                count = overdueTasks,
                icon = Res.drawable.overdue_icon,
                backgroundColor = Color(0xFFDE5151),
                isSelected = selectedFilter == "Overdue",
                onClick = { onFilterSelected("Overdue") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    count: Int,
    icon: DrawableResource,
    backgroundColor: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) backgroundColor.copy(alpha = 0.9f)
            else backgroundColor
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) backgroundColor
            else backgroundColor.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = title,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black
                )
                Text(
                    text = "$count Tasks",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    }
}