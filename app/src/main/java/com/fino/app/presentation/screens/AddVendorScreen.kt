package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
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
import com.fino.app.domain.model.EventSubCategory
import com.fino.app.presentation.components.SlideInCard
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.AddVendorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVendorScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddVendorViewModel = hiltViewModel()
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
                        text = if (uiState.isEditMode) "Edit Vendor" else "Add Vendor",
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
                    VendorFormSection(title = "Vendor Name *") {
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.setName(it) },
                            placeholder = { Text("e.g., Sharma Caterers", color = TextTertiary) },
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

            // Sub-Category Selector
            item {
                SlideInCard(delay = 100) {
                    VendorFormSection(title = "Category") {
                        SubCategorySelector(
                            subCategories = uiState.subCategories,
                            selectedId = uiState.selectedSubCategoryId,
                            onSelected = { viewModel.setSubCategory(it) }
                        )
                    }
                }
            }

            // Contact Info
            item {
                SlideInCard(delay = 150) {
                    VendorFormSection(title = "Contact Info") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = uiState.phone,
                                onValueChange = { viewModel.setPhone(it) },
                                placeholder = { Text("Phone Number", color = TextTertiary) },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = DarkSurfaceHigh,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = Primary
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = uiState.email,
                                onValueChange = { viewModel.setEmail(it) },
                                placeholder = { Text("Email Address", color = TextTertiary) },
                                leadingIcon = {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondary)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = DarkSurfaceHigh,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = Primary
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // Quoted Amount
            item {
                SlideInCard(delay = 200) {
                    VendorFormSection(title = "Quoted Amount") {
                        OutlinedTextField(
                            value = uiState.quotedAmount,
                            onValueChange = { viewModel.setQuotedAmount(it) },
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

            // Description
            item {
                SlideInCard(delay = 250) {
                    VendorFormSection(title = "Description") {
                        OutlinedTextField(
                            value = uiState.description,
                            onValueChange = { viewModel.setDescription(it) },
                            placeholder = { Text("Services provided", color = TextTertiary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = DarkSurfaceHigh,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = Primary
                            ),
                            maxLines = 3
                        )
                    }
                }
            }

            // Notes
            item {
                SlideInCard(delay = 300) {
                    VendorFormSection(title = "Notes") {
                        OutlinedTextField(
                            value = uiState.notes,
                            onValueChange = { viewModel.setNotes(it) },
                            placeholder = { Text("Additional notes", color = TextTertiary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = DarkSurfaceHigh,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = Primary
                            ),
                            maxLines = 3
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
                                    text = if (uiState.isEditMode) "Save Changes" else "Add Vendor",
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
private fun VendorFormSection(
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
private fun SubCategorySelector(
    subCategories: List<EventSubCategory>,
    selectedId: Long?,
    onSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (subCategories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No sub-categories yet. Add sub-categories first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        } else {
            // No category option
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (selectedId == null) Primary.copy(alpha = 0.2f)
                        else DarkSurfaceVariant
                    )
                    .clickable { onSelected(null) }
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "📦", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "No Category",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    if (selectedId == null) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            subCategories.forEach { subCategory ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (subCategory.id == selectedId) Primary.copy(alpha = 0.2f)
                            else DarkSurfaceVariant
                        )
                        .clickable { onSelected(subCategory.id) }
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = subCategory.emoji, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = subCategory.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        if (subCategory.id == selectedId) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
