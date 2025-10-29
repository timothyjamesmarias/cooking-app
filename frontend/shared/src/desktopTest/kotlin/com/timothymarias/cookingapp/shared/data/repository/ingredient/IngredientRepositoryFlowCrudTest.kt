package com.timothymarias.cookingapp.shared.data.repository.ingredient

import app.cash.turbine.test
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.timothymarias.cookingapp.shared.db.CookingDatabase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IngredientRepositoryFlowCrudTest {
    @Test
    fun `watchAll and watchById reflect inserts, updates, deletes for ingredients`() = runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        CookingDatabase.Schema.create(driver)
        val db = CookingDatabase(driver)
        val repo = DbIngredientRepository(db)

        // watchAll flow should start empty, then reflect changes
        repo.watchAll().test {
            assertEquals(0, awaitItem().size, "initial emission should be empty")

            // Insert one
            db.ingredientsQueries.insertIngredient("i1", "Garlic")
            assertEquals(listOf("Garlic"), awaitItem().map { it.name })

            // Update
            db.ingredientsQueries.updateIngredientName(name = "Minced Garlic", local_id = "i1")
            assertEquals(listOf("Minced Garlic"), awaitItem().map { it.name })

            // Delete
            db.ingredientsQueries.deleteById("i1")
            assertEquals(emptyList(), awaitItem())

            cancelAndConsumeRemainingEvents()
        }

        // watchById should emit null then value changes
        repo.watchById("i2").test {
            assertNull(awaitItem(), "no row -> null first emission")
            db.ingredientsQueries.insertIngredient("i2", "Onion")
            assertEquals("Onion", awaitItem()!!.name)
            db.ingredientsQueries.updateIngredientName(name = "Red Onion", local_id = "i2")
            assertEquals("Red Onion", awaitItem()!!.name)
            db.ingredientsQueries.deleteById("i2")
            assertNull(awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }
}
