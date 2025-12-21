package com.fino.app.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.EventRepository
import com.fino.app.data.repository.EventTypeRepository
import com.fino.app.domain.model.Event
import com.fino.app.domain.model.EventStatus
import com.fino.app.domain.model.EventType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * UI State for creating/editing an event
 */
data class CreateEventUiState(
    val isEditMode: Boolean = false,
    val editingEventId: Long? = null,
    val name: String = "",
    val description: String = "",
    val selectedEmoji: String = "🎉",
    val eventTypes: List<EventType> = emptyList(),
    val selectedEventTypeId: Long? = null,
    val hasBudget: Boolean = false,
    val budgetAmount: String = "",
    val alertAt75: Boolean = true,
    val alertAt100: Boolean = true,
    val startDate: LocalDate = LocalDate.now(),
    val hasEndDate: Boolean = false,
    val endDate: LocalDate = LocalDate.now().plusDays(7),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel for the Create/Edit Event screen
 */
@HiltViewModel
class CreateEventViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
    private val eventTypeRepository: EventTypeRepository
) : ViewModel() {

    private val eventId: Long? = savedStateHandle.get<Long>("eventId")

    private val _uiState = MutableStateFlow(CreateEventUiState())
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    init {
        loadEventTypes()
        if (eventId != null && eventId > 0) {
            loadEventForEditing(eventId)
        }
    }

    /**
     * Load existing event data for editing
     */
    private fun loadEventForEditing(eventId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val event = eventRepository.getById(eventId)
                if (event != null) {
                    _uiState.update {
                        it.copy(
                            isEditMode = true,
                            editingEventId = eventId,
                            name = event.name,
                            description = event.description ?: "",
                            selectedEmoji = event.emoji,
                            selectedEventTypeId = event.eventTypeId,
                            hasBudget = event.budgetAmount != null,
                            budgetAmount = event.budgetAmount?.toLong()?.toString() ?: "",
                            alertAt75 = event.alertAt75,
                            alertAt100 = event.alertAt100,
                            startDate = event.startDate,
                            hasEndDate = event.endDate != null,
                            endDate = event.endDate ?: LocalDate.now().plusDays(7),
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Event not found") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load event") }
            }
        }
    }

    /**
     * Load event types for selection
     */
    private fun loadEventTypes() {
        viewModelScope.launch {
            eventTypeRepository.getAllActiveFlow().collect { types ->
                _uiState.update {
                    it.copy(
                        eventTypes = types,
                        selectedEventTypeId = types.firstOrNull()?.id
                    )
                }
            }
        }
    }

    /**
     * Update event name
     */
    fun setName(name: String) {
        _uiState.update { it.copy(name = name, error = null) }
    }

    /**
     * Update description
     */
    fun setDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    /**
     * Update selected emoji
     */
    fun setEmoji(emoji: String) {
        _uiState.update { it.copy(selectedEmoji = emoji) }
    }

    /**
     * Select event type
     */
    fun setEventType(eventTypeId: Long) {
        _uiState.update { it.copy(selectedEventTypeId = eventTypeId) }
    }

    /**
     * Toggle budget tracking
     */
    fun setHasBudget(hasBudget: Boolean) {
        _uiState.update { it.copy(hasBudget = hasBudget) }
    }

    /**
     * Update budget amount
     */
    fun setBudgetAmount(amount: String) {
        _uiState.update { it.copy(budgetAmount = amount, error = null) }
    }

    /**
     * Toggle 75% budget alert
     */
    fun setAlertAt75(enabled: Boolean) {
        _uiState.update { it.copy(alertAt75 = enabled) }
    }

    /**
     * Toggle 100% budget alert
     */
    fun setAlertAt100(enabled: Boolean) {
        _uiState.update { it.copy(alertAt100 = enabled) }
    }

    /**
     * Update start date
     */
    fun setStartDate(date: LocalDate) {
        _uiState.update {
            val state = it.copy(startDate = date)
            // Ensure end date is after start date
            if (state.hasEndDate && state.endDate < date) {
                state.copy(endDate = date.plusDays(1))
            } else {
                state
            }
        }
    }

    /**
     * Toggle end date
     */
    fun setHasEndDate(hasEndDate: Boolean) {
        _uiState.update {
            it.copy(
                hasEndDate = hasEndDate,
                endDate = if (hasEndDate) it.startDate.plusDays(7) else it.endDate
            )
        }
    }

    /**
     * Update end date
     */
    fun setEndDate(date: LocalDate) {
        _uiState.update { it.copy(endDate = date) }
    }

    /**
     * Save the event (create new or update existing)
     */
    fun saveEvent() {
        viewModelScope.launch {
            val state = _uiState.value

            // Validate event name
            if (state.name.isBlank()) {
                _uiState.update { it.copy(error = "Event name is required") }
                return@launch
            }

            // Validate event type selected
            if (state.selectedEventTypeId == null) {
                _uiState.update { it.copy(error = "Please select an event type") }
                return@launch
            }

            // Validate budget amount if budget is enabled
            val budgetAmount = if (state.hasBudget) {
                val amount = state.budgetAmount.toDoubleOrNull()
                if (amount == null || amount <= 0) {
                    _uiState.update { it.copy(error = "Please enter a valid budget amount") }
                    return@launch
                }
                amount
            } else {
                null
            }

            // Validate date range
            if (state.hasEndDate && state.endDate < state.startDate) {
                _uiState.update { it.copy(error = "End date must be after start date") }
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                if (state.isEditMode && state.editingEventId != null) {
                    // Update existing event
                    val existingEvent = eventRepository.getById(state.editingEventId)
                    if (existingEvent != null) {
                        val updatedEvent = existingEvent.copy(
                            name = state.name,
                            description = state.description.ifBlank { null },
                            emoji = state.selectedEmoji,
                            eventTypeId = state.selectedEventTypeId,
                            budgetAmount = budgetAmount,
                            alertAt75 = if (state.hasBudget) state.alertAt75 else true,
                            alertAt100 = if (state.hasBudget) state.alertAt100 else true,
                            startDate = state.startDate,
                            endDate = if (state.hasEndDate) state.endDate else null,
                            updatedAt = LocalDateTime.now()
                        )
                        eventRepository.update(updatedEvent)
                    }
                } else {
                    // Create new event
                    val event = Event(
                        id = 0L,
                        name = state.name,
                        description = state.description.ifBlank { null },
                        emoji = state.selectedEmoji,
                        eventTypeId = state.selectedEventTypeId,
                        budgetAmount = budgetAmount,
                        alertAt75 = if (state.hasBudget) state.alertAt75 else true,
                        alertAt100 = if (state.hasBudget) state.alertAt100 else true,
                        startDate = state.startDate,
                        endDate = if (state.hasEndDate) state.endDate else null,
                        status = EventStatus.ACTIVE,
                        isActive = true,
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                    eventRepository.insert(event)
                }

                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save event"
                    )
                }
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Reset the form
     */
    fun resetForm() {
        _uiState.update {
            CreateEventUiState(eventTypes = it.eventTypes, selectedEventTypeId = it.selectedEventTypeId)
        }
    }
}
