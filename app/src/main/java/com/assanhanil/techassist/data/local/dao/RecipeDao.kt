package com.assanhanil.techassist.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.assanhanil.techassist.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for recipe operations.
 * Master Recipes allow operators to load pre-filled maintenance templates.
 */
@Dao
interface RecipeDao {

    /**
     * Get all active recipes.
     */
    @Query("SELECT * FROM recipes WHERE isActive = 1 ORDER BY name ASC")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    /**
     * Get recipes by category (stored in description field as prefix).
     */
    @Query("SELECT * FROM recipes WHERE isActive = 1 AND description LIKE :category || '%' ORDER BY name ASC")
    fun getRecipesByCategory(category: String): Flow<List<RecipeEntity>>

    /**
     * Search recipes by name.
     */
    @Query("SELECT * FROM recipes WHERE isActive = 1 AND name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchRecipes(query: String): Flow<List<RecipeEntity>>

    /**
     * Get a recipe by ID.
     */
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Long): RecipeEntity?

    /**
     * Insert a new recipe (returns the generated ID).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    /**
     * Insert multiple recipes.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<RecipeEntity>)

    /**
     * Update an existing recipe.
     */
    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    /**
     * Delete a recipe.
     */
    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)

    /**
     * Soft delete (deactivate) a recipe.
     */
    @Query("UPDATE recipes SET isActive = 0, updatedAt = :timestamp WHERE id = :recipeId")
    suspend fun deactivateRecipe(recipeId: Long, timestamp: Long)

    /**
     * Get count of recipes.
     */
    @Query("SELECT COUNT(*) FROM recipes WHERE isActive = 1")
    suspend fun getCount(): Int
}
