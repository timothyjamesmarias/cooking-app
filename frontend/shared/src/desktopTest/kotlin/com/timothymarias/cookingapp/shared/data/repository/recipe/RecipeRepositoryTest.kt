package com.timothymarias.cookingapp.shared.data.repository.recipe

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.timothymarias.cookingapp.shared.db.CookingDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class RecipeRepositoryTest {
    @Test
    fun `watchAll emits empty list initially`() = runBlocking {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        CookingDatabase.Schema.create(driver)
        val db = CookingDatabase(driver)
        val repo = DbRecipeRepository(db, Dispatchers.Default)

        val first = repo.watchAll().first()
        assertEquals(0, first.size, "Expected no recipes initially")
    }
}
