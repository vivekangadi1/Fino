package com.fino.app.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fino.app.R

// Variable fonts bundled in res/font/. FontVariation picks the axis value for each weight.

@OptIn(ExperimentalTextApi::class)
val InterTight = FontFamily(
    Font(
        R.font.inter_tight,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.inter_tight,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        R.font.inter_tight,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600))
    ),
    Font(
        R.font.inter_tight,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700))
    ),
    Font(
        R.font.inter_tight_italic,
        weight = FontWeight.Normal,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    )
)

@OptIn(ExperimentalTextApi::class)
val Newsreader = FontFamily(
    Font(
        R.font.newsreader,
        weight = FontWeight.Light,
        variationSettings = FontVariation.Settings(FontVariation.weight(300))
    ),
    Font(
        R.font.newsreader,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.newsreader,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    ),
    Font(
        R.font.newsreader_italic,
        weight = FontWeight.Normal,
        style = FontStyle.Italic,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    )
)

@OptIn(ExperimentalTextApi::class)
val JetBrainsMono = FontFamily(
    Font(
        R.font.jetbrains_mono,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400))
    ),
    Font(
        R.font.jetbrains_mono,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500))
    )
)

// Tabular-figure text feature settings. cv11 = alt 1 variant in Inter.
private const val TabularFeatures = "tnum, cv11"
private val NoPadding = PlatformTextStyle(includeFontPadding = false)

val FinoTypography = Typography(
    // Display — Newsreader serif for hero numbers and editorial amounts.
    displayLarge = TextStyle(
        fontFamily = Newsreader,
        fontWeight = FontWeight.Normal,
        fontSize = 54.sp,
        lineHeight = 60.sp,
        letterSpacing = (-1.62).sp,
        fontFeatureSettings = TabularFeatures,
        platformStyle = NoPadding
    ),
    displayMedium = TextStyle(
        fontFamily = Newsreader,
        fontWeight = FontWeight.Normal,
        fontSize = 42.sp,
        lineHeight = 48.sp,
        letterSpacing = (-1.26).sp,
        fontFeatureSettings = TabularFeatures,
        platformStyle = NoPadding
    ),
    displaySmall = TextStyle(
        fontFamily = Newsreader,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.64).sp,
        fontFeatureSettings = TabularFeatures,
        platformStyle = NoPadding
    ),
    // Headline — Newsreader for section anchors and mid-weight amounts.
    headlineLarge = TextStyle(
        fontFamily = Newsreader,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.56).sp,
        fontFeatureSettings = TabularFeatures,
        platformStyle = NoPadding
    ),
    headlineMedium = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.44).sp,
        platformStyle = NoPadding
    ),
    headlineSmall = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.27).sp,
        platformStyle = NoPadding
    ),
    // Titles — InterTight, mid weight.
    titleLarge = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.34).sp,
        platformStyle = NoPadding
    ),
    titleMedium = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.15).sp,
        platformStyle = NoPadding
    ),
    titleSmall = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.13).sp,
        platformStyle = NoPadding
    ),
    // Body — InterTight, paragraph/cell text.
    bodyLarge = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.15).sp,
        platformStyle = NoPadding
    ),
    bodyMedium = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.14).sp,
        platformStyle = NoPadding
    ),
    bodySmall = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.12).sp,
        platformStyle = NoPadding
    ),
    // Labels — InterTight for buttons/chips; labelSmall switches to JetBrainsMono uppercase for eyebrows.
    labelLarge = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        platformStyle = NoPadding
    ),
    labelMedium = TextStyle(
        fontFamily = InterTight,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
        platformStyle = NoPadding
    ),
    labelSmall = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Medium,
        fontSize = 10.5.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.3.sp,
        platformStyle = NoPadding
    )
)

// Numeric helper — applied to numeric Text values so tabular/alt-1 features turn on.
val NumericStyle = TextStyle(
    fontFeatureSettings = TabularFeatures,
    platformStyle = NoPadding
)

// Explicit editorial serif style for amounts rendered outside Material typography slots.
val SerifHero = TextStyle(
    fontFamily = Newsreader,
    fontWeight = FontWeight.Normal,
    fontSize = 54.sp,
    lineHeight = 60.sp,
    letterSpacing = (-1.62).sp,
    fontFeatureSettings = TabularFeatures,
    platformStyle = NoPadding
)

val SerifMedium = TextStyle(
    fontFamily = Newsreader,
    fontWeight = FontWeight.Normal,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    letterSpacing = (-0.56).sp,
    fontFeatureSettings = TabularFeatures,
    platformStyle = NoPadding
)

// Extra-large Newsreader — used for the Add Transaction amount (design spec: 64sp).
val SerifXL = TextStyle(
    fontFamily = Newsreader,
    fontWeight = FontWeight.Normal,
    fontSize = 64.sp,
    lineHeight = 68.sp,
    letterSpacing = (-1.92).sp,
    fontFeatureSettings = TabularFeatures,
    platformStyle = NoPadding
)

// Small Newsreader — used for currency prefix and .00 decimals on centered amount.
val SerifSm = TextStyle(
    fontFamily = Newsreader,
    fontWeight = FontWeight.Normal,
    fontSize = 22.sp,
    lineHeight = 26.sp,
    letterSpacing = (-0.22).sp,
    fontFeatureSettings = TabularFeatures,
    platformStyle = NoPadding
)

// Eyebrow — uppercase JetBrainsMono for metadata labels (SATURDAY · APR 19, SPENT THIS MONTH).
val EyebrowStyle = TextStyle(
    fontFamily = JetBrainsMono,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 1.6.sp,
    platformStyle = NoPadding
)
