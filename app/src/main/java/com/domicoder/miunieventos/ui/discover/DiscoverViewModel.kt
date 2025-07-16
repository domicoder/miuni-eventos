package com.domicoder.miunieventos.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domicoder.miunieventos.data.model.Event
import com.domicoder.miunieventos.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory
    
    private val _selectedDepartment = MutableStateFlow<String?>(null)
    val selectedDepartment: StateFlow<String?> = _selectedDepartment
    
    val events = combine(
        eventRepository.getUpcomingEvents(),
        searchQuery,
        selectedCategory,
        selectedDepartment
    ) { events, query, category, department ->
        events.filter { event ->
            val matchesQuery = if (query.isNotBlank()) {
                event.title.contains(query, ignoreCase = true) ||
                event.description.contains(query, ignoreCase = true) ||
                event.location.contains(query, ignoreCase = true)
            } else true
            
            val matchesCategory = if (category != null) {
                event.category == category
            } else true
            
            val matchesDepartment = if (department != null) {
                event.department == department
            } else true
            
            matchesQuery && matchesCategory && matchesDepartment
        }
    }.stateIn(
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
    
    fun clearFilters() {
        _selectedCategory.value = null
        _selectedDepartment.value = null
    }
} 