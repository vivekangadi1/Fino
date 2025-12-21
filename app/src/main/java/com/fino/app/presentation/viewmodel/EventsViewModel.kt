package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.EventRepository
import com.fino.app.domain.model.EventStatus
import com.fino.app.domain.model.EventSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Filter options for events list
 */
enum class EventFilter {
    ACTIVE,
    COMPLETED,
    ALL
}

/**
 * UI State for Events screen
 */
data class EventsUiState(
    val activeEvents: List<EventSummary> = emptyList(),
    val completedEvents: List<EventSummary> = emptyList(),
    val selectedFilter: EventFilter = EventFilter.ACTIVE,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    /**
     * Get filtered events based on selected filter
     */
    val filteredEvents: List<EventSummary>
        get() = when (selectedFilter) {
            EventFilter.ACTIVE -> activeEvents
            EventFilter.COMPLETED -> completedEvents
            EventFilter.ALL -> activeEvents + completedEvents
        }
}

/**
 * ViewModel for the Events screen
 */
@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    /**
     * Load all events and categorize them
     */
    private fun loadEvents() {
        viewModelScope.launch {
            eventRepository.getEventSummariesFlow().collect { summaries ->
                val active = summaries.filter {
                    it.event.status == EventStatus.ACTIVE && it.event.isActive
                }
                val completed = summaries.filter {
                    it.event.status == EventStatus.COMPLETED
                }

                _uiState.update {
                    it.copy(
                        activeEvents = active,
                        completedEvents = completed,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    /**
     * Set the filter for events list
     */
    fun setFilter(filter: EventFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    /**
     * Complete an event
     */
    fun completeEvent(eventId: Long) {
        viewModelScope.launch {
            try {
                eventRepository.completeEvent(eventId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to complete event: ${e.message}")
                }
            }
        }
    }

    /**
     * Delete an event
     */
    fun deleteEvent(eventId: Long) {
        viewModelScope.launch {
            try {
                val event = eventRepository.getById(eventId)
                if (event != null) {
                    eventRepository.delete(event)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to delete event: ${e.message}")
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
     * Refresh events list
     */
    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadEvents()
    }
}
