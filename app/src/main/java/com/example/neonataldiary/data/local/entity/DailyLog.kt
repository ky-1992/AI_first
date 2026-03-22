package com.example.neonataldiary.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "date")
    val date: String,
    
    @ColumnInfo(name = "feeding")
    val feeding: String? = null,
    
    @ColumnInfo(name = "mood")
    val mood: String? = null,
    
    @ColumnInfo(name = "weight_kg")
    val weightKg: Float? = null,
    
    @ColumnInfo(name = "note")
    val note: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
