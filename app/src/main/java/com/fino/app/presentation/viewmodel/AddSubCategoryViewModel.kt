package com.fino.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.EventSubCategoryRepository
import com.fino.app.domain.model.EventSubCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class AddSubCategoryUiState(
    val name: String = "",
    val emoji: String = "📦",
    val budgetAmount: String = "",
    val hasBudget: Boolean = false,
    val eventId: Long = 0,
    val isEditMode: Boolean = false,
    val subCategoryId: Long? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddSubCategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val subCategoryRepository: EventSubCategoryRepository
) : ViewModel() {

    private val eventId: Long = savedStateHandle.get<Long>("eventId") ?: 0L
    private val subCategoryId: Long? = savedStateHandle.get<Long>("subCategoryId")

    private val _uiState = MutableStateFlow(AddSubCategoryUiState(eventId = eventId))
    val uiState: StateFlow<AddSubCategoryUiState> = _uiState.asStateFlow()

    init {
        if (subCategoryId != null && subCategoryId > 0) {
            loadSubCategory(subCategoryId)
        }
    }

    private fun loadSubCategory(id: Long) {
        viewModelScope.launch {
            val subCategory = subCategoryRepository.getById(id)
            if (subCategory != null) {
                _uiState.update {
                    it.copy(
                        name = subCategory.name,
                        emoji = subCategory.emoji,
                        budgetAmount = subCategory.budgetAmount?.toString() ?: "",
                        hasBudget = subCategory.budgetAmount != null && subCategory.budgetAmount > 0,
                        isEditMode = true,
                        subCategoryId = id
                    )
                }
            }
        }
    }

    fun setName(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    fun setEmoji(emoji: String) {
        _uiState.update { it.copy(emoji = emoji) }
    }

    fun setBudgetAmount(amount: String) {
        _uiState.update { it.copy(budgetAmount = amount) }
    }

    fun setHasBudget(hasBudget: Boolean) {
        _uiState.update { it.copy(hasBudget = hasBudget) }
    }

    fun save() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a name") }
            return
        }

        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val budgetAmount = if (state.hasBudget) {
                    state.budgetAmount.toDoubleOrNull()
                } else null

                if (state.isEditMode && state.subCategoryId != null) {
                    val existing = subCategoryRepository.getById(state.subCategoryId)
                    if (existing != null) {
                        val updated = existing.copy(
                            name = state.name,
                            emoji = state.emoji,
                            budgetAmount = budgetAmount
                        )
                        subCategoryRepository.update(updated)
                    }
                } else {
                    val newSubCategory = EventSubCategory(
                        eventId = eventId,
                        name = state.name,
                        emoji = state.emoji,
                        budgetAmount = budgetAmount,
                        createdAt = LocalDateTime.now()
                    )
                    subCategoryRepository.insert(newSubCategory)
                }

                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        val EMOJI_OPTIONS = listOf(
            "📦", "🍽️", "💐", "📸", "👗", "💍",
            "🏨", "🎨", "🪔", "🎁", "🚗", "✈️",
            "🏛️", "🎵", "🎈", "🛍️", "🧱", "👷"
        )
    }
}
