package com.assanhanil.techassist.data.repository

import com.assanhanil.techassist.data.local.dao.RecipeDao
import com.assanhanil.techassist.data.local.entity.RecipeEntity
import com.assanhanil.techassist.domain.model.Recipe
import com.assanhanil.techassist.domain.model.RecipeSection
import com.assanhanil.techassist.domain.model.RecipeField
import com.assanhanil.techassist.domain.model.FieldType
import com.assanhanil.techassist.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

/**
 * Implementation of RecipeRepository using Room database.
 * Provides offline-first recipe storage functionality.
 */
class RecipeRepositoryImpl(
    private val recipeDao: RecipeDao
) : RecipeRepository {

    override fun getAllRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecipesByCategory(category: String): Flow<List<Recipe>> {
        return recipeDao.getRecipesByCategory(category).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchRecipes(query: String): Flow<List<Recipe>> {
        return recipeDao.searchRecipes(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRecipeById(id: Long): Recipe? {
        return recipeDao.getRecipeById(id)?.toDomain()
    }

    override suspend fun saveRecipe(recipe: Recipe): Long {
        return recipeDao.insertRecipe(recipe.toEntity())
    }

    override suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.deleteRecipe(recipe.toEntity())
    }

    override suspend fun deactivateRecipe(recipeId: Long) {
        recipeDao.deactivateRecipe(recipeId, System.currentTimeMillis())
    }

    // Extension functions for mapping between domain and entity
    private fun RecipeEntity.toDomain(): Recipe {
        return Recipe(
            id = id,
            name = name,
            description = description,
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt),
            createdBy = createdBy,
            sections = parseSectionsJson(sectionsJson),
            isActive = isActive
        )
    }

    private fun Recipe.toEntity(): RecipeEntity {
        return RecipeEntity(
            id = id,
            name = name,
            description = description,
            createdAt = createdAt.time,
            updatedAt = updatedAt.time,
            createdBy = createdBy,
            sectionsJson = sectionsToJson(sections),
            isActive = isActive
        )
    }

    /**
     * Parse JSON string to list of RecipeSections.
     */
    private fun parseSectionsJson(json: String): List<RecipeSection> {
        if (json.isBlank() || json == "[]") return emptyList()
        
        return try {
            val jsonArray = JSONArray(json)
            val sections = mutableListOf<RecipeSection>()
            
            for (i in 0 until jsonArray.length()) {
                val sectionObj = jsonArray.getJSONObject(i)
                val fieldsArray = sectionObj.optJSONArray("fields") ?: JSONArray()
                val fields = mutableListOf<RecipeField>()
                
                for (j in 0 until fieldsArray.length()) {
                    val fieldObj = fieldsArray.getJSONObject(j)
                    fields.add(
                        RecipeField(
                            id = fieldObj.optLong("id", 0),
                            label = fieldObj.optString("label", ""),
                            fieldType = parseFieldType(fieldObj.optString("fieldType", "TEXT")),
                            defaultValue = fieldObj.optString("defaultValue", ""),
                            isRequired = fieldObj.optBoolean("isRequired", false),
                            order = fieldObj.optInt("order", 0)
                        )
                    )
                }
                
                sections.add(
                    RecipeSection(
                        id = sectionObj.optLong("id", 0),
                        title = sectionObj.optString("title", ""),
                        order = sectionObj.optInt("order", 0),
                        fields = fields
                    )
                )
            }
            
            sections
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Convert list of RecipeSections to JSON string.
     */
    private fun sectionsToJson(sections: List<RecipeSection>): String {
        val jsonArray = JSONArray()
        
        sections.forEach { section ->
            val sectionObj = JSONObject().apply {
                put("id", section.id)
                put("title", section.title)
                put("order", section.order)
                
                val fieldsArray = JSONArray()
                section.fields.forEach { field ->
                    fieldsArray.put(JSONObject().apply {
                        put("id", field.id)
                        put("label", field.label)
                        put("fieldType", field.fieldType.name)
                        put("defaultValue", field.defaultValue)
                        put("isRequired", field.isRequired)
                        put("order", field.order)
                    })
                }
                put("fields", fieldsArray)
            }
            jsonArray.put(sectionObj)
        }
        
        return jsonArray.toString()
    }

    private fun parseFieldType(type: String): FieldType {
        return try {
            FieldType.valueOf(type)
        } catch (e: Exception) {
            FieldType.TEXT
        }
    }
}
