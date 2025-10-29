package com.timothymarias.cookingapp.shared.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.timothymarias.cookingapp.shared.db.CookingDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class IngredientDbCrudTest {
    @Test
    fun `insert, select, update, delete work via SQLDelight queries for ingredients`() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        CookingDatabase.Schema.create(driver)
        val db = CookingDatabase(driver)

        // Initial empty
        assertEquals(0, db.ingredientsQueries.selectAll().executeAsList().size)

        // Create
        val id = "i1"
        db.ingredientsQueries.insertIngredient(id, "Garlic")

        // Read
        val row1 = db.ingredientsQueries.selectById(id).executeAsOneOrNull()
        assertNotNull(row1)
        assertEquals(id, row1.local_id)
        assertEquals("Garlic", row1.name)

        // Update
        db.ingredientsQueries.updateIngredientName(name = "Minced Garlic", local_id = id)
        val row2 = db.ingredientsQueries.selectById(id).executeAsOneOrNull()
        assertNotNull(row2)
        assertEquals("Minced Garlic", row2.name)

        // Delete
        db.ingredientsQueries.deleteById(id)
        val row3 = db.ingredientsQueries.selectById(id).executeAsOneOrNull()
        assertNull(row3)

        // Still empty overall
        assertEquals(0, db.ingredientsQueries.selectAll().executeAsList().size)
    }
}
