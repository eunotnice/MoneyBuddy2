package com.example.moneybuddy2.ui.screens.home

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneybuddy2.data.model.Expense
import com.example.moneybuddy2.data.model.Income
import com.example.moneybuddy2.ui.navigation.Routes
import com.example.moneybuddy2.ui.viewmodel.HomeUiState
import com.example.moneybuddy2.ui.viewmodel.HomeViewModel
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import com.example.moneybuddy2.R

// ─── Colour tokens ──────────────────────────────────────────────────────────
private val GreenMint      = Color(0xFF00C896)
private val GreenDark      = Color(0xFF009E78)
private val GreenLight     = Color(0xFFE6FBF5)
private val SurfaceGray    = Color(0xFFF7F8FA)
private val TextPrimary    = Color(0xFF1A1D23)
private val TextSecondary  = Color(0xFF6B7280)
private val DividerColor   = Color(0xFFE5E7EB)
private val RedSoft        = Color(0xFFE53935)
private val AmberWarm      = Color(0xFFFFB300)
private val PurpleDeep     = Color(0xFF5A2D82)
private val CardWhite      = Color(0xFFFFFFFF)

// ─── Sealed class (unchanged) ────────────────────────────────────────────────
sealed class TransactionItem {
    abstract val id: String
    abstract val dataMillis: Long

    data class ExpenseItem(val expense: Expense) : TransactionItem() {
        override val id = expense.id
        override val dataMillis = expense.dateMillis
    }

    data class IncomeItem(val income: Income) : TransactionItem() {
        override val id = income.id
        override val dataMillis = income.dateMillis
    }
}

// ─── Entry point ─────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onOpenSettings: () -> Unit,
    onAddExpense: () -> Unit,
    onOpenProfile: () -> Unit,
    onAddReceipt: () -> Unit,
    onEditExpense: (String) -> Unit,
    onEditIncome: (String) -> Unit,
    onOpenChat: () -> Unit,
    onOpenRecommendations: () -> Unit,
    onOpenBot: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenGame: () -> Unit
) {
    val ui by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { vm.loadHome() }
    LaunchedEffect(ui.needsProfileSetup) { if (ui.needsProfileSetup) onOpenProfile() }

    HomeScreenContent(
        ui = ui,
        onMonthSelected = { vm.setMonth(it) },
        onOpenSettings = onOpenSettings,
        onAddExpense = onAddExpense,
        onOpenProfile = onOpenProfile,
        onAddReceipt = onAddReceipt,
        onOpenChat = onOpenChat,
        onOpenRecommendations = onOpenRecommendations,
        onDeleteExpense = { vm.deleteExpense(it) },
        onOpenBot = onOpenBot,
        onOpenAnalytics = onOpenAnalytics,
        onOpenGame = onOpenGame,
        onEditIncome = onEditIncome,
        onEditExpense = onEditExpense
    )
}

// ─── Content ──────────────────────────────────────────────────────────────────
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
    onOpenAnalytics: () -> Unit,
    onOpenGame: () -> Unit,
    onEditIncome: (String) -> Unit,
    onEditExpense: (String) -> Unit
) {
    val zoneId = ZoneId.systemDefault()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

    val groupedByDate: List<Pair<LocalDate, List<TransactionItem>>> =
        remember(ui.latestExpenses, ui.latestIncome) {
            buildList<TransactionItem> {
                addAll(ui.latestExpenses.map { TransactionItem.ExpenseItem(it) })
                addAll(ui.latestIncome.map { TransactionItem.IncomeItem(it) })
            }
                .groupBy { Instant.ofEpochMilli(it.dataMillis).atZone(zoneId).toLocalDate() }
                .toList()
                .sortedByDescending { (date, _) -> date }
        }

    Scaffold(
        containerColor = SurfaceGray,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = null,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                        Text(
                            "MoneyBuddy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenAnalytics) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = "Analytics",
                            tint = TextSecondary
                        )
                    }
                    IconButton(onClick = onOpenProfile) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardWhite)
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Loading / Error ──────────────────────────────────────────
            if (ui.loading) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = GreenMint,
                        trackColor = GreenLight
                    )
                }
            }
            ui.error?.let { err ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFECEC))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = RedSoft)
                        Text(err, color = RedSoft, fontSize = 13.sp)
                    }
                }
            }

            // ── Month picker ─────────────────────────────────────────────
            item {
                MonthYearDropdown(
                    selected = ui.selectedMonth,
                    onSelect = onMonthSelected,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── Income / Expense summary card (preserved, restyled) ──────
            item {
                IncomeExpenseSummaryCard(
                    income = ui.incomeTotal,
                    expenses = ui.monthTotal,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── Carbon footprint pill ────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardWhite)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌱", fontSize = 16.sp)
                    }
                    Column {
                        Text(
                            "Carbon footprint",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            when {
                                ui.carbonLoading -> "Calculating…"
                                ui.monthlyCarbonKg != null ->
                                    "%.2f kgCO₂e this month".format(ui.monthlyCarbonKg)
                                else -> "No data yet"
                            },
                            fontSize = 14.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ── Quick actions ────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Outlined.AddCircle,
                        label = "Add Expense",
                        color = GreenMint,
                        modifier = Modifier.weight(1f),
                        onClick = onAddExpense
                    )
                    QuickActionButton(
                        icon = Icons.Outlined.Receipt,
                        label = "Scan Receipt",
                        color = Color(0xFF4A90E2),
                        modifier = Modifier.weight(1f),
                        onClick = onAddReceipt
                    )
                    QuickActionButton(
                        icon = Icons.Outlined.Chat,
                        label = "AI Chat",
                        color = PurpleDeep,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenChat
                    )
                    QuickActionButton(
                        icon = Icons.Outlined.Lightbulb,
                        label = "Tips",
                        color = AmberWarm,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenRecommendations
                    )
                }
            }

            // ── Transactions header ──────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Transactions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    if (groupedByDate.isNotEmpty()) {
                        val totalCount = groupedByDate.sumOf { it.second.size }
                        Text(
                            "$totalCount entries",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // ── Empty state ──────────────────────────────────────────────
            if (groupedByDate.isEmpty() && !ui.loading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💸", fontSize = 40.sp)
                        Text(
                            "No transactions yet",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Text(
                            "Tap 'Add Expense' to get started",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // ── Grouped transaction list ─────────────────────────────────
            groupedByDate.forEach { (date, items) ->
                stickyHeader {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = SurfaceGray
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(GreenMint)
                            )
                            Text(
                                date.format(dateFormatter),
                                style = MaterialTheme.typography.labelLarge,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = DividerColor
                            )
                        }
                    }
                }

                items(items = items, key = { it.id }) { item ->
                    when (item) {
                        is TransactionItem.ExpenseItem ->
                            ExpenseCard(
                                item.expense,
                                onClick = { expense ->
                                    onEditExpense(expense.id)
                                }
                            )

                        is TransactionItem.IncomeItem ->
                            IncomeCard(
                                item.income,
                                onClick = { income ->
                                    onEditIncome(income.id)
                                }
                            )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// ─── Income / Expense summary card (preserved layout, polished style) ─────────
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

    val incomeWeight = when {
        income > expenses -> 0.65f
        expenses > income -> 0.35f
        else -> 0.5f
    }
    val expenseWeight = 1f - incomeWeight

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            Row(Modifier.fillMaxSize()) {

                // Left: Income (purple)
                Box(
                    modifier = Modifier
                        .weight(incomeWeight)
                        .fillMaxHeight()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF6A3D9A), Color(0xFF5A2D82))
                            )
                        )
                        .padding(14.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.TrendingUp,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                "Income",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Text(
                            "RM ${fmt.format(income)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.weight(1f))
                        AssistChip(
                            onClick = onIncomeDetails,
                            label = { Text("Details", fontSize = 11.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                labelColor = Color.White
                            ),
                            border = null,
                            modifier = Modifier.height(26.dp)
                        )
                    }
                }

                // Right: Expenses (amber)
                Box(
                    modifier = Modifier
                        .weight(expenseWeight)
                        .fillMaxHeight()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFFB300), Color(0xFFFF8F00))
                            )
                        )
                        .padding(14.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.TrendingDown,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                "Expenses",
                                color = Color.White.copy(alpha = 0.95f),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Text(
                            "RM ${fmt.format(expenses)}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.weight(1f))
                        AssistChip(
                            onClick = onExpenseDetails,
                            label = { Text("Details", fontSize = 11.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.White.copy(alpha = 0.25f),
                                labelColor = Color.White
                            ),
                            border = null,
                            modifier = Modifier.height(26.dp)
                        )
                    }
                }
            }

            // Centre balance pill (unchanged concept, cleaner style)
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(20.dp)),
                color = Color.White,
                shadowElevation = 4.dp,
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        (if (balance >= 0) "+" else "") + "RM ${fmt.format(abs(balance))}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (balance >= 0) Color(0xFF00875A) else RedSoft
                    )
                }
            }
        }
    }
}

// ─── Month / Year dropdown (unchanged logic, same style) ─────────────────────
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
            label = { Text("Month", fontSize = 13.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenMint,
                unfocusedBorderColor = DividerColor
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { displayYear -= 1 }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous year")
                    }
                    Text(
                        displayYear.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { displayYear += 1 },
                        enabled = displayYear < now.year
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next year")
                    }
                }

                Month.values().asList().chunked(3).forEach { rowMonths ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowMonths.forEach { month ->
                            val ym = YearMonth.of(displayYear, month)
                            val isSelected = ym == selected
                            val enabled = ym <= now

                            AssistChip(
                                onClick = { onSelect(ym); expanded = false },
                                enabled = enabled,
                                label = {
                                    Text(
                                        month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSelected) GreenLight else Color.Transparent,
                                    labelColor = if (isSelected) GreenDark else TextPrimary
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = true,
                                    borderColor = if (isSelected) GreenMint else DividerColor
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Quick action button ──────────────────────────────────────────────────────
@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardWhite)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
        }
        Text(
            label,
            fontSize = 10.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

// ─── Expense card ─────────────────────────────────────────────────────────────
@Composable
fun ExpenseCard(e: Expense, onClick: (Expense) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(e) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category icon circle
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFECEC)),
                contentAlignment = Alignment.Center
            ) {
                Text(categoryEmoji(e.category), fontSize = 18.sp)
            }

            Column(Modifier.weight(1f)) {
                Text(
                    e.category,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                if (e.merchant.isNotBlank()) {
                    Text(
                        e.merchant,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                if (e.description.isNotBlank()) {
                    Text(
                        e.description,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                e.co2eKg?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text("🌱", fontSize = 10.sp)
                        Text(
                            "%.2f kgCO₂e".format(it),
                            fontSize = 11.sp,
                            color = GreenDark
                        )
                    }
                }
            }

            Text(
                "-RM %.2f".format(e.amount),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = RedSoft
            )
        }
    }
}

// ─── Income card ──────────────────────────────────────────────────────────────
@Composable
fun IncomeCard(i: Income, onClick: (Income) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(i) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(GreenLight),
                contentAlignment = Alignment.Center
            ) {
                Text(incomeEmoji(i.category), fontSize = 18.sp)
            }

            Column(Modifier.weight(1f)) {
                Text(
                    i.category,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                if (i.description.isNotBlank()) {
                    Text(
                        i.description,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Text(
                "+RM %.2f".format(i.amount),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF00875A)
            )
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
private fun categoryEmoji(category: String): String = when {
    category.contains("food",       ignoreCase = true) -> "🍔"
    category.contains("transport",  ignoreCase = true) -> "🚗"
    category.contains("grocery",    ignoreCase = true) -> "🛒"
    category.contains("shopping",   ignoreCase = true) -> "🛍️"
    category.contains("health",     ignoreCase = true) -> "💊"
    category.contains("entertain",  ignoreCase = true) -> "🎬"
    category.contains("bill",       ignoreCase = true) -> "📄"
    category.contains("utility",    ignoreCase = true) -> "💡"
    category.contains("education",  ignoreCase = true) -> "📚"
    category.contains("travel",     ignoreCase = true) -> "✈️"
    else -> "💸"
}

private fun incomeEmoji(category: String): String = when {
    category.contains("salary",    ignoreCase = true) -> "💼"
    category.contains("freelance", ignoreCase = true) -> "💻"
    category.contains("invest",    ignoreCase = true) -> "📈"
    category.contains("bonus",     ignoreCase = true) -> "🎉"
    category.contains("gift",      ignoreCase = true) -> "🎁"
    else -> "💰"
}