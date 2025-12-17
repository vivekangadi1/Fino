package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.presentation.components.*
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.AddTransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate back on successful save
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            TransactionHeader(
                onClose = onNavigateBack,
                isExpense = uiState.isExpense,
                onToggleType = { viewModel.setIsExpense(it) }
            )

            // Amount Section
            AmountSection(
                amount = uiState.amount,
                onAmountChange = { viewModel.setAmount(it) },
                isExpense = uiState.isExpense
            )

            // Merchant Input
            MerchantSection(
                merchant = uiState.merchant,
                onMerchantChange = { viewModel.setMerchant(it) }
            )

            // Category Grid
            CategorySection(
                selectedCategoryId = uiState.selectedCategoryId,
                categories = uiState.categories,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Error message
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = ExpenseRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Save Button
            SaveButton(
                enabled = uiState.amount.isNotBlank() && uiState.selectedCategoryId != null && !uiState.isSaving,
                isLoading = uiState.isSaving,
                onClick = { viewModel.saveTransaction() }
            )
        }
    }
}

@Composable
private fun TransactionHeader(
    onClose: () -> Unit,
    isExpense: Boolean,
    onToggleType: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkSurface, DarkBackground)
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Close Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextPrimary
                )
            }

            // Type Toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .padding(4.dp)
            ) {
                // Expense
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isExpense) ExpenseRed else Color.Transparent)
                        .clickable { onToggleType(true) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Expense",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isExpense) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isExpense) TextPrimary else TextSecondary
                    )
                }
                // Income
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isExpense) IncomeGreen else Color.Transparent)
                        .clickable { onToggleType(false) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (!isExpense) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (!isExpense) TextPrimary else TextSecondary
                    )
                }
            }

            // Spacer for balance
            Spacer(modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
private fun AmountSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    isExpense: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isExpense) "How much did you spend?" else "How much did you earn?",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "₹",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpense) ExpenseRed else IncomeGreen
                )
                BasicTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        // Only allow numbers and one decimal point
                        val filtered = newValue.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) {
                            onAmountChange(filtered)
                        }
                    },
                    textStyle = TextStyle(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    cursorBrush = SolidColor(Primary),
                    modifier = Modifier.widthIn(min = 100.dp, max = 250.dp),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            if (amount.isEmpty()) {
                                Text(
                                    text = "0",
                                    style = TextStyle(
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextTertiary
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MerchantSection(
    merchant: String,
    onMerchantChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceVariant)
                .padding(16.dp)
        ) {
            BasicTextField(
                value = merchant,
                onValueChange = onMerchantChange,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = TextPrimary
                ),
                singleLine = true,
                cursorBrush = SolidColor(Primary),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box {
                        if (merchant.isEmpty()) {
                            Text(
                                text = "e.g., Swiggy, Amazon, Salary...",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = TextTertiary
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
private fun CategorySection(
    selectedCategoryId: Long?,
    categories: List<com.fino.app.domain.model.Category>,
    onCategorySelected: (Long) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Category",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Use categories from ViewModel if available, otherwise fall back to hardcoded list
        val displayCategories = if (categories.isNotEmpty()) {
            categories.map { cat ->
                TransactionCategory(
                    id = cat.id,
                    name = cat.name,
                    emoji = cat.emoji,
                    color = getCategoryColor(cat.name)
                )
            }
        } else {
            transactionCategories
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(200.dp)
        ) {
            items(displayCategories) { category ->
                CategoryItem(
                    category = category,
                    isSelected = selectedCategoryId == category.id,
                    onClick = { onCategorySelected(category.id) }
                )
            }
        }
    }
}

private fun getCategoryColor(name: String): Color {
    return when (name.lowercase()) {
        "food" -> CategoryFood
        "transport" -> CategoryTransport
        "shopping" -> CategoryShopping
        "health" -> CategoryHealth
        "fun", "entertainment" -> CategoryEntertainment
        "bills" -> CategoryBills
        "education" -> CategoryEducation
        "travel" -> CategoryTravel
        "groceries" -> CategoryGroceries
        "personal" -> CategoryPersonal
        "salary" -> IncomeGreen
        else -> CategoryOther
    }
}

@Composable
private fun CategoryItem(
    category: TransactionCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) category.color.copy(alpha = 0.2f) else DarkSurfaceVariant)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) category.color else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = category.emoji,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) category.color else TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun SaveButton(
    enabled: Boolean,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        BouncyButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            gradient = if (enabled) FinoGradients.Primary else Brush.linearGradient(
                listOf(TextTertiary, TextTertiary)
            ),
            enabled = enabled && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = TextPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Saving...",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            } else {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Transaction",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}

data class TransactionCategory(
    val id: Long,
    val name: String,
    val emoji: String,
    val color: Color
)

val transactionCategories = listOf(
    TransactionCategory(1L, "Food", "🍔", CategoryFood),
    TransactionCategory(2L, "Transport", "🚗", CategoryTransport),
    TransactionCategory(3L, "Shopping", "🛍️", CategoryShopping),
    TransactionCategory(4L, "Health", "💊", CategoryHealth),
    TransactionCategory(5L, "Fun", "🎬", CategoryEntertainment),
    TransactionCategory(6L, "Bills", "📱", CategoryBills),
    TransactionCategory(7L, "Education", "📚", CategoryEducation),
    TransactionCategory(8L, "Travel", "✈️", CategoryTravel),
    TransactionCategory(9L, "Groceries", "🛒", CategoryGroceries),
    TransactionCategory(10L, "Personal", "💅", CategoryPersonal),
    TransactionCategory(11L, "Salary", "💰", IncomeGreen),
    TransactionCategory(12L, "Other", "📦", CategoryOther)
)
