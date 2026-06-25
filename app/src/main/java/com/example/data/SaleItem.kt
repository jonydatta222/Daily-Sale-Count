package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val price: Double,
    val type: String, // "cash" or "due"
    val date: String, // "YYYY-MM-DD"
    val time: String, // "hh:mm a"
    val timestamp: Long = System.currentTimeMillis()
)
