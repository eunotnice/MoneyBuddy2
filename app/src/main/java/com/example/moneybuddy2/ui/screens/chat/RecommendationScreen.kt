package com.example.moneybuddy2.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneybuddy2.ui.viewmodel.AiRecommendationViewModel

// ─────────────────────────────────────────────
// Colour tokens (adjust to match your theme)
// ─────────────────────────────────────────────
private val GreenMint   = Color(0xFF00C896)
private val GreenLight  = Color(0xFFE6FBF5)
private val AmberWarm   = Color(0xFFFFB347)
private val AmberLight  = Color(0xFFFFF4E0)
private val BlueAccent  = Color(0xFF4A90E2)
private val BlueLight   = Color(0xFFEAF2FF)
private val RedSoft     = Color(0xFFFF6B6B)
private val RedLight    = Color(0xFFFFECEC)
private val PurpleAccent= Color(0xFF9B6DFF)
private val PurpleLight = Color(0xFFF3EEFF)
private val SurfaceGray = Color(0xFFF7F8FA)
private val TextPrimary = Color(0xFF1A1D23)
private val TextSecondary = Color(0xFF6B7280)
private val DividerColor  = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiRecommendationScreen(
    vm: AiRecommendationViewModel,
    onBack: () -> Unit
) {
    val ui by vm.ui.collectAsState()

    var lifestyleNote   by remember { mutableStateOf("") }
    var prioritiesText  by remember { mutableStateOf("") }
    var riskPreference  by remember { mutableStateOf("") }
    var savingGoalNote  by remember { mutableStateOf("") }

    Scaffold(
        containerColor = SurfaceGray,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Smart Recommendations",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Input section ──────────────────────────────────────
            item {
                SectionCard(title = "Tell us about yourself", icon = Icons.Outlined.Person) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StyledTextField(
                            value = lifestyleNote,
                            onValueChange = { lifestyleNote = it },
                            label = "Lifestyle / special requirements",
                            placeholder = "e.g. I support my parents and want to save carefully.",
                            icon = Icons.Outlined.Home
                        )
                        StyledTextField(
                            value = prioritiesText,
                            onValueChange = { prioritiesText = it },
                            label = "Priorities",
                            placeholder = "e.g. emergency fund, family support, transport",
                            icon = Icons.Outlined.Star
                        )
                        StyledTextField(
                            value = riskPreference,
                            onValueChange = { riskPreference = it },
                            label = "Risk preference",
                            placeholder = "e.g. low, moderate, high",
                            icon = Icons.Outlined.Shield
                        )
                        StyledTextField(
                            value = savingGoalNote,
                            onValueChange = { savingGoalNote = it },
                            label = "Financial goal",
                            placeholder = "e.g. build emergency fund first",
                            icon = Icons.Outlined.Flag
                        )
                    }
                }
            }

            // ── Generate button ────────────────────────────────────
            item {
                Button(
                    onClick = {
                        val priorities = prioritiesText
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                        vm.generate(
                            lifestyleNote  = lifestyleNote,
                            priorities     = priorities,
                            riskPreference = riskPreference,
                            savingGoalNote = savingGoalNote
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !ui.loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenMint,
                        contentColor   = Color.White
                    )
                ) {
                    if (ui.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Generating your plan…", fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Generate Smart Plan", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }
            }

            // ── Error ──────────────────────────────────────────────
            ui.error?.let { err ->
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(RedLight)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = RedSoft)
                        Text(err, color = RedSoft, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                }
            }

            // ── Summary ────────────────────────────────────────────
            if (ui.summary.isNotBlank()) {
                item {
                    SectionCard(
                        title = "Overview",
                        icon  = Icons.Outlined.Lightbulb,
                        accentColor = PurpleAccent,
                        accentBg    = PurpleLight
                    ) {
                        Text(
                            text  = ui.summary,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // ── Budget Plan ────────────────────────────────────────
            ui.budgetPlan?.let { plan ->
                item {
                    SectionCard(
                        title = "Budget Plan — ${plan.ruleLabel}",
                        icon  = Icons.Outlined.PieChart,
                        accentColor = BlueAccent,
                        accentBg    = BlueLight
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                            // Income confidence chip
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Chip(
                                    label = "Confidence: ${plan.incomeConfidence}",
                                    color = when {
                                        plan.incomeConfidence.contains("high", ignoreCase = true) -> GreenMint
                                        plan.incomeConfidence.contains("low", ignoreCase = true)  -> RedSoft
                                        else -> AmberWarm
                                    }
                                )
                            }

                            plan.incomeUsed?.let {
                                BudgetRow(
                                    label  = "Monthly Income",
                                    amount = it,
                                    color  = TextPrimary,
                                    isBold = true
                                )
                                HorizontalDivider(color = DividerColor)
                            }

                            plan.needsTarget?.let {
                                BudgetRow(label = "Needs (50%)",    amount = it, color = BlueAccent)
                                BudgetBar(fraction = plan.needsTarget / (plan.incomeUsed ?: 1.0), color = BlueAccent)
                            }
                            plan.wantsTarget?.let {
                                BudgetRow(label = "Wants (30%)",    amount = it, color = AmberWarm)
                                BudgetBar(fraction = plan.wantsTarget / (plan.incomeUsed ?: 1.0), color = AmberWarm)
                            }
                            plan.savingsTarget?.let {
                                BudgetRow(label = "Savings (20%)",  amount = it, color = GreenMint)
                                BudgetBar(fraction = plan.savingsTarget / (plan.incomeUsed ?: 1.0), color = GreenMint)
                            }

                            if (plan.rationale.isNotBlank()) {
                                HorizontalDivider(color = DividerColor)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BlueLight)
                                        .padding(10.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Info,
                                        contentDescription = null,
                                        tint = BlueAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        plan.rationale,
                                        color    = BlueAccent,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Recommendations ────────────────────────────────────
            if (ui.recommendations.isNotEmpty()) {
                item {
                    Text(
                        "Recommendations (${ui.recommendations.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp,
                        color      = TextPrimary
                    )
                }

                items(ui.recommendations) { rec ->
                    val (accent, bg) = priorityColors(rec.priority)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {

                            // Header row: icon + title + priority badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(bg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector        = categoryIcon(rec.category),
                                        contentDescription = null,
                                        tint               = accent,
                                        modifier           = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    rec.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 15.sp,
                                    color      = TextPrimary,
                                    modifier   = Modifier.weight(1f)
                                )
                                Chip(label = priorityLabel(rec.priority), color = accent)
                            }

                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = DividerColor)
                            Spacer(Modifier.height(10.dp))

                            // Message — formatted as bullet points if multi-sentence
                            val sentences = rec.message
                                .split(Regex("(?<=[.!?])\\s+"))
                                .map { it.trim() }
                                .filter { it.isNotBlank() }

                            if (sentences.size > 1) {
                                sentences.forEach { sentence ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(vertical = 3.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 7.dp)
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(accent)
                                        )
                                        Text(sentence, fontSize = 14.sp, color = TextPrimary, lineHeight = 21.sp)
                                    }
                                }
                            } else {
                                Text(rec.message, fontSize = 14.sp, color = TextPrimary, lineHeight = 21.sp)
                            }

                            Spacer(Modifier.height(10.dp))

                            // Category tag
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceGray)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Label,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    rec.category.lowercase()
                                        .replaceFirstChar { it.uppercaseChar() },
                                    fontSize = 12.sp,
                                    color    = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ─────────────────────────────────────────────
// Reusable small composables
// ─────────────────────────────────────────────

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    accentColor: Color = GreenMint,
    accentBg: Color    = GreenLight,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            }
            content()
        }
    }
}

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = Modifier.fillMaxWidth(),
        label         = { Text(label, fontSize = 13.sp) },
        placeholder   = { Text(placeholder, color = TextSecondary, fontSize = 13.sp) },
        leadingIcon   = { Icon(icon, contentDescription = null, tint = GreenMint, modifier = Modifier.size(20.dp)) },
        shape         = RoundedCornerShape(12.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = GreenMint,
            unfocusedBorderColor = DividerColor
        )
    )
}

@Composable
private fun Chip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BudgetRow(label: String, amount: Double, color: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize   = 14.sp,
            color      = if (isBold) TextPrimary else TextSecondary,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            "RM %.2f".format(amount),
            fontSize   = 14.sp,
            color      = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BudgetBar(fraction: Double, color: Color) {
    val safeF = fraction.coerceIn(0.0, 1.0).toFloat()
    val anim by animateFloatAsState(
        targetValue   = safeF,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
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
                .fillMaxWidth(anim)
                .fillMaxHeight()
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

// ─────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────

/** Safely converts Int priority (1=high, 2=medium, 3=low) or String to a display label. */
private fun priorityLabel(priority: Any?): String = when (priority) {
    is Int -> when (priority) { 1 -> "High"; 2 -> "Medium"; 3 -> "Low"; else -> "Normal" }
    else   -> priority?.toString()
        ?.lowercase()
        ?.replaceFirstChar { it.uppercaseChar() }
        ?: "Normal"
}

private fun priorityColors(priority: Any?): Pair<Color, Color> {
    val label = priorityLabel(priority).lowercase()
    return when {
        label.contains("high")   -> RedSoft    to RedLight
        label.contains("medium") -> AmberWarm  to AmberLight
        label.contains("low")    -> GreenMint  to GreenLight
        else                     -> BlueAccent to BlueLight
    }
}

private fun categoryIcon(category: String): ImageVector = when {
    category.contains("food",      ignoreCase = true) -> Icons.Outlined.Restaurant
    category.contains("transport", ignoreCase = true) -> Icons.Outlined.DirectionsCar
    category.contains("saving",    ignoreCase = true) -> Icons.Outlined.Savings
    category.contains("emergency", ignoreCase = true) -> Icons.Outlined.HealthAndSafety
    category.contains("invest",    ignoreCase = true) -> Icons.Outlined.TrendingUp
    category.contains("housing",   ignoreCase = true) -> Icons.Outlined.House
    category.contains("entertain", ignoreCase = true) -> Icons.Outlined.Movie
    category.contains("debt",      ignoreCase = true) -> Icons.Outlined.CreditCard
    else                                              -> Icons.Outlined.Lightbulb
}