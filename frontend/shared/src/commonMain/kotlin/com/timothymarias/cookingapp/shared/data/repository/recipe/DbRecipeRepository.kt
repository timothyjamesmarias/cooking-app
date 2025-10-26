package com.timothymarias.cookingapp.shared.data.repository.recipe

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.timothymarias.cookingapp.shared.db.CookingDatabase
import com.timothymarias.cookingapp.shared.domain.model.Recipe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

/**
 * Local-first repository backed by SQLDelight. Currently read-focused; write methods are TODO for TDD.
 */
class DbRecipeRepository(
    private val db: CookingDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : RecipeRepository {
    override fun watchAll(): Flow<List<Recipe>> =
        db.recipesQueries.selectAll()
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map { Recipe(localId = it.local_id, name = it.name) } }

    override fun watchById(localId: String): Flow<Recipe?> =
        db.recipesQueries.selectById(localId)
            .asFlow()
            .mapToOneOrNull(dispatcher)
            .map { row -> row?.let { Recipe(localId = it.local_id, name = it.name) } }

    override suspend fun create(recipe: Recipe): Recipe {
        val newLocalId = recipe.localId.takeIf { it.isNotBlank() } ?: UUID.generateUUID().toString()
        db.recipesQueries.insertRecipe(newLocalId, recipe.name)
        return Recipe(localId = newLocalId, name = recipe.name)
    }

    override suspend fun updateName(localId: String, name: String): Recipe {
        db.recipesQueries.updateRecipeName(name = name, local_id = localId)
        return Recipe(localId = localId, name = name)
    }

    override suspend fun delete(localId: String) {
        db.recipesQueries.deleteById(local_id = localId)
    }
}
