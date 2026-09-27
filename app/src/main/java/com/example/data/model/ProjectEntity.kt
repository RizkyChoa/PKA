package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectCode: String,
    val name: String,
    val clientName: String,
    val location: String,
    val startDate: String,
    val targetDate: String,
    val contractAmount: Double,
    val status: String, // "PLANNING", "ACTIVE", "COMPLETED", "CANCELLED"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
