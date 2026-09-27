package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receivables")
data class ReceivableEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long? = null,
    val projectName: String = "",
    val clientName: String,
    val invoiceNumber: String,
    val description: String,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val dueDate: String,
    val status: String = "UNPAID", // "UNPAID", "PARTIAL", "PAID"
    val createdAt: Long = System.currentTimeMillis()
) {
    val remainingAmount: Double get() = (totalAmount - paidAmount).coerceAtLeast(0.0)
}
