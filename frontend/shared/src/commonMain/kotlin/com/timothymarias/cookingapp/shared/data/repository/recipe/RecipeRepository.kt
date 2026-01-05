package com.timothymarias.cookingapp.shared.data.repository.recipe

import com.timothymarias.cookingapp.shared.domain.model.Ingredient
import com.timothymarias.cookingapp.shared.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for accessing recipes from the local database (SQLDelight-backed).
 * Read APIs are implemented; write APIs (CRUD) will be implemented next.
 */
interface RecipeRepository {
    // Read
    fun watchAll(): Flow<List<Recipe>>
    fun watchById(localId: String): Flow<Recipe?>

    // Write (skeleton, to be implemented in TDD)
    suspend fun create(recipe: Recipe): Recipe
    suspend fun updateName(localId: String, name: String): Recipe
    suspend fun delete(localId: String)
    suspend fun getIngredients(localId: String): List<Ingredient>
    suspend fun assignIngredient(recipeId: String, ingredientId: String)
    suspend fun removeIngredient(recipeId: String, ingredientId: String)
    suspend fun isIngredientAssigned(recipeId: String, ingredientId: String): Boolean
    suspend fun updateIngredientQuantity(recipeId: String, ingredientId: String, quantityId: String?)
}