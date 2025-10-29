package com.timothymarias.cookingapp.shared.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.timothymarias.cookingapp.shared.db.CookingDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DatabaseInitTest {
    @Test
    fun `database schema creates and selectAll works on empty table`() {
        // Use in-memory SQLite for fast tests
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        // Create schema
        CookingDatabase.Schema.create(driver)

        val db = CookingDatabase(driver)
        assertNotNull(db)

        // Query should return empty list initially
        val rows = db.recipesQueries.selectAll().executeAsList()
        assertEquals(0, rows.size, "Expected no recipes initially")
    }
}
