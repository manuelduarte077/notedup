package dev.wondertech.notedup.common

import kotlinx.datetime.LocalDate

data class CalendarUiModel(
    val selectedDate: Date,
    val visibleDates: List<Date>
) {
    val startDate = visibleDates.first()
    val endDate = visibleDates.last()

    data class Date(
        val date: LocalDate,
        val isSelected: Boolean,
        val isToday: Boolean
    ) {
        val day: String = date.dayOfWeek.name.take(3)
    }
}