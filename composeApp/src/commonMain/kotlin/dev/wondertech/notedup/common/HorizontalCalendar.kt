package dev.wondertech.notedup.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import notedup.composeapp.generated.resources.Res
import notedup.composeapp.generated.resources.left_arrow
import notedup.composeapp.generated.resources.right_arrow
import org.jetbrains.compose.resources.painterResource

@Composable
fun HorizontalCalendar(
    modifier: Modifier = Modifier,
    onDateClickListener: (LocalDate) -> Unit
) {
    val dataSource = remember { CalendarDataSource() }
    var data by remember {
        mutableStateOf(dataSource.getData(lastSelectedDate = dataSource.today))
    }

    Column(
        modifier = modifier.fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(12.dp))
    ) {

        Header(
            data = data,
            onPrevClickListener = { startDate ->
                data = dataSource.getData(
                    startDate = startDate.minus(7, DateTimeUnit.DAY),
                    lastSelectedDate = data.selectedDate.date
                )
            },
            onNextClickListener = { endDate ->
                data = dataSource.getData(
                    startDate = endDate.plus(1, DateTimeUnit.DAY),
                    lastSelectedDate = data.selectedDate.date
                )
            }
        )

        Content(
            data = data,
        ) { date ->
            data = data.copy(
                selectedDate = date,
                visibleDates = data.visibleDates.map {
                    it.copy(isSelected = it.date == date.date)
                }
            )
            onDateClickListener(date.date)
        }
    }
}


@Composable
fun Header(
    data: CalendarUiModel,
    onPrevClickListener: (LocalDate) -> Unit,
    onNextClickListener: (LocalDate) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        Text(
            text = "${
                data.startDate.date.month.name.lowercase().replaceFirstChar { it.uppercase() }
            }, ${data.startDate.date.year}",
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontSize = 19.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Icon(
            painter = painterResource(Res.drawable.left_arrow),
            contentDescription = null,
            modifier = Modifier.size(20.dp).clip(CircleShape).clickable {
                onPrevClickListener(data.startDate.date)
            }
        )

        Spacer(Modifier.width(12.dp))

        Icon(
            painter = painterResource(Res.drawable.right_arrow),
            contentDescription = null,
            modifier = Modifier.size(20.dp).clip(CircleShape).clickable {
                onNextClickListener(data.endDate.date)
            }
        )
    }
}


@Composable
fun Content(
    data: CalendarUiModel,
    onDateClickListener: (CalendarUiModel.Date) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        data.visibleDates.forEach {
            ContentItem(
                date = it,
                onClickListener = onDateClickListener
            )
        }
    }
}


@Composable
fun RowScope.ContentItem(
    date: CalendarUiModel.Date,
    onClickListener: (CalendarUiModel.Date) -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 6.dp)
            .background(
                if (date.isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                CircleShape
            )
            .clip(CircleShape)
            .clickable { onClickListener(date) }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.day,
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (date.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(
                    alpha = 0.75f
                )
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = date.date.dayOfMonth.toString(),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}