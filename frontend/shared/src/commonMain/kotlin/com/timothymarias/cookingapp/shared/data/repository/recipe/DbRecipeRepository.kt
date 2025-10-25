package com.timothymarias.cookingapp.shared.data.repository.recipe

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.timothymarias.cookingapp.shared.db.CookingDatabase
import com.timothymarias.cookingapp.shared.domain.model.Recipe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Local-first repository backed by SQLDelight. Read-only for now (no CRUD).
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
}
