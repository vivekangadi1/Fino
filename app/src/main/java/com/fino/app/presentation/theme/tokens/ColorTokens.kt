package com.fino.app.presentation.theme.tokens

import androidx.compose.ui.graphics.Color

// Fino — quiet luxury fintech tokens.
// oklch source values kept as comments; hex is the sRGB approximation committed to code.

// --- Light (warm paper) ---
val PaperLight = Color(0xFFF7F5F0)
val Paper2Light = Color(0xFFEFECE5)
val Paper3Light = Color(0xFFE6E2D8)

val InkLight = Color(0xFF14130F)
val Ink2Light = Color(0xFF3A3833)
val Ink3Light = Color(0xFF6B6862)
val Ink4Light = Color(0xFF9A9690)
val Ink5Light = Color(0xFFCFCBC2)

val CardLight = Color(0xFFFFFFFF)
val CardTintLight = Color(0xFFFBFAF6)

val LineLight = Color(0x14141310)   // rgba(20,19,15,0.08)
val Line2Light = Color(0x24141310)  // rgba(20,19,15,0.14)

// --- Dark (deep paper) ---
val PaperDark = Color(0xFF0D0D0C)
val Paper2Dark = Color(0xFF161614)
val Paper3Dark = Color(0xFF1F1E1B)

val InkDark = Color(0xFFF4F1EA)
val Ink2Dark = Color(0xFFD4D1CA)
val Ink3Dark = Color(0xFF94918A)
val Ink4Dark = Color(0xFF66635D)
val Ink5Dark = Color(0xFF3D3B37)

val CardDark = Color(0xFF161614)
val CardTintDark = Color(0xFF1A1A17)

val LineDark = Color(0x14F4F1EA)
val Line2Dark = Color(0x24F4F1EA)

// --- Accent (botanical green oklch(0.62 0.13 158) light / 0.78 0.13 158 dark) ---
val AccentLight = Color(0xFF4B8A6F)       // oklch(0.62 0.13 158)
val AccentDark = Color(0xFF86D0A5)        // oklch(0.78 0.13 158)
val AccentSoftLight = Color(0xFFDBEBE0)   // oklch(0.92 0.05 158)
val AccentSoftDark = Color(0xFF23332B)    // oklch(0.30 0.06 158)
val AccentInkLight = Color(0xFF214A39)    // oklch(0.32 0.08 158)
val AccentInkDark = Color(0xFFB6E4C3)     // oklch(0.88 0.10 158)

// --- Warn (warm coral oklch(0.66 0.16 45)) ---
val WarnLight = Color(0xFFD97757)
val WarnDark = Color(0xFFE89A7F)
val WarnSoftLight = Color(0xFFF5E5DE)
val WarnSoftDark = Color(0xFF3B2A24)

// --- Negative (deep terracotta oklch(0.55 0.17 28)) ---
val NegativeLight = Color(0xFFC5432F)
val NegativeDark = Color(0xFFDD7F6E)

// --- Positive = Accent ---
val PositiveLight = AccentLight
val PositiveDark = AccentDark

// --- Chart palette (6 tinted neutrals, c1..c6) ---
val C1Light = Color(0xFF4B8A6F)  // oklch(0.62 0.13 158) accent
val C2Light = Color(0xFF6D88B0)  // oklch(0.55 0.10 230) slate blue
val C3Light = Color(0xFFB08B4C)  // oklch(0.65 0.12 75) warm gold
val C4Light = Color(0xFF8C6A9E)  // oklch(0.55 0.13 320) plum
val C5Light = Color(0xFF627384)  // oklch(0.50 0.08 250) steel
val C6Light = Color(0xFF9BA563)  // oklch(0.70 0.10 110) olive

val C1Dark = Color(0xFF86D0A5)
val C2Dark = Color(0xFFA0B9DB)
val C3Dark = Color(0xFFD8B37A)
val C4Dark = Color(0xFFB69CC4)
val C5Dark = Color(0xFF95A7B8)
val C6Dark = Color(0xFFC5CF98)
