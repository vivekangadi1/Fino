package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.presentation.components.SlideInCard
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.AddSubCategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubCategoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddSubCategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navigate back on success
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onNavigateBack()
        }
    }

    // Error dialog
    if (uiState.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(uiState.error!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Edit Sub-Category" else "Add Sub-Category",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Name
            item {
                SlideInCard(delay = 50) {
                    FormSection(title = "Name") {
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.setName(it) },
                            placeholder = { Text("e.g., Decoration, Catering", color = TextTertiary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = DarkSurfaceHigh,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = Primary
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            // Emoji Selector
            item {
                SlideInCard(delay = 100) {
                    FormSection(title = "Icon") {
                        SubCategoryEmojiSelector(
                            selectedEmoji = uiState.emoji,
                            onEmojiSelected = { viewModel.setEmoji(it) }
                        )
                    }
                }
            }

            // Budget Section
            item {
                SlideInCard(delay = 150) {
                    FormSection(title = "Budget") {
                        SubCategoryBudgetSection(
                            hasBudget = uiState.hasBudget,
                            budgetAmount = uiState.budgetAmount,
                            onToggleBudget = { viewModel.setHasBudget(it) },
                            onBudgetAmountChange = { viewModel.setBudgetAmount(it) }
                        )
                    }
                }
            }

            // Save Button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = { viewModel.save() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                            contentColor = TextPrimary
                        ),
                        enabled = !uiState.isSaving
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(FinoGradients.Primary, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    color = TextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = if (uiState.isEditMode) "Save Changes" else "Add Sub-Category",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun SubCategoryEmojiSelector(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val emojis = AddSubCategoryViewModel.EMOJI_OPTIONS

    Column(modifier = modifier.fillMaxWidth()) {
        // Selected emoji preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceVariant)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = selectedEmoji,
                fontSize = 48.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Emoji grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            emojis.chunked(6).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (emoji == selectedEmoji) Primary.copy(alpha = 0.3f)
                                    else DarkSurfaceVariant
                                )
                                .clickable { onEmojiSelected(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 24.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubCategoryBudgetSection(
    hasBudget: Boolean,
    budgetAmount: String,
    onToggleBudget: (Boolean) -> Unit,
    onBudgetAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Budget toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Set Budget",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Switch(
                checked = hasBudget,
                onCheckedChange = onToggleBudget,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextPrimary,
                    checkedTrackColor = Primary,
                    uncheckedThumbColor = TextTertiary,
                    uncheckedTrackColor = DarkSurfaceHigh
                )
            )
        }

        // Budget amount input (if enabled)
        if (hasBudget) {
            OutlinedTextField(
                value = budgetAmount,
                onValueChange = onBudgetAmountChange,
                label = { Text("Budget Amount", color = TextTertiary) },
                placeholder = { Text("e.g., 50000", color = TextTertiary) },
                leadingIcon = { Text("₹", color = TextPrimary, style = MaterialTheme.typography.titleMedium) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = DarkSurfaceHigh,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = Primary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
    }
}
