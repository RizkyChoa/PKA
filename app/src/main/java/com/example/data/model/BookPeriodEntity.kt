package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book_periods")
data class BookPeriodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val periodCode: String, // e.g. "PERIOD-2026-01-04"
    val name: String, // e.g. "Data 2026 Januari - April"
    val year: Int, // e.g. 2026
    val startDate: String, // "YYYY-MM-DD" e.g. "2026-01-01"
    val endDate: String, // "YYYY-MM-DD" e.g. "2026-04-30"
    val status: String = "ACTIVE", // "ACTIVE", "ARCHIVED", "CLOSED"
    val createdAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val notes: String = "",
    // Financial snapshots at closing for carry forward & historical reference
    val openingCashBalance: Double = 0.0,
    val closingCashBalance: Double = 0.0,
    val openingPayableBalance: Double = 0.0,
    val closingPayableBalance: Double = 0.0,
    val openingReceivableBalance: Double = 0.0,
    val closingReceivableBalance: Double = 0.0,
    val activeProjectCount: Int = 0
) {
    val isArchived: Boolean get() = status == "ARCHIVED" || status == "CLOSED"
    val isActive: Boolean get() = status == "ACTIVE"
}
