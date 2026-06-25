package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SaleItem
import com.example.data.SaleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SaleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SaleRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SaleRepository(database.saleDao())
    }

    private val _selectedDate = MutableStateFlow(getCurrentDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val salesForSelectedDate: StateFlow<List<SaleItem>> = _selectedDate
        .flatMapLatest { date ->
            repository.getSalesForDate(date)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Form inputs
    private val _itemName = MutableStateFlow("")
    val itemName = _itemName.asStateFlow()

    private val _itemPrice = MutableStateFlow("")
    val itemPrice = _itemPrice.asStateFlow()

    private val _saleType = MutableStateFlow("cash") // "cash" or "due"
    val saleType = _saleType.asStateFlow()

    fun onDateSelected(date: String) {
        _selectedDate.value = date
    }

    fun onItemNameChanged(name: String) {
        _itemName.value = name
    }

    fun onItemPriceChanged(price: String) {
        _itemPrice.value = price
    }

    fun onSaleTypeChanged(type: String) {
        _saleType.value = type
    }

    fun addSale(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val name = _itemName.value.trim()
        val priceStr = _itemPrice.value.trim()
        val type = _saleType.value

        if (name.isEmpty()) {
            onError("দয়া করে সঠিক বিবরণ লিখুন।")
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            onError("দয়া করে সঠিক দাম লিখুন।")
            return
        }

        viewModelScope.launch {
            val now = Date()
            val autoDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now)
            val timeString = SimpleDateFormat("hh:mm a", Locale.US).format(now)

            val saleItem = SaleItem(
                name = name,
                price = price,
                type = type,
                date = autoDate,
                time = timeString
            )

            repository.insert(saleItem)
            
            // Set view date back to today (as in HTML logic)
            _selectedDate.value = autoDate

            // Clear form inputs
            _itemName.value = ""
            _itemPrice.value = ""
            _saleType.value = "cash"
            
            onSuccess()
        }
    }

    fun deleteSale(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }

    fun clearSalesForSelectedDate() {
        viewModelScope.launch {
            repository.deleteForDate(_selectedDate.value)
        }
    }

    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}
