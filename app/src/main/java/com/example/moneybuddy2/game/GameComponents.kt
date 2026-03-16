package com.example.moneybuddy2.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneybuddy2.game.InvestIQColors

// ── Card wrapper ──────────────────────────────────────────────────────────────

@Composable
fun IQCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(InvestIQColors.Surface)
            .border(0.5.dp, InvestIQColors.Border, RoundedCornerShape(12.dp))
            .padding(16.dp),
        content = content
    )
}

// ── Section label ─────────────────────────────────────────────────────────────

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text     = text,
        fontSize = 12.sp,
        color    = InvestIQColors.TextSecondary,
        modifier = modifier.padding(bottom = 4.dp)
    )
}

// ── Metric chip (small stat box) ──────────────────────────────────────────────

@Composable
fun MetricChip(label: String, value: String, valueColor: Color = InvestIQColors.TextPrimary) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(InvestIQColors.SurfaceSecondary)
            .padding(10.dp)
    ) {
        Text(label, fontSize = 11.sp, color = InvestIQColors.TextSecondary)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}

// ── Allocation status label ────────────────────────────────────────────────────

@Composable
fun AllocationStatusText(totalAllocated: Int) {
    val (text, color) = when {
        totalAllocated == 0   -> "0% allocated — drag the sliders below" to InvestIQColors.WarnText
        totalAllocated < 100  -> "$totalAllocated% — need ${100 - totalAllocated}% more" to InvestIQColors.WarnText
        totalAllocated == 100 -> "100% allocated — ready to simulate!" to InvestIQColors.TealDark
        else                  -> "$totalAllocated% — reduce by ${totalAllocated - 100}%" to InvestIQColors.WarnText
    }
    Text(text, fontSize = 12.sp, color = color)
}

// ── Insight card ──────────────────────────────────────────────────────────────

@Composable
fun InsightCard(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(InvestIQColors.InsightBg)
            .padding(12.dp)
    ) {
        Text(text, fontSize = 13.sp, color = InvestIQColors.InsightText)
    }
}

// ── Horizontal stacked allocation bar ────────────────────────────────────────

@Composable
fun AllocationBar(segments: List<Pair<Color, Int>>, modifier: Modifier = Modifier) {
    val total = segments.sumOf { it.second }.coerceAtLeast(1)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(InvestIQColors.SurfaceSecondary)
    ) {
        segments.forEach { (color, pct) ->
            if (pct > 0) {
                Box(
                    modifier = Modifier
                        .weight(pct.toFloat() / total)
                        .fillMaxHeight()
                        .background(color)
                )
            }
        }
        val remainder = 100 - segments.sumOf { it.second }
        if (remainder > 0) {
            Box(
                modifier = Modifier
                    .weight(remainder.toFloat() / total.coerceAtLeast(100))
                    .fillMaxHeight()
                    .background(InvestIQColors.SurfaceSecondary)
            )
        }
    }
}

// ── Result bar row ────────────────────────────────────────────────────────────

@Composable
fun ResultBarRow(
    name: String,
    value: String,
    gainPct: String,
    fraction: Float,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, fontSize = 13.sp, color = InvestIQColors.TextSecondary,
                modifier = Modifier.weight(1f))
            Text(gainPct, fontSize = 11.sp, color = InvestIQColors.TealMid,
                modifier = Modifier.padding(end = 8.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                color = InvestIQColors.TextPrimary)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(InvestIQColors.SurfaceSecondary)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}