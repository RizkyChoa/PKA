package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "INCOME", "EXPENSE", "TRANSFER"
    val classification: String, // "PROJECT_REVENUE", "PROJECT_COST", "OPERATIONAL_EXPENSE", "DEBT", "RECEIVABLE", "EQUITY", "ASSET", "TRANSFER"
    val costGroup: String = "" // For project costs: "MATERIAL", "LABOR", "MOBILIZATION", "FUEL", "EQUIPMENT", "MAINTENANCE", "OTHER"
)
