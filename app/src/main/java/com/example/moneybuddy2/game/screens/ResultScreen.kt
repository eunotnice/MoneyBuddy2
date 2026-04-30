package com.example.moneybuddy2.game.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moneybuddy2.game.IQCard
import com.example.moneybuddy2.game.InsightCard
import com.example.moneybuddy2.game.InvestIQColors
import com.example.moneybuddy2.game.ResultBarRow
import com.example.moneybuddy2.game.SectionLabel
import com.example.moneybuddy2.game.SimulationResult
import com.example.moneybuddy2.game.SimulatorViewModel
import com.example.moneybuddy2.game.investmentColor


@Composable
fun ResultScreen(
    viewModel: SimulatorViewModel,
    onReset: () -> Unit
) {
    val state  by viewModel.uiState.collectAsStateWithLifecycle()
    val result = state.simulationResult ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(InvestIQColors.PageBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Simulation results", fontSize = 22.sp, fontWeight = FontWeight.Medium,
            color = InvestIQColors.TextPrimary)
        Text(
            "After ${result.input.years} year${if (result.input.years > 1) "s" else ""}",
            fontSize = 14.sp, color = InvestIQColors.TextSecondary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Summary
        IQCard {
            SectionLabel("Total value at end")
            Text(
                text = "RM %,.0f".format(result.grandTotal),
                fontSize = 28.sp, fontWeight = FontWeight.Medium,
                color = InvestIQColors.TealMid,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip("Invested",
                    "RM %,.0f".format(result.input.principalAmount),
                    modifier = Modifier.weight(1f))
                MetricChip("Returns",
                    "+RM %,.0f".format(result.totalGain),
                    valueColor = InvestIQColors.TealMid,
                    modifier = Modifier.weight(1f))
                MetricChip("Gain",
                    "+${"%.1f".format(result.overallGainPercent)}%",
                    valueColor = InvestIQColors.TealMid,
                    modifier = Modifier.weight(1f))
            }
        }

        // Breakdown
        IQCard {
            SectionLabel("Breakdown by type")
            Spacer(Modifier.height(8.dp))
            val maxFV = result.results.maxOf { it.futureValue }
            result.results.forEach { inv ->
                ResultBarRow(
                    name     = inv.type.displayName,
                    value    = "RM %,.0f".format(inv.futureValue),
                    gainPct  = "+${"%.0f".format(inv.gainPercent)}%",
                    fraction = (inv.futureValue / maxFV).toFloat(),
                    color    = investmentColor(inv.type.colorHex)
                )
                Spacer(Modifier.height(6.dp))
            }
        }

        // Chart — FIXED
        IQCard {
            SectionLabel("Growth over time")
            Spacer(Modifier.height(8.dp))

            // Legend
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement   = Arrangement.spacedBy(4.dp)
            ) {
                result.results.forEach { inv ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(investmentColor(inv.type.colorHex),
                                    RoundedCornerShape(2.dp))
                        )
                        Text(inv.type.displayName, fontSize = 11.sp,
                            color = InvestIQColors.TextSecondary)
                    }
                }
            }

            // Chart — clipped to card, fixed Y direction
            GrowthLineChart(
                result   = result,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(4.dp))   // hard-clips any overflow
            )

            Spacer(Modifier.height(6.dp))
            Text(
                "Projected using compound interest. Not a guarantee of future performance.",
                fontSize  = 10.sp,
                color     = InvestIQColors.TextSecondary,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )
        }

        // Insights
        state.insights.forEach { InsightCard(text = it) }

        // Rate sources reminder
        IQCard {
            Text("About these rates", fontSize = 13.sp, fontWeight = FontWeight.Medium,
                color = InvestIQColors.TextPrimary)
            Spacer(Modifier.height(8.dp))
            result.results.forEach { inv ->
                val rs = RATE_SOURCES.first { it.type == inv.type }
                val color = investmentColor(inv.type.colorHex)
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                    Column {
                        Text("${inv.type.displayName} · ${rs.rateDisplay}",
                            fontSize = 12.sp, fontWeight = FontWeight.Medium,
                            color = InvestIQColors.TextPrimary)
                        Text("Source: ${rs.source}", fontSize = 11.sp,
                            color = InvestIQColors.TextSecondary, lineHeight = 16.sp)
                    }
                }
            }
        }

        OutlinedButton(
            onClick  = { viewModel.reset(); onReset() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.outlinedButtonColors(
                contentColor = InvestIQColors.TextSecondary
            )
        ) {
            Text("Try a different allocation", fontSize = 14.sp)
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Fixed Growth Line Chart — drawn with Canvas
// Key fixes:
//   1. yOf() now maps higher values to SMALLER y (top of canvas)
//   2. padL is wide enough for y-axis labels
//   3. Entire drawing is clipped by the Modifier.clip() on the composable
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GrowthLineChart(result: SimulationResult, modifier: Modifier = Modifier) {
    val density = LocalDensity.current

    Canvas(modifier = modifier) {
        val w      = size.width
        val h      = size.height
        val padL   = 88f
        val padR   = 12f
        val padT   = 12f
        val padB   = 28f
        val chartW = w - padL - padR
        val chartH = h - padT - padB

        val years   = result.input.years
        val allVals = result.results.flatMap { it.yearlyValues }
        val minV    = 0.0
        val maxV    = (allVals.maxOrNull() ?: 1.0) * 1.1

        // Map year → x pixel (left edge = year 0)
        fun xOf(year: Int): Float = padL + (year.toFloat() / years.toFloat()) * chartW

        // Map value → y pixel — HIGHER value = SMALLER y (towards top)
        fun yOf(v: Double): Float {
            val fraction = ((v - minV) / (maxV - minV)).coerceIn(0.0, 1.0)
            return (padT + (1.0 - fraction) * chartH).toFloat()
        }

        val labelTextSize = with(density) { 9.sp.toPx() }

        // ── Horizontal grid lines + y labels ─────────────────────────────────
        val gridPaint = android.graphics.Paint().apply {
            color       = android.graphics.Color.parseColor("#22888780")
            strokeWidth = 1f
            style       = android.graphics.Paint.Style.STROKE
        }
        val yLabelPaint = android.graphics.Paint().apply {
            color     = android.graphics.Color.parseColor("#99888780")
            textSize  = labelTextSize
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
        }
        val gridSteps = 4
        for (i in 0..gridSteps) {
            val fraction = i.toFloat() / gridSteps
            val value    = minV + (1.0 - fraction) * (maxV - minV)  // top=max, bottom=min
            val y        = padT + fraction * chartH

            drawContext.canvas.nativeCanvas.drawLine(padL, y, padL + chartW, y, gridPaint)

            val label = when {
                value >= 1_000_000 -> "RM${"%.1f".format(value / 1_000_000)}M"
                value >= 1_000     -> "RM${"%.0f".format(value / 1_000)}k"
                else               -> "RM%.0f".format(value)
            }

            drawContext.canvas.nativeCanvas.drawText(label, padL - 6f, y + labelTextSize / 3, yLabelPaint)
        }

        // ── X-axis labels ─────────────────────────────────────────────────────
        val xLabelPaint = android.graphics.Paint().apply {
            color       = android.graphics.Color.parseColor("#99888780")
            textSize    = labelTextSize
            textAlign   = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        val step = maxOf(1, years / 5)
        for (y in 0..years step step) {
            val label = if (y == 0) "Now" else "Yr $y"
            drawContext.canvas.nativeCanvas.drawText(
                label, xOf(y), h - 4f, xLabelPaint
            )
        }

        // ── Principal dashed reference line ───────────────────────────────────
        val dashedPaint = android.graphics.Paint().apply {
            color       = android.graphics.Color.parseColor("#66B4B2A9")
            strokeWidth = 1.5f
            style       = android.graphics.Paint.Style.STROKE
            pathEffect  = android.graphics.DashPathEffect(floatArrayOf(8f, 6f), 0f)
        }
        val py = yOf(result.input.principalAmount)
        drawContext.canvas.nativeCanvas.drawLine(padL, py, padL + chartW, py, dashedPaint)

        // ── Investment lines ──────────────────────────────────────────────────
        result.results.forEach { inv ->
            val lineColor = android.graphics.Color.parseColor(inv.type.colorHex)
            val linePaint = android.graphics.Paint().apply {
                color       = lineColor
                strokeWidth = 2.5f
                style       = android.graphics.Paint.Style.STROKE
                isAntiAlias = true
                strokeCap   = android.graphics.Paint.Cap.ROUND
                strokeJoin  = android.graphics.Paint.Join.ROUND
            }
            val path = android.graphics.Path()
            inv.yearlyValues.forEachIndexed { i, v ->
                val px = xOf(i)
                val py2 = yOf(v)
                if (i == 0) path.moveTo(px, py2) else path.lineTo(px, py2)
            }
            drawContext.canvas.nativeCanvas.drawPath(path, linePaint)
        }
    }
}

// ── MetricChip with Modifier support ─────────────────────────────────────────

@Composable
fun MetricChip(
    label: String,
    value: String,
    valueColor: Color = InvestIQColors.TextPrimary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(InvestIQColors.SurfaceSecondary, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(label, fontSize = 11.sp, color = InvestIQColors.TextSecondary)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}