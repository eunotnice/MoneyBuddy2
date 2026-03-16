package com.example.moneybuddy2.game.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.moneybuddy2.game.SimulatorViewModel
import com.example.moneybuddy2.game.AllocationBar
import com.example.moneybuddy2.game.AllocationStatusText
import com.example.moneybuddy2.game.IQCard
import com.example.moneybuddy2.game.InvestIQColors
import com.example.moneybuddy2.game.SectionLabel
import com.example.moneybuddy2.game.investmentColor

@Composable
fun SimulatorScreen(
    viewModel: SimulatorViewModel,
    onSimulate: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate to results when simulation is ready
    LaunchedEffect(state.simulationResult) {
        if (state.simulationResult != null) onSimulate()
    }

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
            text       = "Investment simulator",
            fontSize   = 22.sp,
            fontWeight = FontWeight.Medium,
            color      = InvestIQColors.TextPrimary
        )
        Text(
            text     = "Where does your money grow best?",
            fontSize = 14.sp,
            color    = InvestIQColors.TextSecondary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // ── Amount card ───────────────────────────────────────────────────────
        IQCard {
            SectionLabel("How much are you investing?")
            Text(
                text       = "RM %,.0f".format(state.principalAmount),
                fontSize   = 24.sp,
                fontWeight = FontWeight.Medium,
                color      = InvestIQColors.TextPrimary,
                modifier   = Modifier.padding(vertical = 4.dp)
            )
            Slider(
                value         = ((state.principalAmount - 1_000) / 1_000).toFloat(),
                onValueChange = { viewModel.setAmount((it * 1_000 + 1_000).coerceIn(1000.0F,
                    100000.0F
                )) },
                valueRange    = 0f..99f,
                steps         = 98,
                colors        = SliderDefaults.colors(
                    thumbColor       = InvestIQColors.PurpleMid,
                    activeTrackColor = InvestIQColors.PurpleMid
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("RM 1,000", fontSize = 11.sp, color = InvestIQColors.TextSecondary)
                Text("RM 100,000", fontSize = 11.sp, color = InvestIQColors.TextSecondary)
            }
        }

        // ── Years card ────────────────────────────────────────────────────────
        IQCard {
            SectionLabel("How long will you invest?")
            Text(
                text       = "${state.years} year${if (state.years > 1) "s" else ""}",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Medium,
                color      = InvestIQColors.TextPrimary,
                modifier   = Modifier.padding(vertical = 4.dp)
            )
            Slider(
                value         = (state.years - 1).toFloat(),
                onValueChange = { viewModel.setYears((it + 1).toInt()) },
                valueRange    = 0f..29f,
                steps         = 28,
                colors        = SliderDefaults.colors(
                    thumbColor       = InvestIQColors.PurpleMid,
                    activeTrackColor = InvestIQColors.PurpleMid
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1 year", fontSize = 11.sp, color = InvestIQColors.TextSecondary)
                Text("30 years", fontSize = 11.sp, color = InvestIQColors.TextSecondary)
            }
        }

        // ── Allocation card ───────────────────────────────────────────────────
        IQCard {
            SectionLabel("Allocate your money (must total 100%)")
            Spacer(Modifier.height(4.dp))

            state.allocations.forEach { item ->
                AllocationRow(
                    item      = item,
                    onChanged = { pct -> viewModel.setAllocation(item.type, pct) }
                )
                Spacer(Modifier.height(8.dp))
            }

            // Stacked allocation bar
            AllocationBar(
                segments = state.allocations.map {
                    investmentColor(it.type.colorHex) to it.allocationPercent
                },
                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
            )

            AllocationStatusText(totalAllocated = state.totalAllocated)
        }

        // ── Error ─────────────────────────────────────────────────────────────
        if (state.errorMessage != null) {
            Text(
                text     = state.errorMessage!!,
                fontSize = 12.sp,
                color    = InvestIQColors.WarnText
            )
        }

        // ── CTA button ────────────────────────────────────────────────────────
        Button(
            onClick  = { viewModel.simulate() },
            enabled  = state.allocationIsValid,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = InvestIQColors.PurpleMid,
                contentColor           = InvestIQColors.PurpleLight,
                disabledContainerColor = InvestIQColors.PurpleMid.copy(alpha = 0.4f),
                disabledContentColor   = InvestIQColors.PurpleLight.copy(alpha = 0.6f)
            )
        ) {
            Text("Simulate my investment", fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ── Single allocation row (one investment type) ───────────────────────────────

@Composable
private fun AllocationRow(
    item: com.example.moneybuddy2.game.AllocationItem,
    onChanged: (Int) -> Unit
) {
    val color = investmentColor(item.type.colorHex)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (item.isAllocated) color.copy(alpha = 0.08f)
                else InvestIQColors.SurfaceSecondary
            )
            .then(
                if (item.isAllocated)
                    Modifier.border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                else Modifier
            )
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = item.type.displayName,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color      = InvestIQColors.TextPrimary
                )
                Text(
                    text     = item.type.description,
                    fontSize = 12.sp,
                    color    = InvestIQColors.TextSecondary
                )
                Text(
                    text     = "${"%.1f".format(item.type.annualReturnRate * 100)}% p.a. · ${item.type.riskLabel}",
                    fontSize = 11.sp,
                    color    = color
                )
            }
            Text(
                text       = "${item.allocationPercent}%",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Medium,
                color      = if (item.isAllocated) color else InvestIQColors.TextSecondary
            )
        }
        Slider(
            value         = (item.allocationPercent / 5).toFloat(),
            onValueChange = { onChanged((it.toInt() * 5).coerceIn(0, 100)) },
            valueRange    = 0f..20f,
            steps         = 19,
            modifier      = Modifier.padding(top = 4.dp),
            colors        = SliderDefaults.colors(
                thumbColor            = color,
                activeTrackColor      = color,
                inactiveTrackColor    = color.copy(alpha = 0.2f)
            )
        )
    }
}