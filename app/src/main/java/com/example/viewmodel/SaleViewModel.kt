package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SaleItem
import com.example.data.SaleRepository
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SaleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SaleRepository
    private val prefs = application.getSharedPreferences("sales_app_prefs", android.content.Context.MODE_PRIVATE)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SaleRepository(database.saleDao())
    }

    private val _selectedDate = MutableStateFlow(getCurrentDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _googleAccountEmail = MutableStateFlow(prefs.getString("google_account_email", null))
    val googleAccountEmail: StateFlow<String?> = _googleAccountEmail.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncedTime = MutableStateFlow(prefs.getString("last_synced_time", null))
    val lastSyncedTime: StateFlow<String?> = _lastSyncedTime.asStateFlow()

    // Language configuration (default is Bangla = true)
    private val _isBangla = MutableStateFlow(prefs.getBoolean("is_bangla", true))
    val isBangla: StateFlow<Boolean> = _isBangla.asStateFlow()

    fun toggleLanguage() {
        val newVal = !_isBangla.value
        _isBangla.value = newVal
        prefs.edit().putBoolean("is_bangla", newVal).apply()
    }

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

    val allDueSales: StateFlow<List<SaleItem>> = repository.getAllDueSales()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalDueSales: StateFlow<Double> = repository.getAllDueSales()
        .map { list -> list.sumOf { it.price } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    // Flow of all sales for the "Old Ledger" feature
    val allSalesList: StateFlow<List<SaleItem>> = repository.getAllSalesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Form inputs
    private val _itemName = MutableStateFlow("")
    val itemName = _itemName.asStateFlow()

    private val _customerName = MutableStateFlow("")
    val customerName = _customerName.asStateFlow()

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

    fun onCustomerNameChanged(name: String) {
        _customerName.value = name
    }

    fun onItemPriceChanged(price: String) {
        _itemPrice.value = price
    }

    fun onSaleTypeChanged(type: String) {
        _saleType.value = type
    }

    fun addSale(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val name = _itemName.value.trim()
        val customer = _customerName.value.trim()
        val priceStr = _itemPrice.value.trim()
        val type = _saleType.value

        if (type == "due" && customer.isEmpty()) {
            onError(if (_isBangla.value) "দয়া করে গ্রাহকের নাম লিখুন।" else "Please enter customer name.")
            return
        }

        if (type != "due" && name.isEmpty()) {
            onError(if (_isBangla.value) "দয়া করে সঠিক বিবরণ লিখুন।" else "Please enter correct description.")
            return
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            onError(if (_isBangla.value) "দয়া করে সঠিক দাম লিখুন।" else "Please enter correct price.")
            return
        }

        viewModelScope.launch {
            val now = Date()
            val autoDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now)
            val timeString = SimpleDateFormat("hh:mm a", Locale.US).format(now)

            val saleItem = SaleItem(
                name = if (type == "due" && name.isEmpty()) (if (_isBangla.value) "বাকি বিক্রি" else "Due Sale") else name,
                customerName = if (type == "due") customer else "",
                price = price,
                type = type,
                date = autoDate,
                time = timeString
            )

            repository.insert(saleItem)
            
            // Set view date back to today
            _selectedDate.value = autoDate

            // Clear form inputs
            _itemName.value = ""
            _customerName.value = ""
            _itemPrice.value = ""
            _saleType.value = "cash"
            
            onSuccess()
            uploadToFirebase()
        }
    }

    fun editSale(id: Long, name: String, customerName: String, price: Double, type: String, date: String, time: String) {
        viewModelScope.launch {
            val existing = repository.getAllSales().find { it.id == id } ?: return@launch
            val updated = existing.copy(
                name = name,
                customerName = if (type == "due") customerName else "",
                price = price,
                type = type,
                date = date,
                time = time
            )
            repository.insert(updated)
            uploadToFirebase()
        }
    }

    fun addExpense(description: String, amount: Double, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (description.trim().isEmpty()) {
            onError(if (_isBangla.value) "দয়া করে খরচের বিবরণ লিখুন।" else "Please enter expense description.")
            return
        }
        if (amount <= 0) {
            onError(if (_isBangla.value) "দয়া করে সঠিক পরিমাণ লিখুন।" else "Please enter correct amount.")
            return
        }
        viewModelScope.launch {
            val now = Date()
            val autoDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now)
            val timeString = SimpleDateFormat("hh:mm a", Locale.US).format(now)

            val expenseItem = SaleItem(
                name = description.trim(),
                customerName = "",
                price = amount,
                type = "expense",
                date = autoDate,
                time = timeString
            )
            repository.insert(expenseItem)
            _selectedDate.value = autoDate
            onSuccess()
            uploadToFirebase()
        }
    }

    fun depositDue(dueItem: SaleItem, depositAmount: Double, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (depositAmount <= 0) return@launch

            val remaining = dueItem.price - depositAmount
            if (remaining <= 0) {
                // Fully paid
                repository.delete(dueItem.id)
            } else {
                // Partially paid
                val updatedDue = dueItem.copy(price = remaining)
                repository.insert(updatedDue)
            }

            // Insert cash deposit item for today
            val now = Date()
            val autoDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now)
            val timeString = SimpleDateFormat("hh:mm a", Locale.US).format(now)

            val bakiName = dueItem.customerName.ifEmpty { dueItem.name }
            val depositTitle = if (_isBangla.value) {
                "বাকির টাকা জমা (গ্রাহক: $bakiName)"
            } else {
                "Due Payment Received (Customer: $bakiName)"
            }

            val depositItem = SaleItem(
                name = depositTitle,
                customerName = "",
                price = depositAmount,
                type = "cash",
                date = autoDate,
                time = timeString
            )
            repository.insert(depositItem)

            _selectedDate.value = autoDate
            onSuccess()
            uploadToFirebase()
        }
    }

    fun deleteSale(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
            uploadToFirebase()
        }
    }

    fun collectDueSale(dueItem: SaleItem) {
        // Fallback or full collection
        depositDue(dueItem, dueItem.price) {}
    }

    fun clearSalesForSelectedDate() {
        viewModelScope.launch {
            repository.deleteForDate(_selectedDate.value)
            uploadToFirebase()
        }
    }

    fun signInWithGoogle(email: String) {
        viewModelScope.launch {
            _googleAccountEmail.value = email
            prefs.edit().putString("google_account_email", email).apply()
            syncNow()
        }
    }

    fun signOutGoogle() {
        viewModelScope.launch {
            _googleAccountEmail.value = null
            _lastSyncedTime.value = null
            prefs.edit().remove("google_account_email").remove("last_synced_time").apply()
        }
    }

    fun syncNow() {
        val email = _googleAccountEmail.value ?: return
        _isSyncing.value = true
        val sanitizedEmail = email.replace(".", "_").replace("@", "_")
        
        var dbRef: com.google.firebase.database.DatabaseReference? = null
        val urls = listOf(
            null,
            "https://daily-sale-count-default-rtdb.asia-southeast1.firebasedatabase.app",
            "https://daily-sale-count-default-rtdb.firebaseio.com",
            "https://daily-sale-count.firebaseio.com",
            "https://daily-sale-count.asia-southeast1.firebasedatabase.app",
            "https://daily-sale-count.europe-west1.firebasedatabase.app",
            "https://daily-sale-count-default-rtdb.europe-west1.firebasedatabase.app"
        )
        
        var lastErrorMsg: String? = null
        for (url in urls) {
            try {
                val db = if (url == null) {
                    FirebaseDatabase.getInstance()
                } else {
                    FirebaseDatabase.getInstance(url)
                }
                dbRef = db.reference.child("users").child(sanitizedEmail).child("sales")
                break
            } catch (e: Exception) {
                lastErrorMsg = e.localizedMessage
            }
        }
        
        if (dbRef == null) {
            _isSyncing.value = false
            android.widget.Toast.makeText(
                getApplication(),
                if (_isBangla.value) "ফায়ারবেস ডাটাবেজ সংযোগ ব্যর্থ: $lastErrorMsg" else "Firebase connection failed: $lastErrorMsg",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return
        }
        
        viewModelScope.launch {
            try {
                val localSales = repository.getAllSales()
                dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        viewModelScope.launch {
                            val remoteSales = mutableListOf<SaleItem>()
                            for (child in snapshot.children) {
                                val sale = child.getValue(SaleItem::class.java)
                                if (sale != null) {
                                    remoteSales.add(sale)
                                }
                            }
                            
                            if (remoteSales.isNotEmpty()) {
                                repository.insertSales(remoteSales)
                            }
                            
                            val mergedSales = repository.getAllSales()
                            dbRef.setValue(mergedSales).addOnCompleteListener { task ->
                                _isSyncing.value = false
                                if (task.isSuccessful) {
                                    val nowStr = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
                                    _lastSyncedTime.value = nowStr
                                    prefs.edit().putString("last_synced_time", nowStr).apply()
                                    android.widget.Toast.makeText(
                                        getApplication(),
                                        if (_isBangla.value) "ক্লাউড ব্যাকআপ সফল হয়েছে!" else "Cloud backup successful!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val err = task.exception?.localizedMessage ?: "অনুমতি অস্বীকৃত (Rules check)"
                                    android.widget.Toast.makeText(
                                        getApplication(),
                                        if (_isBangla.value) "সিঙ্ক ব্যর্থ: $err" else "Sync failed: $err",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        _isSyncing.value = false
                        android.widget.Toast.makeText(
                            getApplication(),
                            if (_isBangla.value) "অনুমতি নেই বা সংযোগ ত্রুটি: ${error.message}" else "Permission denied or connection error: ${error.message}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                })
            } catch (e: Exception) {
                _isSyncing.value = false
                android.widget.Toast.makeText(
                    getApplication(),
                    "Error: ${e.localizedMessage}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun uploadToFirebase() {
        val email = _googleAccountEmail.value ?: return
        val sanitizedEmail = email.replace(".", "_").replace("@", "_")
        
        var dbRef: com.google.firebase.database.DatabaseReference? = null
        val urls = listOf(
            null,
            "https://daily-sale-count-default-rtdb.asia-southeast1.firebasedatabase.app",
            "https://daily-sale-count-default-rtdb.firebaseio.com",
            "https://daily-sale-count.firebaseio.com",
            "https://daily-sale-count.asia-southeast1.firebasedatabase.app",
            "https://daily-sale-count.europe-west1.firebasedatabase.app",
            "https://daily-sale-count-default-rtdb.europe-west1.firebasedatabase.app"
        )
        
        for (url in urls) {
            try {
                val db = if (url == null) {
                    FirebaseDatabase.getInstance()
                } else {
                    FirebaseDatabase.getInstance(url)
                }
                dbRef = db.reference.child("users").child(sanitizedEmail).child("sales")
                break
            } catch (e: Exception) {
                // ignore
            }
        }
        
        if (dbRef == null) return
        
        viewModelScope.launch {
            try {
                val localSales = repository.getAllSales()
                dbRef.setValue(localSales).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val nowStr = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
                        _lastSyncedTime.value = nowStr
                        prefs.edit().putString("last_synced_time", nowStr).apply()
                    }
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}
