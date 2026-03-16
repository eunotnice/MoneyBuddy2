package com.example.moneybuddy2.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.moneybuddy2.ui.viewmodel.AnalyticsUiState
import com.example.moneybuddy2.data.model.CategorySlice
import com.example.moneybuddy2.ui.viewmodel.ChatViewModel
import com.example.moneybuddy2.data.model.Role
import com.example.moneybuddy2.ui.viewmodel.AnalyticsViewModel
import java.time.YearMonth
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.moneybuddy2.ui.screens.home.MonthYearDropdown
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.collections.chunked
import kotlin.collections.forEach
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(vm: AnalyticsViewModel, onBack: () -> Unit) {
    val ui by vm.ui.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        if (ui.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(padding))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            //MonthPickerRow(selected = ui.selectedMonth, onSelect = vm::setMonth)
            MonthYearDropdown(selected = ui.selectedMonth, onSelect = vm::setMonth)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ){
                CategoryPieCard(ui.categoryTotals)
                SpendingOverviewCard(ui)
            }

            CategoryRankList(ui.categoryTotals)

           // ExpensesThisMonthList(ui.expensesThisMonth)

            SustainabilityCard(
                dailyAverage = ui.dailyAverage,
                monthlyCarbonKg = ui.monthlyCarbonKg,
                treesEquivalent = ui.treesEquivalent
            )
        }
    }
}

@Composable
fun SpendingOverviewCard(ui: AnalyticsUiState) {
    Card (
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ){
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Spending overview", style = MaterialTheme.typography.titleMedium)
            Text("Total: RM %.2f".format(ui.thisMonthTotal), style = MaterialTheme.typography.headlineSmall)

            val pct = ui.pctChangeVsLastMonth
            val changeText = if (pct == null) {
                "Change vs last month: —"
            } else {
                val sign = if (pct >= 0) "+" else ""
                "Change vs last month: $sign%.1f%%".format(pct)
            }
            Text(changeText)
            Text("Last month: RM %.2f".format(ui.lastMonthTotal), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun PieChartCanvas(
    slices: List<CategorySlice>,
    modifier: Modifier = Modifier,
    donut: Boolean = false
){
    val filtered = slices.filter { it.amount > 0.0 }
    val total = filtered.sumOf { it.amount }

    if(filtered.isEmpty() || total <= 0.0){
        return
    }

//    val palette: List<Color> = listOf(
//        Color(0xFFFFC1CC), // pastel pink
//        Color(0xFFFFE0B2), // pastel peach
//        Color(0xFFFFF9C4), // pastel yellow
//        Color(0xFFC8E6C9), // pastel green
//        Color(0xFFB3E5FC), // pastel blue
//        Color(0xFFD1C4E9), // pastel purple
//        Color(0xFFFFCDD2), // soft rose
//        Color(0xFFB2DFDB)  // pastel teal
//    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        var startAngle = -90f

        filtered.forEachIndexed { index, s ->
            val sweep = ((s.amount / total) * 360.0).toFloat()
            //val color = palette[index % palette.size]

            drawArc(
                color = s.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                size = Size(size.width, size.height)     // or just: size = size
            )

            startAngle += sweep
        }
    }
}

@Composable
fun CategoryPieCard(slices: List<CategorySlice>) {
    Card (
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ){
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Categories", style = MaterialTheme.typography.titleMedium)

            val filtered = slices.filter { it.amount > 0.0 }
            if(filtered.isEmpty()){
                Text("No expenses this month")
                return@Column
            }

            PieChartCanvas(
                slices = filtered,
                modifier =Modifier.size(180.dp)
            )

        }
    }
}

@Composable
fun CategoryRankList(slices: List<CategorySlice>) {
    Card (
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ){
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Categories (ranked)", style = MaterialTheme.typography.titleMedium)

            if (slices.isEmpty()) {
                Text("—")
                return@Card
            }

            slices.forEach { s ->

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    s.color,
                                    shape = CircleShape
                                )
                        )

                        Text(
                            text = "${s.category} (${s.percent.toInt()}%)",
                            color = s.color
                        )
                    }

                    Text("RM %.2f".format(s.amount))
                }
            }
        }
    }
}

@Composable
fun SustainabilityCard(
    dailyAverage: Double,
    monthlyCarbonKg: Double?,
    treesEquivalent: Double?
) {
    Card (
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ){
        Column(modifier = Modifier
            .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Sustainability insights", style = MaterialTheme.typography.titleMedium)
            Text("Daily average spending: RM %.2f".format(dailyAverage))

            if (monthlyCarbonKg == null) {
                Text("Carbon estimate: — (no eligible categories yet)")
            } else {
                Text("Estimated carbon (this month): %.2f kgCO₂e".format(monthlyCarbonKg))
                treesEquivalent?.let {
                    Text("Tree equivalent (assumption-based): %.2f trees/year".format(it))
                    Text(
                        "Note: This uses a configurable absorption assumption; it varies by species and environment.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Very simple rule-based recommendations (extend later)
            if ((monthlyCarbonKg ?: 0.0) > 0.0) {
                Text("Recommendations", style = MaterialTheme.typography.titleSmall)
                Text("• Review Transport spending; consider consolidating trips or using public transport when feasible.")
                Text("• Set a monthly transport budget to monitor trends.")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthPickerRow(
    selected: YearMonth,
    onSelect: (YearMonth) -> Unit
) {
    val months = remember {
        val now = YearMonth.now()
        (0..11).map { now.minusMonths(it.toLong()) }
    }

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        TextField(
            value = selected.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Month") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            months.forEach { m ->
                DropdownMenuItem(
                    text = { Text(m.toString()) },
                    onClick = { expanded = false; onSelect(m) }
                )
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


