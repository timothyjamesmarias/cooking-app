package com.timothymarias.cookingapp.shared.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.timothymarias.cookingapp.shared.db.CookingDatabase
import java.io.File

actual class DatabaseDriverFactory actual constructor(private val config: DriverConfig) {
    actual fun createDriver(): SqlDriver {
        val dbName = config.name
        val url = "jdbc:sqlite:$dbName"
        val driver = JdbcSqliteDriver(url)
        try {
            // Attempt to create schema; if it already exists, ignore the error.
            CookingDatabase.Schema.create(driver)
        } catch (t: Throwable) {
            // Common when the DB file already exists; safe to ignore if schema is present.
            // In a future iteration, we can add a proper migration check here.
        }
        return driver
    }
}
