package dev.wondertech.notedup.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.wondertech.notedup.common.TaskCardConcise
import dev.wondertech.notedup.modal.TaskData
import dev.wondertech.notedup.primaryLiteColorVariant

@Composable
fun HourColumnItem(
    hour: String,
    items: List<TaskData>,
    modifier: Modifier = Modifier,
    onTaskItemToggle: (String, Boolean) -> Unit = { _, _ -> },
    onTaskClick: (TaskData) -> Unit = {},
    onTaskLongClick: (TaskData) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(top = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                text = hour,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.fillMaxSize().weight(1f)) {
                Spacer(
                    modifier = Modifier.padding(top = 14.dp).height(1.dp).fillMaxWidth(1f)
                        .background(primaryLiteColorVariant)
                )
                items.forEach { taskData ->
                    TaskCardConcise(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        taskData = taskData,
                        onTaskItemToggle = onTaskItemToggle,
                        onClick = { onTaskClick(taskData) },
                        onLongClick = { onTaskLongClick(taskData) })
                }
            }
        }
    }
}
