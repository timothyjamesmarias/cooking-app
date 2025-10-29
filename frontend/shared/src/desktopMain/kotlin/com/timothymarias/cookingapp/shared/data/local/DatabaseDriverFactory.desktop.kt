package com.timothymarias.cookingapp.shared.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.sqldelight.db.QueryResult
import com.timothymarias.cookingapp.shared.db.CookingDatabase
import java.io.File

actual class DatabaseDriverFactory actual constructor(private val config: DriverConfig) {
    actual fun createDriver(): SqlDriver {
        val dbName = config.name
        val url = "jdbc:sqlite:$dbName"
        val driver = JdbcSqliteDriver(url)

        // Decide between create vs migrate. For in-memory DBs, always create.
        val isInMemory = dbName == ":memory:" || dbName.startsWith("file::memory:")
        if (isInMemory) {
            CookingDatabase.Schema.create(driver)
            return driver
        }

        val dbFile = File(dbName)
        if (!dbFile.exists()) {
            // Fresh database file: create full schema
            CookingDatabase.Schema.create(driver)
            return driver
        }

        // Existing database file: attempt migration from current PRAGMA user_version to target.
        try {
            val currentVersion: Long = getUserVersion(driver)
            val targetVersion: Long = CookingDatabase.Schema.version
            if (currentVersion < targetVersion) {
                CookingDatabase.Schema.migrate(driver, currentVersion, targetVersion)
            }
            // If currentVersion == 0 and targetVersion > 0, migrate() will apply all migrations.
        } catch (t: Throwable) {
            // As a safety net, if migration introspection fails, do not destroy user data.
            // Leave the database as-is; callers may handle missing tables/errors at a higher level.
        }
        return driver
    }

    private fun getUserVersion(driver: SqlDriver): Long {
        // PRAGMA user_version returns an integer; default is 0 for pre-schema or unmanaged DBs.
        val result = driver.executeQuery<Long>(
            identifier = null,
            sql = "PRAGMA user_version;",
            mapper = { cursor ->
                val hasRow = cursor.next().value
                val v = if (!hasRow) 0L else (cursor.getLong(0) ?: 0L)
                QueryResult.Value(v)
            },
            parameters = 0
        )
        return result.value
    }
}
