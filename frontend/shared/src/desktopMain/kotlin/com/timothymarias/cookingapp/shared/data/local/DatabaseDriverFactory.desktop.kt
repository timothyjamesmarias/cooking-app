package com.timothymarias.cookingapp.shared.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.timothymarias.cookingapp.shared.db.CookingDatabase

actual class DatabaseDriverFactory actual constructor(private val config: DriverConfig) {
    actual fun createDriver(): SqlDriver {
        val url = "jdbc:sqlite:${'$'}{config.name}"
        val driver = JdbcSqliteDriver(url)
        // Ensure schema exists for desktop
        CookingDatabase.Schema.create(driver)
        return driver
    }
}
