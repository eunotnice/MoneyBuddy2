package com.example.moneybuddy2.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneybuddy2.data.model.Expense
import com.example.moneybuddy2.data.model.Income
import com.example.moneybuddy2.ui.viewmodel.HomeUiState
import com.example.moneybuddy2.ui.viewmodel.HomeViewModel
import java.text.NumberFormat
import androidx.compose.foundation.verticalScroll
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
import com.example.moneybuddy2.ui.theme.AppColors

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
    //LaunchedEffect(ui.needsProfileSetup) { if (ui.needsProfileSetup) onOpenProfile() }

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

    var showCarbonInfo by remember { mutableStateOf(false) }

    if (showCarbonInfo) {
        CarbonMethodologySheet(onDismiss = { showCarbonInfo = false })
    }
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
        containerColor = AppColors.Background,
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
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Text(
                            "MoneyBuddy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AppColors.TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenAnalytics) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = "Analytics",
                            tint = AppColors.TextSecondary
                        )
                    }
                    IconButton(onClick = onOpenProfile) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = AppColors.TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Surface)
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
                        color = AppColors.Primary,
                        trackColor = AppColors.PrimaryLight
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
                        Icon(Icons.Default.Warning, contentDescription = null, tint = AppColors.Error)
                        Text(err, color = AppColors.Error, fontSize = 13.sp)
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

//            item {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(12.dp))
//                        .background(AppColors.Surface)
//                        .padding(horizontal = 16.dp, vertical = 12.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(10.dp)
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(36.dp)
//                            .clip(CircleShape)
//                            .background(AppColors.PrimaryLight),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text("🌱", fontSize = 16.sp)
//                    }
//                    Column {
//                        Text(
//                            "Carbon footprint",
//                            fontSize = 12.sp,
//                            color = AppColors.TextSecondary,
//                            fontWeight = FontWeight.Medium
//                        )
//                        Text(
//                            when {
//                                ui.carbonLoading -> "Calculating…"
//                                ui.monthlyCarbonKg != null ->
//                                    "%.2f kgCO₂e this month".format(ui.monthlyCarbonKg)
//                                else -> "No data yet"
//                            },
//                            fontSize = 14.sp,
//                            color = AppColors.TextPrimary,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                        // ── Disclaimer ───────────────────────────────────
//                        if (!ui.carbonLoading && ui.monthlyCarbonKg != null) {
//                            Spacer(Modifier.height(2.dp))
//                            Row(
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.spacedBy(3.dp)
//                            ) {
//                                Icon(
//                                    Icons.Outlined.Info,
//                                    contentDescription = null,
//                                    tint = AppColors.TextSecondary,
//                                    modifier = Modifier.size(11.dp)
//                                )
//                                Text(
//                                    "Based on estimates — actual emissions may vary",
//                                    fontSize = 10.sp,
//                                    color = AppColors.TextSecondary,
//                                    lineHeight = 13.sp
//                                )
//                            }
//                        }
//                    }
//                }
//            }


// ── Inside your LazyColumn ───────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.Surface)
                        .clickable { showCarbonInfo = true }   // whole pill is tappable
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AppColors.PrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌱", fontSize = 16.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Carbon footprint",
                            fontSize   = 12.sp,
                            color      = AppColors.TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            when {
                                ui.carbonLoading       -> "Calculating…"
                                ui.monthlyCarbonKg != null ->
                                    "%.2f kgCO₂e this month".format(ui.monthlyCarbonKg)
                                else                   -> "No data yet"
                            },
                            fontSize   = 14.sp,
                            color      = AppColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!ui.carbonLoading && ui.monthlyCarbonKg != null) {
                            Spacer(Modifier.height(2.dp))
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint     = AppColors.TextSecondary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    "Estimates only · Tap to learn how this is calculated",
                                    fontSize   = 10.sp,
                                    color      = AppColors.TextSecondary,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }

                    // Chevron hint
                    if (!ui.carbonLoading && ui.monthlyCarbonKg != null) {
                        Icon(
                            Icons.Outlined.ChevronRight,
                            contentDescription = "Learn more",
                            tint     = AppColors.TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

//            // ── Quick actions ────────────────────────────────────────────
//            item {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(10.dp)
//                ) {
//                    QuickActionButton(
//                        icon = Icons.Outlined.AddCircle,
//                        label = "Add Expense",
//                        color = AppColors.Primary,
//                        modifier = Modifier.weight(1f),
//                        onClick = onAddExpense
//                    )
//                    QuickActionButton(
//                        icon = Icons.Outlined.Receipt,
//                        label = "Scan Receipt",
//                        color = Color(0xFF4A90E2),
//                        modifier = Modifier.weight(1f),
//                        onClick = onAddReceipt
//                    )
//                    QuickActionButton(
//                        icon = Icons.Outlined.Chat,
//                        label = "AI Chat",
//                        color = AppColors.PurpleDeep,
//                        modifier = Modifier.weight(1f),
//                        onClick = onOpenChat
//                    )
//                    QuickActionButton(
//                        icon = Icons.Outlined.Lightbulb,
//                        label = "Tips",
//                        color = AppColors.PrimaryYellow,
//                        modifier = Modifier.weight(1f),
//                        onClick = onOpenRecommendations
//                    )
//                }
//            }

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
                        color = AppColors.TextPrimary
                    )
                    if (groupedByDate.isNotEmpty()) {
                        val totalCount = groupedByDate.sumOf { it.second.size }
                        Text(
                            "$totalCount entries",
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary
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
                            color = AppColors.TextPrimary
                        )
                        Text(
                            "Tap 'Add Expense' to get started",
                            fontSize = 13.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                }
            }

            // ── Grouped transaction list ─────────────────────────────────
            groupedByDate.forEach { (date, items) ->
                stickyHeader {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppColors.Background
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
                                    .background(AppColors.Primary)
                            )
                            Text(
                                date.format(dateFormatter),
                                style = MaterialTheme.typography.labelLarge,
                                color = AppColors.TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = AppColors.Divider
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
                                listOf(Color(0xFF4A225E), Color(0xFF652F80))
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
//                        Spacer(Modifier.weight(1f))
//                        AssistChip(
//                            onClick = onIncomeDetails,
//                            label = { Text("Details", fontSize = 11.sp) },
//                            colors = AssistChipDefaults.assistChipColors(
//                                containerColor = Color.White.copy(alpha = 0.2f),
//                                labelColor = Color.White
//                            ),
//                            border = null,
//                            modifier = Modifier.height(26.dp)
//                        )
                    }
                }

                // Right: Expenses (amber)
                Box(
                    modifier = Modifier
                        .weight(expenseWeight)
                        .fillMaxHeight()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFFC915), Color(0xFFFF8F00))
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
//                        Spacer(Modifier.weight(1f))
//                        AssistChip(
//                            onClick = onExpenseDetails,
//                            label = { Text("Details", fontSize = 11.sp) },
//                            colors = AssistChipDefaults.assistChipColors(
//                                containerColor = Color.White.copy(alpha = 0.25f),
//                                labelColor = Color.White
//                            ),
//                            border = null,
//                            modifier = Modifier.height(26.dp)
//                        )
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
                        color = AppColors.TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        (if (balance >= 0) "+" else "") + "RM ${fmt.format(abs(balance))}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (balance >= 0) Color(0xFF00875A) else AppColors.Error
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
            label = { Text("Month", fontSize = 13.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary, // Brand Purple #652F80
                unfocusedBorderColor = AppColors.Divider,
                focusedLabelColor = AppColors.Primary,
                cursorColor = AppColors.Primary
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            // Adding this to prevent the "pinkish" tonal tint on the dropdown background
            containerColor = AppColors.Surface,
            tonalElevation = 0.dp
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
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "Previous year",
                            tint = AppColors.Primary // Brand Purple
                        )
                    }
                    Text(
                        displayYear.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    IconButton(
                        onClick = { if (displayYear < now.year) displayYear += 1 },
                        enabled = displayYear < now.year
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Next year",
                            tint = if (displayYear < now.year) AppColors.Primary else AppColors.TextSecondary
                        )
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
                                onClick = { if (enabled) { onSelect(ym); expanded = false } },
                                enabled = enabled,
                                label = {
                                    Text(
                                        month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    // isSelected uses our new light lavender tint
                                    containerColor = if (isSelected) AppColors.PrimaryLight else Color.Transparent,
                                    labelColor = if (isSelected) AppColors.Primary else AppColors.TextPrimary,
                                    disabledLabelColor = AppColors.TextSecondary.copy(alpha = 0.5f)
                                ),
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = enabled,
                                    borderColor = if (isSelected) AppColors.Primary else AppColors.Divider,
                                    disabledBorderColor = AppColors.Divider.copy(alpha = 0.5f),
                                    borderWidth = if (isSelected) 1.5.dp else 1.dp
                                )
                            )
                        }
                    }
                }
            }
        }
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
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
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
                    color = AppColors.TextPrimary
                )
                if (e.merchant.isNotBlank()) {
                    Text(
                        e.merchant,
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary
                    )
                }
                if (e.description.isNotBlank()) {
                    Text(
                        e.description,
                        fontSize = 11.sp,
                        color = AppColors.TextSecondary
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
                            color = AppColors.PrimaryDark
                        )
                    }
                }
            }

            Text(
                "-RM %.2f".format(e.amount),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = AppColors.Error
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
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
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
                    .background(AppColors.PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Text(incomeEmoji(i.category), fontSize = 18.sp)
            }

            Column(Modifier.weight(1f)) {
                Text(
                    i.category,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = AppColors.TextPrimary
                )
                if (i.description.isNotBlank()) {
                    Text(
                        i.description,
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary
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

private val GreenMint     = Color(0xFF00C896)
private val GreenDark     = Color(0xFF009E78)
private val GreenLight    = Color(0xFFE6FBF5)
private val CardWhite     = Color(0xFFFFFFFF)
private val TextPrimary   = Color(0xFF1A1D23)
private val TextSecondary = Color(0xFF6B7280)
private val DividerColor  = Color(0xFFE5E7EB)
private val AmberWarm     = Color(0xFFFFB300)
private val AmberLight    = Color(0xFFFFF4E0)
private val BlueAccent    = Color(0xFF4A90E2)
private val BlueLight     = Color(0xFFEAF2FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarbonMethodologySheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = CardWhite,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Header ───────────────────────────────────────────────────
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌱", fontSize = 20.sp)
                }
                Column {
                    Text(
                        "How your carbon footprint is calculated",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 17.sp,
                        color      = TextPrimary
                    )
                    Text(
                        "Methodology & data sources",
                        fontSize = 13.sp,
                        color    = TextSecondary
                    )
                }
            }

            // ── Important disclaimer ─────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AmberLight)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment     = Alignment.Top
            ) {
                Icon(
                    Icons.Outlined.Warning,
                    contentDescription = null,
                    tint     = AmberWarm,
                    modifier = Modifier.size(18.dp).padding(top = 1.dp)
                )
                Text(
                    "These are estimates, not measurements. Actual emissions depend " +
                            "on your specific vehicle, energy provider, diet, and behaviour. " +
                            "Use these figures as a directional guide, not exact values.",
                    fontSize   = 13.sp,
                    color      = Color(0xFF7A5200),
                    lineHeight = 19.sp
                )
            }

            HorizontalDivider(color = DividerColor)

            // ── How it works overview ────────────────────────────────────
            SectionTitle("How it works")
            Text(
                "Every time you add an expense, MoneyBuddy estimates its carbon " +
                        "footprint based on the category, merchant name, and amount. " +
                        "Different rules apply depending on what type of expense it is.",
                fontSize   = 14.sp,
                color      = TextSecondary,
                lineHeight = 21.sp
            )

            // ── Rule cards ───────────────────────────────────────────────
            SectionTitle("Calculation rules")

            RuleCard(
                emoji      = "⛽",
                title      = "Fuel & Petrol",
                ruleType   = "Physics-based",
                ruleColor  = GreenMint,
                ruleBg     = GreenLight,
                formula    = "RM spent ÷ petrol price per litre × 2.31 kgCO₂e/litre",
                example    = "e.g. RM50 fill-up at RM2.05/L → 24.4L × 2.31 = 56.3 kgCO₂e",
                triggers   = "Category contains 'transport', 'fuel', or 'petrol'; " +
                        "or merchant is Petronas, Shell, Petron, BPetrol, Caltex",
                sources    = listOf(
                    "Petrol emission factor: IPCC AR6 Working Group III (2022)",
                    "Petrol price: configurable in app settings (default: current Malaysian pump price)"
                )
            )

            RuleCard(
                emoji      = "💡",
                title      = "Electricity Bills",
                ruleType   = "Physics-based",
                ruleColor  = BlueAccent,
                ruleBg     = BlueLight,
                formula    = "RM spent ÷ electricity rate per kWh × 0.585 kgCO₂e/kWh",
                example    = "e.g. RM100 bill at RM0.50/kWh → 200 kWh × 0.585 = 117 kgCO₂e",
                triggers   = "Category contains 'bills' or 'electric'; merchant is TNB/Tenaga; " +
                        "or description mentions 'electricity' or 'utility'",
                sources    = listOf(
                    "Malaysia grid emission factor: Suruhanjaya Tenaga (Energy Commission) 2022",
                    "Electricity rate: configurable in app settings"
                )
            )

            RuleCard(
                emoji      = "🚗",
                title      = "Ride-hailing",
                ruleType   = "Spend-based",
                ruleColor  = Color(0xFF9B59B6),
                ruleBg     = Color(0xFFF3EEFF),
                formula    = "RM spent ÷ RM1.75/km × 0.171 kgCO₂e/km",
                example    = "e.g. RM35 Grab ride → ~20 km × 0.171 = 3.4 kgCO₂e",
                triggers   = "Merchant is Grab, MyCar, Maxim, or inDriver; " +
                        "or description mentions 'grab', 'ride', 'taxi', or 'e-hailing'",
                sources    = listOf(
                    "Passenger car emission factor: UK DEFRA 2023 (0.171 kgCO₂e/km, petrol average)",
                    "Fare rate: RM1.75/km (midpoint of Grab/MyCar Malaysian base rates)"
                )
            )

            RuleCard(
                emoji      = "🍔",
                title      = "Food & Dining",
                ruleType   = "Spend-based",
                ruleColor  = Color(0xFFFF6B6B),
                ruleBg     = Color(0xFFFFECEC),
                formula    = "RM spent × 0.033 kgCO₂e/RM",
                example    = "e.g. RM30 meal → 30 × 0.033 = 0.99 kgCO₂e",
                triggers   = "Category contains 'food', 'dining', 'restaurant', or 'cafe'",
                sources    = listOf(
                    "Emission intensity: Eco2 Malaysia food lifecycle study, mixed Malaysian diet average",
                    "Equivalent to ~3.3 kgCO₂e per RM100 spent on food"
                )
            )

            RuleCard(
                emoji      = "🛒",
                title      = "Groceries",
                ruleType   = "Spend-based",
                ruleColor  = GreenDark,
                ruleBg     = GreenLight,
                formula    = "RM spent × 0.025 kgCO₂e/RM",
                example    = "e.g. RM80 grocery run → 80 × 0.025 = 2.0 kgCO₂e",
                triggers   = "Category contains 'groceries' or 'supermarket'; " +
                        "or merchant is Mydin, Giant, Tesco, AEON, Jaya Grocer, " +
                        "Village Grocer, 99 Speedmart, or KK Mart",
                sources    = listOf(
                    "Emission intensity: EXIOBASE 3 supply chain database, adjusted for Malaysia",
                    "Slightly lower than dining as it excludes cooking and service energy"
                )
            )

            RuleCard(
                emoji      = "🛍️",
                title      = "Shopping & Retail",
                ruleType   = "Spend-based",
                ruleColor  = Color(0xFFFF9F43),
                ruleBg     = AmberLight,
                formula    = "RM spent × 0.018 kgCO₂e/RM",
                example    = "e.g. RM200 clothing purchase → 200 × 0.018 = 3.6 kgCO₂e",
                triggers   = "Category contains 'shopping', 'clothing', 'electronics', or 'retail'",
                sources    = listOf(
                    "EXIOBASE 3 global retail goods average (~0.4 kgCO₂e/USD)",
                    "Converted to MYR using purchasing power parity (PPP) adjustment"
                )
            )

            RuleCard(
                emoji      = "🎬",
                title      = "Entertainment",
                ruleType   = "Spend-based",
                ruleColor  = Color(0xFF4A90E2),
                ruleBg     = BlueLight,
                formula    = "RM spent × 0.010 kgCO₂e/RM",
                example    = "e.g. RM60 cinema tickets → 60 × 0.010 = 0.6 kgCO₂e",
                triggers   = "Category contains 'entertainment', 'leisure', or 'recreation'",
                sources    = listOf(
                    "Service sector average emission intensity (EXIOBASE 3)",
                    "Lower than physical goods as most emissions are indirect (venue energy, etc.)"
                )
            )

            RuleCard(
                emoji      = "💸",
                title      = "Everything else",
                ruleType   = "Fallback estimate",
                ruleColor  = TextSecondary,
                ruleBg     = Color(0xFFF0F0F0),
                formula    = "RM spent × 0.008 kgCO₂e/RM",
                example    = "e.g. RM100 miscellaneous → 100 × 0.008 = 0.8 kgCO₂e",
                triggers   = "Any expense that doesn't match a more specific rule above",
                sources    = listOf(
                    "Conservative service-sector lower bound",
                    "Used to ensure no expense shows zero — even unrecognised categories have some footprint"
                )
            )

            HorizontalDivider(color = DividerColor)

            // ── Malaysian benchmark ──────────────────────────────────────
            SectionTitle("Malaysian benchmark")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BlueLight)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment     = Alignment.Top
            ) {
                Icon(Icons.Outlined.Public, null, tint = BlueAccent, modifier = Modifier.size(18.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "The average Malaysian produces ~7,000 kgCO₂e per year " +
                                "(~583 kg/month), based on Our World in Data (2022).",
                        fontSize   = 13.sp,
                        color      = Color(0xFF1A3D6B),
                        lineHeight = 19.sp
                    )
                    Text(
                        "Note: this figure covers all emissions including those not tracked " +
                                "by this app (e.g. flights, government services). Your in-app " +
                                "estimate will typically be lower.",
                        fontSize   = 12.sp,
                        color      = BlueAccent,
                        lineHeight = 18.sp
                    )
                }
            }

            // ── Tree equivalence ─────────────────────────────────────────
            SectionTitle("Tree equivalence")
            Text(
                "The '🌳 trees/year' figure estimates how many trees would need to grow " +
                        "for a full year to absorb your monthly footprint, projected annually.\n\n" +
                        "Formula: (monthly kgCO₂e × 12) ÷ 21 kg/tree/year\n\n" +
                        "The 21 kg/tree/year figure is the IPCC midpoint estimate. " +
                        "Real absorption ranges from ~10 kg (young trees, dry climates) to " +
                        "~48 kg (mature tropical trees) per year.",
                fontSize   = 13.sp,
                color      = TextSecondary,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
}

@Composable
private fun RuleCard(
    emoji: String,
    title: String,
    ruleType: String,
    ruleColor: Color,
    ruleBg: Color,
    formula: String,
    example: String,
    triggers: String,
    sources: List<String>
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(emoji, fontSize = 22.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(ruleBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(ruleType, fontSize = 10.sp, color = ruleColor, fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider(color = DividerColor)

            // Formula
            InfoRow(label = "Formula", value = formula, valueColor = TextPrimary)

            // Example
            InfoRow(label = "Example", value = example, valueColor = GreenDark)

            // Triggers
            InfoRow(label = "Applies when", value = triggers, valueColor = TextSecondary)

            // Sources
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Sources", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                sources.forEach { source ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "•",
                            fontSize = 11.sp,
                            color    = GreenMint,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                        Text(source, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 12.sp, color = valueColor, lineHeight = 17.sp)
    }
}
