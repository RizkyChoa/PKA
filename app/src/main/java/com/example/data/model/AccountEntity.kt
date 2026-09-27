package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "CASH", "BANK", "EWALLET"
    val accountNumber: String = "",
    val initialBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val isActive: Boolean = true
)
