package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.Category
import com.fino.app.domain.model.Transaction
import com.fino.app.presentation.components.primitives.Eyebrow
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.ReviewUncategorizedViewModel
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewUncategorizedScreen(
    onNavigateBack: () -> Unit,
    onManageMappings: () -> Unit = {},
    viewModel: ReviewUncategorizedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentIndex by remember { mutableStateOf(0) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(uiState.categorizeSuccess) {
        if (uiState.categorizeSuccess) {
            viewModel.clearSuccess()
            selectedCategoryId = null
            if (currentIndex >= uiState.uncategorizedTransactions.size) {
                currentIndex = 0
            }
        }
    }

    val transactions = uiState.uncategorizedTransactions
    val total = transactions.size
    val transaction = transactions.getOrNull(currentIndex)

    Scaffold(containerColor = FinoColors.paper()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = FinoColors.ink3(),
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(onClick = onNavigateBack)
                        .padding(6.dp)
                )
                Text(
                    text = if (total > 0) "Review · ${currentIndex + 1} of $total" else "Review",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = FinoColors.ink()
                )
                Text(
                    text = "Mappings",
                    fontSize = 12.sp,
                    color = FinoColors.ink3(),
                    modifier = Modifier
                        .clickable(onClick = onManageMappings)
                        .padding(8.dp)
                )
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = FinoColors.accentColor())
                    }
                }
                transaction == null -> {
                    AllCaughtUpState(onBack = onNavigateBack, modifier = Modifier.fillMaxSize())
                }
                else -> {
                    val suggestions = computeSuggestions(transaction, uiState.categories)
                    if (selectedCategoryId == null) {
                        selectedCategoryId = suggestions.firstOrNull()?.category?.id
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 12.dp, bottom = 20.dp)
                    ) {
                        TransactionCard(transaction = transaction)

                        Spacer(Modifier.height(22.dp))
                        Eyebrow(text = "Most likely")
                        Spacer(Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            suggestions.take(3).forEach { sug ->
                                SuggestRow(
                                    emoji = sug.category.emoji,
                                    category = sug.category.name,
                                    subtitle = sug.subtitle,
                                    confidence = sug.confidence,
                                    selected = sug.category.id == selectedCategoryId,
                                    onClick = { selectedCategoryId = sug.category.id }
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PrimaryBtn(
                                text = "Skip",
                                primary = false,
                                enabled = true,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    currentIndex = (currentIndex + 1).coerceAtMost(total - 1)
                                    selectedCategoryId = null
                                }
                            )
                            val confirmText = uiState.categories
                                .find { it.id == selectedCategoryId }
                                ?.name
                                ?.let { "Confirm $it" }
                                ?: "Confirm"
                            PrimaryBtn(
                                text = confirmText,
                                primary = true,
                                enabled = selectedCategoryId != null,
                                modifier = Modifier.weight(1.4f),
                                onClick = {
                                    val catId = selectedCategoryId ?: return@PrimaryBtn
                                    viewModel.categorizeTransaction(
                                        transaction = transaction,
                                        categoryId = catId,
                                        applyToAllFromMerchant = true
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionCard(transaction: Transaction) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, h:mm a") }

    val sender = transaction.smsSender ?: transaction.bankName ?: "Manual"
    val sourceLabel = when (transaction.source.name) {
        "SMS" -> "From SMS · $sender"
        else -> "From ${transaction.source.name.lowercase()} · $sender"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(FinoColors.card())
            .border(1.dp, FinoColors.line(), RoundedCornerShape(18.dp))
            .padding(22.dp)
    ) {
        Eyebrow(text = sourceLabel)
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = transaction.merchantName,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                color = FinoColors.ink(),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "−${currencyFormatter.format(transaction.amount)}",
                style = SerifSm.copy(
                    color = FinoColors.ink(),
                    fontFeatureSettings = "tnum, cv11"
                )
            )
        }

        val subLine = buildString {
            append(transaction.transactionDate.format(dateFormatter))
            transaction.cardLastFour?.let { append(" · Card $it") }
                ?: transaction.paymentMethod?.let { append(" · $it") }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = subLine,
            fontSize = 12.sp,
            color = FinoColors.ink3()
        )

        val quote = transaction.rawSmsBody
        if (!quote.isNullOrBlank()) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(FinoColors.paper2())
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "\"${quote.trim()}\"",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = FinoColors.ink3(),
                    fontFamily = JetBrainsMono
                )
            }
        }
    }
}

@Composable
private fun SuggestRow(
    emoji: String,
    category: String,
    subtitle: String,
    confidence: Float,
    selected: Boolean,
    onClick: () -> Unit
) {
    val top = confidence > 0.7f || selected
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (top) FinoColors.accentSoft() else FinoColors.card())
            .border(
                1.dp,
                if (top) FinoColors.accentColor().copy(alpha = 0.3f) else FinoColors.line(),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(FinoColors.paper2()),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                color = FinoColors.ink(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = FinoColors.ink3(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.width(60.dp),
            horizontalAlignment = Alignment.End
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(FinoColors.paper3())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(confidence.coerceIn(0.05f, 1f))
                        .fillMaxHeight()
                        .background(if (top) FinoColors.accentColor() else FinoColors.ink4())
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${(confidence * 100).toInt()}%",
                style = androidx.compose.ui.text.TextStyle(
                    fontSize = 10.sp,
                    color = FinoColors.ink3(),
                    fontFeatureSettings = "tnum, cv11",
                    fontFamily = InterTight
                )
            )
        }
    }
}

@Composable
private fun PrimaryBtn(
    text: String,
    primary: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (primary) FinoColors.ink() else Color.Transparent
    val fg = if (primary) FinoColors.paper() else FinoColors.ink2()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bg.copy(alpha = if (enabled) 1f else 0.5f))
            .then(
                if (!primary) Modifier.border(1.dp, FinoColors.line2(), RoundedCornerShape(100.dp))
                else Modifier
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AllCaughtUpState(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "All caught up", style = SerifMedium, color = FinoColors.ink())
            Spacer(Modifier.height(8.dp))
            Text(
                text = "No uncategorized transactions",
                fontSize = 13.sp,
                color = FinoColors.ink3()
            )
            Spacer(Modifier.height(24.dp))
            TextButton(onClick = onBack) {
                Text("Go back", color = FinoColors.accentInk(), fontWeight = FontWeight.Medium)
            }
        }
    }
}

private data class Suggestion(
    val category: Category,
    val confidence: Float,
    val subtitle: String
)

/**
 * Simple heuristic: score categories by keyword presence in merchant name.
 * Returns sorted top suggestions — the selection is just surfaced UX,
 * the full category list remains available via Mappings.
 */
private fun computeSuggestions(
    transaction: Transaction,
    categories: List<Category>
): List<Suggestion> {
    if (categories.isEmpty()) return emptyList()

    val name = transaction.merchantName.lowercase()
    val keywordMap = mapOf(
        "food" to listOf("swiggy", "zomato", "restaurant", "cafe", "coffee", "tokai", "pizza", "burger", "eat"),
        "transport" to listOf("uber", "ola", "rapido", "metro", "petrol", "fuel", "fastag", "taxi", "auto"),
        "shopping" to listOf("amazon", "flipkart", "myntra", "ajio", "mall", "store", "shop"),
        "groceries" to listOf("bigbasket", "blinkit", "zepto", "dmart", "kirana", "grocer"),
        "health" to listOf("apollo", "medplus", "pharma", "hospital", "clinic", "medical"),
        "bills" to listOf("airtel", "jio", "electricity", "water", "gas", "bill"),
        "travel" to listOf("makemytrip", "goibibo", "indigo", "airline", "hotel", "oyo"),
        "entertainment" to listOf("netflix", "hotstar", "prime", "spotify", "bookmyshow", "cinema")
    )

    return categories
        .map { cat ->
            val keywords = keywordMap[cat.name.lowercase()] ?: emptyList()
            val score = keywords.count { name.contains(it) }
            val confidence = when {
                score >= 1 -> 0.92f
                cat.name.equals("Other", ignoreCase = true) -> 0.15f
                else -> 0.05f + (cat.name.length % 7) * 0.01f
            }
            val subtitle = when (cat.name.lowercase()) {
                "food" -> "Dining · Delivery"
                "transport" -> "Cab · Fuel"
                "shopping" -> "Online"
                "groceries" -> "Daily essentials"
                "health" -> "Medical"
                "bills" -> "Recurring"
                "travel" -> "Trips"
                "entertainment" -> "Subscriptions"
                else -> "General"
            }
            Suggestion(cat, confidence, subtitle)
        }
        .sortedByDescending { it.confidence }
}

@Suppress("unused")
private val sampleDate: LocalDate = LocalDate.now()
