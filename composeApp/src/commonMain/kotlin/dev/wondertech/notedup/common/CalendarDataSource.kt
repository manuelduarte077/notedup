package dev.wondertech.notedup.common

import dev.wondertech.notedup.utils.todayDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus


class CalendarDataSource {

    val today: LocalDate
        get() = todayDate()

    /**
     * Generates calendar data for a week starting from the specified date
     *
     * @param startDate The first date to display in the calendar
     * @param lastSelectedDate The date that should be marked as selected
     * @return CalendarUiModel containing the week's dates and selection state
     */
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

    /**
     * Converts a LocalDate to a CalendarUiModel.Date
     *
     * @param date The date to convert
     * @param isSelected Whether this date should be marked as selected
     * @return CalendarUiModel.Date with appropriate flags set
     */
    private fun toItem(date: LocalDate, isSelected: Boolean) =
        CalendarUiModel.Date(
            date = date,
            isSelected = isSelected,
            isToday = date == today
        )
}
