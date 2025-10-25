package com.timothymarias.cookingapp.shared.data.local

import app.cash.sqldelight.db.SqlDriver
actual class DatabaseDriverFactory actual constructor(private val config: DriverConfig) {
    actual fun createDriver(): SqlDriver {
        // JS driver setup can vary by runtime; defer until we add a web DB.
        // For now, this is intentionally unimplemented to keep shared compiling.
        error("SQLDelight driver for JS not configured yet")
    }
}
