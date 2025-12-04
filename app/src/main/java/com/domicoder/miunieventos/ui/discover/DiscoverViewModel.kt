package com.domicoder.miunieventos.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domicoder.miunieventos.data.model.Category
import com.domicoder.miunieventos.data.model.Department
import com.domicoder.miunieventos.data.model.Event
import com.domicoder.miunieventos.data.repository.ConfigRepository
import com.domicoder.miunieventos.data.repository.EventRepository
import com.domicoder.miunieventos.data.repository.RSVPRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val rsvpRepository: RSVPRepository,
    private val configRepository: ConfigRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _selectedDepartment = MutableStateFlow<String?>(null)
    val selectedDepartment: StateFlow<String?> = _selectedDepartment

    private val _selectedStartDate = MutableStateFlow<LocalDate?>(null)
    val selectedStartDate: StateFlow<LocalDate?> = _selectedStartDate

    private val _selectedEndDate = MutableStateFlow<LocalDate?>(null)
    val selectedEndDate: StateFlow<LocalDate?> = _selectedEndDate

    private val _showOnlySelectedEvents = MutableStateFlow(false)
    val showOnlySelectedEvents: StateFlow<Boolean> = _showOnlySelectedEvents

    val events: StateFlow<List<Event>> = eventRepository.getUpcomingEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories: StateFlow<List<Category>> = configRepository.getCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val departments: StateFlow<List<Department>> = configRepository.getDepartments()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setSelectedDepartment(department: String?) {
        _selectedDepartment.value = department
    }

    fun setSelectedStartDate(date: LocalDate?) {
        _selectedStartDate.value = date
    }

    fun setSelectedEndDate(date: LocalDate?) {
        _selectedEndDate.value = date
    }

    fun clearFilters() {
        _selectedCategory.value = null
        _selectedDepartment.value = null
        _selectedStartDate.value = null
        _selectedEndDate.value = null
        _showOnlySelectedEvents.value = false
    }

    fun resetSelectedEventsFilter() {
        _showOnlySelectedEvents.value = false
    }

    fun toggleShowOnlySelected() {
        _showOnlySelectedEvents.value = !_showOnlySelectedEvents.value
    }

    fun getUserRSVPs(userId: String) = rsvpRepository.getRSVPsByUserId(userId)
}
