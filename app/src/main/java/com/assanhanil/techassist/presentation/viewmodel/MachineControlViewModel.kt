package com.assanhanil.techassist.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.assanhanil.techassist.domain.model.ControlItemData
import com.assanhanil.techassist.domain.model.MachineControl
import com.assanhanil.techassist.domain.repository.MachineControlRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Machine Control feature.
 * Handles machine control CRUD operations and state management.
 */
class MachineControlViewModel(
    private val machineControlRepository: MachineControlRepository
) : ViewModel() {

    // All machine controls from database
    val machineControls: StateFlow<List<MachineControl>> = machineControlRepository.getAllMachineControls()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedMachineControl = MutableStateFlow<MachineControl?>(null)
    val selectedMachineControl: StateFlow<MachineControl?> = _selectedMachineControl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    /**
     * Load a machine control by ID.
     */
    fun loadMachineControl(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _selectedMachineControl.value = machineControlRepository.getMachineControlById(id)
            } catch (e: Exception) {
                _error.value = "Makina kontrolü yüklenirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load a machine control by title.
     */
    fun loadMachineControlByTitle(title: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _selectedMachineControl.value = machineControlRepository.getMachineControlByTitle(title)
            } catch (e: Exception) {
                _error.value = "Makina kontrolü yüklenirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Save a new machine control or update existing one.
     */
    fun saveMachineControl(machineControl: MachineControl, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedMachineControl = machineControl.copy(updatedAt = System.currentTimeMillis())
                machineControlRepository.saveMachineControl(updatedMachineControl)
                _error.value = null
                _saveSuccess.value = true
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Makina kontrolü kaydedilirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Save machine control with control items.
     */
    fun saveMachineControlWithItems(
        title: String,
        description: String,
        controlItems: List<ControlItemData>,
        operatorIds: List<Long> = emptyList(),
        existingId: Long = 0,
        onSuccess: () -> Unit = {}
    ) {
        val machineControl = MachineControl(
            id = existingId,
            title = title,
            description = description,
            createdAt = if (existingId == 0L) System.currentTimeMillis() else _selectedMachineControl.value?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            controlItems = controlItems,
            operatorIds = operatorIds
        )
        
        saveMachineControl(machineControl, onSuccess)
    }

    /**
     * Delete a machine control.
     */
    fun deleteMachineControl(machineControl: MachineControl, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                machineControlRepository.deleteMachineControl(machineControl)
                _error.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Makina kontrolü silinirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Deactivate a machine control (soft delete).
     */
    fun deactivateMachineControl(machineControlId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                machineControlRepository.deactivateMachineControl(machineControlId)
                _error.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Makina kontrolü devre dışı bırakılırken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear selected machine control.
     */
    fun clearSelection() {
        _selectedMachineControl.value = null
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
     * Factory for creating MachineControlViewModel with dependencies.
     */
    class Factory(
        private val machineControlRepository: MachineControlRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MachineControlViewModel::class.java)) {
                return MachineControlViewModel(machineControlRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
