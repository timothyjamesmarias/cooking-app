package com.timothymarias.cookingapp.shared.data.repository.recipe

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
}