package com.assanhanil.techassist.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.assanhanil.techassist.domain.model.ExcelTemplate
import com.assanhanil.techassist.domain.model.TemplateColumn
import com.assanhanil.techassist.domain.model.TemplateRow
import com.assanhanil.techassist.domain.model.TemplateCell
import com.assanhanil.techassist.domain.repository.ExcelTemplateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Excel Template Builder feature.
 * Handles template CRUD operations and state management.
 */
class ExcelTemplateViewModel(
    private val excelTemplateRepository: ExcelTemplateRepository
) : ViewModel() {

    // All templates from database
    val templates: StateFlow<List<ExcelTemplate>> = excelTemplateRepository.getAllTemplates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedTemplate = MutableStateFlow<ExcelTemplate?>(null)
    val selectedTemplate: StateFlow<ExcelTemplate?> = _selectedTemplate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    /**
     * Load a template by ID.
     */
    fun loadTemplate(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _selectedTemplate.value = excelTemplateRepository.getTemplateById(id)
            } catch (e: Exception) {
                _error.value = "Şablon yüklenirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load a template by name.
     */
    fun loadTemplateByName(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _selectedTemplate.value = excelTemplateRepository.getTemplateByName(name)
            } catch (e: Exception) {
                _error.value = "Şablon yüklenirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Save a new template or update existing one.
     */
    fun saveTemplate(template: ExcelTemplate, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedTemplate = template.copy(updatedAt = System.currentTimeMillis())
                excelTemplateRepository.saveTemplate(updatedTemplate)
                _error.value = null
                _saveSuccess.value = true
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Şablon kaydedilirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Save template from the builder UI state.
     */
    fun saveTemplateFromBuilder(
        name: String,
        description: String,
        columns: List<TemplateColumn>,
        rows: List<TemplateRow>,
        onSuccess: () -> Unit = {}
    ) {
        val template = ExcelTemplate(
            id = 0,
            name = name,
            description = description,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            columns = columns,
            rows = rows,
            headerRowCount = rows.count { it.isHeader }
        )
        
        saveTemplate(template, onSuccess)
    }

    /**
     * Delete a template.
     */
    fun deleteTemplate(template: ExcelTemplate, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                excelTemplateRepository.deleteTemplate(template)
                _error.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Şablon silinirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Deactivate a template (soft delete).
     */
    fun deactivateTemplate(templateId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                excelTemplateRepository.deactivateTemplate(templateId)
                _error.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Şablon devre dışı bırakılırken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear selected template.
     */
    fun clearSelection() {
        _selectedTemplate.value = null
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
     * Factory for creating ExcelTemplateViewModel with dependencies.
     */
    class Factory(
        private val excelTemplateRepository: ExcelTemplateRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExcelTemplateViewModel::class.java)) {
                return ExcelTemplateViewModel(excelTemplateRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
