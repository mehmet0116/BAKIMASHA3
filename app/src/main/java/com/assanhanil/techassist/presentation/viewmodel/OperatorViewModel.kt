package com.assanhanil.techassist.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.assanhanil.techassist.domain.model.Operator
import com.assanhanil.techassist.domain.repository.OperatorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Operator management.
 * Handles operator CRUD operations and state management.
 */
class OperatorViewModel(
    private val operatorRepository: OperatorRepository
) : ViewModel() {

    // All operators from database
    val operators: StateFlow<List<Operator>> = operatorRepository.getAllOperators()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedOperator = MutableStateFlow<Operator?>(null)
    val selectedOperator: StateFlow<Operator?> = _selectedOperator.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    /**
     * Add a new operator.
     */
    fun addOperator(name: String, department: String = "", onSuccess: () -> Unit = {}) {
        if (name.isBlank()) {
            _error.value = "Operatör adı boş olamaz"
            return
        }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val operator = Operator(
                    name = name.trim(),
                    department = department.trim()
                )
                operatorRepository.saveOperator(operator)
                _error.value = null
                _saveSuccess.value = true
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Operatör eklenirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update an existing operator.
     */
    fun updateOperator(operator: Operator, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                operatorRepository.saveOperator(operator)
                _error.value = null
                _saveSuccess.value = true
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Operatör güncellenirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete an operator (soft delete).
     */
    fun deleteOperator(operatorId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                operatorRepository.deactivateOperator(operatorId)
                _error.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Operatör silinirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load operator by ID.
     */
    fun loadOperator(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _selectedOperator.value = operatorRepository.getOperatorById(id)
            } catch (e: Exception) {
                _error.value = "Operatör yüklenirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get operators by their IDs.
     */
    suspend fun getOperatorsByIds(ids: List<Long>): List<Operator> {
        return try {
            operatorRepository.getOperatorsByIds(ids)
        } catch (e: Exception) {
            _error.value = "Operatörler yüklenirken hata oluştu: ${e.message}"
            emptyList()
        }
    }

    /**
     * Clear selection.
     */
    fun clearSelection() {
        _selectedOperator.value = null
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Reset save success flag.
     */
    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }

    /**
     * Factory for creating OperatorViewModel with dependencies.
     */
    class Factory(
        private val operatorRepository: OperatorRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OperatorViewModel::class.java)) {
                return OperatorViewModel(operatorRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
