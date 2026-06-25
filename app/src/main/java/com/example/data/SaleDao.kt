package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSalesFlow(): Flow<List<SaleItem>>

    @Query("SELECT * FROM sales WHERE date = :date ORDER BY timestamp DESC")
    fun getSalesForDateFlow(date: String): Flow<List<SaleItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleItem)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSale(id: Long)

    @Query("DELETE FROM sales WHERE date = :date")
    suspend fun deleteSalesForDate(date: String)
}
