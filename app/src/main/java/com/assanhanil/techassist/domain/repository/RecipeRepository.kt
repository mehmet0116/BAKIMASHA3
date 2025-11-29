package com.assanhanil.techassist.domain.repository

import com.assanhanil.techassist.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for recipe data operations.
 * Master Recipes allow operators to load pre-filled maintenance templates.
 */
interface RecipeRepository {
    
    /**
     * Get all active recipes.
     */
    fun getAllRecipes(): Flow<List<Recipe>>

    /**
     * Get recipes by category.
     */
    fun getRecipesByCategory(category: String): Flow<List<Recipe>>

    /**
     * Search recipes by name.
     */
    fun searchRecipes(query: String): Flow<List<Recipe>>

    /**
     * Get a recipe by ID.
     */
    suspend fun getRecipeById(id: Long): Recipe?

    /**
     * Save/update a recipe.
     */
    suspend fun saveRecipe(recipe: Recipe): Long

    /**
     * Delete a recipe.
     */
    suspend fun deleteRecipe(recipe: Recipe)

    /**
     * Deactivate a recipe (soft delete).
     */
    suspend fun deactivateRecipe(recipeId: Long)
}
