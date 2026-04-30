package com.example.moneybuddy2.game

import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color

// ── Brand colours ─────────────────────────────────────────────────────────────
object InvestIQColors {
    val PurpleLight  = Color(0xFFF3EBF7)
    val PurpleMid    = Color(0xFF652F80)
    val PurpleDark   = Color(0xFF4A225E)
    val TealMid      = Color(0xFF1D9E75)
    val TealDark     = Color(0xFF0F6E56)
    val WarnText     = Color(0xFF993C1D)
    val InsightBg    = Color(0xFFF3EBF7)
    val InsightText  = Color(0xFF4A225E)
    val Surface      = Color(0xFFFFFFFF)
    val SurfaceSecondary = Color(0xFFF4F1F6)
    val Border       = Color(0xFFE1DAE5)
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

