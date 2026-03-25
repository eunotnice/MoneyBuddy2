package com.example.moneybuddy2.ui.screens.analytics

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneybuddy2.data.model.CategorySlice
import com.example.moneybuddy2.ui.screens.home.MonthYearDropdown
import com.example.moneybuddy2.ui.viewmodel.AnalyticsUiState
import com.example.moneybuddy2.ui.viewmodel.AnalyticsViewModel
import java.time.YearMonth

// ─── Colour tokens ────────────────────────────────────────────────────────────
private val GreenMint     = Color(0xFF00C896)
private val GreenDark     = Color(0xFF009E78)
private val GreenLight    = Color(0xFFE6FBF5)
private val SurfaceGray   = Color(0xFFF7F8FA)
private val CardWhite     = Color(0xFFFFFFFF)
private val TextPrimary   = Color(0xFF1A1D23)
private val TextSecondary = Color(0xFF6B7280)
private val DividerColor  = Color(0xFFE5E7EB)
private val RedSoft       = Color(0xFFE53935)
private val AmberWarm     = Color(0xFFFFB300)
private val BlueAccent    = Color(0xFF4A90E2)
private val PurpleDeep    = Color(0xFF5A2D82)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(vm: AnalyticsViewModel, onBack: () -> Unit) {
    val ui by vm.ui.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        containerColor = SurfaceGray,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Analytics",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardWhite)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Loading bar
            if (ui.loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = GreenMint,
                    trackColor = GreenLight
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Error
                ui.error?.let { err ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFECEC))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Warning, contentDescription = null, tint = RedSoft)
                        Text(err, color = RedSoft, fontSize = 13.sp)
                    }
                }

                // Month picker
                MonthYearDropdown(
                    selected = ui.selectedMonth,
                    onSelect = vm::setMonth,
                    modifier = Modifier.fillMaxWidth()
                )

                // ── Spending overview stat row ────────────────────────────
                SpendingOverviewRow(ui)

                // ── Donut chart + legend ─────────────────────────────────
                if (ui.categoryTotals.isNotEmpty()) {
                    DonutChartCard(ui.categoryTotals)
                }

                // ── Category ranked list ─────────────────────────────────
                if (ui.categoryTotals.isNotEmpty()) {
                    CategoryRankCard(ui.categoryTotals)
                }

                // ── Sustainability card ──────────────────────────────────
                SustainabilityCard(
                    dailyAverage    = ui.dailyAverage,
                    monthlyCarbonKg = ui.monthlyCarbonKg,
                    treesEquivalent = ui.treesEquivalent
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ─── Spending overview: three stat chips in a row ────────────────────────────
@Composable
private fun SpendingOverviewRow(ui: AnalyticsUiState) {
    val pct = ui.pctChangeVsLastMonth
    val changeColor = when {
        pct == null   -> TextSecondary
        pct >= 0      -> RedSoft
        else          -> GreenDark
    }
    val changeText = when {
        pct == null -> "—"
        pct >= 0    -> "+%.1f%%".format(pct)
        else        -> "%.1f%%".format(pct)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatChip(
            modifier     = Modifier.weight(1f),
            label        = "This month",
            value        = "RM %.0f".format(ui.thisMonthTotal),
            valueColor   = TextPrimary,
            icon         = Icons.Outlined.AccountBalanceWallet,
            iconBg       = GreenLight,
            iconTint     = GreenMint
        )
        StatChip(
            modifier     = Modifier.weight(1f),
            label        = "vs Last month",
            value        = changeText,
            valueColor   = changeColor,
            icon         = if ((pct ?: 0.0) >= 0) Icons.Outlined.TrendingUp else Icons.Outlined.TrendingDown,
            iconBg       = if ((pct ?: 0.0) >= 0) Color(0xFFFFECEC) else GreenLight,
            iconTint     = changeColor
        )
        StatChip(
            modifier     = Modifier.weight(1f),
            label        = "Daily avg",
            value        = "RM %.0f".format(ui.dailyAverage),
            valueColor   = BlueAccent,
            icon         = Icons.Outlined.CalendarMonth,
            iconBg       = Color(0xFFEAF2FF),
            iconTint     = BlueAccent
        )
    }
}

@Composable
private fun StatChip(
    modifier: Modifier,
    label: String,
    value: String,
    valueColor: Color,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

// ─── Donut chart card ─────────────────────────────────────────────────────────
@Composable
private fun DonutChartCard(slices: List<CategorySlice>) {
    val filtered = slices.filter { it.amount > 0.0 }
    val total = filtered.sumOf { it.amount }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(title = "Spending by Category", icon = Icons.Outlined.PieChart, iconTint = PurpleDeep, iconBg = Color(0xFFF3EEFF))

            if (filtered.isEmpty()) {
                Text("No expenses this month", color = TextSecondary, fontSize = 13.sp)
                return@Column
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Donut
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutCanvas(slices = filtered, modifier = Modifier.fillMaxSize())
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total", fontSize = 10.sp, color = TextSecondary)
                        Text(
                            "RM %.0f".format(total),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                // Legend
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filtered.take(5).forEach { s ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(s.color)
                            )
                            Text(
                                s.category,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            Text(
                                "${s.percent.toInt()}%",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (filtered.size > 5) {
                        Text(
                            "+${filtered.size - 5} more",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutCanvas(slices: List<CategorySlice>, modifier: Modifier = Modifier) {
    val filtered = slices.filter { it.amount > 0.0 }
    val total = filtered.sumOf { it.amount }
    if (filtered.isEmpty() || total <= 0.0) return

    Canvas(modifier = modifier.padding(8.dp)) {
        var startAngle = -90f
        val strokeWidth = size.minDimension * 0.18f
        val radius = (size.minDimension - strokeWidth) / 2f
        val topLeft = Offset(
            x = center.x - radius,
            y = center.y - radius
        )
        val arcSize = Size(radius * 2, radius * 2)

        filtered.forEach { s ->
            val sweep = ((s.amount / total) * 360.0).toFloat()
            drawArc(
                color      = s.color,
                startAngle = startAngle,
                sweepAngle = sweep - 2f, // small gap between slices
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweep
        }
    }
}

// ─── Category ranked list ─────────────────────────────────────────────────────
@Composable
private fun CategoryRankCard(slices: List<CategorySlice>) {
    val total = slices.sumOf { it.amount }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(title = "Category Breakdown", icon = Icons.Outlined.BarChart, iconTint = BlueAccent, iconBg = Color(0xFFEAF2FF))

            if (slices.isEmpty()) {
                Text("—", color = TextSecondary)
                return@Column
            }

            slices.forEach { s ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(s.color)
                            )
                            Text(s.category, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${s.percent.toInt()}%",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                "RM %.2f".format(s.amount),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    // Animated progress bar
                    AnimatedProgressBar(
                        fraction = (s.amount / total.coerceAtLeast(1.0)).toFloat(),
                        color    = s.color
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedProgressBar(fraction: Float, color: Color) {
    val animFraction by animateFloatAsState(
        targetValue   = fraction.coerceIn(0f, 1f),
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label         = "bar"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animFraction)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

// ─── Sustainability card ──────────────────────────────────────────────────────
@Composable
fun SustainabilityCard(
    dailyAverage: Double,
    monthlyCarbonKg: Double?,
    treesEquivalent: Double?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(title = "Sustainability", icon = Icons.Outlined.Eco, iconTint = GreenDark, iconBg = GreenLight)

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SustainStat(
                    modifier  = Modifier.weight(1f),
                    emoji     = "📅",
                    label     = "Daily avg spend",
                    value     = "RM %.2f".format(dailyAverage),
                    bg        = Color(0xFFEAF2FF),
                )
                SustainStat(
                    modifier  = Modifier.weight(1f),
                    emoji     = "🌍",
                    label     = "Carbon this month",
                    value     = if (monthlyCarbonKg != null) "%.2f kgCO₂e".format(monthlyCarbonKg) else "—",
                    bg        = GreenLight,
                )
                treesEquivalent?.let {
                    SustainStat(
                        modifier = Modifier.weight(1f),
                        emoji    = "🌳",
                        label    = "Trees / year",
                        value    = "%.1f".format(it),
                        bg       = Color(0xFFF0FFF4),
                    )
                }
            }

            // Disclaimer
            if (treesEquivalent != null) {
                Text(
                    "Tree absorption estimate varies by species and environment.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }

            // Recommendations
            if ((monthlyCarbonKg ?: 0.0) > 0.0) {
                HorizontalDivider(color = DividerColor)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "💡 Tips to reduce footprint",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GreenDark
                    )
                    TipRow("Consider public transport or consolidating trips to cut emissions.")
                    TipRow("Set a monthly transport budget to track spending trends.")
                }
            }
        }
    }
}

@Composable
private fun SustainStat(
    modifier: Modifier,
    emoji: String,
    label: String,
    value: String,
    bg: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(emoji, fontSize = 20.sp)
        Text(label, fontSize = 10.sp, color = TextSecondary, textAlign = TextAlign.Center, lineHeight = 14.sp)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary, textAlign = TextAlign.Center)
    }
}

@Composable
private fun TipRow(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(GreenMint)
        )
        Text(text, fontSize = 13.sp, color = TextPrimary, lineHeight = 19.sp)
    }
}

// ─── Shared section header ────────────────────────────────────────────────────
@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
    }
}