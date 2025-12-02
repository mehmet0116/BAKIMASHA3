package com.assanhanil.techassist.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.assanhanil.techassist.domain.model.MachineName
import com.assanhanil.techassist.domain.repository.MachineNameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Machine Name feature.
 * Handles machine name template CRUD operations.
 * Machine names persist as templates for quick selection,
 * but control data is not saved with them.
 */
class MachineNameViewModel(
    private val machineNameRepository: MachineNameRepository
) : ViewModel() {

    // All machine names from database
    val machineNames: StateFlow<List<MachineName>> = machineNameRepository.getAllMachineNames()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Save a new machine name.
     * If the name already exists, it will be ignored.
     */
    fun saveMachineName(name: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                // Check if name already exists
                val existing = machineNameRepository.getMachineNameByName(name)
                if (existing == null) {
                    machineNameRepository.saveMachineName(name)
                }
                _error.value = null
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving machine name: ${e.message}", e)
                _error.value = "Makina adı kaydedilemedi: ${e.message}"
            }
        }
    }

    /**
     * Delete a machine name by ID.
     */
    fun deleteMachineName(id: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                machineNameRepository.deleteMachineName(id)
                _error.value = null
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting machine name: ${e.message}", e)
                _error.value = "Makina adı silinemedi: ${e.message}"
            }
        }
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Factory for creating MachineNameViewModel with dependencies.
     */
    class Factory(
        private val machineNameRepository: MachineNameRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MachineNameViewModel::class.java)) {
                return MachineNameViewModel(machineNameRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private const val TAG = "MachineNameViewModel"
    }
}
