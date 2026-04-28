package com.fino.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fino.app.data.repository.CategoryRepository
import com.fino.app.data.repository.EventRepository
import com.fino.app.data.repository.TransactionRepository
import com.fino.app.domain.model.EventStatus
import com.fino.app.domain.model.EventSummary
import com.fino.app.domain.model.FeaturedEventData
import com.fino.app.domain.usecase.EventInsightsCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class EventFilter {
    ACTIVE,
    COMPLETED,
    ALL
}

data class EventsUiState(
    val activeEvents: List<EventSummary> = emptyList(),
    val completedEvents: List<EventSummary> = emptyList(),
    val featured: FeaturedEventData? = null,
    val selectedFilter: EventFilter = EventFilter.ACTIVE,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    val filteredEvents: List<EventSummary>
        get() = when (selectedFilter) {
            EventFilter.ACTIVE -> activeEvents
            EventFilter.COMPLETED -> completedEvents
            EventFilter.ALL -> activeEvents + completedEvents
        }
}

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            combine(
                eventRepository.getEventSummariesFlow(),
                transactionRepository.getAllTransactionsFlow(),
                categoryRepository.getAllActive()
            ) { summaries, allTxns, categories ->
                Triple(summaries, allTxns, categories)
            }.collect { (summaries, allTxns, categories) ->
                val active = summaries.filter {
                    it.event.status == EventStatus.ACTIVE && it.event.isActive
                }
                val completed = summaries.filter {
                    it.event.status == EventStatus.COMPLETED
                }

                val categoryNames = categories.associate { it.id to it.name }
                val featured = active.firstOrNull()?.let { summary ->
                    val eventTxns = allTxns.filter { it.eventId == summary.event.id }
                    EventInsightsCalculator.build(
                        summary = summary,
                        eventTxns = eventTxns,
                        categoryNames = categoryNames
                    )
                }

                _uiState.update {
                    it.copy(
                        activeEvents = active,
                        completedEvents = completed,
                        featured = featured,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun setFilter(filter: EventFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadEvents()
    }
}
