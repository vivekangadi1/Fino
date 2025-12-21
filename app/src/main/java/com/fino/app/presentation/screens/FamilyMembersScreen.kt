package com.fino.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.fino.app.domain.model.FamilyMember
import com.fino.app.presentation.theme.*
import com.fino.app.presentation.viewmodel.FamilyMembersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyMembersScreen(
    onNavigateBack: () -> Unit,
    viewModel: FamilyMembersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var memberToDelete by remember { mutableStateOf<FamilyMember?>(null) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Family Members",
                        fontWeight = FontWeight.SemiBold,
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
                actions = {
                    IconButton(onClick = { viewModel.showAddDialog() }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Member",
                            tint = Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (uiState.members.isEmpty()) {
            EmptyMembersState(
                onAddClick = { viewModel.showAddDialog() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Manage family members for expense tracking",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(uiState.members) { member ->
                    FamilyMemberCard(
                        member = member,
                        onEditClick = { viewModel.showEditDialog(member) },
                        onDeleteClick = { memberToDelete = member },
                        onSetDefaultClick = { viewModel.setAsDefault(member) }
                    )
                }
            }
        }

        // Add/Edit Dialog
        if (uiState.showAddDialog) {
            AddEditMemberDialog(
                isEditMode = uiState.editingMember != null,
                name = uiState.newMemberName,
                relationship = uiState.newMemberRelationship,
                onNameChange = { viewModel.setName(it) },
                onRelationshipChange = { viewModel.setRelationship(it) },
                onSave = { viewModel.saveMember() },
                onDismiss = { viewModel.hideDialog() },
                isSaving = uiState.isSaving,
                error = uiState.error
            )
        }

        // Delete Confirmation Dialog
        memberToDelete?.let { member ->
            AlertDialog(
                onDismissRequest = { memberToDelete = null },
                title = { Text("Delete Member?", color = TextPrimary) },
                text = {
                    Text(
                        "Are you sure you want to delete ${member.name}?",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteMember(member)
                            memberToDelete = null
                        }
                    ) {
                        Text("Delete", color = ExpenseRed)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { memberToDelete = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = DarkSurface
            )
        }

        // Error Snackbar
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Auto-clear after delay
                kotlinx.coroutines.delay(3000)
                viewModel.clearError()
            }
        }
    }
}

@Composable
private fun EmptyMembersState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "👨‍👩‍👧‍👦",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Family Members",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Text(
            text = "Add family members to track who paid for expenses",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Family Member")
        }
    }
}

@Composable
private fun FamilyMemberCard(
    member: FamilyMember,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSetDefaultClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVariant)
            .clickable(onClick = onEditClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name and Relationship
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (member.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Primary.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Default",
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary
                            )
                        }
                    }
                }
                if (!member.relationship.isNullOrBlank()) {
                    Text(
                        text = member.relationship,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Actions
            if (!member.isDefault) {
                IconButton(
                    onClick = onSetDefaultClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Set as Default",
                        tint = TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = ExpenseRed.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AddEditMemberDialog(
    isEditMode: Boolean,
    name: String,
    relationship: String,
    onNameChange: (String) -> Unit,
    onRelationshipChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    isSaving: Boolean,
    error: String?
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkSurface)
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = if (isEditMode) "Edit Member" else "Add Member",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Name", color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = DarkSurfaceHigh,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Primary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Relationship field
                OutlinedTextField(
                    value = relationship,
                    onValueChange = onRelationshipChange,
                    label = { Text("Relationship (optional)", color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = DarkSurfaceHigh,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Primary
                    )
                )

                // Error message
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = ExpenseRed
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSave,
                        enabled = !isSaving && name.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = TextPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (isEditMode) "Save" else "Add")
                        }
                    }
                }
            }
        }
    }
}
