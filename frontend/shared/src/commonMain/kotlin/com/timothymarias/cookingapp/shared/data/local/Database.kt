package com.timothymarias.cookingapp.shared.data.local

import app.cash.sqldelight.db.SqlDriver
import com.timothymarias.cookingapp.shared.db.CookingDatabase

// Simple helper to construct the generated SQLDelight database from a provided driver.
// Platform code (android/desktop/iOS) is responsible for creating the correct SqlDriver
// and passing it into this function.
fun createDatabase(driver: SqlDriver): CookingDatabase = CookingDatabase(driver)
