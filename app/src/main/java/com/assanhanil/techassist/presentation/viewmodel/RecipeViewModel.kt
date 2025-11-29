package com.assanhanil.techassist.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.assanhanil.techassist.domain.model.Recipe
import com.assanhanil.techassist.domain.model.RecipeSection
import com.assanhanil.techassist.domain.model.RecipeField
import com.assanhanil.techassist.domain.model.FieldType
import com.assanhanil.techassist.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel for the Recipes feature.
 * Handles recipe CRUD operations and state management.
 */
class RecipeViewModel(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    // All recipes from database
    val recipes: StateFlow<List<Recipe>> = recipeRepository.getAllRecipes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Load a recipe by ID.
     */
    fun loadRecipe(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _selectedRecipe.value = recipeRepository.getRecipeById(id)
            } catch (e: Exception) {
                _error.value = "Tarif yüklenirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Save a new recipe or update existing one.
     */
    fun saveRecipe(recipe: Recipe, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedRecipe = recipe.copy(updatedAt = Date())
                recipeRepository.saveRecipe(updatedRecipe)
                _error.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Tarif kaydedilirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Create a new recipe from the simple model used in RecipesScreen.
     */
    fun saveSimpleRecipe(
        name: String,
        description: String,
        category: String,
        steps: List<Pair<String, String>>, // title to description
        onSuccess: () -> Unit = {}
    ) {
        val fields = steps.mapIndexed { index, (title, desc) ->
            RecipeField(
                id = 0,
                label = title,
                fieldType = FieldType.CHECKBOX,
                defaultValue = desc,
                isRequired = false,
                order = index + 1
            )
        }
        
        val section = RecipeSection(
            id = 0,
            title = category,
            order = 0,
            fields = fields
        )
        
        val recipe = Recipe(
            id = 0,
            name = name,
            description = "$category: $description",
            createdAt = Date(),
            updatedAt = Date(),
            createdBy = "",
            sections = listOf(section),
            isActive = true
        )
        
        saveRecipe(recipe, onSuccess)
    }

    /**
     * Delete a recipe.
     */
    fun deleteRecipe(recipe: Recipe, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                recipeRepository.deleteRecipe(recipe)
                _error.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Tarif silinirken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Deactivate a recipe (soft delete).
     */
    fun deactivateRecipe(recipeId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                recipeRepository.deactivateRecipe(recipeId)
                _error.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Tarif devre dışı bırakılırken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear selected recipe.
     */
    fun clearSelection() {
        _selectedRecipe.value = null
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Factory for creating RecipeViewModel with dependencies.
     */
    class Factory(
        private val recipeRepository: RecipeRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
                return RecipeViewModel(recipeRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
