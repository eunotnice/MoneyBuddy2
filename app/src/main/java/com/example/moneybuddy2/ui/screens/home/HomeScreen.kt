package com.example.moneybuddy2.ui.screens.home
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneybuddy2.data.model.Expense
import com.example.moneybuddy2.data.model.UserProfile
import com.example.moneybuddy2.di.AppContainer
import com.example.moneybuddy2.ui.viewmodel.HomeUiState
import com.example.moneybuddy2.ui.viewmodel.HomeViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onOpenSettings: () -> Unit,
    onAddExpense: () -> Unit,
    onOpenProfile: () -> Unit,
    onAddReceipt: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenRecommendations: () -> Unit,
    onOpenBot: () -> Unit,
    onOpenAnalytics: () -> Unit
) {

    val ui by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { vm.loadHome() }

    LaunchedEffect(ui.needsProfileSetup) {
        if (ui.needsProfileSetup) onOpenProfile()
    }

    HomeScreenContent(
        ui = ui,
        onMonthSelected = { newMonth ->
            vm.setMonth(newMonth)
        },
        onOpenSettings = onOpenSettings,
        onAddExpense = onAddExpense,
        onOpenProfile = onOpenProfile,
        onAddReceipt = onAddReceipt,
        onOpenChat = onOpenChat,
        onOpenRecommendations = onOpenRecommendations,
        onDeleteExpense = { expenseId -> vm.deleteExpense(expenseId) },
        onOpenBot = onOpenBot,
        onOpenAnalytics = onOpenAnalytics
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    ui: HomeUiState,
    onMonthSelected: (YearMonth) -> Unit,
    onOpenSettings: () -> Unit,
    onAddExpense: () -> Unit,
    onOpenProfile: () -> Unit,
    onAddReceipt: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenRecommendations: () -> Unit,
    onDeleteExpense: (String) -> Unit,
    onOpenBot: () -> Unit,
    onOpenAnalytics: () -> Unit
) {

    Text("### HOME SCREEN CONTENT IS RENDERING ###")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MoneyBuddy") },
                actions = {
                    IconButton(
                        onClick = onOpenAnalytics
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = ""
                        )
                    }
                }

            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpense) { Text("+") }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (ui.loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            if (ui.error != null) Text(ui.error!!, color = MaterialTheme.colorScheme.error)
            //val income = ui.profile?.monthlyIncome ?: 0.0

            IncomeExpenseSummaryCard(
                income = ui.incomeTotal,
                expenses = ui.monthTotal,
                modifier = Modifier.fillMaxWidth(),
                onIncomeDetails = { /* navigate to income list */ },
                onExpenseDetails = { /* navigate to expenses list */ }
            )

            MonthYearDropdown(
                selected = ui.selectedMonth,
                onSelect = onMonthSelected
            )

            Text(
                text = when {
                    ui.carbonLoading -> "This month’s carbon: calculating…"
                    ui.monthlyCarbonKg != null ->
                        "This month’s carbon: %.2f kgCO₂e".format(ui.monthlyCarbonKg)
                    else -> "This month’s carbon: —"
                }
            )
            val zoneId = ZoneId.systemDefault() // or ZoneId.of("Asia/Kuala_Lumpur")
            val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

            val groupedByDate: List<Pair<LocalDate, List<Expense>>> = remember(ui.latestExpenses) {
                ui.latestExpenses
                    .groupBy { e ->
                        Instant.ofEpochMilli(e.dateMillis).atZone(zoneId).toLocalDate()
                    }
                    .toList()
                    .sortedByDescending { (date, _) -> date }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedByDate.forEach { (date, expensesOnDate) ->

                    stickyHeader {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = date.format(dateFormatter),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }
                    }

                    items(
                        items = expensesOnDate,
                        key = { it.id }
                    ) { e ->
                        Card {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        e.merchant.ifBlank { "(No merchant)" },
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text("MYR %.2f • %s".format(e.amount, e.category))
                                    if (e.description.isNotBlank()) {
                                        Text(
                                            e.description,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Text("RM %.2f".format(e.amount))
                                    e.co2eKg?.let {
                                        Text("≈ %.2f kgCO₂e".format(it), style = MaterialTheme.typography.bodySmall)
                                    }

                                }
                                TextButton(onClick = { onDeleteExpense(e.id) }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IncomeExpenseSummaryCard(
    income: Double,
    expenses: Double,
    modifier: Modifier = Modifier,
    currencyLocale: Locale = Locale("en", "MY"),
    onIncomeDetails: () -> Unit = {},
    onExpenseDetails: () -> Unit = {}
) {
    val fmt = NumberFormat.getNumberInstance(currencyLocale).apply { minimumFractionDigits = 2 }
    val balance = income - expenses

    // Optional: make the split proportional to values (comment out if you always want 50/50)
    val total = (income + expenses).coerceAtLeast(1.0)
    val incomeWeight = (income / total).toFloat().coerceIn(0.2f, 0.8f)
    val expenseWeight = (expenses / total).toFloat().coerceIn(0.2f, 0.8f)

    val cardShape = RoundedCornerShape(18.dp)

    Card(
        modifier = modifier,
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp)
        ) {
            val incomeMore = income>expenses
            val expenseMore = expenses > income

            val incomeWeight = when {
                incomeMore -> 0.65f
                expenseMore -> 0.35f
                else -> 0.5f
            }

            val expenseWeight = 1f - incomeWeight

            // Background split (Income | Expenses)
            Row(Modifier.fillMaxSize()) {

                // Left: Income
                Box(
                    modifier = Modifier
                        .weight(incomeWeight)
                        .fillMaxHeight()
                        .background(Color(0xFF5A2D82))
                        .padding(14.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            "Income",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            fmt.format(income),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.weight(1f))
                        AssistChip(
                            onClick = onIncomeDetails,
                            label = { Text("Details") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.White.copy(alpha = 0.25f),
                                labelColor = Color.White
                            ),
                            border = null
                        )
                    }
                }

                // Right: Expenses
                Box(
                    modifier = Modifier
                        .weight(expenseWeight)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFFC107), Color(0xFFFFD54F))
                            )
                        )
                        .padding(14.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxHeight().fillMaxWidth()
                    ) {
                        Text(
                            "Expenses",
                            color = Color.White.copy(alpha = 0.95f),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            fmt.format(expenses),
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.weight(1f))
                        AssistChip(
                            onClick = onExpenseDetails,
                            label = { Text("Details") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.White.copy(alpha = 0.35f),
                                labelColor = Color.White
                            ),
                            border = null
                        )
                    }
                }
            }


            // Center overlay "Balance" pill
            val pillShape = RoundedCornerShape(16.dp)
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(pillShape),
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                shape = pillShape,
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF6B6B6B)
                    )
                    Text(
                        (if (balance >= 0) "+" else "-") + fmt.format(kotlin.math.abs(balance)),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (balance >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthYearDropdown(
    selected: YearMonth,
    onSelect: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var displayYear by remember { mutableIntStateOf(selected.year) }
    val now = YearMonth.now()
    val formatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            value = selected.format(formatter),
            onValueChange = {},
            readOnly = true,
            label = { Text("Month") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Year row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { displayYear -= 1 }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous year")
                    }

                    Text(displayYear.toString(), style = MaterialTheme.typography.titleMedium)

                    IconButton(
                        onClick = { displayYear += 1 },
                        enabled = displayYear < now.year
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next year")
                    }
                }

                // Month grid (3 columns)
                Month.values().asList().chunked(3).forEach { rowMonths ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowMonths.forEach { month ->
                            val ym = YearMonth.of(displayYear, month)
                            val enabled = ym <= now

                            AssistChip(
                                onClick = {
                                    onSelect(ym)
                                    expanded = false
                                },
                                enabled = enabled,
                                label = {
                                    Text(
                                        month.getDisplayName(
                                            TextStyle.SHORT,
                                            Locale.getDefault()
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
//@Preview(showBackground = true)
//@Composable
//private fun PreviewIncomeExpenseSummaryCard() {
//    MaterialTheme {
//        Column(Modifier.padding(16.dp)) {
//            IncomeExpenseSummaryCard(
//                income = 2500.00,
//                expenses = 1200.00,
//                modifier = Modifier.fillMaxWidth()
//            )
//        }
//    }
//}
