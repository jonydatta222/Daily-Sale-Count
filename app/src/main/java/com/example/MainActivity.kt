package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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

    val itemName by viewModel.itemName.collectAsState()
    val itemPrice by viewModel.itemPrice.collectAsState()
    val saleType by viewModel.saleType.collectAsState()

    // Dialog flags
    var showClearConfirm by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<SaleItem?>(null) }

    // Summary calculation
    val totalSales = salesList.sumOf { it.price }
    val cashSales = salesList.filter { it.type == "cash" }.sumOf { it.price }
    val dueSales = salesList.filter { it.type == "due" }.sumOf { it.price }

    Scaffold(
        modifier = modifier.background(Color(0xFFF7F9FC)),
        bottomBar = {
            // Elegant persistent input sheet/form styled according to Clean Minimalism
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Row 1: Product Name & Sale Type Choice
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Product Name
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "পণ্যের নাম",
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
                                    .testTag("item_name_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC)
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
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC)
                                )
                            )
                        }

                        // Compact Plus FAB button next to price
                        Button(
                            onClick = {
                                viewModel.addSale(
                                    onSuccess = {
                                        Toast.makeText(context, "বিক্রি সফলভাবে এন্ট্রি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { errorMsg ->
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                    }
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
                                onSuccess = {
                                    Toast.makeText(context, "বিক্রি সফলভাবে এন্ট্রি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                onError = { errorMsg ->
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                }
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

                    // Clickable date display pill matching Clean Minimalism badge
                    DateCapsuleBadge(
                        selectedDateStr = selectedDate,
                        onDateSelected = { date -> viewModel.onDateSelected(date) }
                    )
                }
            }

            // Tonal Summary Cards Grid Row
            item {
                SummaryCardsSection(
                    totalSales = totalSales,
                    cashSales = cashSales,
                    dueSales = dueSales
                )
            }

            // Entries section header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "সাম্প্রতিক এন্ট্রি",
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

            // List of Sales or Empty placeholder
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
                items(salesList, key = { it.id }) { sale ->
                    SaleItemRow(
                        sale = sale,
                        onDelete = { itemToDelete = sale }
                    )
                }
            }
        }
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
    dueSales: Double
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
            testTag = "due_sales_card"
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
    testTag: String
) {
    Card(
        modifier = modifier.testTag(testTag),
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
                text = title,
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
    val displayLabel = if (saleType == "cash") "নগদ (Cash)" else "বাকি (Due)"

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
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
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF64748B)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            DropdownMenuItem(
                text = { Text("নগদ (Cash)") },
                onClick = {
                    onSaleTypeChanged("cash")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("বাকি (Due)") },
                onClick = {
                    onSaleTypeChanged("due")
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun SaleItemRow(
    sale: SaleItem,
    onDelete: () -> Unit
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
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isCash) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isCash) "নগদ" else "বাকি",
                        color = if (isCash) Color(0xFF15803D) else Color(0xFFB91C1C),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
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
                    onClick = onDelete,
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
