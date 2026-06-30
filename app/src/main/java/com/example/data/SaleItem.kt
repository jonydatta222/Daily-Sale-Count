package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    indices = [Index(value = ["date"])]
)
data class SaleItem(
    @PrimaryKey val id: Long = java.util.UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
    val name: String = "",
    val customerName: String = "",
    val price: Double = 0.0,
    val type: String = "cash", // "cash" or "due"
    val date: String = "", // "YYYY-MM-DD"
    val time: String = "", // "hh:mm a"
    val timestamp: Long = System.currentTimeMillis()
)
