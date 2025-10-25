package com.timothymarias.cookingapp.shared.data.repository.recipe

import com.timothymarias.cookingapp.shared.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for accessing recipes from the local database (SQLDelight-backed).
 * This interface is read-only for now; CRUD will be added later.
 */
interface RecipeRepository {
    fun watchAll(): Flow<List<Recipe>>
}