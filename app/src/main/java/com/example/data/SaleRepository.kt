package com.example.data

import kotlinx.coroutines.flow.Flow

class SaleRepository(private val saleDao: SaleDao) {
    fun getSalesForDate(date: String): Flow<List<SaleItem>> = saleDao.getSalesForDateFlow(date)
    
    suspend fun insert(sale: SaleItem) = saleDao.insertSale(sale)
    
    suspend fun delete(id: Long) = saleDao.deleteSale(id)
    
    suspend fun deleteForDate(date: String) = saleDao.deleteSalesForDate(date)
}
