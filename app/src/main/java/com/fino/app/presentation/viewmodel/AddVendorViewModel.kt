package com.fino.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.EventSubCategoryRepository
import com.fino.app.data.repository.EventVendorRepository
import com.fino.app.domain.model.EventSubCategory
import com.fino.app.domain.model.EventVendor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class AddVendorUiState(
    val name: String = "",
    val description: String = "",
    val phone: String = "",
    val email: String = "",
    val quotedAmount: String = "",
    val notes: String = "",
    val selectedSubCategoryId: Long? = null,
    val subCategories: List<EventSubCategory> = emptyList(),
    val eventId: Long = 0,
    val isEditMode: Boolean = false,
    val vendorId: Long? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddVendorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vendorRepository: EventVendorRepository,
    private val subCategoryRepository: EventSubCategoryRepository
) : ViewModel() {

    private val eventId: Long = savedStateHandle.get<Long>("eventId") ?: 0L
    private val vendorId: Long? = savedStateHandle.get<Long>("vendorId")

    private val _uiState = MutableStateFlow(AddVendorUiState(eventId = eventId))
    val uiState: StateFlow<AddVendorUiState> = _uiState.asStateFlow()

    init {
        loadSubCategories()
        if (vendorId != null && vendorId > 0) {
            loadVendor(vendorId)
        }
    }

    private fun loadSubCategories() {
        viewModelScope.launch {
            subCategoryRepository.getByEventIdFlow(eventId).collect { subCategories ->
                _uiState.update { it.copy(subCategories = subCategories) }
            }
        }
    }

    private fun loadVendor(id: Long) {
        viewModelScope.launch {
            val vendor = vendorRepository.getById(id)
            if (vendor != null) {
                _uiState.update {
                    it.copy(
                        name = vendor.name,
                        description = vendor.description ?: "",
                        phone = vendor.phone ?: "",
                        email = vendor.email ?: "",
                        quotedAmount = vendor.quotedAmount?.toString() ?: "",
                        notes = vendor.notes ?: "",
                        selectedSubCategoryId = vendor.subCategoryId,
                        isEditMode = true,
                        vendorId = id
                    )
                }
            }
        }
    }

    fun setName(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun setDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun setPhone(phone: String) {
        _uiState.update { it.copy(phone = phone) }
    }

    fun setEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun setQuotedAmount(amount: String) {
        _uiState.update { it.copy(quotedAmount = amount) }
    }

    fun setNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun setSubCategory(subCategoryId: Long?) {
        _uiState.update { it.copy(selectedSubCategoryId = subCategoryId) }
    }

    fun save() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Please enter vendor name") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val quotedAmount = state.quotedAmount.toDoubleOrNull()
                val now = LocalDateTime.now()

                if (state.isEditMode && state.vendorId != null) {
                    val existing = vendorRepository.getById(state.vendorId)
                    if (existing != null) {
                        val updated = existing.copy(
                            name = state.name,
                            description = state.description.takeIf { it.isNotBlank() },
                            phone = state.phone.takeIf { it.isNotBlank() },
                            email = state.email.takeIf { it.isNotBlank() },
                            quotedAmount = quotedAmount,
                            notes = state.notes.takeIf { it.isNotBlank() },
                            subCategoryId = state.selectedSubCategoryId,
                            updatedAt = now
                        )
                        vendorRepository.update(updated)
                    }
                } else {
                    val newVendor = EventVendor(
                        eventId = eventId,
                        name = state.name,
                        description = state.description.takeIf { it.isNotBlank() },
                        phone = state.phone.takeIf { it.isNotBlank() },
                        email = state.email.takeIf { it.isNotBlank() },
                        quotedAmount = quotedAmount,
                        notes = state.notes.takeIf { it.isNotBlank() },
                        subCategoryId = state.selectedSubCategoryId,
                        createdAt = now,
                        updatedAt = now
                    )
                    vendorRepository.insert(newVendor)
                }

                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save vendor"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
