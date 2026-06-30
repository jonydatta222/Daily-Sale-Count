package com.example.data

import kotlinx.coroutines.flow.Flow

class SaleRepository(private val saleDao: SaleDao) {
    fun getSalesForDate(date: String): Flow<List<SaleItem>> = saleDao.getSalesForDateFlow(date)
    
    fun getAllDueSales(): Flow<List<SaleItem>> = saleDao.getAllDueSalesFlow()
    
    suspend fun getAllSales(): List<SaleItem> = saleDao.getAllSales()
    
    suspend fun insert(sale: SaleItem) = saleDao.insertSale(sale)
    
    suspend fun insertSales(sales: List<SaleItem>) = saleDao.insertSales(sales)
    
    suspend fun delete(id: Long) = saleDao.deleteSale(id)
    
    suspend fun deleteForDate(date: String) = saleDao.deleteSalesForDate(date)
}
