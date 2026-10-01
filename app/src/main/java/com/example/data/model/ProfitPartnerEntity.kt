package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profit_partners")
data class ProfitPartnerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sharePercentage: Double, // e.g. 60.0 for 60%
    val phone: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
