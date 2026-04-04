package com.example.moneybuddy2.game.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.moneybuddy2.game.InvestmentType
import com.example.moneybuddy2.game.ResultBarRow
import com.example.moneybuddy2.game.SectionLabel
import com.example.moneybuddy2.game.SimulationResult
import com.example.moneybuddy2.game.SimulatorViewModel
import com.example.moneybuddy2.game.investmentColor


data class RateSource(
    val type: InvestmentType,
    val rateDisplay: String,
    val source: String,
    val note: String
)

val RATE_SOURCES = listOf(
    RateSource(
        type        = InvestmentType.FIXED_DEPOSIT,
        rateDisplay = "~3.8% p.a.",
        source      = "Bank Negara Malaysia (BNM) / major Malaysian banks",
        note        = "Average 12-month FD rate across Maybank, CIMB, Public Bank (2023–2024). " +
                "Rate is guaranteed by PIDM up to RM 250,000."
    ),
    RateSource(
        type        = InvestmentType.BONDS,
        rateDisplay = "~5.5% p.a.",
        source      = "Malaysian Government Securities (MGS) / Bursa Malaysia",
        note        = "Based on average 10-year MGS yield and investment-grade corporate sukuk " +
                "returns. Actual returns vary by bond rating and tenure."
    ),
    RateSource(
        type        = InvestmentType.STOCKS,
        rateDisplay = "~10% p.a.",
        source      = "Bursa Malaysia / FTSE Bursa Malaysia KLCI historical data",
        note        = "Long-run average annual return of global and Malaysian equities (KLCI + " +
                "global indices). Past performance does not guarantee future results. " +
                "Volatility is high — actual year-to-year returns vary widely."
    ),
    RateSource(
        type        = InvestmentType.UNIT_TRUST,
        rateDisplay = "~7.5% p.a.",
        source      = "Securities Commission Malaysia / FIMM",
        note        = "Average annualised return of equity and balanced unit trust funds in " +
                "Malaysia over a 10-year period (Federation of Investment Managers Malaysia)."
    ),
    RateSource(
        type        = InvestmentType.PROPERTY,
        rateDisplay = "~6.5% p.a.",
        source      = "National Property Information Centre (NAPIC) / REHDA",
        note        = "Blended estimate of rental yield (~4%) and capital appreciation (~2.5%) " +
                "based on NAPIC national house price index data. Excludes transaction costs, " +
                "maintenance, and vacancy periods."
    )
)

@Composable
fun IntroScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(InvestIQColors.PageBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Hero
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(InvestIQColors.PurpleLight)
                .padding(20.dp)
        ) {
            Text(
                text       = "InvestIQ",
                fontSize   = 28.sp,
                fontWeight = FontWeight.Medium,
                color      = InvestIQColors.PurpleDark
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text     = "See where your money grows — before you commit a single ringgit.",
                fontSize = 15.sp,
                color    = InvestIQColors.PurpleDark,
                lineHeight = 22.sp
            )
        }

        // How it works
        IQCard {
            Text("How it works", fontSize = 15.sp, fontWeight = FontWeight.Medium,
                color = InvestIQColors.TextPrimary)
            Spacer(Modifier.height(10.dp))

            listOf(
                "1" to "Set your starting amount and how many years you want to invest.",
                "2" to "Drag the sliders to split your money across up to 5 investment types.",
                "3" to "Tap Simulate to see your projected returns, a growth chart, and personalised insights."
            ).forEach { (num, text) ->
                Row(
                    modifier = Modifier.padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(InvestIQColors.PurpleMid),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(num, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                            color = InvestIQColors.PurpleLight)
                    }
                    Text(text, fontSize = 13.sp, color = InvestIQColors.TextSecondary,
                        modifier = Modifier.weight(1f), lineHeight = 19.sp)
                }
            }
        }

        // Return rates & sources
        IQCard {
            Text("Where do the return rates come from?",
                fontSize = 15.sp, fontWeight = FontWeight.Medium,
                color = InvestIQColors.TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(
                "All rates are based on Malaysian market data and are long-run historical averages. " +
                        "They are estimates for educational purposes only — not financial advice.",
                fontSize = 12.sp, color = InvestIQColors.TextSecondary,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(12.dp))

            RATE_SOURCES.forEach { rs ->
                val color = investmentColor(rs.type.colorHex)
                var expanded by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.07f))
                        .clickable { expanded = !expanded }
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(color)
                            )
                            Text(rs.type.displayName, fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = InvestIQColors.TextPrimary)
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(rs.rateDisplay, fontSize = 13.sp,
                                fontWeight = FontWeight.Medium, color = color)
                            Text(if (expanded) "▲" else "▼", fontSize = 10.sp,
                                color = InvestIQColors.TextSecondary)
                        }
                    }
                    if (expanded) {
                        Spacer(Modifier.height(8.dp))
                        Text("Source: ${rs.source}", fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = InvestIQColors.TextSecondary)
                        Spacer(Modifier.height(4.dp))
                        Text(rs.note, fontSize = 11.sp,
                            color = InvestIQColors.TextSecondary, lineHeight = 17.sp)
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }

        // Disclaimer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(InvestIQColors.SurfaceSecondary)
                .padding(12.dp)
        ) {
            Text(
                text = "This simulator uses compound interest to project growth. It does not " +
                        "account for inflation, taxes, fees, or market volatility. Treat results " +
                        "as a learning tool, not a financial forecast.",
                fontSize  = 11.sp,
                color     = InvestIQColors.TextSecondary,
                lineHeight = 17.sp
            )
        }

        Button(
            onClick  = onStart,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = InvestIQColors.PurpleMid,
                contentColor   = InvestIQColors.PurpleLight
            )
        ) {
            Text("Start simulating", fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(16.dp))
    }
}