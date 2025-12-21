package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.FamilyMemberRepository
import com.fino.app.domain.model.FamilyMember
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FamilyMembersUiState(
    val members: List<FamilyMember> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val editingMember: FamilyMember? = null,
    val newMemberName: String = "",
    val newMemberRelationship: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FamilyMembersViewModel @Inject constructor(
    private val familyMemberRepository: FamilyMemberRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FamilyMembersUiState())
    val uiState: StateFlow<FamilyMembersUiState> = _uiState.asStateFlow()

    init {
        loadMembers()
    }

    private fun loadMembers() {
        viewModelScope.launch {
            familyMemberRepository.getAllFlow().collect { members ->
                _uiState.update {
                    it.copy(
                        members = members,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                editingMember = null,
                newMemberName = "",
                newMemberRelationship = ""
            )
        }
    }

    fun showEditDialog(member: FamilyMember) {
        _uiState.update {
            it.copy(
                showAddDialog = true,
                editingMember = member,
                newMemberName = member.name,
                newMemberRelationship = member.relationship ?: ""
            )
        }
    }

    fun hideDialog() {
        _uiState.update {
            it.copy(
                showAddDialog = false,
                editingMember = null,
                newMemberName = "",
                newMemberRelationship = "",
                error = null
            )
        }
    }

    fun setName(name: String) {
        _uiState.update { it.copy(newMemberName = name, error = null) }
    }

    fun setRelationship(relationship: String) {
        _uiState.update { it.copy(newMemberRelationship = relationship) }
    }

    fun saveMember() {
        val state = _uiState.value
        if (state.newMemberName.isBlank()) {
            _uiState.update { it.copy(error = "Name is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                if (state.editingMember != null) {
                    // Update existing member
                    val updated = state.editingMember.copy(
                        name = state.newMemberName.trim(),
                        relationship = state.newMemberRelationship.trim().ifBlank { null }
                    )
                    familyMemberRepository.update(updated)
                } else {
                    // Create new member
                    val newMember = FamilyMember(
                        name = state.newMemberName.trim(),
                        relationship = state.newMemberRelationship.trim().ifBlank { null },
                        isDefault = false,
                        sortOrder = state.members.size + 1
                    )
                    familyMemberRepository.insert(newMember)
                }
                hideDialog()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save"
                    )
                }
            }
        }
    }

    fun deleteMember(member: FamilyMember) {
        viewModelScope.launch {
            try {
                familyMemberRepository.delete(member)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to delete") }
            }
        }
    }

    fun setAsDefault(member: FamilyMember) {
        viewModelScope.launch {
            try {
                // Clear current default
                _uiState.value.members.filter { it.isDefault }.forEach {
                    familyMemberRepository.update(it.copy(isDefault = false))
                }
                // Set new default
                familyMemberRepository.update(member.copy(isDefault = true))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to set default") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
