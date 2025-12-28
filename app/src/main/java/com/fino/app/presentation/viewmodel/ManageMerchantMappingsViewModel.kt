package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.MerchantMappingRepository
import com.fino.app.domain.model.Category
import com.fino.app.domain.model.MerchantMapping
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class ManageMerchantMappingsUiState(
    val mappings: List<MerchantMapping> = emptyList(),
    val categories: List<Category> = emptyList(),
    val categoryMap: Map<Long, Category> = emptyMap(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class ManageMerchantMappingsViewModel @Inject constructor(
    private val merchantMappingRepository: MerchantMappingRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageMerchantMappingsUiState())
    val uiState: StateFlow<ManageMerchantMappingsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                merchantMappingRepository.getAllMappingsFlow(),
                categoryRepository.getAllActive()
            ) { mappings, categories ->
                Pair(mappings, categories)
            }.collect { (mappings, categories) ->
                val categoryMap = categories.associateBy { it.id }

                // Sort mappings by last used date (most recent first)
                val sortedMappings = mappings.sortedByDescending { it.lastUsedAt }

                _uiState.update {
                    it.copy(
                        mappings = sortedMappings,
                        categories = categories,
                        categoryMap = categoryMap,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Update the category for a mapping
     */
    fun updateMappingCategory(mapping: MerchantMapping, newCategoryId: Long) {
        viewModelScope.launch {
            try {
                merchantMappingRepository.updateMapping(
                    mapping.copy(
                        categoryId = newCategoryId,
                        lastUsedAt = LocalDateTime.now()
                    )
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    /**
     * Delete a merchant mapping
     */
    fun deleteMapping(mapping: MerchantMapping) {
        viewModelScope.launch {
            try {
                merchantMappingRepository.deleteMapping(mapping)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
