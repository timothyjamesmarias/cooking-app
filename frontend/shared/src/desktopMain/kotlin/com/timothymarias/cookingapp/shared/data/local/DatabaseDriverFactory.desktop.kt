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
            setUserVersion(driver, CookingDatabase.Schema.version)
            return driver
        }

        val dbFile = File(dbName)
        if (!dbFile.exists()) {
            // Fresh database file: create full schema and set version
            println("Creating new database at version ${CookingDatabase.Schema.version}")
            CookingDatabase.Schema.create(driver)
            setUserVersion(driver, CookingDatabase.Schema.version)
            println("Database created successfully")
            return driver
        }

        // Existing database file: attempt migration from current PRAGMA user_version to target.
        val currentVersion: Long = getUserVersion(driver)
        val targetVersion: Long = CookingDatabase.Schema.version

        if (currentVersion == 0L && targetVersion > 0L) {
            // Database exists but has no version set (pre-migration schema).
            // This can happen if the DB was created before migrations were added.
            println("WARNING: Database exists but PRAGMA user_version is 0. This indicates a pre-migration database.")
            println("Attempting to apply all migrations from 0 to $targetVersion...")
            try {
                CookingDatabase.Schema.migrate(driver, 0, targetVersion)
                setUserVersion(driver, targetVersion)
                println("Migration successful: 0 → $targetVersion")
            } catch (e: Exception) {
                // Migration failed - this is critical and should not be silently ignored
                System.err.println("CRITICAL: Database migration failed!")
                System.err.println("Current version: $currentVersion, Target version: $targetVersion")
                System.err.println("Error: ${e.message}")
                e.printStackTrace()
                throw IllegalStateException(
                    "Database migration failed. Cannot start app with incompatible schema. " +
                    "Current: $currentVersion, Target: $targetVersion. " +
                    "Consider backing up and deleting the database file to start fresh.",
                    e
                )
            }
        } else if (currentVersion < targetVersion) {
            // Normal migration path: incrementally apply missing migrations
            println("Migrating database from version $currentVersion to $targetVersion...")
            try {
                CookingDatabase.Schema.migrate(driver, currentVersion, targetVersion)
                setUserVersion(driver, targetVersion)
                println("Migration successful: $currentVersion → $targetVersion")
            } catch (e: Exception) {
                System.err.println("CRITICAL: Database migration failed!")
                System.err.println("Current version: $currentVersion, Target version: $targetVersion")
                System.err.println("Error: ${e.message}")
                e.printStackTrace()
                throw IllegalStateException(
                    "Database migration failed. Cannot start app with incompatible schema. " +
                    "Current: $currentVersion, Target: $targetVersion. " +
                    "Consider backing up and deleting the database file to start fresh.",
                    e
                )
            }
        } else if (currentVersion > targetVersion) {
            // User downgraded the app - this is problematic
            System.err.println("WARNING: Database version ($currentVersion) is newer than app schema ($targetVersion)")
            System.err.println("User may have downgraded the app. This may cause errors.")
        } else {
            // Versions match - no migration needed
            println("Database schema is up to date (version $currentVersion)")
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

    private fun setUserVersion(driver: SqlDriver, version: Long) {
        driver.execute(
            identifier = null,
            sql = "PRAGMA user_version = $version;",
            parameters = 0
        )
    }
}
