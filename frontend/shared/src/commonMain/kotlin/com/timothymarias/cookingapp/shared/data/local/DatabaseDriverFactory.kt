package com.timothymarias.cookingapp.shared.data.local

import app.cash.sqldelight.db.SqlDriver

// Cross-platform configuration for driver creation.
data class DriverConfig(
    val androidContext: Any? = null,
    val name: String = "cooking.db"
)

// Expect/actual for platform-specific SQLDelight driver creation.
expect class DatabaseDriverFactory(config: DriverConfig) {
    fun createDriver(): SqlDriver
}
