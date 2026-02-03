package dev.wondertech.notedup.common

import dev.wondertech.notedup.utils.todayDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus


class CalendarDataSource {

    val today: LocalDate
        get() = todayDate()

    fun getData(
        startDate: LocalDate = today,
        lastSelectedDate: LocalDate
    ): CalendarUiModel {

        val visibleDates = (0..6).map {
            startDate.plus(it, DateTimeUnit.DAY)
        }

        return CalendarUiModel(
            selectedDate = toItem(lastSelectedDate, true),
            visibleDates = visibleDates.map {
                toItem(it, it == lastSelectedDate)
            }
        )
    }

    private fun toItem(date: LocalDate, isSelected: Boolean) =
        CalendarUiModel.Date(
            date = date,
            isSelected = isSelected,
            isToday = date == today
        )
}
