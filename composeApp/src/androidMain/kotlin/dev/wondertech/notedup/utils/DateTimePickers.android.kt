package dev.wondertech.notedup.utils

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import dev.wondertech.notedup.R

@Composable
actual fun NativeDatePicker(
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val themedContext = ContextThemeWrapper(context, R.style.AppDatePickerTheme)

        val dialog = DatePickerDialog(
            themedContext,
            { _, year, month, dayOfMonth ->
                onDateSelected(year, month + 1, dayOfMonth)
            },
            initialYear,
            initialMonth - 1,
            initialDay
        )

        dialog.setOnCancelListener {
            onDismiss()
        }

        dialog.setOnDismissListener {
            onDismiss()
        }

        dialog.setOnShowListener {
            dialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
                ?.setTextColor(hexToColor("#4F634F"))
            dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                ?.setTextColor(hexToColor("#4F634F"))
        }

        dialog.show()
    }
}

@Composable
actual fun NativeTimePicker(
    initialHour: Int,
    initialMinute: Int,
    initialAmPm: String,
    onTimeSelected: (hour: Int, minute: Int, amPm: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val themedContext = ContextThemeWrapper(context, R.style.AppTimePickerTheme)
        val hour24 = if (initialAmPm == "AM") {
            if (initialHour == 12) 0 else initialHour
        } else {
            if (initialHour == 12) 12 else initialHour + 12
        }

        val dialog = TimePickerDialog(
            themedContext,
            { _, hourOfDay, minute ->
                val (hour12, amPm) = convertTo12Hour(hourOfDay)
                onTimeSelected(hour12, minute, amPm)
            },
            hour24,
            initialMinute,
            false
        )

        dialog.setOnCancelListener {
            onDismiss()
        }

        dialog.setOnDismissListener {
            onDismiss()
        }

        dialog.setOnShowListener {
            dialog.getButton(TimePickerDialog.BUTTON_POSITIVE)
                ?.setTextColor(hexToColor("#4F634F"))
            dialog.getButton(TimePickerDialog.BUTTON_NEGATIVE)
                ?.setTextColor(hexToColor("#4F634F"))
        }

        dialog.show()
    }
}

/**
 * Convert 24-hour format to 12-hour format with AM/PM
 */
private fun convertTo12Hour(hour24: Int): Pair<Int, String> {
    return when {
        hour24 == 0 -> Pair(12, "AM")
        hour24 < 12 -> Pair(hour24, "AM")
        hour24 == 12 -> Pair(12, "PM")
        else -> Pair(hour24 - 12, "PM")
    }
}

/**
 * Convert hex color string to Android Color integer
 */
private fun hexToColor(hex: String): Int {
    return android.graphics.Color.parseColor(hex)
}
