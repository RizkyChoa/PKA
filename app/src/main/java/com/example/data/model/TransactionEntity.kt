package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trxNumber: String,
    val date: String, // YYYY-MM-DD
    val type: String, // "MONEY_IN", "MONEY_OUT", "TRANSFER"
    val amount: Double,
    val description: String,
    val paymentMethod: String, // "TRANSFER", "CASH", "GIRO"
    val sourceAccountId: Long,
    val sourceAccountName: String,
    val destinationAccountId: Long? = null,
    val destinationAccountName: String? = null,
    val categoryId: Long,
    val categoryName: String,
    val classification: String, // "PROJECT", "OPERATIONAL", "DEBT", "RECEIVABLE", "EQUITY", "ASSET", "INTERNAL_TRANSFER"
    val projectId: Long? = null,
    val projectName: String? = null,
    val costGroup: String = "", // "MATERIAL", "LABOR", "MOBILIZATION", "FUEL", "EQUIPMENT", "MAINTENANCE", "OTHER"
    val receivableId: Long? = null,
    val payableId: Long? = null,
    val status: String = "VALID", // "VALID", "VOID"
    val voidReason: String = "",
    val createdBy: String = "Admin",
    val createdAt: Long = System.currentTimeMillis()
)
