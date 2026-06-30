package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.SaleItem
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.SaleViewModel
import java.text.SimpleDateFormat
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

fun formatDisplayDate(dateStr: String): String {
    return try {
        val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = inputSdf.parse(dateStr)
        if (date != null) {
            val outputSdf = SimpleDateFormat("dd MMM, yyyy", Locale.US)
            outputSdf.format(date)
        } else {
            dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySalesScreen(
    modifier: Modifier = Modifier,
    viewModel: SaleViewModel = viewModel()
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsState()
    val salesList by viewModel.salesForSelectedDate.collectAsState()
    val allDueSales by viewModel.allDueSales.collectAsState()
    val totalDueSales by viewModel.totalDueSales.collectAsState()

    // Dialog flags
    var showClearConfirm by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<SaleItem?>(null) }
    var showGoogleChooser by remember { mutableStateOf(false) }
    var showSyncDetails by remember { mutableStateOf(false) }
    var showDueListDialog by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(0) }

    // Optimization: wrap summary calculations in remember(salesList) so they don't run on every scroll/recomposition
    val totalSales = remember(salesList) { salesList.sumOf { it.price } }
    val cashSales = remember(salesList) { salesList.filter { it.type == "cash" }.sumOf { it.price } }
    val dueSales = remember(salesList) { salesList.filter { it.type == "due" }.sumOf { it.price } }

    // Optimization: Remember lambdas to make subcomponents and list rows fully skippable
    val onDateSelected = remember { { date: String -> viewModel.onDateSelected(date) } }
    val onSignInClick = remember { { showGoogleChooser = true } }
    val onProfileClick = remember { { showSyncDetails = true } }
    val onSubmitSuccess = remember {
        {
            Toast.makeText(context, "বিক্রি সফলভাবে এন্ট্রি করা হয়েছে", Toast.LENGTH_SHORT).show()
        }
    }
    val onSubmitError = remember {
        { errorMsg: String ->
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
        }
    }
    val onDeleteItem = remember { { item: SaleItem -> itemToDelete = item } }

    Scaffold(
        modifier = modifier.background(Color(0xFFF7F9FC))
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FC))
                .padding(scaffoldPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Elegant Clean Header Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "বিক্রির খাতা",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1E),
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "আপনার ব্যবসার দৈনন্দিন হিসাব",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            fontStyle = FontStyle.Italic
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Clickable date display pill matching Clean Minimalism badge
                        DateCapsuleBadge(
                            selectedDateStr = selectedDate,
                            onDateSelected = onDateSelected
                        )

                        // Compact Circular Google Sync/Backup Button
                        GoogleCircularSyncButton(
                            viewModel = viewModel,
                            onSignInClick = onSignInClick,
                            onProfileClick = onProfileClick
                        )
                    }
                }
            }

            // Tonal Summary Cards Grid Row
            item {
                SummaryCardsSection(
                    totalSales = totalSales,
                    cashSales = cashSales,
                    dueSales = totalDueSales,
                    onDueClick = { showDueListDialog = true }
                )
            }

            // Product adding option (Form) placed where the Recent Entries list used to be
            item {
                DailySalesForm(
                    viewModel = viewModel,
                    onSubmitSuccess = onSubmitSuccess,
                    onSubmitError = onSubmitError
                )
            }

            // Beautiful Custom Tab Row / Segmented Control
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (activeTab == 0) Color.White else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { activeTab = 0 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "আজকের এন্ট্রি (${salesList.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (activeTab == 0) Color(0xFF0F172A) else Color(0xFF64748B)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (activeTab == 1) Color.White else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { activeTab = 1 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "সকল বাকির খাতা (${allDueSales.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (activeTab == 1) Color(0xFFC62828) else Color(0xFF64748B)
                        )
                    }
                }
            }

            if (activeTab == 0) {
                // Entries section header (Recent entries moved below adding option)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "আজকের খতিয়ান",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            letterSpacing = 1.sp
                        )

                        if (salesList.isNotEmpty()) {
                            TextButton(
                                onClick = { showClearConfirm = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444)),
                                modifier = Modifier.testTag("clear_all_button")
                            ) {
                                Text(
                                    text = "এই দিনের সব মুছুন",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // List of Sales or Empty placeholder at the bottom
                if (salesList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            EmptyState()
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
                                // Table Header Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC))
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "বিবরণ/নাম",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.weight(2f)
                                    )
                                    Text(
                                        text = "সময়",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.weight(1.1f),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "ধরণ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "টাকা",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.weight(1.3f),
                                        textAlign = TextAlign.End
                                    )
                                    Spacer(modifier = Modifier.width(36.dp))
                                }
                                
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFFE2E8F0))
                                )
                                
                                salesList.forEachIndexed { index, sale ->
                                    CompactSaleItemRow(
                                        sale = sale,
                                        onDelete = onDeleteItem
                                    )
                                    if (index < salesList.lastIndex) {
                                        Spacer(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(0.8.dp)
                                                .background(Color(0xFFF1F5F9))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Tab 1: All Due Ledger
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "সকল বাকি ও আদায় খাতা",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828),
                            letterSpacing = 1.sp
                        )
                    }
                }

                if (allDueSales.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "কোনো বাকির হিসাব পাওয়া যায়নি।",
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
                                // Table Header Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFF5F5))
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "গ্রাহকের নাম",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFFC62828),
                                        modifier = Modifier.weight(1.8f)
                                    )
                                    Text(
                                        text = "তারিখ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFFC62828),
                                        modifier = Modifier.weight(1.2f),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "টাকা",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFFC62828),
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.End
                                    )
                                    Spacer(modifier = Modifier.width(48.dp))
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
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF0F172A),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (dueItem.customerName.isNotEmpty() && dueItem.name.isNotEmpty() && dueItem.name != "বাকি বিক্রি") {
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
                                            text = dueItem.date,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            modifier = Modifier.weight(1.2f),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "৳${String.format(Locale.US, "%.0f", dueItem.price)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC62828),
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.End
                                        )
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        // Collect due button
                                        Button(
                                            onClick = {
                                                viewModel.collectDueSale(dueItem)
                                                val bakiName = dueItem.customerName.ifEmpty { dueItem.name }
                                                Toast.makeText(
                                                    context,
                                                    "${bakiName}-এর বাকির টাকা জমা নেওয়া হয়েছে এবং আজকের বিক্রির খাতায় যোগ করা হয়েছে!",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier
                                                .height(28.dp)
                                                .testTag("collect_due_button_${dueItem.id}"),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "জমা",
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
                                                .height(0.8.dp)
                                                .background(Color(0xFFF1F5F9))
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

    // Google Chooser Dialog
    if (showGoogleChooser) {
        AlertDialog(
            onDismissRequest = { showGoogleChooser = false },
            title = {
                Text(
                    text = "গুগল অ্যাকাউন্ট বেছে নিন",
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
                        text = "এই অ্যাকাউন্টটি দিয়ে আপনার দৈনন্দিন বিক্রির ডাটা অনলাইনে সুরক্ষিতভাবে ব্যাকআপ ও সিঙ্ক করা হবে।",
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
                    Text("বাতিল", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Google Sync Details Dialog
    if (showSyncDetails) {
        val googleEmail by viewModel.googleAccountEmail.collectAsState()
        val isSyncing by viewModel.isSyncing.collectAsState()
        val lastSyncedTime by viewModel.lastSyncedTime.collectAsState()

        AlertDialog(
            onDismissRequest = { showSyncDetails = false },
            title = {
                Text(
                    text = "গুগল ক্লাউড সিঙ্ক",
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
                                text = "স্ট্যাটাস:",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "✓ ক্লাউড ব্যাকআপ সক্রিয়",
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
                                text = "শেষ সিঙ্ক:",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (lastSyncedTime != null) lastSyncedTime!! else "সিঙ্ক করা হয়নি",
                                fontSize = 12.sp,
                                color = Color(0xFF1E293B),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Sync and Logout Actions
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
                                    text = if (isSyncing) "সিঙ্ক..." else "সিঙ্ক করুন",
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
                                text = "সাইন-আউট",
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
                    Text("বন্ধ করুন", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showDueListDialog) {
        AlertDialog(
            onDismissRequest = { showDueListDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF005FB0),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "মোট বাকির হিসাব",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    if (allDueSales.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "কোনো বাকির হিসাব পাওয়া যায়নি।",
                                fontSize = 14.sp,
                                color = Color(0xFF64748B),
                                fontStyle = FontStyle.Italic
                            )
                        }
                    } else {
                        // Header row of the dialog list
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "গ্রাহকের নাম",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.weight(1.8f)
                            )
                            Text(
                                text = "তারিখ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.weight(1.2f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "টাকা",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                            Spacer(modifier = Modifier.width(40.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(allDueSales, key = { it.id.toString() + "_" + it.timestamp }) { dueItem ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.8f)) {
                                        val displayName = dueItem.customerName.ifEmpty { dueItem.name }
                                        Text(
                                            text = displayName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (dueItem.customerName.isNotEmpty() && dueItem.name.isNotEmpty() && dueItem.name != "বাকি বিক্রি") {
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
                                        text = dueItem.date,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.weight(1.2f),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "৳${String.format(Locale.US, "%.0f", dueItem.price)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC62828),
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.End
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    // Collect due button
                                    Button(
                                        onClick = {
                                            viewModel.collectDueSale(dueItem)
                                            val bakiName = dueItem.customerName.ifEmpty { dueItem.name }
                                            Toast.makeText(
                                                context,
                                                "${bakiName}-এর বাকির টাকা জমা নেওয়া হয়েছে এবং আজকের বিক্রির খাতায় যোগ করা হয়েছে!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier
                                            .height(28.dp)
                                            .testTag("dialog_collect_due_button_${dueItem.id}"),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "জমা",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
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
            },
            confirmButton = {
                TextButton(onClick = { showDueListDialog = false }) {
                    Text("বন্ধ করুন", color = Color(0xFF005FB0), fontWeight = FontWeight.Bold)
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
                    Text("নিশ্চিতকরণ")
                }
            },
            text = { Text("$selectedDate তারিখের সমস্ত বিক্রির ডাটা মুছে যাবে। আপনি কি নিশ্চিত?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearSalesForSelectedDate()
                        showClearConfirm = false
                        Toast.makeText(context, "সমস্ত ডাটা মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("হ্যাঁ, মুছুন", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("বাতিল", color = Color(0xFF64748B))
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
                    Text("নিশ্চিতকরণ")
                }
            },
            text = { Text("এই বিক্রির হিসাবটি মুছে ফেলতে চান?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSale(saleItem.id)
                        itemToDelete = null
                        Toast.makeText(context, "বিক্রি মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("হ্যাঁ, মুছুন", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("বাতিল", color = Color(0xFF64748B))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateCapsuleBadge(
    selectedDateStr: String,
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
        color = Color(0xFFD3E4FF),
        shape = RoundedCornerShape(50.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📅 " + formatDisplayDate(selectedDateStr),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF001C38)
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
                    Text("ঠিক আছে", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("বাতিল")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun SummaryCardsSection(
    totalSales: Double,
    cashSales: Double,
    dueSales: Double,
    onDueClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MinimalSummaryCard(
            title = "মোট বিক্রি",
            amount = totalSales,
            containerColor = Color(0xFFD3E4FF),
            textColor = Color(0xFF001C38),
            modifier = Modifier.weight(1f),
            testTag = "total_sales_card"
        )
        MinimalSummaryCard(
            title = "নগদ",
            amount = cashSales,
            containerColor = Color(0xFFC2F0D5),
            textColor = Color(0xFF063119),
            modifier = Modifier.weight(1f),
            testTag = "cash_sales_card"
        )
        MinimalSummaryCard(
            title = "বাকি",
            amount = dueSales,
            containerColor = Color(0xFFFFDAD6),
            textColor = Color(0xFF410002),
            modifier = Modifier.weight(1f),
            testTag = "due_sales_card",
            onClick = onDueClick
        )
    }
}

@Composable
fun MinimalSummaryCard(
    title: String,
    amount: Double,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .testTag(testTag)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (onClick != null) "$title ⓘ" else title,
                fontSize = 10.sp,
                color = textColor.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "৳${String.format(Locale.US, "%.0f", amount)}",
                fontSize = 16.sp,
                color = textColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SaleTypeDropdown(
    saleType: String,
    onSaleTypeChanged: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isCash = saleType == "cash"
    val displayLabel = if (isCash) "নগদ (Cash)" else "বাকি (Due)"
    
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
                        "নগদ (Cash)", 
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
                        "বাকি (Due)", 
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
fun SaleItemRow(
    sale: SaleItem,
    onDelete: (SaleItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sale_item_card"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text(
                    text = sale.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = sale.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
            }

            Row(
                modifier = Modifier.weight(1.2f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Cash or Due Badge matching Clean Minimalism badges
                val isCash = sale.type == "cash"
                Surface(
                    color = if (isCash) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, if (isCash) Color(0xFF86EFAC) else Color(0xFFFCA5A5)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isCash) "নগদ" else "বাকি",
                        color = if (isCash) Color(0xFF15803D) else Color(0xFFB91C1C),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "৳${String.format(Locale.US, "%.0f", sale.price)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { onDelete(sale) },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_sale_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "মুছে ফেলুন",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CompactSaleItemRow(
    sale: SaleItem,
    onDelete: (SaleItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sale_item_card")
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Name Column (Weight 2)
        Column(modifier = Modifier.weight(2f)) {
            val isDue = sale.type == "due"
            val primaryText = if (isDue && sale.customerName.isNotEmpty()) sale.customerName else sale.name
            Text(
                text = primaryText,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color(0xFF1E293B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isDue && sale.customerName.isNotEmpty() && sale.name.isNotEmpty() && sale.name != "বাকি বিক্রি") {
                Text(
                    text = sale.name,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        // Time Column (Weight 1.1)
        Text(
            text = sale.time,
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1.1f)
        )
        
        // Type Column (Weight 1)
        val isCash = sale.type == "cash"
        Box(
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(Alignment.CenterHorizontally)
        ) {
            Surface(
                color = if (isCash) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                border = BorderStroke(1.dp, if (isCash) Color(0xFF86EFAC) else Color(0xFFFCA5A5)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (isCash) "নগদ" else "বাকি",
                    color = if (isCash) Color(0xFF15803D) else Color(0xFFB91C1C),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        
        // Price Column (Weight 1.3)
        Text(
            text = "৳${String.format(Locale.US, "%.0f", sale.price)}",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFF0F172A),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.3f)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // Delete Action Column
        IconButton(
            onClick = { onDelete(sale) },
            modifier = Modifier
                .size(28.dp)
                .testTag("delete_sale_button")
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "মুছে ফেলুন",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
fun EmptyState() {
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
            text = "এই তারিখে কোনো বিক্রির এন্ট্রি নেই।",
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
    onSubmitSuccess: () -> Unit,
    onSubmitError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemName by viewModel.itemName.collectAsState()
    val customerName by viewModel.customerName.collectAsState()
    val itemPrice by viewModel.itemPrice.collectAsState()
    val saleType by viewModel.saleType.collectAsState()

    // Elegant input form styled as a complete card for clean inline layout
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Row 1: Product Name & Sale Type Choice
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Product Name / Customer Name
                Column(modifier = Modifier.weight(1f)) {
                    val isDue = saleType == "due"
                    Text(
                        text = if (isDue) "গ্রাহকের নাম (বাধ্যতামূলক)" else "পণ্যের নাম",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDue) Color(0xFFC62828) else Color(0xFF64748B),
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = if (isDue) customerName else itemName,
                        onValueChange = { if (isDue) viewModel.onCustomerNameChanged(it) else viewModel.onItemNameChanged(it) },
                        placeholder = { Text(if (isDue) "যেমন: রহিম" else "যেমন: শার্ট", fontSize = 14.sp) },
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

                // Sale Type Dropdown
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "বিক্রির ধরন",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    SaleTypeDropdown(
                        saleType = saleType,
                        onSaleTypeChanged = { viewModel.onSaleTypeChanged(it) }
                    )
                }
            }

            AnimatedVisibility(visible = saleType == "due") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "পণ্যের নাম / বিবরণ (ঐচ্ছিক)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { viewModel.onItemNameChanged(it) },
                        placeholder = { Text("যেমন: শার্ট", fontSize = 14.sp) },
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

            // Row 2: Price Input & Floating Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Price Input
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "টাকার পরিমাণ (৳)",
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

                // Compact Plus FAB button next to price
                Button(
                    onClick = {
                        viewModel.addSale(
                            onSuccess = onSubmitSuccess,
                            onError = onSubmitError
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FB0)),
                    modifier = Modifier
                        .size(50.dp)
                        .testTag("plus_add_button"),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "যোগ করুন",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Row 3: Main Full-width Submit button
            Button(
                onClick = {
                    viewModel.addSale(
                        onSuccess = onSubmitSuccess,
                        onError = onSubmitError
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1B1F)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "বিক্রি নিশ্চিত করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
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
            
            // Tiny green sync active badge
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(if (isSyncing) Color(0xFF3B82F6) else Color(0xFF10B981), CircleShape)
                    .border(1.5.dp, Color.White, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        } else {
            // Stylized 'G' icon
            Text(
                text = "G",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFEA4335)
            )
        }
    }
}
