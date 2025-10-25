package com.timothymarias.cookingapp.shared.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.timothymarias.cookingapp.shared.db.CookingDatabase

actual class DatabaseDriverFactory actual constructor(private val config: DriverConfig) {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(CookingDatabase.Schema, config.name)
    }
}
