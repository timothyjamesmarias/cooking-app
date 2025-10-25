package com.timothymarias.cookingapp.shared.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.timothymarias.cookingapp.shared.db.CookingDatabase
// Note: com.timothymarias.cookingapp.shared.db is a SQLDelight-generated package, not a separate module.

actual class DatabaseDriverFactory actual constructor(private val config: DriverConfig) {
    actual fun createDriver(): SqlDriver {
        val context = requireNotNull(config.androidContext as? Context) {
            "Android Context must be provided in DriverConfig.androidContext"
        }
        return AndroidSqliteDriver(CookingDatabase.Schema, context, config.name)
    }
}
