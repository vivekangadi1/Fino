package com.fino.app.presentation.theme

import androidx.compose.ui.graphics.Color

// ===========================================
// FINO MODERN DARK THEME - CRED INSPIRED
// ===========================================

// Dark Background Palette
val DarkBackground = Color(0xFF0D0D0D)       // Near black
val DarkSurface = Color(0xFF1A1A2E)          // Dark purple-blue
val DarkSurfaceVariant = Color(0xFF252542)   // Elevated surface
val DarkSurfaceHigh = Color(0xFF2D2D4A)      // Higher elevation

// Primary Colors (Purple-Blue gradient endpoints)
val PrimaryStart = Color(0xFF667EEA)         // Purple-blue
val PrimaryEnd = Color(0xFF764BA2)           // Deep purple
val Primary = Color(0xFF7C5CFF)              // Main purple

// Secondary Colors (Teal-Green gradient endpoints)
val SecondaryStart = Color(0xFF11998E)       // Teal
val SecondaryEnd = Color(0xFF38EF7D)         // Bright green
val Secondary = Color(0xFF00D9A5)            // Main teal

// Accent / Tertiary
val Accent = Color(0xFFFFD700)               // Gold for XP/rewards
val AccentPink = Color(0xFFFF6B9D)           // Pink accent
val AccentCyan = Color(0xFF00E5FF)           // Cyan accent

// Text Colors
val TextPrimary = Color(0xFFFFFFFF)          // White
val TextSecondary = Color(0xFFB0B0C0)        // Muted grey-purple
val TextTertiary = Color(0xFF6B6B80)         // Dimmed
val TextOnGradient = Color(0xFFFFFFFF)       // Text on gradients

// Status Colors (Vibrant versions)
val Success = Color(0xFF00D9A5)              // Bright teal
val Warning = Color(0xFFFFB800)              // Amber
val Error = Color(0xFFFF5A5A)                // Soft red
val Info = Color(0xFF5C9DFF)                 // Info blue

// Transaction Colors
val ExpenseRed = Color(0xFFFF5A5A)           // Expense/Debit
val IncomeGreen = Color(0xFF00D9A5)          // Income/Credit
val ExpenseRedStart = Color(0xFFFF416C)      // Expense gradient start
val ExpenseRedEnd = Color(0xFFFF4B2B)        // Expense gradient end
val IncomeGreenStart = Color(0xFF11998E)     // Income gradient start
val IncomeGreenEnd = Color(0xFF38EF7D)       // Income gradient end

// Category Colors (Vibrant for dark theme)
val CategoryFood = Color(0xFFFF7043)         // Orange-red
val CategoryTransport = Color(0xFF5C9DFF)    // Blue
val CategoryShopping = Color(0xFFE040FB)     // Purple-pink
val CategoryHealth = Color(0xFF00E676)       // Green
val CategoryEntertainment = Color(0xFFFFD740)// Yellow
val CategoryBills = Color(0xFF78909C)        // Grey-blue
val CategoryEducation = Color(0xFF18FFFF)    // Cyan
val CategoryTravel = Color(0xFFFF80AB)       // Pink
val CategoryGroceries = Color(0xFF69F0AE)    // Light green
val CategoryPersonal = Color(0xFFB388FF)     // Light purple
val CategoryOther = Color(0xFF90A4AE)        // Grey

// Budget Status
val BudgetSafe = Color(0xFF00D9A5)           // Under budget
val BudgetWarning = Color(0xFFFFB800)        // Approaching limit
val BudgetDanger = Color(0xFFFF5A5A)         // Over budget

// Gamification Colors
val XpGold = Color(0xFFFFD700)               // XP points
val XpGoldStart = Color(0xFFFFD700)          // XP gradient start
val XpGoldEnd = Color(0xFFFFA500)            // XP gradient end
val StreakFire = Color(0xFFFF5722)           // Streak fire
val StreakFireStart = Color(0xFFFF416C)      // Streak gradient start
val StreakFireEnd = Color(0xFFFF4B2B)        // Streak gradient end

// Achievement Tiers
val AchievementLocked = Color(0xFF3D3D5C)    // Locked achievement
val AchievementBronze = Color(0xFFCD7F32)    // Bronze tier
val AchievementSilver = Color(0xFFC0C0C0)    // Silver tier
val AchievementGold = Color(0xFFFFD700)      // Gold tier
val AchievementPlatinum = Color(0xFFE5E4E2)  // Platinum tier

// Level Colors
val Level1 = Color(0xFF5C9DFF)               // Beginner blue
val Level2 = Color(0xFF00D9A5)               // Teal
val Level3 = Color(0xFF69F0AE)               // Green
val Level4 = Color(0xFFFFD740)               // Yellow
val Level5 = Color(0xFFFFAB40)               // Orange
val Level6 = Color(0xFFFF5722)               // Deep orange
val Level7 = Color(0xFFE040FB)               // Purple
val Level8 = Color(0xFFFFD700)               // Gold (max)

// Card Gradients (for credit cards)
val CardBlueStart = Color(0xFF667EEA)
val CardBlueEnd = Color(0xFF764BA2)
val CardGoldStart = Color(0xFFFFD700)
val CardGoldEnd = Color(0xFFFF8C00)
val CardPlatinumStart = Color(0xFF8E9AAF)
val CardPlatinumEnd = Color(0xFFDEE2E6)

// Overlay & Shadow
val Overlay = Color(0x80000000)              // 50% black overlay
val GlowPurple = Color(0x407C5CFF)           // Purple glow
val GlowTeal = Color(0x4000D9A5)             // Teal glow

// Dividers & Borders
val Divider = Color(0xFF2D2D4A)              // Subtle divider
val Border = Color(0xFF3D3D5C)               // Card borders

// Legacy support (keeping old names for compatibility)
val FinoPrimary = Primary
val FinoSecondary = Secondary
val FinoAccent = Accent
val FinoLightBackground = Color(0xFFFAFAFA)
val FinoLightSurface = Color(0xFFFFFFFF)
val FinoLightOnBackground = Color(0xFF1C1B1F)
val FinoLightOnSurface = Color(0xFF1C1B1F)
val FinoDarkBackground = DarkBackground
val FinoDarkSurface = DarkSurface
val FinoDarkOnBackground = TextPrimary
val FinoDarkOnSurface = TextPrimary
val FinoOnPrimary = Color.White
val FinoOnSecondary = Color.White
val FinoSuccess = Success
val FinoWarning = Warning
val FinoError = Error
val FinoDebit = ExpenseRed
val FinoCredit = IncomeGreen
