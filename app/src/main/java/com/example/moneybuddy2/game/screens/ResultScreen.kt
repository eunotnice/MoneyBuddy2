package com.example.moneybuddy2.game.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
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
        // ── Header ────────────────────────────────────────────────────────────
        Text(
            text       = "Simulation results",
            fontSize   = 22.sp,
            fontWeight = FontWeight.Medium,
            color      = InvestIQColors.TextPrimary
        )
        Text(
            text     = "After ${result.input.years} year${if (result.input.years > 1) "s" else ""}",
            fontSize = 14.sp,
            color    = InvestIQColors.TextSecondary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // ── Summary card ──────────────────────────────────────────────────────
        IQCard {
            SectionLabel("Total value at end")
            Text(
                text       = "RM %,.0f".format(result.grandTotal),
                fontSize   = 28.sp,
                fontWeight = FontWeight.Medium,
                color      = InvestIQColors.TealMid,
                modifier   = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    label = "Invested",
                    value = "RM %,.0f".format(result.input.principalAmount),
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    label      = "Returns",
                    value      = "+RM %,.0f".format(result.totalGain),
                    valueColor = InvestIQColors.TealMid,
                    modifier   = Modifier.weight(1f)
                )
                MetricChip(
                    label      = "Gain",
                    value      = "+${"%.1f".format(result.overallGainPercent)}%",
                    valueColor = InvestIQColors.TealMid,
                    modifier   = Modifier.weight(1f)
                )
            }
        }

        // ── Breakdown card ────────────────────────────────────────────────────
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

        // ── Chart card ────────────────────────────────────────────────────────
        IQCard {
            SectionLabel("Growth over time")
            Spacer(Modifier.height(8.dp))
            // Legend
            FlowRow(
                modifier            = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                result.results.forEach { inv ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment     = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    color = investmentColor(inv.type.colorHex),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                        Text(inv.type.displayName, fontSize = 11.sp,
                            color = InvestIQColors.TextSecondary)
                    }
                }
            }
            GrowthLineChart(result = result, modifier = Modifier.fillMaxWidth().height(220.dp))
        }

        // ── Insights ──────────────────────────────────────────────────────────
        state.insights.forEach { insight ->
            InsightCard(text = insight)
        }

        // ── Reset button ──────────────────────────────────────────────────────
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

// ── Line chart drawn with Canvas API ─────────────────────────────────────────

@Composable
private fun GrowthLineChart(result: SimulationResult, modifier: Modifier = Modifier) {
    val density = LocalDensity.current

    Canvas(modifier = modifier) {
        val w      = size.width
        val h      = size.height
        val padL   = 56f
        val padR   = 16f
        val padT   = 16f
        val padB   = 32f
        val chartW = w - padL - padR
        val chartH = h - padT - padB

        val years   = result.input.years
        val allVals = result.results.flatMap { it.yearlyValues }
        val minV    = result.input.principalAmount * 0.9
        val maxV    = (allVals.maxOrNull() ?: minV) * 1.05

        fun xOf(year: Int) = padL + (year.toFloat() / years) * chartW
        fun yOf(v: Double) = padT + ((1.0 - (v - minV) / (maxV - minV)) * chartH).toFloat()

        // Grid lines (3 horizontal)
        val gridPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#22888780")
            strokeWidth = 1f
        }
        for (i in 0..3) {
            val y = padT + (i / 3f) * chartH
            drawContext.canvas.nativeCanvas.drawLine(padL, y, padL + chartW, y, gridPaint)
            val label = "RM %,.0f".format(maxV - (i / 3.0) * (maxV - minV))
                .replace(",000", "k").replace("RM ", "")
            val txtPaint = android.graphics.Paint().apply {
                color     = android.graphics.Color.parseColor("#885F5E5A")
                textSize  = with(density) { 9.sp.toPx() }
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            drawContext.canvas.nativeCanvas.drawText(label, padL - 4f, y + 4f, txtPaint)
        }

        // X-axis labels
        val xLabelPaint = android.graphics.Paint().apply {
            color     = android.graphics.Color.parseColor("#885F5E5A")
            textSize  = with(density) { 9.sp.toPx() }
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val step = maxOf(1, years / 5)
        for (y in 0..years step step) {
            val label = if (y == 0) "Now" else "Yr $y"
            drawContext.canvas.nativeCanvas.drawText(label, xOf(y), h - 4f, xLabelPaint)
        }

        // Principal dashed line
        val dashedPaint = android.graphics.Paint().apply {
            color       = android.graphics.Color.parseColor("#88B4B2A9")
            strokeWidth = 1.5f
            style       = android.graphics.Paint.Style.STROKE
            pathEffect  = android.graphics.DashPathEffect(floatArrayOf(8f, 6f), 0f)
        }
        val principalY = yOf(result.input.principalAmount)
        drawContext.canvas.nativeCanvas.drawLine(
            padL, principalY, padL + chartW, principalY, dashedPaint
        )

        // Investment lines
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
                val py = yOf(v)
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            drawContext.canvas.nativeCanvas.drawPath(path, linePaint)
        }
    }
}

// ── Extension: MetricChip with modifier ──────────────────────────────────────

@Composable
fun MetricChip(label: String, value: String, valueColor: Color = InvestIQColors.TextPrimary, modifier: Modifier = Modifier) {
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