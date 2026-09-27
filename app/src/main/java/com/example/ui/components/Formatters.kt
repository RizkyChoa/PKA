package com.example.ui.components

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    private val idLocale = Locale("in", "ID")

    fun rupiah(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(idLocale)
        format.maximumFractionDigits = 0
        val formatted = format.format(amount)
        // Clean standard Rp format
        return formatted.replace("Rp", "Rp ").replace(",00", "")
    }

    fun simpleDate(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", idLocale)
            val parsed = inputFormat.parse(dateStr)
            if (parsed != null) outputFormat.format(parsed) else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    fun todayDateString(): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.format(Date())
    }

    fun generateTrxCode(index: Int): String {
        val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val datePart = format.format(Date())
        val counter = String.format(Locale.US, "%04d", index % 10000)
        return "TRX-$datePart-$counter"
    }
}
