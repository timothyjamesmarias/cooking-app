package com.timothymarias.cookingapp.shared.data.repository.recipe

import app.cash.turbine.test
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.timothymarias.cookingapp.shared.db.CookingDatabase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RecipeRepositoryFlowCrudTest {
    @Test
    fun `watchAll and watchById reflect inserts, updates, deletes`() = runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        CookingDatabase.Schema.create(driver)
        val db = CookingDatabase(driver)
        val repo = DbRecipeRepository(db)

        // watchAll flow should start empty, then reflect changes
        repo.watchAll().test {
            assertEquals(0, awaitItem().size, "initial emission should be empty")

            // Insert one
            db.recipesQueries.insertRecipe("r1", "Soup")
            assertEquals(listOf("Soup"), awaitItem().map { it.name })

            // Update
            db.recipesQueries.updateRecipeName(name = "Tomato Soup", local_id = "r1")
            assertEquals(listOf("Tomato Soup"), awaitItem().map { it.name })

            // Delete
            db.recipesQueries.deleteById("r1")
            assertEquals(emptyList(), awaitItem())

            cancelAndConsumeRemainingEvents()
        }

        // watchById should emit null then value changes
        repo.watchById("r2").test {
            assertNull(awaitItem(), "no row -> null first emission")
            db.recipesQueries.insertRecipe("r2", "Pasta")
            assertEquals("Pasta", awaitItem()!!.name)
            db.recipesQueries.updateRecipeName(name = "Pasta Alfredo", local_id = "r2")
            assertEquals("Pasta Alfredo", awaitItem()!!.name)
            db.recipesQueries.deleteById("r2")
            assertNull(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }
}
