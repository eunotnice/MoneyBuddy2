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
import com.example.moneybuddy2.ui.theme.AppColors
import com.example.moneybuddy2.ui.viewmodel.AnalyticsUiState
import com.example.moneybuddy2.ui.viewmodel.AnalyticsViewModel
import com.example.moneybuddy2.ui.viewmodel.InsightCardUi
import com.example.moneybuddy2.ui.viewmodel.InsightSeverity

private val BlueAccent    = Color(0xFF2D74C4)
private val BlueLight     = Color(0xFFEAF2FF)
private val PurpleDeep    = Color(0xFF5A2D82)
private val PurpleLight   = Color(0xFFF3EEFF)
private val AmberWarm     = Color(0xFFE65100)
private val AmberLight    = Color(0xFFFFF3E0)
private val GreenPos      = Color(0xFF1B5E20)
private val GreenPosLight = Color(0xFFF0FFF4)
private val RedWarn       = Color(0xFFB71C1C)
private val RedWarnLight  = Color(0xFFFFF1F1)
private val NeutralLight  = Color(0xFFF7F9FC)
private val NeutralText   = Color(0xFF374151)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(vm: AnalyticsViewModel, onBack: () -> Unit) {
    val ui by vm.ui.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Analytics", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            if (ui.loading) {
                LinearProgressIndicator(
                    modifier   = Modifier.fillMaxWidth(),
                    color      = AppColors.Primary,
                    trackColor = AppColors.PrimaryLight
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ui.error?.let { err ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(RedWarnLight)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Warning, null, tint = AppColors.Error)
                        Text(err, color = AppColors.Error, fontSize = 13.sp)
                    }
                }

                MonthYearDropdown(
                    selected = ui.selectedMonth,
                    onSelect = vm::setMonth,
                    modifier = Modifier.fillMaxWidth()
                )

                SpendingOverviewRow(ui)

                if (ui.insights.isNotEmpty()) {
                    InsightCardsSection(ui.insights)
                }

                if (ui.categoryTotals.isNotEmpty()) {
                    DonutChartCard(ui.categoryTotals)
                }

                if (ui.categoryTotals.isNotEmpty()) {
                    CategoryRankCard(ui.categoryTotals)
                }

                SustainabilityCard(
                    dailyAverage    = ui.dailyAverage,
                    monthlyCarbonKg = ui.monthlyCarbonKg,
                    treesEquivalent = ui.treesEquivalent,
                    carbonTips      = ui.carbonTips
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ─── Spending overview ────────────────────────────────────────────────────────
@Composable
private fun SpendingOverviewRow(ui: AnalyticsUiState) {
    val pct = ui.pctChangeVsLastMonth
    val changeColor = when {
        pct == null -> AppColors.TextSecondary
        pct >= 0    -> AppColors.Error
        else        -> AppColors.PrimaryDark
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
            modifier   = Modifier.weight(1f),
            label      = "This month",
            value      = "RM %.0f".format(ui.thisMonthTotal),
            valueColor = AppColors.TextPrimary,
            icon       = Icons.Outlined.AccountBalanceWallet,
            iconBg     = AppColors.PrimaryLight,
            iconTint   = AppColors.Primary
        )
        StatChip(
            modifier   = Modifier.weight(1f),
            label      = "vs Last month",
            value      = changeText,
            valueColor = changeColor,
            icon       = if ((pct ?: 0.0) >= 0) Icons.Outlined.TrendingUp else Icons.Outlined.TrendingDown,
            iconBg     = if ((pct ?: 0.0) >= 0) RedWarnLight else GreenPosLight,
            iconTint   = changeColor
        )
        StatChip(
            modifier   = Modifier.weight(1f),
            label      = "Daily avg",
            value      = "RM %.0f".format(ui.dailyAverage),
            valueColor = BlueAccent,
            icon       = Icons.Outlined.CalendarMonth,
            iconBg     = BlueLight,
            iconTint   = BlueAccent
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
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(34.dp).clip(CircleShape).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Text(label, fontSize = 11.sp, color = AppColors.TextSecondary)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

// ─── Insight cards ────────────────────────────────────────────────────────────
@Composable
fun InsightCardsSection(insights: List<InsightCardUi>) {
    if (insights.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Insights", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.TextPrimary)
        insights.forEach { InsightCard(it) }
    }
}

private data class InsightStyle(
    val bg: Color, val iconTint: Color, val iconBg: Color,
    val icon: ImageVector, val titleColor: Color
)

@Composable
private fun InsightCard(insight: InsightCardUi) {
    val style = when (insight.severity) {
        InsightSeverity.POSITIVE -> InsightStyle(
            bg         = GreenPosLight,
            iconTint   = GreenPos,
            iconBg     = Color(0xFFDCEDC8),
            icon       = Icons.Outlined.CheckCircle,
            titleColor = GreenPos
        )
        InsightSeverity.WARNING -> InsightStyle(
            bg         = RedWarnLight,
            iconTint   = RedWarn,
            iconBg     = Color(0xFFFFCDD2),
            icon       = Icons.Outlined.Warning,
            titleColor = RedWarn
        )
        InsightSeverity.NEUTRAL -> InsightStyle(
            bg         = NeutralLight,
            iconTint   = NeutralText,
            iconBg     = Color(0xFFE2E8F0),
            icon       = Icons.Outlined.Info,
            titleColor = NeutralText
        )
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = style.bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(style.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(style.icon, null, tint = style.iconTint, modifier = Modifier.size(18.dp))
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    insight.title,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp,
                    color      = style.titleColor
                )
                Text(
                    insight.message,
                    fontSize   = 13.sp,
                    color      = AppColors.TextSecondary,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

// ─── Donut chart card ─────────────────────────────────────────────────────────
@Composable
private fun DonutChartCard(slices: List<CategorySlice>) {
    val filtered = slices.filter { it.amount > 0.0 }
    val total    = filtered.sumOf { it.amount }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader("Spending by Category", Icons.Outlined.PieChart, PurpleDeep, PurpleLight)

            if (filtered.isEmpty()) {
                Text("No expenses this month", color = AppColors.TextSecondary, fontSize = 13.sp)
                return@Column
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                    DonutCanvas(slices = filtered, modifier = Modifier.fillMaxSize())
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total", fontSize = 10.sp, color = AppColors.TextSecondary)
                        Text(
                            "RM %.0f".format(total),
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color      = AppColors.TextPrimary
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filtered.take(5).forEach { s ->
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(10.dp).clip(CircleShape).background(s.color)
                            )
                            Text(s.category, fontSize = 12.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f), maxLines = 1)
                            Text("${s.percent.toInt()}%", fontSize = 12.sp, color = AppColors.TextSecondary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (filtered.size > 5) {
                        Text("+${filtered.size - 5} more", fontSize = 11.sp, color = AppColors.TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutCanvas(slices: List<CategorySlice>, modifier: Modifier = Modifier) {
    val filtered = slices.filter { it.amount > 0.0 }
    val total    = filtered.sumOf { it.amount }
    if (filtered.isEmpty() || total <= 0.0) return

    Canvas(modifier = modifier.padding(8.dp)) {
        var startAngle  = -90f
        val strokeWidth = size.minDimension * 0.18f
        val radius      = (size.minDimension - strokeWidth) / 2f
        val topLeft     = Offset(center.x - radius, center.y - radius)
        val arcSize     = Size(radius * 2, radius * 2)

        filtered.forEach { s ->
            val sweep = ((s.amount / total) * 360.0).toFloat()
            drawArc(
                color      = s.color,
                startAngle = startAngle,
                sweepAngle = sweep - 2f,
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
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader("Category Breakdown", Icons.Outlined.BarChart, BlueAccent, BlueLight)

            if (slices.isEmpty()) {
                Text("—", color = AppColors.TextSecondary); return@Column
            }

            slices.forEachIndexed { index, s ->
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Numbered rank circle
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(s.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${index + 1}",
                                    fontSize   = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = s.color
                                )
                            }
                            Text(s.category, fontSize = 13.sp, color = AppColors.TextPrimary, fontWeight = FontWeight.Medium)
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text("${s.percent.toInt()}%", fontSize = 12.sp, color = AppColors.TextSecondary)
                            Text("RM %.2f".format(s.amount), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                        }
                    }
                    AnimatedProgressBar(
                        fraction = (s.amount / total.coerceAtLeast(1.0)).toFloat(),
                        color    = s.color
                    )
                }
                if (index < slices.lastIndex) {
                    HorizontalDivider(color = AppColors.Divider, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun AnimatedProgressBar(fraction: Float, color: Color) {
    val anim by animateFloatAsState(
        targetValue   = fraction.coerceIn(0f, 1f),
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label         = "bar"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(anim)
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
    treesEquivalent: Double?,
    carbonTips: List<String> = emptyList()
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader("Sustainability", Icons.Outlined.Eco, AppColors.PrimaryDark, AppColors.PrimaryLight)

            // Stat boxes
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SustainStat(Modifier.weight(1f), "📅", "Daily avg", "RM %.2f".format(dailyAverage), BlueLight)
                SustainStat(
                    Modifier.weight(1f), "🌍", "Carbon / month",
                    if (monthlyCarbonKg != null) "%.2f kg".format(monthlyCarbonKg) else "—",
                    AppColors.PrimaryLight
                )
                if (treesEquivalent != null) {
                    SustainStat(Modifier.weight(1f), "🌳", "Trees / year", "%.1f".format(treesEquivalent), Color(0xFFF0FFF4))
                }
            }

            // Estimate disclaimer
            if (monthlyCarbonKg != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AmberLight)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Info, null, tint = AmberWarm, modifier = Modifier.size(13.dp))
                    Text(
                        "Estimates only — based on spend-based emission factors. " +
                                "Tree absorption varies by species and environment.",
                        fontSize   = 11.sp,
                        color      = AmberWarm,
                        lineHeight = 15.sp
                    )
                }
            }

            // Tips
            if (carbonTips.isNotEmpty()) {
                HorizontalDivider(color = AppColors.Divider)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.Lightbulb, null, tint = AppColors.PrimaryDark, modifier = Modifier.size(16.dp))
                        Text(
                            "Tips to reduce your footprint",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = AppColors.PrimaryDark
                        )
                    }
                    carbonTips.forEach { TipRow(it) }
                }
            }
        }
    }
}

@Composable
private fun SustainStat(modifier: Modifier, emoji: String, label: String, value: String, bg: Color) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(bg).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(emoji, fontSize = 20.sp)
        Text(label, fontSize = 10.sp, color = AppColors.TextSecondary, textAlign = TextAlign.Center, lineHeight = 14.sp)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary, textAlign = TextAlign.Center)
    }
}

@Composable
private fun TipRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.PrimaryLight)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.Top
    ) {
        Box(
            modifier = Modifier.padding(top = 5.dp).size(6.dp).clip(CircleShape).background(AppColors.Primary)
        )
        Text(text, fontSize = 13.sp, color = AppColors.TextPrimary, lineHeight = 19.sp)
    }
}

// ─── Shared section header ────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, icon: ImageVector, iconTint: Color, iconBg: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.TextPrimary)
    }
}