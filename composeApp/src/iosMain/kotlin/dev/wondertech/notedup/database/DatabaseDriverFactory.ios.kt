package dev.wondertech.notedup.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

/**
 * iOS implementation of database driver factory.
 * No context parameter needed for iOS native driver.
 */
actual class DatabaseDriverFactory {
    /**
     * Creates an iOS native SQLite driver for the task database.
     *
     * @return SqlDriver instance configured for iOS platform
     */
    actual fun createDriver(): SqlDriver {
        val driver = NativeSqliteDriver(NotedUpDatabase.Schema, "notedup.db")

        try {
            driver.executeQuery(null, "SELECT isTaskDone FROM Task LIMIT 1", { cursor ->
                cursor.next()
            }, 0)
        } catch (_: Exception) {
            driver.execute(null, "ALTER TABLE Task ADD COLUMN isTaskDone INTEGER NOT NULL DEFAULT 0", 0)
        }

        return driver
    }
}
