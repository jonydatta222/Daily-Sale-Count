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
            onError("দয়া করে গ্রাহকের নাম লিখুন।")
            return
        }

        if (type != "due" && name.isEmpty()) {
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
                name = if (type == "due" && name.isEmpty()) "বাকি বিক্রি" else name,
                customerName = if (type == "due") customer else "",
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
            _customerName.value = ""
            _itemPrice.value = ""
            _saleType.value = "cash"
            
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
        viewModelScope.launch {
            repository.delete(dueItem.id)
            
            val now = Date()
            val autoDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now)
            val timeString = SimpleDateFormat("hh:mm a", Locale.US).format(now)
            
            val bakiName = dueItem.customerName.ifEmpty { dueItem.name }
            val collectionItem = SaleItem(
                name = "বাকির টাকা জমা (গ্রাহক: $bakiName)",
                customerName = "",
                price = dueItem.price,
                type = "cash",
                date = autoDate,
                time = timeString
            )
            repository.insert(collectionItem)
            
            _selectedDate.value = autoDate
            uploadToFirebase()
        }
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
                "ফায়ারবেস ডাটাবেজ সংযোগ ব্যর্থ: $lastErrorMsg",
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
                                        "ক্লাউড ব্যাকআপ সফল হয়েছে!",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val err = task.exception?.localizedMessage ?: "অনুমতি অস্বীকৃত (Rules check)"
                                    android.widget.Toast.makeText(
                                        getApplication(),
                                        "সিঙ্ক ব্যর্থ: $err",
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
                            "অনুমতি নেই বা সংযোগ ত্রুটি: ${error.message}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                })
            } catch (e: Exception) {
                _isSyncing.value = false
                android.widget.Toast.makeText(
                    getApplication(),
                    "ত্রুটি: ${e.localizedMessage}",
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
