package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.SaleItem
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.SaleViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    DailySalesScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Global helper to convert English digits to Bangla
fun convertDigits(input: String, isBangla: Boolean): String {
    if (!isBangla) return input
    val english = "0123456789"
    val bangla = "০১২৩৪৫৬৭৮৯"
    return input.map { char ->
        val index = english.indexOf(char)
        if (index != -1) bangla[index] else char
    }.joinToString("")
}

fun parseDateStringToMillis(dateStr: String): Long {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return try {
        sdf.parse(dateStr)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

fun formatMillisToDateString(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date(millis))
}

fun formatDisplayDateLocalized(dateStr: String, isBangla: Boolean): String {
    try {
        val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = inputSdf.parse(dateStr) ?: return dateStr
        val cal = Calendar.getInstance()
        cal.time = date
        
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        
        val bnMonths = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
        val enMonths = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        
        val monthName = if (isBangla) bnMonths[month] else enMonths[month]
        val dayStr = convertDigits(dayOfMonth.toString(), isBangla)
        val yearStr = convertDigits(year.toString(), isBangla)
        
        return "$dayStr $monthName, $yearStr"
    } catch (e: Exception) {
        return dateStr
    }
}

// Represents summary of a previous date
data class DateSummary(
    val date: String,
    val totalSales: Double,
    val cashReceived: Double,
    val due: Double,
    val expense: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySalesScreen(
    modifier: Modifier = Modifier,
    viewModel: SaleViewModel = viewModel()
) {
    val context = LocalContext.current
    val isBangla by viewModel.isBangla.collectAsState()
    
    val selectedDate by viewModel.selectedDate.collectAsState()
    val salesList by viewModel.salesForSelectedDate.collectAsState()
    val allDueSales by viewModel.allDueSales.collectAsState()
    val totalDueSales by viewModel.totalDueSales.collectAsState()
    val allSalesList by viewModel.allSalesList.collectAsState()

    // Dialog & UI states
    var showClearConfirm by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<SaleItem?>(null) }
    var itemToEdit by remember { mutableStateOf<SaleItem?>(null) }
    
    var showGoogleChooser by remember { mutableStateOf(false) }
    var showSyncDetails by remember { mutableStateOf(false) }
    
    // Active Tab: 0 -> Today's entries, 1 -> Due List, 2 -> Old Ledger
    var activeTab by remember { mutableStateOf(0) }
    
    // Interactive Dialog States
    var showCalculator by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var dueItemForDeposit by remember { mutableStateOf<SaleItem?>(null) }

    // Summary calculations
    val totalSales = remember(salesList) { 
        salesList.filter { it.type != "expense" }.sumOf { it.price } 
    }
    val cashSales = remember(salesList) { 
        salesList.filter { it.type == "cash" }.sumOf { it.price } 
    }
    val todayExpense = remember(salesList) { 
        salesList.filter { it.type == "expense" }.sumOf { it.price } 
    }
    
    // Optimization: Remember lambdas
    val onDateSelected = remember { { date: String -> viewModel.onDateSelected(date) } }
    val onSignInClick = remember { { showGoogleChooser = true } }
    val onProfileClick = remember { { showSyncDetails = true } }
    val onSubmitSuccess = remember {
        {
            Toast.makeText(
                context,
                if (isBangla) "বিক্রি সফলভাবে এন্ট্রি করা হয়েছে" else "Sale recorded successfully",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    val onSubmitError = remember {
        { errorMsg: String ->
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
        }
    }
    val onDeleteItem = remember { { item: SaleItem -> itemToDelete = item } }
    val onEditItem = remember { { item: SaleItem -> itemToEdit = item } }

    // Live Ticking Clock state (adapts automatically to locale changes)
    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            val formatted = SimpleDateFormat("hh:mm:ss a", Locale.US).format(now)
            currentTimeString = formatted
            kotlinx.coroutines.delay(1000)
        }
    }

    // Process old ledger records beautifully grouped by Year and Month
    val oldLedgerData = remember(allSalesList) {
        val formats = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        allSalesList
            .groupBy { item ->
                try {
                    val date = formats.parse(item.date)
                    val cal = Calendar.getInstance()
                    if (date != null) cal.time = date
                    val year = cal.get(Calendar.YEAR).toString()
                    val month = cal.get(Calendar.MONTH) // 0-indexed
                    Pair(year, month)
                } catch (e: Exception) {
                    Pair("অন্যান্য", -1)
                }
            }
            .mapValues { (_, items) ->
                items.groupBy { it.date }
                    .map { (dateStr, dateItems) ->
                        val cashVal = dateItems.filter { it.type == "cash" }.sumOf { it.price }
                        val dueVal = dateItems.filter { it.type == "due" }.sumOf { it.price }
                        val expVal = dateItems.filter { it.type == "expense" }.sumOf { it.price }
                        val totVal = cashVal + dueVal
                        
                        DateSummary(
                            date = dateStr,
                            totalSales = totVal,
                            cashReceived = cashVal,
                            due = dueVal,
                            expense = expVal
                        )
                    }
                    .sortedByDescending { it.date }
            }
            .toList()
            .sortedWith(compareByDescending<Pair<Pair<String, Int>, List<DateSummary>>> { it.first.first }
                .thenByDescending { it.first.second })
    }

    Scaffold(
        modifier = modifier.background(Color(0xFFF8FAFC))
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(scaffoldPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Elegant bilingual header section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Side: Logo & App Name, and Action Buttons below them
                    Column(modifier = Modifier.weight(1.3f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color(0xFF005FB0), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isBangla) "হিসাব খাতা" else "Hisab Khata",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F172A),
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    text = if (isBangla) "দোকানের দৈনিক খতিয়ান" else "Daily store accounts",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Buttons row below Logo and Name
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Calculator Button
                            IconButton(
                                onClick = { showCalculator = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFFF1F5F9), CircleShape)
                                    .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = "Calculator",
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Settings Button
                            IconButton(
                                onClick = { showSettingsDialog = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFFF1F5F9), CircleShape)
                                    .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Language Translation Button
                            IconButton(
                                onClick = { viewModel.toggleLanguage() },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFF005FB0).copy(alpha = 0.1f), CircleShape)
                                    .border(1.dp, Color(0xFF005FB0).copy(alpha = 0.3f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Change Language",
                                    tint = Color(0xFF005FB0),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Right Side: Calendar Date, Day, Month, and ticking Clock (Dynamic translation!)
                    Column(
                        modifier = Modifier.weight(1.1f),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Clock UI Box
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (currentTimeString.isNotEmpty()) {
                                    val formattedTime = currentTimeString
                                        .replace("AM", if (isBangla) "এএম" else "AM")
                                        .replace("PM", if (isBangla) "পিএম" else "PM")
                                    convertDigits(formattedTime, isBangla)
                                } else {
                                    "--:--:-- --"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Date Display text
                        val dateText = remember(isBangla, currentTimeString) {
                            val now = Calendar.getInstance()
                            val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)
                            val dayOfMonth = now.get(Calendar.DAY_OF_MONTH)
                            val month = now.get(Calendar.MONTH) // 0-indexed
                            val year = now.get(Calendar.YEAR)
                            
                            val bnDays = listOf("রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার", "শনিবার")
                            val enDays = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                            
                            val bnMonths = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
                            val enMonths = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                            
                            val dayName = if (isBangla) bnDays[dayOfWeek - 1] else enDays[dayOfWeek - 1]
                            val monthName = if (isBangla) bnMonths[month] else enMonths[month]
                            
                            val dayStr = convertDigits(dayOfMonth.toString(), isBangla)
                            val yearStr = convertDigits(year.toString(), isBangla)
                            
                            "$dayName, $dayStr $monthName $yearStr"
                        }
                        
                        Text(
                            text = dateText,
                            fontSize = 11.sp,
                            color = Color(0xFF1E293B),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Active Date Capsule Badge with custom Picker inside
                        DateCapsuleBadge(
                            selectedDateStr = selectedDate,
                            isBangla = isBangla,
                            onDateSelected = onDateSelected
                        )
                    }
                }
            }

            // Nicely margined 4 smaller boxes display
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Box 1: Total Sales
                    MinimalSummaryCard2(
                        title = if (isBangla) "মোট বিক্রি" else "Total Sales",
                        amount = totalSales,
                        containerColor = Color(0xFFEFF6FF),
                        textColor = Color(0xFF1E40AF),
                        icon = Icons.Default.ShoppingCart,
                        isBangla = isBangla,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Box 2: Cash Received
                    MinimalSummaryCard2(
                        title = if (isBangla) "নগদ জমা" else "Cash Recd",
                        amount = cashSales,
                        containerColor = Color(0xFFECFDF5),
                        textColor = Color(0xFF065F46),
                        icon = Icons.Default.AttachMoney,
                        isBangla = isBangla,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Box 3: Due (unpaid total)
                    MinimalSummaryCard2(
                        title = if (isBangla) "বাকি" else "Due",
                        amount = totalDueSales,
                        containerColor = Color(0xFFFEF2F2),
                        textColor = Color(0xFF991B1B),
                        icon = Icons.Default.Warning,
                        isBangla = isBangla,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Box 4: Today's Expense
                    MinimalSummaryCard2(
                        title = if (isBangla) "আজকের খরচ" else "Expense",
                        amount = todayExpense,
                        containerColor = Color(0xFFFFFBEB),
                        textColor = Color(0xFF92400E),
                        icon = Icons.Default.RemoveCircleOutline,
                        isBangla = isBangla,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Sold item inputs Form & "Add Expense" Option Button below boxes
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DailySalesForm(
                        viewModel = viewModel,
                        isBangla = isBangla,
                        onSubmitSuccess = onSubmitSuccess,
                        onSubmitError = onSubmitError
                    )
                    
                    // Custom standalone Button for adding Expense
                    Button(
                        onClick = { showAddExpenseDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF92400E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RemoveCircleOutline,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isBangla) "আজকের খরচ যোগ করুন" else "Add Today's Expense",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Beautiful tabbed row navigation (Today's Entries, All Due List, Old Ledger)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Today's Entries Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (activeTab == 0) Color.White else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { activeTab = 0 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBangla) "আজকের খতিয়ান" else "Today's Entries",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (activeTab == 0) Color(0xFF0F172A) else Color(0xFF64748B)
                        )
                    }
                    
                    // All Due Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (activeTab == 1) Color.White else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { activeTab = 1 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBangla) "বাকির লিস্ট" else "Due List",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (activeTab == 1) Color(0xFFB91C1C) else Color(0xFF64748B)
                        )
                    }
                    
                    // Old Ledger Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (activeTab == 2) Color.White else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { activeTab = 2 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBangla) "পুরোনো হিসাব" else "Old Ledger",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (activeTab == 2) Color(0xFF005FB0) else Color(0xFF64748B)
                        )
                    }
                }
            }

            // Tab contents
            when (activeTab) {
                0 -> { // Today's Entries list
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isBangla) {
                                    "এন্ট্রি তালিকা (${convertDigits(salesList.size.toString(), true)})"
                                } else {
                                    "Entries list (${salesList.size})"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF475569)
                            )

                            if (salesList.isNotEmpty()) {
                                TextButton(
                                    onClick = { showClearConfirm = true },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444)),
                                    modifier = Modifier.testTag("clear_all_button")
                                ) {
                                    Text(
                                        text = if (isBangla) "এই দিনের সব মুছুন" else "Clear This Date",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    if (salesList.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                EmptyState(isBangla = isBangla)
                            }
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Table Header Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF8FAFC))
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isBangla) "পণ্যের নাম/বিবরণ" else "Product/Details",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            modifier = Modifier.weight(1.8f)
                                        )
                                        Text(
                                            text = if (isBangla) "সময়" else "Time",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = if (isBangla) "ধরণ" else "Type",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            modifier = Modifier.weight(0.9f),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = if (isBangla) "টাকা" else "Price",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            modifier = Modifier.weight(1.1f),
                                            textAlign = TextAlign.End
                                        )
                                        Spacer(modifier = Modifier.width(60.dp))
                                    }
                                    
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFFE2E8F0))
                                    )
                                    
                                    salesList.forEachIndexed { index, sale ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Name
                                            Column(modifier = Modifier.weight(1.8f)) {
                                                val primaryText = if (sale.type == "due" && sale.customerName.isNotEmpty()) {
                                                    sale.customerName
                                                } else {
                                                    sale.name
                                                }
                                                Text(
                                                    text = primaryText,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF1E293B),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (sale.type == "due" && sale.customerName.isNotEmpty() && sale.name.isNotEmpty() && sale.name != "বাকি বিক্রি" && sale.name != "Due Sale") {
                                                    Text(
                                                        text = sale.name,
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF64748B),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            
                                            // Time
                                            Text(
                                                text = convertDigits(sale.time, isBangla),
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.weight(1f)
                                            )
                                            
                                            // Type Badge
                                            Box(
                                                modifier = Modifier
                                                    .weight(0.9f)
                                                    .wrapContentWidth(Alignment.CenterHorizontally)
                                            ) {
                                                val isCash = sale.type == "cash"
                                                val isExpense = sale.type == "expense"
                                                
                                                val badgeBg = if (isExpense) Color(0xFFFEF3C7) else (if (isCash) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                                val badgeBorder = if (isExpense) Color(0xFFFDE68A) else (if (isCash) Color(0xFF86EFAC) else Color(0xFFFCA5A5))
                                                val badgeText = if (isExpense) Color(0xFF92400E) else (if (isCash) Color(0xFF15803D) else Color(0xFFB91C1C))
                                                val badgeLabel = if (isExpense) (if (isBangla) "খরচ" else "Exp") else (if (isCash) (if (isBangla) "নগদ" else "Cash") else (if (isBangla) "বাকি" else "Due"))
                                                
                                                Surface(
                                                    color = badgeBg,
                                                    border = BorderStroke(1.dp, badgeBorder),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = badgeLabel,
                                                        color = badgeText,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            
                                            // Price
                                            Text(
                                                text = "৳" + convertDigits(String.format(Locale.US, "%.0f", sale.price), isBangla),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (sale.type == "expense") Color(0xFFB45309) else (if (sale.type == "due") Color(0xFFB91C1C) else Color(0xFF0F172A)),
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.weight(1.1f)
                                            )
                                            
                                            Spacer(modifier = Modifier.width(6.dp))
                                            
                                            // Action Buttons: Edit & Delete
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(
                                                    onClick = { onEditItem(sale) },
                                                    modifier = Modifier.size(26.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "সংশোধন",
                                                        tint = Color(0xFF005FB0),
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { onDeleteItem(sale) },
                                                    modifier = Modifier.size(26.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "মুছে ফেলুন",
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                            }
                                        }
                                        
                                        if (index < salesList.lastIndex) {
                                            Spacer(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(0.5.dp)
                                                    .background(Color(0xFFE2E8F0))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> { // Due List
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isBangla) "সকল বাকির খাতা" else "Outstanding Debts",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB91C1C)
                            )
                        }
                    }

                    if (allDueSales.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isBangla) "কোনো বাকির হিসাব পাওয়া যায়নি।" else "No active dues found.",
                                        fontSize = 14.sp,
                                        color = Color(0xFF64748B),
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFFEF2F2))
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isBangla) "গ্রাহকের নাম" else "Customer Name",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFFB91C1C),
                                            modifier = Modifier.weight(1.8f)
                                        )
                                        Text(
                                            text = if (isBangla) "তারিখ" else "Date",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFFB91C1C),
                                            modifier = Modifier.weight(1.2f),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = if (isBangla) "টাকা" else "Amount",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFFB91C1C),
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.End
                                        )
                                        Spacer(modifier = Modifier.width(62.dp))
                                    }
                                    
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFFE2E8F0))
                                    )
                                    
                                    allDueSales.forEachIndexed { index, dueItem ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1.8f)) {
                                                val displayName = dueItem.customerName.ifEmpty { dueItem.name }
                                                Text(
                                                    text = displayName,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0F172A),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (dueItem.customerName.isNotEmpty() && dueItem.name.isNotEmpty() && dueItem.name != "বাকি বিক্রি" && dueItem.name != "Due Sale") {
                                                    Text(
                                                        text = dueItem.name,
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF64748B),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                            Text(
                                                text = convertDigits(formatDisplayDateLocalized(dueItem.date, isBangla), isBangla),
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B),
                                                modifier = Modifier.weight(1.2f),
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "৳" + convertDigits(String.format(Locale.US, "%.0f", dueItem.price), isBangla),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB91C1C),
                                                modifier = Modifier.weight(1f),
                                                textAlign = TextAlign.End
                                            )
                                            
                                            Spacer(modifier = Modifier.width(12.dp))
                                            
                                            // Deposit button (opens Deposit amount entering Dialog)
                                            Button(
                                                onClick = { dueItemForDeposit = dueItem },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                modifier = Modifier
                                                    .height(28.dp)
                                                    .testTag("collect_due_button_${dueItem.id}"),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = if (isBangla) "জমা" else "Pay",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        
                                        if (index < allDueSales.lastIndex) {
                                            Spacer(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(0.5.dp)
                                                    .background(Color(0xFFE2E8F0))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> { // Old Ledger (collapsible collapsible year & month views!)
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isBangla) "পুরোনো হিসাব খাতা" else "Previous Ledgers",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF005FB0)
                            )
                        }
                    }

                    if (oldLedgerData.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isBangla) "কোনো বিগত হিসাব পাওয়া যায়নি।" else "No previous entries found.",
                                        fontSize = 14.sp,
                                        color = Color(0xFF64748B),
                                        fontStyle = FontStyle.Italic
                                    )
                                }
                            }
                        }
                    } else {
                        // Render grouped data beautifully
                        oldLedgerData.forEach { (yearMonth, summaries) ->
                            val (yearStr, monthInt) = yearMonth
                            
                            val monthNamesBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
                            val monthNamesEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
                            val monthLabel = if (monthInt != -1) {
                                if (isBangla) monthNamesBn[monthInt] else monthNamesEn[monthInt]
                            } else {
                                if (isBangla) "অন্যান্য" else "Others"
                            }
                            
                            val headerTitle = if (isBangla) {
                                "$monthLabel, ${convertDigits(yearStr, true)}"
                            } else {
                                "$monthLabel $yearStr"
                            }

                            item {
                                Text(
                                    text = headerTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF005FB0),
                                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                                )
                            }

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        summaries.forEachIndexed { iIndex, daySum ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        // Switch active date and open Tab 0 (Today's entries)
                                                        viewModel.onDateSelected(daySum.date)
                                                        activeTab = 0
                                                        Toast
                                                            .makeText(
                                                                context,
                                                                if (isBangla) "${formatDisplayDateLocalized(daySum.date, true)} তারিখের এন্ট্রিগুলো লোড করা হয়েছে" else "Loaded records for ${formatDisplayDateLocalized(daySum.date, false)}",
                                                                Toast.LENGTH_SHORT
                                                            )
                                                            .show()
                                                    }
                                                    .padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1.2f)) {
                                                    Text(
                                                        text = formatDisplayDateLocalized(daySum.date, isBangla),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color(0xFF0F172A)
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = if (isBangla) "ট্যাপ করে বিস্তারিত দেখুন" else "Tap to view details",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF94A3B8)
                                                    )
                                                }
                                                
                                                Column(
                                                    modifier = Modifier.weight(1.8f),
                                                    horizontalAlignment = Alignment.End
                                                ) {
                                                    // Quick statistics block
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.End) {
                                                            Text(
                                                                text = (if (isBangla) "বিক্রি: ৳" else "Sales: ৳") + convertDigits(String.format(Locale.US, "%.0f", daySum.totalSales), isBangla),
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                color = Color(0xFF0284C7)
                                                            )
                                                            Text(
                                                                text = (if (isBangla) "নগদ: ৳" else "Cash: ৳") + convertDigits(String.format(Locale.US, "%.0f", daySum.cashReceived), isBangla),
                                                                fontSize = 11.sp,
                                                                color = Color(0xFF059669)
                                                            )
                                                        }
                                                        
                                                        Column(horizontalAlignment = Alignment.End) {
                                                            Text(
                                                                text = (if (isBangla) "বাকি: ৳" else "Due: ৳") + convertDigits(String.format(Locale.US, "%.0f", daySum.due), isBangla),
                                                                fontSize = 11.sp,
                                                                color = Color(0xFFDC2626)
                                                            )
                                                            Text(
                                                                text = (if (isBangla) "খরচ: ৳" else "Exp: ৳") + convertDigits(String.format(Locale.US, "%.0f", daySum.expense), isBangla),
                                                                fontSize = 11.sp,
                                                                color = Color(0xFFD97706)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            if (iIndex < summaries.lastIndex) {
                                                Spacer(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(0.5.dp)
                                                        .background(Color(0xFFE2E8F0))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ----------------------------------------------------
    // POPUPS AND DIALOGS SECTIONS (Robust, styled & bilingual)
    // ----------------------------------------------------

    // Calculator interactive Dialog
    if (showCalculator) {
        var equation by remember { mutableStateOf("") }
        var calcResult by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showCalculator = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = Color(0xFF005FB0),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBangla) "ক্যালকুলেটর" else "Calculator",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Display screen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (equation.isEmpty()) "0" else convertDigits(equation, isBangla),
                                fontSize = 16.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (calcResult.isEmpty()) "0" else convertDigits(calcResult, isBangla),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    // Buttons grid
                    val keys = listOf(
                        "C", "(", ")", "/",
                        "7", "8", "9", "*",
                        "4", "5", "6", "-",
                        "1", "2", "3", "+",
                        "0", ".", "=", "←"
                    )
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(keys) { key ->
                            val isOperator = key in listOf("/", "*", "-", "+", "(", ")")
                            val isSpecial = key in listOf("C", "←")
                            val isEqual = key == "="
                            
                            val btnBg = when {
                                isEqual -> Color(0xFF005FB0)
                                isSpecial -> Color(0xFFEF4444)
                                isOperator -> Color(0xFFE2E8F0)
                                else -> Color(0xFFF1F5F9)
                            }
                            
                            val btnTextCol = when {
                                isEqual || isSpecial -> Color.White
                                isOperator -> Color(0xFF0F172A)
                                else -> Color(0xFF1E293B)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(btnBg, RoundedCornerShape(10.dp))
                                    .clickable {
                                        when (key) {
                                            "C" -> {
                                                equation = ""
                                                calcResult = ""
                                            }
                                            "←" -> {
                                                if (equation.isNotEmpty()) {
                                                    equation = equation.dropLast(1)
                                                }
                                            }
                                            "=" -> {
                                                if (equation.isNotEmpty()) {
                                                    try {
                                                        val formattedEquation = equation
                                                        val rawRes = evaluateExpression(formattedEquation)
                                                        calcResult = if (rawRes % 1 == 0.0) {
                                                            String.format(Locale.US, "%.0f", rawRes)
                                                        } else {
                                                            String.format(Locale.US, "%.2f", rawRes)
                                                        }
                                                        equation = calcResult
                                                    } catch (e: Exception) {
                                                        calcResult = if (isBangla) "ভুল ইনপুট" else "Error"
                                                    }
                                                }
                                            }
                                            else -> {
                                                equation += key
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isBangla && key !in listOf("C", "←", "=")) convertDigits(key, true) else key,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = btnTextCol
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCalculator = false }) {
                    Text(
                        text = if (isBangla) "বন্ধ করুন" else "Close",
                        color = Color(0xFF005FB0),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    // Standalone Add Expense Dialog
    if (showAddExpenseDialog) {
        var expenseDesc by remember { mutableStateOf("") }
        var expenseAmt by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RemoveCircleOutline,
                        contentDescription = null,
                        tint = Color(0xFF92400E)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) "আজকের খরচ যোগ করুন" else "Add Today's Expense",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF92400E)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isBangla) "খরচের বিবরণ (যেমন: বিদ্যুৎ বিল, চা/নাস্তা)" else "Expense Details (e.g., Electric Bill, Snacks)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    OutlinedTextField(
                        value = expenseDesc,
                        onValueChange = { expenseDesc = it },
                        placeholder = { Text(if (isBangla) "যেমন: দোকানের ভাড়া" else "e.g. Shop Rent", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    Text(
                        text = if (isBangla) "খরচের পরিমাণ (৳)" else "Expense Amount (৳)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    OutlinedTextField(
                        value = expenseAmt,
                        onValueChange = { expenseAmt = it },
                        placeholder = { Text("0.00", fontSize = 14.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = expenseAmt.toDoubleOrNull() ?: 0.0
                        viewModel.addExpense(
                            description = expenseDesc,
                            amount = amount,
                            onSuccess = {
                                showAddExpenseDialog = false
                                Toast.makeText(
                                    context,
                                    if (isBangla) "খরচ সফলভাবে যোগ করা হয়েছে" else "Expense added successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF92400E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = if (isBangla) "যোগ করুন" else "Add", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) {
                    Text(text = if (isBangla) "বাতিল" else "Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Deposit due payment dialog
    dueItemForDeposit?.let { dueItem ->
        var depositAmountStr by remember { mutableStateOf(String.format(Locale.US, "%.0f", dueItem.price)) }
        
        AlertDialog(
            onDismissRequest = { dueItemForDeposit = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) "বাকির টাকা জমা" else "Receive Due Payment",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val displayName = dueItem.customerName.ifEmpty { dueItem.name }
                    Text(
                        text = (if (isBangla) "গ্রাহক: " else "Customer: ") + displayName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = (if (isBangla) "মোট বকেয়া পরিমাণ: ৳" else "Outstanding Due: ৳") + convertDigits(String.format(Locale.US, "%.0f", dueItem.price), isBangla),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB91C1C)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = if (isBangla) "জমার পরিমাণ লিখুন (৳)" else "Enter Deposit Amount (৳)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    OutlinedTextField(
                        value = depositAmountStr,
                        onValueChange = { depositAmountStr = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val depAmt = depositAmountStr.toDoubleOrNull() ?: 0.0
                        if (depAmt <= 0) {
                            Toast.makeText(context, if (isBangla) "সঠিক পরিমাণ লিখুন" else "Enter a valid amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.depositDue(dueItem, depAmt) {
                            dueItemForDeposit = null
                            val customerDisp = dueItem.customerName.ifEmpty { dueItem.name }
                            Toast.makeText(
                                context,
                                if (isBangla) {
                                    "${customerDisp}-এর বাকির টাকা জমা নেওয়া হয়েছে এবং আজকের বিক্রির খাতায় যোগ করা হয়েছে!"
                                } else {
                                    "Payment received from ${customerDisp} and added to today's sales!"
                                },
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = if (isBangla) "জমা নিশ্চিত করুন" else "Confirm", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { dueItemForDeposit = null }) {
                    Text(text = if (isBangla) "বাতিল" else "Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Dynamic Edit Sale item dialog
    itemToEdit?.let { sale ->
        var editName by remember { mutableStateOf(sale.name) }
        var editCustName by remember { mutableStateOf(sale.customerName) }
        var editPriceStr by remember { mutableStateOf(String.format(Locale.US, "%.0f", sale.price)) }
        var editType by remember { mutableStateOf(sale.type) }
        
        AlertDialog(
            onDismissRequest = { itemToEdit = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color(0xFF005FB0)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) "এন্ট্রি সংশোধন করুন" else "Modify Entry",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF005FB0)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Type Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("cash", "due", "expense").forEach { type ->
                            val isSel = editType == type
                            val typeLabel = when (type) {
                                "cash" -> if (isBangla) "নগদ" else "Cash"
                                "due" -> if (isBangla) "বাকি" else "Due"
                                else -> if (isBangla) "খরচ" else "Expense"
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSel) Color(0xFF005FB0) else Color(0xFFF1F5F9),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { editType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    typeLabel,
                                    color = if (isSel) Color.White else Color(0xFF475569),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))

                    if (editType == "due") {
                        Text(
                            text = if (isBangla) "গ্রাহকের নাম" else "Customer Name",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        OutlinedTextField(
                            value = editCustName,
                            onValueChange = { editCustName = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Text(
                        text = if (editType == "expense") {
                            if (isBangla) "খরচের বিবরণ" else "Expense Description"
                        } else {
                            if (isBangla) "পণ্যের বিবরণ/নাম" else "Product Name"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Text(
                        text = if (isBangla) "টাকার পরিমাণ (৳)" else "Price / Amount (৳)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    OutlinedTextField(
                        value = editPriceStr,
                        onValueChange = { editPriceStr = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = editPriceStr.toDoubleOrNull() ?: 0.0
                        if (amount <= 0) {
                            Toast.makeText(context, if (isBangla) "সঠিক পরিমাণ লিখুন" else "Enter correct amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (editType == "due" && editCustName.trim().isEmpty()) {
                            Toast.makeText(context, if (isBangla) "গ্রাহকের নাম দিন" else "Enter customer name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        viewModel.editSale(
                            id = sale.id,
                            name = editName,
                            customerName = editCustName,
                            price = amount,
                            type = editType,
                            date = sale.date,
                            time = sale.time
                        )
                        itemToEdit = null
                        Toast.makeText(
                            context,
                            if (isBangla) "সফলভাবে সংশোধন করা হয়েছে" else "Updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FB0)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = if (isBangla) "সংরক্ষণ" else "Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToEdit = null }) {
                    Text(text = if (isBangla) "বাতিল" else "Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Settings stand-alone Dialog
    if (showSettingsDialog) {
        val googleEmail by viewModel.googleAccountEmail.collectAsState()
        
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBangla) "সেটিংস ও ব্যাকআপ" else "Settings & Backup",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isBangla) "আপনার হিসাব খাতার ডাটা ব্যাকআপ ও রিস্টোর করার জন্য জিমেইল ব্যবহার করুন।" else "Configure automatic backups or restore ledger entries via Gmail.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Profile button click
                    if (googleEmail != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showSettingsDialog = false
                                    showSyncDetails = true
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(Color(0xFF3B82F6), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val letter = googleEmail?.take(1)?.uppercase(Locale.US) ?: "U"
                                    Text(letter, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(googleEmail ?: "", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                                    Text(if (isBangla) "ব্যাকআপ তথ্য দেখতে ক্লিক করুন" else "Tap to view sync status", fontSize = 10.sp, color = Color(0xFF3B82F6))
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                showSettingsDialog = false
                                showGoogleChooser = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (isBangla) "গুগল অ্যাকাউন্ট দিয়ে সাইন-ইন" else "Sign in with Google",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text(text = if (isBangla) "ঠিক আছে" else "OK", color = Color(0xFF005FB0), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Confirmation dialog for clearing a date
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isBangla) "নিশ্চিতকরণ" else "Confirm Action")
                }
            },
            text = {
                Text(
                    text = if (isBangla) {
                        "${formatDisplayDateLocalized(selectedDate, true)} তারিখের সমস্ত বিক্রির ডাটা মুছে যাবে। আপনি কি নিশ্চিত?"
                    } else {
                        "All ledger data for ${formatDisplayDateLocalized(selectedDate, false)} will be deleted forever. Are you sure?"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearSalesForSelectedDate()
                        showClearConfirm = false
                        Toast.makeText(
                            context,
                            if (isBangla) "সমস্ত ডাটা মুছে ফেলা হয়েছে" else "All records cleared",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Text(text = if (isBangla) "হ্যাঁ, মুছুন" else "Yes, Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(text = if (isBangla) "বাতিল" else "Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Confirmation dialog for deleting individual item
    itemToDelete?.let { saleItem ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isBangla) "নিশ্চিতকরণ" else "Confirm Action")
                }
            },
            text = {
                Text(
                    text = if (isBangla) {
                        "আপনি কি সত্যিই এই হিসাবটি মুছে ফেলতে চান?"
                    } else {
                        "Are you sure you want to delete this entry?"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSale(saleItem.id)
                        itemToDelete = null
                        Toast.makeText(
                            context,
                            if (isBangla) "হিসাবটি মুছে ফেলা হয়েছে" else "Entry deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Text(text = if (isBangla) "হ্যাঁ, মুছুন" else "Yes, Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(text = if (isBangla) "বাতিল" else "Cancel", color = Color(0xFF64748B))
                }
            }
        )
    }

    // Google account chooser
    if (showGoogleChooser) {
        AlertDialog(
            onDismissRequest = { showGoogleChooser = false },
            title = {
                Text(
                    text = if (isBangla) "গুগল অ্যাকাউন্ট বেছে নিন" else "Choose Google Account",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E293B)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isBangla) {
                            "এই অ্যাকাউন্টটি দিয়ে আপনার দৈনন্দিন বিক্রির ডাটা অনলাইনে ব্যাকআপ ও সিঙ্ক করা হবে।"
                        } else {
                            "Select account to securely sync and backup your ledger data."
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.signInWithGoogle("jonydatta222@gmail.com")
                                showGoogleChooser = false
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF3B82F6), RoundedCornerShape(18.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("J", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Jony Datta",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "jonydatta222@gmail.com",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showGoogleChooser = false }) {
                    Text(text = if (isBangla) "বাতিল" else "Cancel", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Google sync details dialog
    if (showSyncDetails) {
        val googleEmail by viewModel.googleAccountEmail.collectAsState()
        val isSyncing by viewModel.isSyncing.collectAsState()
        val lastSyncedTime by viewModel.lastSyncedTime.collectAsState()

        AlertDialog(
            onDismissRequest = { showSyncDetails = false },
            title = {
                Text(
                    text = if (isBangla) "গুগল ক্লাউড সিঙ্ক" else "Google Cloud Sync",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E293B)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFE0F2FE), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val letter = googleEmail?.take(1)?.uppercase(Locale.US) ?: "U"
                                Text(letter, color = Color(0xFF0369A1), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Jony Datta",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = googleEmail ?: "",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBangla) "স্ট্যাটাস:" else "Status:",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (isBangla) "✓ ক্লাউড ব্যাকআপ সক্রিয়" else "✓ Cloud Backup Active",
                                fontSize = 12.sp,
                                color = Color(0xFF059669),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBangla) "শেষ সিঙ্ক:" else "Last Synced:",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (lastSyncedTime != null) convertDigits(lastSyncedTime!!, isBangla) else (if (isBangla) "সিঙ্ক করা হয়নি" else "Not synced yet"),
                                fontSize = 12.sp,
                                color = Color(0xFF1E293B),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.syncNow() },
                            enabled = !isSyncing,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FB0)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.5f),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "সিঙ্ক করুন",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSyncing) (if (isBangla) "সিঙ্ক..." else "Syncing...") else (if (isBangla) "সিঙ্ক করুন" else "Sync Now"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.signOutGoogle()
                                showSyncDetails = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(
                                text = if (isBangla) "সাইন-আউট" else "Sign Out",
                                color = Color(0xFFDC2626),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSyncDetails = false }) {
                    Text(text = if (isBangla) "বন্ধ করুন" else "Close", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateCapsuleBadge(
    selectedDateStr: String,
    isBangla: Boolean,
    onDateSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    val initialMillis = remember(selectedDateStr) {
        parseDateStringToMillis(selectedDateStr)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    Surface(
        modifier = Modifier
            .clickable { showDialog = true }
            .testTag("filter_date_card"),
        color = Color(0xFF005FB0).copy(alpha = 0.1f),
        border = BorderStroke(1.dp, Color(0xFF005FB0).copy(alpha = 0.3f)),
        shape = RoundedCornerShape(50.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📅 " + formatDisplayDateLocalized(selectedDateStr, isBangla),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF005FB0)
            )
        }
    }

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(formatMillisToDateString(millis))
                        }
                        showDialog = false
                    }
                ) {
                    Text(text = if (isBangla) "ঠিক আছে" else "OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(text = if (isBangla) "বাতিল" else "Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun MinimalSummaryCard2(
    title: String,
    amount: Double,
    containerColor: Color,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isBangla: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = textColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "৳" + convertDigits(String.format(Locale.US, "%.0f", amount), isBangla),
                fontSize = 13.sp,
                color = textColor,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SaleTypeDropdown(
    saleType: String,
    isBangla: Boolean,
    onSaleTypeChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isCash = saleType == "cash"
    val displayLabel = if (isCash) {
        if (isBangla) "নগদ (Cash)" else "Cash"
    } else {
        if (isBangla) "বাকি (Due)" else "Due"
    }
    
    val bgColor = if (isCash) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
    val borderColor = if (isCash) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
    val textColor = if (isCash) Color(0xFF15803D) else Color(0xFFB91C1C)

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = bgColor,
            border = BorderStroke(2.dp, borderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = displayLabel,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = textColor
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            DropdownMenuItem(
                text = { 
                    Text(
                        text = if (isBangla) "নগদ (Cash)" else "Cash", 
                        color = Color(0xFF15803D), 
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ) 
                },
                onClick = {
                    onSaleTypeChanged("cash")
                    expanded = false
                },
                modifier = Modifier.background(Color(0xFFDCFCE7).copy(alpha = 0.3f))
            )
            DropdownMenuItem(
                text = { 
                    Text(
                        text = if (isBangla) "বাকি (Due)" else "Due", 
                        color = Color(0xFFB91C1C), 
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ) 
                },
                onClick = {
                    onSaleTypeChanged("due")
                    expanded = false
                },
                modifier = Modifier.background(Color(0xFFFEE2E2).copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
fun EmptyState(isBangla: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            tint = Color(0xFF94A3B8).copy(alpha = 0.3f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (isBangla) "এই তারিখে কোনো বিক্রির এন্ট্রি নেই।" else "No sales entries for this date.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF94A3B8),
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DailySalesForm(
    viewModel: SaleViewModel,
    isBangla: Boolean,
    onSubmitSuccess: () -> Unit,
    onSubmitError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemName by viewModel.itemName.collectAsState()
    val customerName by viewModel.customerName.collectAsState()
    val itemPrice by viewModel.itemPrice.collectAsState()
    val saleType by viewModel.saleType.collectAsState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Product Name or Customer Name
                Column(modifier = Modifier.weight(1f)) {
                    val isDue = saleType == "due"
                    Text(
                        text = if (isDue) {
                            if (isBangla) "গ্রাহকের নাম (বাধ্যতামূলক)" else "Customer Name (Req.)"
                        } else {
                            if (isBangla) "পণ্যের নাম" else "Product Name"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDue) Color(0xFFC62828) else Color(0xFF64748B),
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = if (isDue) customerName else itemName,
                        onValueChange = { if (isDue) viewModel.onCustomerNameChanged(it) else viewModel.onItemNameChanged(it) },
                        placeholder = { 
                            Text(
                                text = if (isDue) (if (isBangla) "যেমন: রহিম" else "e.g. Rahim") else (if (isBangla) "যেমন: শার্ট" else "e.g. Shirt"), 
                                fontSize = 14.sp
                            ) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("item_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1E293B),
                            unfocusedTextColor = Color(0xFF1E293B),
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedPlaceholderColor = Color(0xFF94A3B8),
                            unfocusedPlaceholderColor = Color(0xFF94A3B8)
                        )
                    )
                }

                // Sale Type Selection Dropdown
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBangla) "বিক্রির ধরন" else "Sale Type",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    SaleTypeDropdown(
                        saleType = saleType,
                        isBangla = isBangla,
                        onSaleTypeChanged = { viewModel.onSaleTypeChanged(it) }
                    )
                }
            }

            // Optional description if due is selected
            AnimatedVisibility(visible = saleType == "due") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isBangla) "পণ্যের নাম / বিবরণ (ঐচ্ছিক)" else "Product Name / Details (Opt.)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { viewModel.onItemNameChanged(it) },
                        placeholder = { Text(if (isBangla) "যেমন: শার্ট" else "e.g. Shirt", fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("due_item_details_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1E293B),
                            unfocusedTextColor = Color(0xFF1E293B),
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedPlaceholderColor = Color(0xFF94A3B8),
                            unfocusedPlaceholderColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }

            // Price/Amount field and Submit button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Price Input field
                Column(modifier = Modifier.weight(1.3f)) {
                    Text(
                        text = if (isBangla) "টাকার পরিমাণ (৳)" else "Amount (৳)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = itemPrice,
                        onValueChange = { viewModel.onItemPriceChanged(it) },
                        placeholder = { Text("0.00", fontSize = 14.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("item_price_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1E293B),
                            unfocusedTextColor = Color(0xFF1E293B),
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedPlaceholderColor = Color(0xFF94A3B8),
                            unfocusedPlaceholderColor = Color(0xFF94A3B8)
                        )
                    )
                }

                // Add button
                Button(
                    onClick = {
                        viewModel.addSale(
                            onSuccess = onSubmitSuccess,
                            onError = onSubmitError
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FB0)),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("submit_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isBangla) "যোগ করুন" else "Add Entry",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleCircularSyncButton(
    viewModel: SaleViewModel,
    onSignInClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val googleEmail by viewModel.googleAccountEmail.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    Box(
        modifier = modifier
            .size(40.dp)
            .background(
                if (googleEmail != null) Color(0xFFE0F2FE) else Color(0xFFF1F5F9),
                CircleShape
            )
            .border(
                1.dp,
                if (googleEmail != null) Color(0xFF3B82F6) else Color(0xFFCBD5E1),
                CircleShape
            )
            .clickable {
                if (googleEmail != null) {
                    onProfileClick()
                } else {
                    onSignInClick()
                }
            }
            .testTag("google_sync_circular_button"),
        contentAlignment = Alignment.Center
    ) {
        if (googleEmail != null) {
            val firstLetter = googleEmail?.take(1)?.uppercase(Locale.US) ?: "U"
            Text(
                text = firstLetter,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0369A1)
            )
            
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(if (isSyncing) Color(0xFF3B82F6) else Color(0xFF10B981), CircleShape)
                    .border(1.5.dp, Color.White, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        } else {
            Text(
                text = "G",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFEA4335)
            )
        }
    }
}

// Complete reliable arithmetic evaluator for the interactive Calculator
fun evaluateExpression(str: String): Double {
    return object : Any() {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm() // addition
                else if (eat('-'.code)) x -= parseTerm() // subtraction
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code)) x *= parseFactor() // multiplication
                else if (eat('/'.code)) {
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("Division by zero")
                    x /= divisor // division
                } else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return +parseFactor() // unary plus
            if (eat('-'.code)) return -parseFactor() // unary minus

            var x: Double
            val startPos = pos
            if (eat('('.code)) { // parentheses
                x = parseExpression()
                if (!eat(')'.code)) throw RuntimeException("Missing closing parenthesis")
            } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
                while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }
            return x
        }
    }.parse()
}
