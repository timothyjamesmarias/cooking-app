package com.timothymarias.cookingapp.shared.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.timothymarias.cookingapp.shared.db.CookingDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RecipeDbCrudTest {
    @Test
    fun `insert, select, update, delete work via SQLDelight queries`() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        CookingDatabase.Schema.create(driver)
        val db = CookingDatabase(driver)

        // Initial empty
        assertEquals(0, db.recipesQueries.selectAll().executeAsList().size)

        // Create
        val id = "r1"
        db.recipesQueries.insertRecipe(id, "Pasta")

        // Read
        val row1 = db.recipesQueries.selectById(id).executeAsOneOrNull()
        assertNotNull(row1)
        assertEquals(id, row1.local_id)
        assertEquals("Pasta", row1.name)

        // Update
        db.recipesQueries.updateRecipeName(name = "Pasta Alfredo", local_id = id)
        val row2 = db.recipesQueries.selectById(id).executeAsOneOrNull()
        assertNotNull(row2)
        assertEquals("Pasta Alfredo", row2.name)

        // Delete
        db.recipesQueries.deleteById(id)
        val row3 = db.recipesQueries.selectById(id).executeAsOneOrNull()
        assertNull(row3)

        // Still empty overall
        assertEquals(0, db.recipesQueries.selectAll().executeAsList().size)
    }
}
