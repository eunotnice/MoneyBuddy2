package com.example.moneybuddy2.game

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Brand colours ─────────────────────────────────────────────────────────────
object InvestIQColors {
    val PurpleLight  = Color(0xFFEEEDFE)
    val PurpleMid    = Color(0xFF534AB7)
    val PurpleDark   = Color(0xFF3C3489)
    val TealMid      = Color(0xFF1D9E75)
    val TealDark     = Color(0xFF0F6E56)
    val WarnText     = Color(0xFF993C1D)
    val InsightBg    = Color(0xFFEEEDFE)
    val InsightText  = Color(0xFF3C3489)
    val Surface      = Color(0xFFFFFFFF)
    val SurfaceSecondary = Color(0xFFF1EFE8)
    val Border       = Color(0xFFD3D1C7)
    val TextPrimary  = Color(0xFF1A1A18)
    val TextSecondary = Color(0xFF5F5E5A)
    val PageBg       = Color(0xFFF8F8F6)
}

// ── Investment type → Color ───────────────────────────────────────────────────
fun investmentColor(hex: String): Color = Color(android.graphics.Color.parseColor(hex))

// ── Material3 theme wiring ────────────────────────────────────────────────────
private val LightColors = lightColorScheme(
    primary          = InvestIQColors.PurpleMid,
    onPrimary        = InvestIQColors.PurpleLight,
    primaryContainer = InvestIQColors.PurpleLight,
    onPrimaryContainer = InvestIQColors.PurpleDark,
    background       = InvestIQColors.PageBg,
    surface          = InvestIQColors.Surface,
    onSurface        = InvestIQColors.TextPrimary,
    onSurfaceVariant = InvestIQColors.TextSecondary,
    outline          = InvestIQColors.Border
)

@Composable
fun InvestIQTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography  = Typography(
            headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Medium, color = InvestIQColors.TextPrimary),
            titleMedium    = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = InvestIQColors.TextPrimary),
            bodyMedium     = TextStyle(fontSize = 14.sp, color = InvestIQColors.TextPrimary),
            bodySmall      = TextStyle(fontSize = 12.sp, color = InvestIQColors.TextSecondary),
            labelSmall     = TextStyle(fontSize = 11.sp, color = InvestIQColors.TextSecondary)
        ),
        shapes = Shapes(
            small  = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large  = RoundedCornerShape(16.dp)
        ),
        content = content
    )
}