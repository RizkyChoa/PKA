package com.example.util

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object CurrencyFormatter {
    private val idSymbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    private val dotFormat = DecimalFormat("#,###", idSymbols)

    /**
     * Formats raw text input containing digits into dot-separated thousands:
     * e.g. "20000" -> "20.000", "5000000" -> "5.000.000"
     */
    fun formatInput(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        if (digits.isEmpty()) return ""
        val num = digits.toLongOrNull() ?: return digits
        return dotFormat.format(num)
    }

    /**
     * Extracts pure Double from dot-formatted string:
     * e.g. "5.000.000" -> 5000000.0
     */
    fun parseInput(formatted: String): Double {
        val digits = formatted.filter { it.isDigit() }
        return digits.toDoubleOrNull() ?: 0.0
    }

    /**
     * Auto-formats date digits input without symbols:
     * e.g. "20260930" -> "2026-09-30", or "30092026" -> "2026-09-30"
     */
    fun formatDateInput(raw: String): String {
        val clean = raw.trim()
        val digitsOnly = clean.filter { it.isDigit() }
        if (digitsOnly.length == 8) {
            // Check if YYYYMMDD
            val yearFirst = digitsOnly.substring(0, 4).toIntOrNull() ?: 0
            if (yearFirst in 2000..2099) {
                val y = digitsOnly.substring(0, 4)
                val m = digitsOnly.substring(4, 6)
                val d = digitsOnly.substring(6, 8)
                return "$y-$m-$d"
            }
            // Check if DDMMYYYY
            val yearLast = digitsOnly.substring(4, 8).toIntOrNull() ?: 0
            if (yearLast in 2000..2099) {
                val d = digitsOnly.substring(0, 2)
                val m = digitsOnly.substring(2, 4)
                val y = digitsOnly.substring(4, 8)
                return "$y-$m-$d"
            }
        }
        return clean
    }

    fun todayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun yesterdayString(): String {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialDate: String = "",
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialMillis = remember(initialDate) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            sdf.parse(initialDate)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val selected = datePickerState.selectedDateMillis
                if (selected != null) {
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = selected
                    }
                    val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                    onDateSelected(formatted)
                }
                onDismiss()
            }) {
                Text("Pilih Tanggal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
