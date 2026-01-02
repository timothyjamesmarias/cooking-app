package com.timothymarias.cookingapp.shared.data.repository.ingredient

import com.timothymarias.cookingapp.shared.domain.model.Ingredient
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for accessing Ingredients from the local database (SQLDelight-backed).
 * Read APIs are implemented; write APIs (CRUD) will be implemented next.
 */
interface IngredientRepository {
    // Read
    fun watchAll(): Flow<List<Ingredient>>
    fun watchById(localId: String): Flow<Ingredient?>
    fun watchByQuery(query: String): Flow<List<Ingredient>>

    // Write (skeleton, to be implemented in TDD)
    suspend fun create(ingredient: Ingredient): Ingredient
    suspend fun updateName(localId: String, name: String): Ingredient
    suspend fun delete(localId: String)
}